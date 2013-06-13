package utc.coinchutc;

import jade.core.MicroRuntime;
import jade.wrapper.AgentController;

import java.util.logging.Level;

import utc.coinchutc.agent.JoueurAgent;
import utc.coinchutc.agent.JoueurInterface;
import android.app.Activity;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.EditText;
import android.widget.TextView;

public class RejoindrePartieActivity extends Activity {
	
	private String identifiant = "";
	private JoueurInterface joueurInterface = null;
	TextView text = null;
	private ArrayList<HashMap<String, Object>> players;
	private LayoutInflater inflater;
	private ListView list_players;
	private MyReceiver myReceiver = new MyReceiver();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rejoindre_partie);
		layout = (RelativeLayout) RelativeLayout.inflate(this, R.layout.activity_rejoindre_partie, null);
		
		// get the identifiant from the intent
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Log.d("RejoindrePartieActivity", "Receive id " + identifiant);
			identifiant = extras.getString("identifiant");
		}
		
		// get the broadcast for chat
		IntentFilter loginFilter = new IntentFilter();
		loginFilter.addAction("coinchutc.REFRESH_CHAT");
		registerReceiver(myReceiver, loginFilter);
		if (extras != null) {
			identifiant = extras.getString("identifiant");
		}
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 

		// create the agent of type JoueurAgent
		if (MicroRuntime.isRunning()) {
			Log.d("RejoindrePartieActivity", "MicroRuntime Running");
			try {
				MicroRuntime.startAgent(identifiant, JoueurAgent.class.getName(), new Object[] { getApplicationContext() });
			} catch (Exception e) {
				Log.d("RejoindrePartieActivity", "Error creating Agent");
			} 
		}
		else {
			Log.d("RejoindrePartieActivity", "MicroRuntime stopped");
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myReceiver);
	}
	public void envoyer(View view) {
		TextView chatField = (TextView) findViewById(R.id.textInput);
		String msg = chatField.getText().toString();
		Log.d("RejoindrePartieActivity", "Message to send: " + msg);
		if (MicroRuntime.isRunning()) {
			Log.d("RejoindrePartieActivity", "MicroRuntime Running");
			try {
				AgentController ac = MicroRuntime.getAgent(identifiant);
				joueurInterface = ac.getO2AInterface(JoueurAgent.class);
				joueurInterface.sendMessage(msg);
				
			} catch (Exception e) {
				Log.d("RejoindrePartieActivity", "Error getting Agent");
			} 
		}
		else {
			Log.d("RejoindrePartieActivity", "MicroRuntime stopped");
		}
	}

	private class MyReceiver extends BroadcastReceiver {
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equalsIgnoreCase("coinchutc.REFRESH_CHAT")) {
				//ShowDialog("Login succeeded!");
				Bundle extras = intent.getExtras();
				if (extras != null) {
					String sender = extras.getString("sender");
					String message = extras.getString("chat");
					
					refreshChat(sender, message);
					Log.d("RejoindrePartieActivity", "Chat Refreshed");
				}

			}
		}

		private void refreshChat(String sender, String message) {
			// TODO Auto-generated method stub
			TextView chatField = (TextView) findViewById(R.id.textOutput);
			//identifiant = nameField.getText().toString();
			chatField.append("\n" + sender + ": " + message);
		}
	}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.rejoindre_partie, menu);
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

	public void partie(View view) {
		Intent intent = new Intent(this, PartieActivity.class);
		intent.putExtra("identifiant", identifiant);
		startActivity(intent);
	}
}
