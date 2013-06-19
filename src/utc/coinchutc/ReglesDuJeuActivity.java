package utc.coinchutc;

import utc.coinchutc.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class ReglesDuJeuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_regles_du_jeu);
		// Show the Up button in the action bar.
		setupActionBar();
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
		getMenuInflater().inflate(R.menu.regles_du_jeu, menu);
		return true;
	}
	
	@Override
	public void onBackPressed () {
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("identifiant", MainActivity.identifiant);
		startActivity(intent);
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
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra("identifiant", MainActivity.identifiant);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void reglePrincipe(View view) {
		Intent intent = new Intent(this, ReglePrincipeActivity.class);
		startActivity(intent);
	}
	
	public void regleContrat(View view) {
		Intent intent = new Intent(this, RegleContratActivity.class);
		startActivity(intent);
	}
	
	public void regleCoinche(View view) {
		Intent intent = new Intent(this, RegleCoincheActivity.class);
		startActivity(intent);
	}
	
	public void regleDeroulement(View view) {
		Intent intent = new Intent(this, RegleDeroulementActivity.class);
		startActivity(intent);
	}
	
	public void reglePoints(View view) {
		Intent intent = new Intent(this, ReglePointsActivity.class);
		startActivity(intent);
	}
	
	public void regleAnnonces(View view) {
		Intent intent = new Intent(this, RegleAnnoncesActivity.class);
		startActivity(intent);
	}
	
	

}
