package utc.coinchutc;

import jade.util.Logger;

import java.util.logging.Level;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class JoueursConnectesActivity extends Activity {
	
	private MyReceiver myReceiver = new MyReceiver();
	private Logger logger = Logger.getJADELogger(this.getClass().getName());
	private String[] joueurs = new String[]{};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_joueurs_connectes);
		// Show the Up button in the action bar.
		setupActionBar();
		Log.d("JoueursConnecteActivity", "Enter Activity!");
		final ListView listview = (ListView) findViewById(R.id.listJoueurs);
		
		IntentFilter loginFilter = new IntentFilter();
		loginFilter.addAction("chat.REFRESH_PARTICIPANTS");
		registerReceiver(myReceiver, loginFilter);

		//Log.d("JoueursConnecteActivity", "Test 1!");
		if (joueurs.length == 0) {
			String[] emptyMsg = {"Il n'y a pas de joueurs"};
			listview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, emptyMsg));
		}
		else {
			listview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, joueurs));
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
		getMenuInflater().inflate(R.menu.joueurs_connectes, menu);
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
	
	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			logger.log(Level.INFO, "Received intent " + action);
			if (action.equalsIgnoreCase("chat.REFRESH_PARTICIPANTS")) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					joueurs = extras.getStringArray("players");
				}
			}
		}
	}

}
