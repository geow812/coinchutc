package utc.coinchutc.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

import java.util.logging.Level;

import utc.coinchutc.ConnexionActivity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

@SuppressWarnings("serial")
public class ConnexionAgent extends Agent implements ConnexionInterface {
	private Logger logger = Logger.getJADELogger(this.getClass().getName());
	private AID[] players = new AID[3];
	private Context context;
	private String mdp;
	private String option;
	//private ACLMessage spokenMsg;
	//private static final String CHAT_ID = "__chat__";
	private static final String LOGAGENT_NAME = "androidconn";
	private static final String DECONNEXION = "__deconnexion__";
	private static final String LOGINSUCCESS = "__login_success__";
	private static final String LOGINFAIL = "__login_fail__";
	
	protected void setup() {
		
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			if (args[0] instanceof Context) {
				context = (Context) args[0];
			}
			if (args.length > 1) {
//				System.out.println("Test");
				mdp = (String) args[1];
			}
			if (args.length > 2) {
				
				option = (String) args[2];
			}
		}
		
		// Enregistrer l'agent dans le DF
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Connextion");
		sd.setName(getLocalName());
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
			Log.d("ConnexionAgent", " "+getAID().getName()+" is registered as '"+sd.getType()+"'");
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		addBehaviour(new ConnexionBehaviour(this));
		
		registerO2AInterface(ConnexionInterface.class, this);
	}
	
	private void notifyPlayersChanged() {
		Intent broadcast = new Intent();
		broadcast.setAction("chat.REFRESH_PARTICIPANTS");
		broadcast.putExtra("players", getPlayersNames());
		logger.log(Level.INFO, "Sending broadcast " + broadcast.getAction());
		context.sendBroadcast(broadcast);
	}
	
	private void notifyLoginSuccess() {
		Intent broadcast = new Intent();
		broadcast.setAction("coinchutc.LOGIN_SUCCESS");
		Log.d("ClientAgent", "Sending broadcast " + broadcast.getAction());
		context.sendBroadcast(broadcast);
	}
	
	private void notifyLoginFail() {
		Intent broadcast = new Intent();
		broadcast.setAction("coinchutc.LOGIN_FAIL");
		logger.log(Level.SEVERE, "Sending broadcast " + broadcast.getAction());
		context.sendBroadcast(broadcast);
	}
	
	/**
	 * Inner class ConnexionBehaviour. This behaviour registers as a chat
	 * participant and keeps the list of participants up to date by managing the
	 * information received from the ChatManager agent.
	 */
	class ConnexionBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = -4845730529175649756L;
		private MessageTemplate template;

		ConnexionBehaviour(Agent a) {
			super(a);
		}

		public void onStart() {
			ACLMessage demande = null;
			if (option == ConnexionActivity.CONNECTER) { 
				// To login with existing account
				demande = new ACLMessage(ACLMessage.SUBSCRIBE);
			}
			else {
				// To create an account
				demande = new ACLMessage(ACLMessage.REQUEST);
			}
			String convId = myAgent.getLocalName();//Conn-identifiant
			demande.setConversationId(convId);
			demande.addReceiver(new AID(LOGAGENT_NAME, AID.ISLOCALNAME));
			demande.setContent(mdp);
			Log.d("ClientAgent", myAgent.getLocalName() + " sends message to " + LOGAGENT_NAME + ": " + mdp);
			myAgent.send(demande);
			
			// Initialize the template used to receive notifications
			// from the ChatManagerAgent
			template = MessageTemplate.MatchConversationId(convId);
		}

		public void action() {
			// Receives information about people joining and leaving
			// the chat from the ChatManager agent
			ACLMessage msg = myAgent.receive(template);
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.INFORM_IF) {
					// Receives message if login succeeds
					String answer = msg.getContent();
					if (answer.equalsIgnoreCase(LOGINSUCCESS)) {
						Log.d("ClientAgent", myAgent.getLocalName() + " receives login success message");
						notifyLoginSuccess();
						notifyPlayersChanged();
					}
					else if (answer.equalsIgnoreCase(LOGINFAIL)) {
						notifyLoginFail();
					}
				}
				else if (msg.getPerformative() == ACLMessage.INFORM) {
					//String player = msg.getContent();
					//players.add(player);
					notifyPlayersChanged();
				}
			} else {
				block();
			}
		}
	} // END of inner class ConnexionBehaviour

	@Override
	public String[] getPlayersNames() {
		String[] pp = new String[3];
		int ind=0;
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Chat");
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			if (result.length > 0) {
				for (int i=0;i<result.length;i++)
				{
					if (!result[i].getName().equals(this.getAID()))
					{
						players[ind] = result[i].getName();
						ind++;
					}
				}

			}
			else System.out.println("Erreur lors de la creation des receivers");
		}
		catch(FIPAException fe) {  }
		ind=0;
		
		for (AID player : players) {
			if (player != null) {
				pp[ind] = player.getLocalName();
				ind ++;
			}
		}
		return pp;
	}

	@Override
	public void deconnexion() {
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
		message.addReceiver(new AID(LOGAGENT_NAME, AID.ISLOCALNAME));
		message.setContent(DECONNEXION);
		this.send(message);
	}

}
