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


import utc.coinchutc.R;
import utc.coinchutc.agent.CoincheClientAgent;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;


public class ConnexionActivity extends Activity {
	private Logger logger = Logger.getJADELogger(this.getClass().getName());

	private MicroRuntimeServiceBinder microRuntimeServiceBinder;
	private ServiceConnection serviceConnection;
	
	private String identifiant = "";
	private String mdp = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connexion);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.connexion, menu);
		return true;
	}
	
	private static boolean checkName(String identifiant, String mdp) {
		if (identifiant == null || identifiant.trim().equals("") 
				|| mdp == null || mdp.trim().equals("")) {
			return false;
		}
		return true;
	}

	public void connecter(View view) {
		// Get the user name and the password
		EditText nameField = (EditText) findViewById(R.id.identifiant);
		identifiant = nameField.getText().toString();
		
		EditText mdpField = (EditText) findViewById(R.id.mdp);
		mdp = mdpField.getText().toString();
		
		if (checkName(identifiant, mdp)) {
			try {
				SharedPreferences settings = getSharedPreferences("jadeChatPrefsFile", 0);
				String host = settings.getString("defaultHost", "");
				String port = settings.getString("defaultPort", "");
				startChat(identifiant, mdp, host, port, agentStartupCallback);
				SharedPreferences.Editor editor = settings.edit();
			    editor.putBoolean(MainActivity.CONNECTE, true);
			    editor.commit();
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
			} catch (Exception ex) {
				logger.log(Level.SEVERE, "Unexpected exception creating chat agent!");
			}
		}
		else {
			logger.log(Level.INFO, "Invalid nickname!");
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
	
	public void startChat(final String identifiant, final String mdp, final String host,
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
					startContainer(identifiant, mdp, profile, agentStartupCallback);
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
			startContainer(identifiant, mdp, profile, agentStartupCallback);
		}
	}
	
	private void startContainer(final String identifiant, final String mdp, Properties profile,
			final RuntimeCallback<AgentController> agentStartupCallback) {
		if (!MicroRuntime.isRunning()) {
			microRuntimeServiceBinder.startAgentContainer(profile,
					new RuntimeCallback<Void>() {
						@Override
						public void onSuccess(Void thisIsNull) {
							logger.log(Level.INFO, "Successfully start of the container...");
							startAgent(identifiant, mdp, agentStartupCallback);
						}

						@Override
						public void onFailure(Throwable throwable) {
							logger.log(Level.SEVERE, "Failed to start the container...");
						}
					});
		} else {
			startAgent(identifiant, mdp, agentStartupCallback);
		}
	}
	
	private void startAgent(final String identifiant, final String mdp, final RuntimeCallback<AgentController> agentStartupCallback) {
		microRuntimeServiceBinder.startAgent(identifiant, CoincheClientAgent.class.getName(),
				new Object[] { getApplicationContext() },
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

}
