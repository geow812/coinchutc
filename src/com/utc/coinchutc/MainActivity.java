package com.utc.coinchutc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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

}
