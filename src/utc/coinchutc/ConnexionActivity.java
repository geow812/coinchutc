package utc.coinchutc;

import jade.android.AndroidHelper;
import jade.android.MicroRuntimeService;
import jade.android.MicroRuntimeServiceBinder;
import jade.android.RuntimeCallback;
import jade.core.MicroRuntime;
import jade.core.Profile;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

import java.util.logging.Level;

import utc.coinchutc.agent.CoincheClientAgent;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;


public class ConnexionActivity extends Activity {
	private Logger logger = Logger.getJADELogger(this.getClass().getName());

	private MicroRuntimeServiceBinder microRuntimeServiceBinder;
	private ServiceConnection serviceConnection;
	
	private String identifiant = "";
	private String mdp = "";
	private MyReceiver myReceiver = new MyReceiver();
	
	public static final String CREER_COMPTE = "__creer_compte__";
	public static final String CONNECTER = "__connecter__";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connexion);
		
		IntentFilter loginFilter = new IntentFilter();
		loginFilter.addAction("coinchutc.LOGIN_SUCCESS");
		registerReceiver(myReceiver, loginFilter);
		
		IntentFilter loginFailFilter = new IntentFilter();
		loginFailFilter.addAction("coinchutc.LOGIN_FAIL");
		registerReceiver(myReceiver, loginFailFilter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.connexion, menu);
		return true;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregisterReceiver(myReceiver);

		logger.log(Level.INFO, "Destroy activity!");
		
		if (serviceConnection != null) {
	        Log.d("Destroy", "Focus onDestroy() attempted to unbind service");
	        unbindService(serviceConnection);
	        serviceConnection = null;
	    }
	    Log.d("Destroy", "Focus onDestroy()");
	}
	
	private static boolean checkName(String identifiant, String mdp) {
		if (identifiant == null || identifiant.trim().equals("") 
				|| mdp == null || mdp.trim().equals("")) {
			return false;
		}
		return true;
	}

	public void configJade(String id, String mdp, String option) {
		SharedPreferences settings = getSharedPreferences("jadeChatPrefsFile", 0);
		String host = settings.getString("defaultHost", "");
		String port = settings.getString("defaultPort", "");
		startChat(id, mdp, option, host, port, agentStartupCallback);
		SharedPreferences.Editor editor = settings.edit();
	    editor.putBoolean(MainActivity.CONNECTE, true);
	    editor.commit();
	}
	
	public void connecter(View view) {
		// Get the user name and the password
		EditText nameField = (EditText) findViewById(R.id.identifiant);
		identifiant = nameField.getText().toString();
		
		EditText mdpField = (EditText) findViewById(R.id.mdp);
		mdp = mdpField.getText().toString();
		
		if (checkName(identifiant, mdp)) {
			try {
				configJade(identifiant, mdp, CONNECTER);
			} catch (Exception ex) {
				logger.log(Level.SEVERE, "Unexpected exception connecting to the server!");
			}
		}
		else {
			logger.log(Level.INFO, "Invalid login or password!");
		}
	}
	
	public void creerCompte(View view) {
		// Get the user name and the password
		EditText nameField = (EditText) findViewById(R.id.identifiant);
		identifiant = nameField.getText().toString();
		
		EditText mdpField = (EditText) findViewById(R.id.mdp);
		mdp = mdpField.getText().toString();
		
		if (checkName(identifiant, mdp)) {
			try {
				configJade(identifiant, mdp, CREER_COMPTE);
			} catch (Exception ex) {
				logger.log(Level.SEVERE, "Unexpected exception creating the account!");
			}
		}
		else {
			logger.log(Level.INFO, "Invalid login or password!");
		}
	}
	
	private RuntimeCallback<AgentController> agentStartupCallback = new RuntimeCallback<AgentController>() {
		@Override
		public void onSuccess(AgentController agent) {
		}

		@Override
		public void onFailure(Throwable throwable) {
			logger.log(Level.INFO, "L'identifiant ou le mot de passe n'est pas valide");
		}
	};
	
	public void startChat(final String identifiant, final String mdp, final String option, final String host,
			final String port,
			final RuntimeCallback<AgentController> agentStartupCallback) {

		final Properties profile = new Properties();
		profile.setProperty(Profile.MAIN_HOST, host);
		profile.setProperty(Profile.MAIN_PORT, port);
		profile.setProperty(Profile.MAIN, Boolean.FALSE.toString());
		profile.setProperty(Profile.JVM, Profile.ANDROID);

		if (AndroidHelper.isEmulator()) {
			// Emulator: this is needed to work with emulated devices
			profile.setProperty(Profile.LOCAL_HOST, AndroidHelper.LOOPBACK);
		} else {
			profile.setProperty(Profile.LOCAL_HOST, AndroidHelper.getLocalIPAddress());
		}
		// Emulator: this is not really needed on a real device
		profile.setProperty(Profile.LOCAL_PORT, "2000");

		if (microRuntimeServiceBinder == null) {
			serviceConnection = new ServiceConnection() {
				public void onServiceConnected(ComponentName className,
						IBinder service) {
					microRuntimeServiceBinder = (MicroRuntimeServiceBinder) service;
					logger.log(Level.INFO, "Gateway successfully bound to MicroRuntimeService");
					startContainer(identifiant, mdp, option, profile, agentStartupCallback);
				};

				public void onServiceDisconnected(ComponentName className) {
					microRuntimeServiceBinder = null;
					logger.log(Level.INFO, "Gateway unbound from MicroRuntimeService");
				}
			};
			logger.log(Level.INFO, "Binding Gateway to MicroRuntimeService...");
			bindService(new Intent(getApplicationContext(),
					MicroRuntimeService.class), serviceConnection,
					Context.BIND_AUTO_CREATE);
		} else {
			logger.log(Level.INFO, "MicroRumtimeGateway already binded to service");
			startContainer(identifiant, mdp, option, profile, agentStartupCallback);
		}
	}
	
	private void startContainer(final String identifiant, final String mdp, final String option, Properties profile,
			final RuntimeCallback<AgentController> agentStartupCallback) {
		if (!MicroRuntime.isRunning()) {
			microRuntimeServiceBinder.startAgentContainer(profile,
					new RuntimeCallback<Void>() {
						@Override
						public void onSuccess(Void thisIsNull) {
							logger.log(Level.INFO, "Successfully start of the container...");
							startAgent(identifiant, mdp, option, agentStartupCallback);
						}

						@Override
						public void onFailure(Throwable throwable) {
							logger.log(Level.SEVERE, "Failed to start the container...");
						}
					});
		} else {
			startAgent(identifiant, mdp, option, agentStartupCallback);
		}
	}
	
	private void startAgent(final String identifiant, final String mdp, final String option, final RuntimeCallback<AgentController> agentStartupCallback) {
		microRuntimeServiceBinder.startAgent(identifiant, CoincheClientAgent.class.getName(),
				new Object[] { getApplicationContext(), mdp, option },
				new RuntimeCallback<Void>() {
					@Override
					public void onSuccess(Void thisIsNull) {
						logger.log(Level.INFO, "Successfully start of the " + CoincheClientAgent.class.getName() + "...");
						try {
							agentStartupCallback.onSuccess(MicroRuntime.getAgent(identifiant));
						} catch (ControllerException e) {
							// Should never happen
							agentStartupCallback.onFailure(e);
						}
					}

					@Override
					public void onFailure(Throwable throwable) {
						logger.log(Level.SEVERE, "Failed to start the " + CoincheClientAgent.class.getName() + "...");
						agentStartupCallback.onFailure(throwable);
					}
				});
	}
	
	public void login() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("identifiant", identifiant);
		startActivity(intent);
	}
	
	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			logger.log(Level.INFO, "Received intent " + action);
			if (action.equalsIgnoreCase("coinchutc.LOGIN_SUCCESS")) {
				//ShowDialog("Login succeeded!");
				login();
			}
			if (action.equalsIgnoreCase("coinchutc.LOGIN_FAIL")) {
				ShowDialog("Login a ¨¦chou¨¦");
			}
		}
	}
	
	public void ShowDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ConnexionActivity.this);
		builder.setMessage(message).setCancelable(false)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

}
