package utc.coinchutc;

import jade.core.MicroRuntime;
import jade.core.NotFoundException;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;
import java.util.HashMap;

import utc.coinchutc.agent.JoueurAgent;
import utc.coinchutc.agent.JoueurInterface;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class RejoindrePartieActivity extends Activity {

	private String identifiant = "";
	private String[] joueurs = new String[3];
	private JoueurInterface joueurInterface = null;

	//ArrayList that will hold the original Data
	private ArrayList<HashMap<String, Object>> players;
	private LayoutInflater inflater;
	private ListView list_players;
	private MyReceiver myReceiver = new MyReceiver();
	private CustomAdapter adapter;

	public void demoPartie(View view) {
		Intent intent = new Intent(this, PartieActivity.class);
		intent.putExtra("identifiant", identifiant);
		startActivity(intent);

	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rejoindre_partie);
		list_players = (ListView) findViewById(R.id.list_connected_players);
		


		// get the identifiant from the intent
		Bundle extras = getIntent().getExtras();
		if (extras != null) {

			identifiant = extras.getString("identifiant");
			//Log.d("RejoindrePartieActivity", "Receive id " + identifiant);
			
			joueurs = extras.getStringArray("joueurs");
			//Log.d("RejoindrePartieActivity", "Receive joueurs " + joueurs[0]);
		}

		// get the broadcast for chat
		IntentFilter loginFilter = new IntentFilter();
		loginFilter.addAction("coinchutc.REFRESH_CHAT");
		registerReceiver(myReceiver, loginFilter);
		
		// get the broadcast for chat
		IntentFilter joueursFilter = new IntentFilter();
		joueursFilter.addAction("coinchutc.REFRESH_JOUEURS");
		registerReceiver(myReceiver, joueursFilter);
		

		//get the LayoutInflater for inflating the customomView
		//this will be used in the custom adapter
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		players = new ArrayList<HashMap<String,Object>>();
		HashMap<String , Object> temp = new HashMap<String, Object>();
		temp.put("name", identifiant); 
		Integer idImage = getResources().getIdentifier(identifiant, "drawable", getPackageName());
		Log.d("RejoindrePartieActivity", "Receive joueurs photo" + idImage);
		if (idImage == 0) {
			idImage = R.drawable.generic;
		}
		temp.put("photo", idImage);
		players.add(temp); 
		
		for (String joueur: joueurs) {
			if (joueur != null) {
				HashMap<String , Object> temp2 = new HashMap<String, Object>();
				Log.d("RejoindrePartieActivity", "Receive joueurs " + joueur);
				temp2.put("name", joueur); 
				temp2.put("photo", getResources().getIdentifier(joueur, "drawable", getPackageName()) == 0?R.drawable.generic : getResources().getIdentifier(joueur, "drawable", getPackageName()));
				players.add(temp2); 
			}
		}
		
		adapter = new CustomAdapter(this, R.layout.list_connected_players, players);
		
		list_players.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) { 
				LayoutInflater factory = LayoutInflater.from(RejoindrePartieActivity.this);
				final View profilDialogBoxView = factory.inflate(R.layout.profil_dialogbox, null);
				@SuppressWarnings("unchecked")
				HashMap<String , Object> value = (HashMap<String, Object>) list_players.getItemAtPosition(position);
				Log.d("RejoindrePartieActivity:", (String)value.get("name"));
				AlertDialog.Builder builder = new AlertDialog.Builder(RejoindrePartieActivity.this);
				int imageId = (Integer)value.get("photo");
				
				if (imageId == 0) {
					imageId = R.drawable.generic;
				}
				builder.setTitle("Profil de " + (String)value.get("name"))
				.setView(profilDialogBoxView)
				.setIcon((Integer)value.get("photo"))
				.setCancelable(false)
				.setPositiveButton("Fermer", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

					}
				});

				builder.create().show();
			}
		});
		
		//finally,set the adapter to the default ListView
		list_players.setAdapter(adapter);


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
		
		if (MicroRuntime.isRunning()) {
			try {
				MicroRuntime.killAgent(identifiant);
			} catch (NotFoundException e) {
				Log.e("RejoindrePartieActivity", "Agent Not Found!");
				e.printStackTrace();
			}
		}
	}

	public void envoyer(View view) {
		TextView inputField = (TextView) findViewById(R.id.textInput);
		String msg = inputField.getText().toString();
		TextView outputField = (TextView) findViewById(R.id.textOutput);
		outputField.append("\nMoi: " + msg);
		Log.d("RejoindrePartieActivity", "Message to send: " + msg);
		if (MicroRuntime.isRunning()) {
			Log.d("RejoindrePartieActivity", "MicroRuntime Running");
			try {
				AgentController ac = MicroRuntime.getAgent(identifiant);
				if (ac == null)
					Log.e("RejoindrePartieActivity", "Error getting controlleur");
				joueurInterface = ac.getO2AInterface(JoueurInterface.class);
				if (joueurInterface != null) {
					joueurInterface.sendMessage(msg);
				}
				else {
					Log.e("RejoindrePartieActivity", "Error getting interface");
				}
			} catch (StaleProxyException e) {
				showAlertDialog(getString(R.string.msg_interface_exc), true);
			} catch (ControllerException e) {
				showAlertDialog(getString(R.string.msg_controller_exc), true);
			}
		}
		else {
			Log.e("RejoindrePartieActivity", "MicroRuntime stopped");
		}
	}

	private void showAlertDialog(String message, final boolean fatal) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				RejoindrePartieActivity.this);
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

	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equalsIgnoreCase("coinchutc.REFRESH_CHAT")) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					String sender = extras.getString("sender");
					String message = extras.getString("chat");

					refreshChat(sender, message);
					Log.d("RejoindrePartieActivity", "Chat Refreshed");
				}
			}
			else if (action.equalsIgnoreCase("coinchutc.REFRESH_JOUEURS")) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					String sender = extras.getString("sender");
					refreshJoueurs(sender);
					Log.d("RejoindrePartieActivity", "Joueurs Refreshed");
				}
			}
		}

		private void refreshJoueurs(final String sender) {
			HashMap<String, Object> newPlayer = new HashMap<String, Object> ();
			newPlayer.put("name", sender);
			newPlayer.put("photo", getResources().getIdentifier(sender, "drawable", getPackageName()) == 0?R.drawable.generic : getResources().getIdentifier(sender, "drawable", getPackageName()));
			players.add(newPlayer);
			adapter.notifyDataSetChanged();
		}

		private void refreshChat(String sender, String message) {
			TextView chatField = (TextView) findViewById(R.id.textOutput);
			chatField.append("\n" + sender + ": " + message);
		}
	}
	


	//define your custom adapter
	private class CustomAdapter extends ArrayAdapter<HashMap<String, Object>>
	{
		// boolean array for storing
		//the state of each CheckBox
		boolean[] checkBoxState;


		ViewHolder viewHolder;

		public CustomAdapter(Context context, int textViewResourceId,
				ArrayList<HashMap<String, Object>> players) {

			//let android do the initializing :)
			super(context, textViewResourceId, players);

			//create the boolean array with
			//initial state as false
			checkBoxState = new boolean[players.size()];
		}

		//class for caching the views in a row 
		private class ViewHolder
		{
			ImageView photo;
			TextView name;
			CheckBox checkBox;
		}

		@Override
		public int getCount () {
		    return players.size ();
		}


		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if(convertView == null)
			{
				convertView=inflater.inflate(R.layout.list_connected_players, null);
				viewHolder = new ViewHolder();

				//cache the views
				viewHolder.photo = (ImageView) convertView.findViewById(R.id.imgIcon);
				viewHolder.name = (TextView) convertView.findViewById(R.id.playerName);
				viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkboxReady);

				//link the cached views to the convertview
				convertView.setTag( viewHolder);


			}
			else
				viewHolder = (ViewHolder) convertView.getTag();


			int photoId = (Integer) players.get(position).get("photo");

			//set the data to be displayed
			viewHolder.photo.setImageDrawable(getResources().getDrawable(photoId));
			viewHolder.name.setText(players.get(position).get("name").toString());

			//VITAL PART!!! Set the state of the
			//CheckBox using the boolean array
			//When the list is updated we have to update the checkBoxState too
			if (checkBoxState.length <= players.size()) {
				boolean[] temps = checkBoxState.clone();
				checkBoxState = new boolean[players.size()];
				
				for (int i=0; i<temps.length; i++) {
					checkBoxState[i] = temps[i];
				}
			}
			viewHolder.checkBox.setChecked(checkBoxState[position]);


			//for managing the state of the boolean
			//array according to the state of the
			//CheckBox

			viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					if(((CheckBox)v).isChecked())
						checkBoxState[position]=true;
					else
						checkBoxState[position]=false;

				}
			});

			//return the view to be displayed
			return convertView;
		}
	}
}