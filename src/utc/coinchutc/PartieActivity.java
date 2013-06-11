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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class PartieActivity extends Activity {
	
	private Logger logger = Logger.getJADELogger(this.getClass().getName());
	private static final String[] annonces={"0","80","90","100","110","120","250"}; 
	private static final String[] couleurs={"Pique","Tr¨¨fle","Carreau","Coeur","Tout-Atout","Sans-Atout"}; 
	private static final String[] cartes={}; 
	private Spinner spinnerAnnonce, spinnerCouleur; 
	private TextView idField;
	private String identifiant = "";
	private MicroRuntimeServiceBinder microRuntimeServiceBinder;
	private ServiceConnection serviceConnection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_partie);
		// Show the Up button in the action bar.
		setupActionBar();
		
		spinnerAnnonce=(Spinner)findViewById(R.id.spinnerAnnonce);
		spinnerCouleur=(Spinner)findViewById(R.id.spinnerCouleur);
		ArrayAdapter<String> adapterAnnonce = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, annonces),
				adapterCouleur = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, couleurs);  
		adapterAnnonce.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerAnnonce.setAdapter(adapterAnnonce);
		adapterCouleur.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerCouleur.setAdapter(adapterCouleur); 
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			identifiant = extras.getString("identifiant");
			idField = (TextView)findViewById(R.id.idField);
			idField.setText(identifiant);
		}
		
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.partie, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void lancePartie(View view) {
		try {
			SharedPreferences settings = getSharedPreferences("jadeChatPrefsFile", 0);
			String host = settings.getString("defaultHost", "");
			String port = settings.getString("defaultPort", "");
			startChat(identifiant, host, port, agentStartupCallback);
			SharedPreferences.Editor editor = settings.edit();
		    editor.putBoolean(MainActivity.CONNECTE, true);
		    editor.commit();
//				Intent intent = new Intent(this, MainActivity.class);
//				intent.putExtra("identifiant", identifiant);
//				startActivity(intent);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Unexpected exception creating chat agent!");
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
	
	public void startChat(final String identifiant, final String host,
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
					startContainer(identifiant, profile, agentStartupCallback);
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
			startContainer(identifiant, profile, agentStartupCallback);
		}
	}
	
	private void startContainer(final String identifiant, Properties profile,
			final RuntimeCallback<AgentController> agentStartupCallback) {
		if (!MicroRuntime.isRunning()) {
			microRuntimeServiceBinder.startAgentContainer(profile,
					new RuntimeCallback<Void>() {
						@Override
						public void onSuccess(Void thisIsNull) {
							logger.log(Level.INFO, "Successfully start of the container...");
							startAgent(identifiant, agentStartupCallback);
						}

						@Override
						public void onFailure(Throwable throwable) {
							logger.log(Level.SEVERE, "Failed to start the container...");
						}
					});
		} else {
			startAgent(identifiant, agentStartupCallback);
		}
	}
	
	private void startAgent(final String identifiant, final RuntimeCallback<AgentController> agentStartupCallback) {
		microRuntimeServiceBinder.startAgent(identifiant, CoincheClientAgent.class.getName(),
				new Object[] { getApplicationContext()},//TODO: passer les arguments ici
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
