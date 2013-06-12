package utc.coinchutc;

import jade.core.MicroRuntime;
import jade.core.NotFoundException;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import utc.coinchutc.agent.ConnextionInterface;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	
	public static final String CONNECTE = "connecte";
	private boolean connecte = false;
	private String identifiant = "";
	private ConnextionInterface coincheClientInterface = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		SharedPreferences settings = getSharedPreferences("jadeChatPrefsFile",0);
		connecte = settings.getBoolean(CONNECTE, false);
		Log.d("MainActivity", "MainActivity starts!");
		Bundle extras = getIntent().getExtras();
		if (extras != null && connecte) {
			Log.d("MainActivity", identifiant);
			identifiant = extras.getString("identifiant");
		}
		else {
			finish();
			login();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void rejoindrePartie(View view) {
		Intent intent = new Intent(this, RejoindrePartieActivity.class);
		intent.putExtra("identifiant", identifiant);
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
				AgentController ac = MicroRuntime.getAgent("Conn-" + identifiant);
				coincheClientInterface = ac.getO2AInterface(ConnextionInterface.class);
				if (coincheClientInterface != null)
					coincheClientInterface.deconnexion();
				try {
					MicroRuntime.killAgent("Conn-" + identifiant);
					//MicroRuntime.detach();
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
