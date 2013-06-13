package utc.coinchutc;

import jade.core.MicroRuntime;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class RejoindrePartieActivity extends Activity {
	
	private String identifiant = "";
	private JoueurInterface joueurInterface = null;

	//ArrayList that will hold the original Data
	private ArrayList<HashMap<String, Object>> players;
	private LayoutInflater inflater;
	private ListView list_players;
	private MyReceiver myReceiver = new MyReceiver();

	protected static String names[];
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rejoindre_partie);
		list_players = (ListView) findViewById(R.id.list_connected_players);
		
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

		//get the LayoutInflater for inflating the customomView
		//this will be used in the custom adapter
		inflater=(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		//these arrays are just the data that
		//I'll be using to populate the ArrayList
		//You can use our own methods to get the data
		String names[]={"Ronaldo","Messi","Torres","Iniesta"};

		Integer[] photos={R.drawable.rclermon,R.drawable.rclermon,
				R.drawable.rclermon,R.drawable.rclermon};

		players=new ArrayList<HashMap<String,Object>>();

		//temporary HashMap for populating the
		//Items in the ListView
		HashMap<String , Object> temp;

		//total number of rows in the ListView
		int noOfPlayers=names.length;

		//now populate the ArrayList players
		for(int i=0;i<noOfPlayers;i++)
		{
			temp=new HashMap<String, Object>();

			temp.put("name", names[i]); 
			temp.put("photo", photos[i]);

			//add the row to the ArrayList
			players.add(temp);       
		}

		/*create the adapter
		 *first param-the context
		 *second param-the id of the layout file
  you will be using to fill a row
		 *third param-the set of values that
   will populate the ListView */
		final CustomAdapter adapter=new CustomAdapter(this, R.layout.list_connected_players, players);
		list_players.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) { 
				View view = (View) findViewById(R.layout.profil_dialogbox);
				LayoutInflater factory = LayoutInflater.from(RejoindrePartieActivity.this);
				final View profilDialogBoxView = factory.inflate(R.layout.profil_dialogbox, null);
				String value = list_players.getItemAtPosition(position).toString();
				Log.i("Position:", value);
				AlertDialog.Builder builder = new AlertDialog.Builder(RejoindrePartieActivity.this);
				builder.setTitle("Profil de RÈmi")
				.setView(profilDialogBoxView)
				.setIcon(R.drawable.rclermon)
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
	}

	public void envoyer(View view) {
		TextView chatField = (TextView) findViewById(R.id.textInput);
		String msg = chatField.getText().toString();
		Log.d("RejoindrePartieActivity", "Message to send: " + msg);
		if (MicroRuntime.isRunning()) {
			Log.d("RejoindrePartieActivity", "MicroRuntime Running");
			try {
				AgentController ac = MicroRuntime.getAgent(identifiant);
				if (ac == null)
					Log.d("RejoindrePartieActivity", "Error getting controlleur");
				joueurInterface = ac.getO2AInterface(JoueurInterface.class);
				if (joueurInterface != null) {
					joueurInterface.sendMessage(msg);
				}
				else {
					Log.d("RejoindrePartieActivity", "Error getting interface");
				}
			} catch (StaleProxyException e) {
				showAlertDialog(getString(R.string.msg_interface_exc), true);
			} catch (ControllerException e) {
				showAlertDialog(getString(R.string.msg_controller_exc), true);
			}
		}
		else {
			Log.d("RejoindrePartieActivity", "MicroRuntime stopped");
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
			checkBoxState=new boolean[players.size()];
		}


		//class for caching the views in a row 
		private class ViewHolder
		{
			ImageView photo;
			TextView name;
			CheckBox checkBox;
		}



		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if(convertView==null)
			{
				convertView=inflater.inflate(R.layout.list_connected_players, null);
				viewHolder=new ViewHolder();

				//cache the views
				viewHolder.photo=(ImageView) convertView.findViewById(R.id.imgIcon);
				viewHolder.name=(TextView) convertView.findViewById(R.id.playerName);
				viewHolder.checkBox=(CheckBox) convertView.findViewById(R.id.checkboxReady);

				//link the cached views to the convertview
				convertView.setTag( viewHolder);


			}
			else
				viewHolder=(ViewHolder) convertView.getTag();


			int photoId=(Integer) players.get(position).get("photo");

			//set the data to be displayed
			viewHolder.photo.setImageDrawable(getResources().getDrawable(photoId));
			viewHolder.name.setText(players.get(position).get("name").toString());

			//VITAL PART!!! Set the state of the
			//CheckBox using the boolean array
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