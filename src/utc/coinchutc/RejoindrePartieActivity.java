package utc.coinchutc;

import utc.coinchutc.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RejoindrePartieActivity extends Activity {

	private String identifiant = "";
	RelativeLayout layout = null;
	TextView text = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rejoindre_partie);
		layout = (RelativeLayout) RelativeLayout.inflate(this, R.layout.activity_rejoindre_partie, null);
	    text = (TextView) layout.findViewById(R.id.profilName1);
	    text.setText("Rémi");
	    text = (TextView) layout.findViewById(R.id.profilName2);
	    text.setText("Rémi");
	    text = (TextView) layout.findViewById(R.id.profilName3);
	    text.setText("Rémi");
	    text = (TextView) layout.findViewById(R.id.profilName4);
	    text.setText("Rémi");
	    setContentView(layout);
		// Show the Up button in the action bar.
		setupActionBar();
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			identifiant = extras.getString("identifiant");
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
