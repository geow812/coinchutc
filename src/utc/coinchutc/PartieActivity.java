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

import utc.coinchutc.agent.ConnexionAgent;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class PartieActivity extends Activity {

	private Logger logger = Logger.getJADELogger(this.getClass().getName());
	private static final String[] annonces={"80","90","100","110","120","130","140","150","160","Capot"}; 
	private static final String[] couleurs={"Pique","Trefle","Carreau","Coeur","Tout-Atout","Sans-Atout"}; 
	private static final String[] cartes={}; 
	private Spinner spinnerAnnonce, spinnerCouleur; 
	private TextView joueur1, joueur2, joueur3, joueur4;
	private String identifiant = "";
	private MicroRuntimeServiceBinder microRuntimeServiceBinder;
	private ServiceConnection serviceConnection;
	private AlertDialog.Builder adb;
	private AlertDialog dialogAnnonce;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_partie);
		// Show the Up button in the action bar.
		setupActionBar();


		// Assign the touch listener to your view which you want to move
		 TODO : findViewById(R.id.carte1).setOnTouchListener(new MyTouchListener());


		// TODO : findViewById(R.id.bottomright).setOnDragListener(new MyDragListener());



		//Affichage des noms des joueurs
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			//TODO : remplacer les string des textView "nom de joueurs" par leur login via JADE

			identifiant = extras.getString("identifiant");
			joueur1 = (TextView)findViewById(R.id.joueur1);
			joueur1.setText(identifiant);
		}
	}

	// This defines your touch listener
	private final class MyTouchListener implements OnTouchListener {
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
				ClipData data = ClipData.newPlainText("", "");
				DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
				view.startDrag(data, shadowBuilder, view, 0);
				view.setVisibility(View.INVISIBLE);
				return true;
			} else {
				return false;
			}
		}
	} 		
/*
	class MyDragListener implements OnDragListener {
		Drawable enterShape = getResources().getDrawable(R.id.tapis);
		Drawable normalShape = getResources().getDrawable(R.id.shape);

		@Override
		public boolean onDrag(View v, DragEvent event) {
			int action = event.getAction();
			switch (event.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED:
				// Do nothing
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				v.setBackgroundDrawable(enterShape);
				break;
			case DragEvent.ACTION_DRAG_EXITED:        
				v.setBackgroundDrawable(normalShape);
				break;
			case DragEvent.ACTION_DROP:
				// Dropped, reassign View to ViewGroup
				View view = (View) event.getLocalState();
				ViewGroup owner = (ViewGroup) view.getParent();
				owner.removeView(view);
				LinearLayout container = (LinearLayout) v;
				container.addView(view);
				view.setVisibility(View.VISIBLE);
				break;
			case DragEvent.ACTION_DRAG_ENDED:
				v.setBackgroundDrawable(normalShape);
			default:
				break;
			}
			return true;
		}
	} */

	public void annoncer() {

		//Boite de dialogue pour annoncer :
		// ------ code récupéré de : http://www.tutomobile.fr/personnaliser-un-alertdialog-tutoriel-android-n%C2%B020/04/11/2010/
		LayoutInflater factory = LayoutInflater.from(this);
		final View annonceDialogView = factory.inflate(R.layout.annonce, null);

		if (adb == null) {
			adb = new AlertDialog.Builder(PartieActivity.this);

			//On affecte la vue personnalisé que l'on a crée à notre AlertDialog
			adb.setView(annonceDialogView);

			//On donne un titre à l'AlertDialog
			adb.setTitle("A vous d'annoncer :");

			//On peut modifier l'icone si besoin (TODO)
			//adb.setIcon(android.R.drawable.ic_dialog_alert);

			//On affecte un bouton "Annoncer" à notre AlertDialog et on lui affecte un évènement
			adb.setPositiveButton("Annoncer", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//TODO : c'est ici qu'on va gérer l'envoi de l'annonce à notre serveur en Jade
					//Lorsque l'on cliquera sur le bouton "OK", on récupère l'EditText correspondant à notre vue personnalisée (cad à alertDialogView)
					//EditText et = (EditText)alertDialogView.findViewById(R.id.EditText1);

					//On affiche dans un Toast le texte contenu dans l'EditText de notre AlertDialog
					//Toast.makeText(Tutoriel18_Android.this, et.getText(), Toast.LENGTH_SHORT).show();
				} });
			//On crée un bouton "Passer" à notre AlertDialog et on lui affecte un évènement
			adb.setNegativeButton("Passer", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//TODO : c'est ici qu'on va gérer signaler que le joueur passe à notre serveur en Jade

				} });



			//Création des menus déroulants (couleur et points a annocner)
			spinnerAnnonce=(Spinner)annonceDialogView.findViewById(R.id.spinnerCouleur);
			ArrayAdapter<String> adapterAnnonce = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, annonces);  
			adapterAnnonce.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnerAnnonce.setAdapter(adapterAnnonce);

			spinnerCouleur=(Spinner)annonceDialogView.findViewById(R.id.spinnerAnnonce);
			ArrayAdapter<String>adapterCouleur = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, couleurs);
			adapterCouleur.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnerCouleur.setAdapter(adapterCouleur); 


			//On affiche :
			adb.show();
		}

		else {
			// TODO : si on a déjà afficher la boite de dialogue, il faut mettre a jour

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
		microRuntimeServiceBinder.startAgent(identifiant, ConnexionAgent.class.getName(),
				new Object[] { getApplicationContext()},//TODO: passer les arguments ici
				new RuntimeCallback<Void>() {
			@Override
			public void onSuccess(Void thisIsNull) {
						logger.log(Level.INFO, "Successfully start of the " + ConnexionAgent.class.getName() + "...");
				try {
					agentStartupCallback.onSuccess(MicroRuntime.getAgent(identifiant));
				} catch (ControllerException e) {
					// Should never happen
					agentStartupCallback.onFailure(e);
				}
			}

			@Override
			public void onFailure(Throwable throwable) {
						logger.log(Level.SEVERE, "Failed to start the " + ConnexionAgent.class.getName() + "...");
				agentStartupCallback.onFailure(throwable);
			}
		});
	}
}


