package utc.coinchutc;

import jade.core.MicroRuntime;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import utc.coinchutc.agent.CoincheClientInterface;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	
	public static final String CONNECTE = "connecte";
	private boolean connecte = false;
	private String identifiant = "";
	private CoincheClientInterface coincheClientInterface = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		SharedPreferences settings = getSharedPreferences("jadeChatPrefsFile",0);
		connecte = settings.getBoolean(CONNECTE, false);
		if (!connecte) {
			finish();
			login();
		}
		else {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				identifiant = extras.getString("identifiant");
				//showAlertDialog(identifiant, false);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void creerPartie(View view) {
		Intent intent = new Intent(this, CreerPartieActivity.class);
		startActivity(intent);
	}
	
	public void rejoindrePartie(View view) {
		Intent intent = new Intent(this, RejoindrePartieActivity.class);
		startActivity(intent);
	}
	
	public void joueursConnectes(View view) {
		Intent intent = new Intent(this, JoueursConnectesActivity.class);
		startActivity(intent);
	}
	
	public void coinchUtc(View view) {
		Intent intent = new Intent(this, CoinchUtcActivity.class);
		startActivity(intent);
	}
	
	public void reglesDuJeu(View view) {
		Intent intent = new Intent(this, ReglesDuJeuActivity.class);
		startActivity(intent);
	}
	
	public void credits(View view) {
		Intent intent = new Intent(this, CreditsActivity.class);
		startActivity(intent);
	}
	
	public void login() {
		Intent intent = new Intent(this, ConnexionActivity.class);
		startActivity(intent);
	}
	
	public void deconnecter(View view) {
		if (MicroRuntime.isRunning()) {
			try {
				AgentController ac = MicroRuntime.getAgent(identifiant);
//				showAlertDialog(ac.getName(), false);
				coincheClientInterface = ac.getO2AInterface(CoincheClientInterface.class);
				if (coincheClientInterface != null)
					coincheClientInterface.deconnexion();
			} catch (StaleProxyException e) {
				showAlertDialog(getString(R.string.msg_interface_exc), true);
			} catch (ControllerException e) {
				showAlertDialog(getString(R.string.msg_controller_exc), true);
			}
		}
		SharedPreferences settings = getSharedPreferences("jadeChatPrefsFile", 0);
		SharedPreferences.Editor editor = settings.edit();
	    editor.putBoolean(CONNECTE, false);
	    editor.commit();
	    Intent intent = new Intent(this, ConnexionActivity.class);
		startActivity(intent);
	}
	
	private void showAlertDialog(String message, final boolean fatal) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				MainActivity.this);
		builder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog, int id) {
								dialog.cancel();
								if(fatal) finish();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();		
	}

}
