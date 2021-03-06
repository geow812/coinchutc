package utc.coinchutc;

import jade.core.MicroRuntime;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;

import utc.coinchutc.agent.Carte;
import utc.coinchutc.agent.JoueurInterface;
import utc.coinchutc.agent.Main;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;


public class PartieActivity extends Activity {

	//private Logger logger = Logger.getJADELogger(this.getClass().getName());
	//private PartieAgent partieAgent;
	private static final String[] annonces={"80","90","100","110","120","130","140","150","160","Capot"}; 
	private static final String[] couleurs={"Pique","Trefle","Carreau","Coeur","Tout-Atout","Sans-Atout"}; 
	private Spinner spinnerAnnonce, spinnerCouleur; 
	private TextView joueur1, joueur2, joueur3, joueur4;
	private ImageView carte1, carte2, carte3, carte4, carte5, carte6, carte7, carte8;
	private String identifiant = "";
	private String[] joueurs;
	private int rang;
	//private MicroRuntimeServiceBinder microRuntimeServiceBinder;
	//private ServiceConnection serviceConnection;
	private AlertDialog.Builder adb;
	private boolean afficherPlis = false;
	private Main main;
	private ArrayList<Carte> cartes;
	private MyReceiver myReceiver;
	
	//TODO : retirer ce flag
	private int simul = 0;
	protected String pointAnnonce;
	protected String couleurAnnonce;
	protected JoueurInterface joueurInterface;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_partie);
		// Show the Up button in the action bar.
		setupActionBar();
		
		myReceiver = new MyReceiver();
		IntentFilter annonceFilter = new IntentFilter("utc.coinchutc.ANNONCE");
		registerReceiver(myReceiver, annonceFilter);
		Log.d("Annonce", "Create filter ANNONCE");
		
		findViewById(R.id.tapis).setOnDragListener(new MyDragListener());

		//Affichage des noms des joueurs et des cartes de joueur 1
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			identifiant = extras.getString("identifiant");
			joueur1 = (TextView)findViewById(R.id.joueur1);
			joueur1.setText(identifiant);
			main = (Main) extras.getSerializable("cards");
			cartes = main.getMain();
			joueurs = extras.getStringArray("joueurs");
			
			carte1 = (ImageView) findViewById(R.id.carte1);
			carte2 = (ImageView) findViewById(R.id.carte2);
			carte3 = (ImageView) findViewById(R.id.carte3);
			carte4 = (ImageView) findViewById(R.id.carte4);
			carte5 = (ImageView) findViewById(R.id.carte5);
			carte6 = (ImageView) findViewById(R.id.carte6);
			carte7 = (ImageView) findViewById(R.id.carte7);
			carte8 = (ImageView) findViewById(R.id.carte8);
			
			carte1.setImageResource(getResources().getIdentifier(cartes.get(0).getCouleur().toLowerCase()+cartes.get(0).getValeur(), "drawable", getPackageName()));
			carte2.setImageResource(getResources().getIdentifier(cartes.get(1).getCouleur().toLowerCase()+cartes.get(1).getValeur(), "drawable", getPackageName()));
			carte3.setImageResource(getResources().getIdentifier(cartes.get(2).getCouleur().toLowerCase()+cartes.get(2).getValeur(), "drawable", getPackageName()));
			carte4.setImageResource(getResources().getIdentifier(cartes.get(3).getCouleur().toLowerCase()+cartes.get(3).getValeur(), "drawable", getPackageName()));
			carte5.setImageResource(getResources().getIdentifier(cartes.get(4).getCouleur().toLowerCase()+cartes.get(4).getValeur(), "drawable", getPackageName()));
			carte6.setImageResource(getResources().getIdentifier(cartes.get(5).getCouleur().toLowerCase()+cartes.get(5).getValeur(), "drawable", getPackageName()));
			carte7.setImageResource(getResources().getIdentifier(cartes.get(6).getCouleur().toLowerCase()+cartes.get(6).getValeur(), "drawable", getPackageName()));
			carte8.setImageResource(getResources().getIdentifier(cartes.get(7).getCouleur().toLowerCase()+cartes.get(7).getValeur(), "drawable", getPackageName()));
		}
		
		for (int i=0; i<4; i++) {
			if (joueurs[i].equalsIgnoreCase(identifiant))
				rang = i;
		}
		
		joueur2 = (TextView)findViewById(R.id.joueur2);
		joueur2.setText(joueurs[(rang+1)%4]);
		joueur3 = (TextView)findViewById(R.id.joueur3);
		joueur3.setText(joueurs[(rang+2)%4]);
		joueur4 = (TextView)findViewById(R.id.joueur4);
		joueur4.setText(joueurs[(rang+3)%4]);

		// Assign the touch listener to your view which you want to move :
		findViewById(R.id.carte1).setOnTouchListener(new MyTouchListener());
		findViewById(R.id.carte2).setOnTouchListener(new MyTouchListener());
		findViewById(R.id.carte3).setOnTouchListener(new MyTouchListener());
		findViewById(R.id.carte4).setOnTouchListener(new MyTouchListener());
		findViewById(R.id.carte5).setOnTouchListener(new MyTouchListener());
		findViewById(R.id.carte6).setOnTouchListener(new MyTouchListener());
		findViewById(R.id.carte7).setOnTouchListener(new MyTouchListener());
		findViewById(R.id.carte8).setOnTouchListener(new MyTouchListener());

		//On appelle la méthode de lancement de partie ci dessous
		//lancePartie();
	}
	//Fin de onCreate()
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myReceiver);
	}

	//TODO : virer cette méthode dans la version finale : (ici pour la simulation
	public void commencerSimulation(View view) {
		joueur3.setTextColor(getResources().getColor(R.color.greenLight));
		findViewById(R.id.commencerSimul).setVisibility(View.GONE);
		
		LinearLayout annonceJ3 = (LinearLayout) findViewById(R.id.annonceLayoutJoueur3);
		annonceJ3.setVisibility(View.VISIBLE);
		((TextView)findViewById(R.id.annonceJ3Couleur)).setText("Carreau");
		((TextView)findViewById(R.id.annonceJ3Points)).setText("80");
		
		
		LinearLayout annonceJ4 = (LinearLayout) findViewById(R.id.annonceLayoutJoueur4);
		annonceJ4.setVisibility(View.VISIBLE);
		((TextView)findViewById(R.id.annonceJ4Couleur)).setText("Trefle");
		((TextView)findViewById(R.id.annonceJ4Points)).setText("90");
		
		LinearLayout annonceJ2 = (LinearLayout) findViewById(R.id.annonceLayoutJoueur2);
		annonceJ2.setVisibility(View.VISIBLE);
		((View)findViewById(R.id.annonceJ2Couleur)).setVisibility(View.GONE);
		((View)findViewById(R.id.annonceJ2Points)).setVisibility(View.GONE);
		((TextView)findViewById(R.id.annonceJ2String)).setText("PASSE");
		
		annoncer();
		
		((Button)findViewById(R.id.continuerSimul)).setVisibility(View.VISIBLE);
	}
	public void continuerSimulation(View view) {
		if (simul == 0) {
			simul = 1;
			((Button)findViewById(R.id.continuerSimul)).setVisibility(View.GONE);
			
			LinearLayout annonceLayout = (LinearLayout) findViewById(R.id.annonceLayout);
			annonceLayout.setVisibility(View.VISIBLE);
			((TextView)findViewById(R.id.annonceCouleur)).setText(((TextView)findViewById(R.id.annonceJ1Couleur)).getText());
			((TextView)findViewById(R.id.annoncePoints)).setText(((TextView)findViewById(R.id.annonceJ1Points)).getText());
			
			ImageView myImage = (ImageView) findViewById(R.id.carte4);
			myImage.setAlpha(127);
			ImageView myImage2 = (ImageView) findViewById(R.id.carte5);
			myImage2.setAlpha(127);
			ImageView myImage3 = (ImageView) findViewById(R.id.carte6);
			myImage3.setAlpha(127);
			ImageView myImage4 = (ImageView) findViewById(R.id.carte7);
			myImage4.setAlpha(127);
			ImageView myImage5 = (ImageView) findViewById(R.id.carte8);
			myImage5.setAlpha(127);
			
			
			LinearLayout annonceJ3 = (LinearLayout) findViewById(R.id.annonceLayoutJoueur3);
			annonceJ3.setVisibility(View.GONE);
			
			LinearLayout annonceJ4 = (LinearLayout) findViewById(R.id.annonceLayoutJoueur4);
			annonceJ4.setVisibility(View.GONE);
			
			LinearLayout annonceJ2 = (LinearLayout) findViewById(R.id.annonceLayoutJoueur2);
			annonceJ2.setVisibility(View.GONE);
			
			LinearLayout annonceJ1 = (LinearLayout) findViewById(R.id.annonceLayoutJoueur1);
			annonceJ1.setVisibility(View.GONE);
		
			((ImageView)findViewById(R.id.cartejouee3)).setImageDrawable(getResources().getDrawable(R.drawable.carreau11));
			((ImageView)findViewById(R.id.cartejouee4)).setImageDrawable(getResources().getDrawable(R.drawable.carreau8));
			
		}
		else if(simul == 1) {
			simul = 2;
			((Button)findViewById(R.id.plis)).setVisibility(View.VISIBLE);
			
			((Button)findViewById(R.id.continuerSimul)).setVisibility(View.GONE);
			LinearLayout annonceLayout = (LinearLayout) findViewById(R.id.annonceLayout);
			annonceLayout.setVisibility(View.VISIBLE);
			((TextView)findViewById(R.id.annonceCouleur)).setText(((TextView)findViewById(R.id.annonceJ1Couleur)).getText());
			((TextView)findViewById(R.id.annoncePoints)).setText(((TextView)findViewById(R.id.annonceJ1Points)).getText());
			
			ImageView myImage = (ImageView) findViewById(R.id.carte1);
			myImage.setAlpha(127);
			ImageView myImage2 = (ImageView) findViewById(R.id.carte2);
			myImage2.setAlpha(127);
			ImageView myImage3 = (ImageView) findViewById(R.id.carte3);
			myImage3.setAlpha(127);
			ImageView myImage4 = (ImageView) findViewById(R.id.carte4);
			myImage4.setAlpha(255);
			ImageView myImage5 = (ImageView) findViewById(R.id.carte5);
			myImage5.setAlpha(255);
			ImageView myImage6 = (ImageView) findViewById(R.id.carte6);
			myImage6.setAlpha(255);
			

			((ImageView)findViewById(R.id.cartejouee3)).setImageDrawable(getResources().getDrawable(R.drawable.pique7));
			((ImageView)findViewById(R.id.cartejouee4)).setImageDrawable(getResources().getDrawable(R.drawable.pique13));
			((ImageView)findViewById(R.id.cartejouee1)).setVisibility(View.GONE);
			((ImageView)findViewById(R.id.cartejouee2)).setVisibility(View.GONE);
		}
		
	}
	
	public void afficherplis(View view) {
		if (!afficherPlis) {
			((LinearLayout)findViewById(R.id.cartesPlis)).setVisibility(View.VISIBLE);
			afficherPlis = true;
		}
		else {
			((LinearLayout)findViewById(R.id.cartesPlis)).setVisibility(View.GONE);
			afficherPlis = false;
		}
	
	}
	
//	public void lancePartie(View view) {
//
//
//		try {
//			SharedPreferences settings = getSharedPreferences("jadeChatPrefsFile", 0);
//			String host = settings.getString("defaultHost", "");
//			String port = settings.getString("defaultPort", "");
//			startChat(identifiant, host, port, agentStartupCallback);
//			SharedPreferences.Editor editor = settings.edit();
//			editor.putBoolean(MainActivity.CONNECTE, true);
//			editor.commit();
//			//				Intent intent = new Intent(this, MainActivity.class);
//			//				intent.putExtra("identifiant", identifiant);
//			//				startActivity(intent);
//		} catch (Exception ex) {
//			logger.log(Level.SEVERE, "Unexpected exception creating chat agent!");
//		}
//	}

	// This defines your touch listener
	private final class MyTouchListener implements OnTouchListener {
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
				ClipData data = ClipData.newPlainText("", "");
				DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
				view.startDrag(data, shadowBuilder, view, 0);
				view.setVisibility(View.GONE);
				return true;
			} else {
				return false;
			}
		}
	} 		

	class MyDragListener implements OnDragListener {
		//Drawable enterShape = getResources().getDrawable(R.id.tapis);
		//Drawable normalShape = getResources().getDrawable(R.id.tapis);

		TextView deposer = (TextView)findViewById(R.id.deposer);

		@Override
		public boolean onDrag(View v, DragEvent event) {
			switch (event.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED:
				// Do nothing
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				// original : v.setBackgroundDrawable(enterShape);
				deposer.setText("Lacher pour deposer");

				break;
			case DragEvent.ACTION_DRAG_EXITED:        
				//original : v.setBackgroundDrawable(normalShape);
				View view2 = (View) event.getLocalState();
				view2.setVisibility(View.VISIBLE);
				deposer.setText("");
				break;
			case DragEvent.ACTION_DROP:
				// Dropped, reassign View to ViewGroup
				ImageView carteJouee = (ImageView) event.getLocalState();
				ImageView carteTapis = (ImageView) findViewById(R.id.cartejouee1);
				carteTapis.setImageDrawable(carteJouee.getDrawable());
				deposer.setText("");
				
				//TODO : virer ce qui suit (fait partie de la simulation) :
				if (simul == 1) {
					((ImageView)findViewById(R.id.cartejouee2)).setImageDrawable(getResources().getDrawable(R.drawable.carreau9));
					((Button)findViewById(R.id.continuerSimul)).setVisibility(View.VISIBLE);
				}
				else if (simul == 2){
					((Button)findViewById(R.id.continuerSimul)).setVisibility(View.GONE);
					((ImageView)findViewById(R.id.cartejouee2)).setImageDrawable(getResources().getDrawable(R.drawable.pique8));
				}
				
				((ImageView)findViewById(R.id.cartejouee1)).setVisibility(View.VISIBLE);
				((ImageView)findViewById(R.id.cartejouee2)).setVisibility(View.VISIBLE);
				//-------------
				
				break;
			case DragEvent.ACTION_DRAG_ENDED:
				//v.setBackgroundDrawable(normalShape);
			default:
				break;
			}
			return true;
		}
	} 

	//Affiche la boite de dialogue pour annoncer :
	public void annoncer() {

		//Boite de dialogue pour annoncer :
		// ------ code récupéré de : http://www.tutomobile.fr/personnaliser-un-alertdialog-tutoriel-android-n%C2%B020/04/11/2010/
		LayoutInflater factory = LayoutInflater.from(this);
		final View annonceDialogView = factory.inflate(R.layout.annonce, null);

		//Si on a pas encore créé le builder, on le fait :
		if (adb == null) { //Note: adb est un attribut de la class
			adb = new AlertDialog.Builder(PartieActivity.this);
		}

		//On affecte la vue personnalisé que l'on a crée à notre AlertDialog
		adb.setView(annonceDialogView);

		//On donne un titre à l'AlertDialog
		adb.setTitle("A vous d'annoncer :");

		//On peut modifier l'icone si besoin (TODO)
		//adb.setIcon(android.R.drawable.ic_dialog_alert);

		//On affecte un bouton "Annoncer" à notre AlertDialog et on lui affecte un évènement
		adb.setPositiveButton("Annoncer", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				LayoutInflater factory = LayoutInflater.from(PartieActivity.this);
				final View annonceDialogView = factory.inflate(R.layout.annonce, null);
				//TODO : c'est ici qu'on va gérer l'envoi de l'annonce à notre serveur en Jade
				//Lorsque l'on cliquera sur le bouton "OK", on récupère l'EditText correspondant à notre vue personnalisée (cad à alertDialogView)
				//EditText et = (EditText)alertDialogView.findViewById(R.id.EditText1);

				//On affiche dans un Toast le texte contenu dans l'EditText de notre AlertDialog
				//Toast.makeText(Tutoriel18_Android.this, et.getText(), Toast.LENGTH_SHORT).show();
				
				//Pour l'instant, on s'occupe de la démo !
				LinearLayout annonceJ1 = (LinearLayout) findViewById(R.id.annonceLayoutJoueur1);
				annonceJ1.setVisibility(View.VISIBLE);
				
				//changes.firePropertyChange("finAnnonce",null,null);
				if (MicroRuntime.isRunning()) {
					Log.d("PartieActivity", "MicroRuntime Running");
					try {
						AgentController ac = MicroRuntime.getAgent(identifiant);
						joueurInterface = ac.getO2AInterface(JoueurInterface.class);
						if (joueurInterface != null)
							joueurInterface.annonce(Integer.parseInt(pointAnnonce), couleurAnnonce);
					} catch (StaleProxyException e) {
					} catch (ControllerException e) {
					}
				}
				else {
					Log.d("RejoindrePartieActivity", "MicroRuntime stopped");
				}
				
			} });
		
		adb.setNeutralButton("Coincher", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//TODO : c'est ici qu'on va gérer signaler que le joueur coinche à notre serveur en Jade

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
		spinnerAnnonce.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		        pointAnnonce = (String) parent.getItemAtPosition(pos);
		        ((TextView)findViewById(R.id.annonceJ1Points)).setText(pointAnnonce);
		    }
		    public void onNothingSelected(AdapterView<?> parent) {
		    }
		});

		spinnerCouleur=(Spinner)annonceDialogView.findViewById(R.id.spinnerAnnonce);
		ArrayAdapter<String> adapterCouleur = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, couleurs);
		adapterCouleur.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerCouleur.setAdapter(adapterCouleur);
		spinnerCouleur.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		        couleurAnnonce = (String) parent.getItemAtPosition(pos);
		        ((TextView)findViewById(R.id.annonceJ1Couleur)).setText((String)couleurAnnonce);
		    }
		    public void onNothingSelected(AdapterView<?> parent) {
		    }
		});


		//On affiche :
		adb.show();
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		//getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().hide();

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
	
	public class MyReceiver extends BroadcastReceiver {
		
		public MyReceiver() {
	        super();
	        Log.d("Annonce", "Receiver constructed");
	    }

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("Annonce", "receive");
			String action = intent.getAction();

			if (action.equalsIgnoreCase("coinchutc.ANNONCE")) {
				Log.d("Annonce", "Receive " + action);
				annoncer();
			}
		}
	}
}


