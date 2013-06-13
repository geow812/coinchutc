package utc.coinchutc.agent;

import jade.content.ContentManager;
import jade.content.Predicate;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;
import jade.util.leap.Iterator;
import jade.util.leap.Set;
import jade.util.leap.SortedSetImpl;

import java.util.List;
import java.util.logging.Level;

import utc.coinchutc.ConnexionActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import chat.ontology.ChatOntology;
import chat.ontology.Joined;
import chat.ontology.Left;

@SuppressWarnings("serial")
public class CoincheClientAgent extends Agent implements CoincheClientInterface {
	private Logger logger = Logger.getJADELogger(this.getClass().getName());
	private Set players = new SortedSetImpl();
	private Context context;
	private String mdp;
	private String option;
	private Codec codec = new SLCodec();
	private Ontology onto = ChatOntology.getInstance();
	private ACLMessage spokenMsg;
	private static final String CHAT_ID = "__chat__";
	private static final String CHAT_MANAGER_NAME = "manager";
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
				System.out.println(option);
			}
		}

		// Register language and ontology
		ContentManager cm = getContentManager();
		cm.registerLanguage(codec);
		cm.registerOntology(onto);
		cm.setValidationMode(false);

		// Add initial behaviours
		addBehaviour(new PlayersManager(this));
		addBehaviour(new PartieListener(this));

		// Initialize the message used to convey spoken sentences
		spokenMsg = new ACLMessage(ACLMessage.INFORM);
		spokenMsg.setConversationId(CHAT_ID);
		
		Intent broadcast = new Intent();
		broadcast.setAction("jade.demo.chat.SHOW_CHAT");
		logger.log(Level.INFO, "Sending broadcast " + broadcast.getAction());
		context.sendBroadcast(broadcast);
		
		registerO2AInterface(CoincheClientInterface.class, this);
	}
	
	private void notifyPlayerAction(String speaker, String sentence) {
		// TODO: implement methods when a player acts
		Intent broadcast = new Intent();
		broadcast.setAction("jade.demo.chat.REFRESH_CHAT");
		broadcast.putExtra("sentence", speaker + ": " + sentence + "\n");
		logger.log(Level.INFO, "Sending broadcast " + broadcast.getAction());
		context.sendBroadcast(broadcast);
	}
	
	private void notifyPlayersChanged() {
		Intent broadcast = new Intent();
		broadcast.setAction("jade.demo.chat.REFRESH_PARTICIPANTS");
		logger.log(Level.INFO, "Sending broadcast " + broadcast.getAction());
		context.sendBroadcast(broadcast);
	}
	
	private void notifyLoginSuccess() {
		Intent broadcast = new Intent();
		broadcast.setAction("coinchutc.LOGIN_SUCCESS");
		logger.log(Level.INFO, "Sending broadcast " + broadcast.getAction());
		context.sendBroadcast(broadcast);
	}
	
	private void notifyLoginFail() {
		Intent broadcast = new Intent();
		broadcast.setAction("coinchutc.LOGIN_FAIL");
		logger.log(Level.SEVERE, "Sending broadcast " + broadcast.getAction());
		context.sendBroadcast(broadcast);
	}
	
	/**
	 * Inner class PlayersManager. This behaviour registers as a chat
	 * participant and keeps the list of participants up to date by managing the
	 * information received from the ChatManager agent.
	 */
	class PlayersManager extends CyclicBehaviour {
		private static final long serialVersionUID = -4845730529175649756L;
		private MessageTemplate template;

		PlayersManager(Agent a) {
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
			String convId = "C-" + myAgent.getLocalName();
			demande.setConversationId(convId);
			demande.addReceiver(new AID(CHAT_MANAGER_NAME, AID.ISLOCALNAME));
			demande.setContent(mdp);
			Log.d("ClientAant", "Send message to " + CHAT_MANAGER_NAME + ": " + mdp);
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
						notifyLoginSuccess();
					}
					else if (answer.equalsIgnoreCase(LOGINFAIL)) {
						notifyLoginFail();
					}
				}
				else if (msg.getPerformative() == ACLMessage.INFORM) {
					try {
						Predicate p = (Predicate) myAgent.getContentManager().extractContent(msg);
						if(p instanceof Joined) {
							Joined joined = (Joined) p;
							List<AID> aid = (List<AID>) joined.getWho();
							for(AID a : aid)
								players.add(a);
							notifyPlayersChanged();
						}
						if(p instanceof Left) {
							Left left = (Left) p;
							List<AID> aid = (List<AID>) left.getWho();
							for(AID a : aid)
								players.remove(a);
							notifyPlayersChanged();
						}
					} catch (Exception e) {
						Logger.println(e.toString());
						e.printStackTrace();
					}
				}
			} else {
				block();
			}
		}
	} // END of inner class PlayersManager
	
	class PartieListener extends CyclicBehaviour {
		private static final long serialVersionUID = 741233963737842521L;
		private MessageTemplate template = MessageTemplate
				.MatchConversationId(CHAT_ID);

		PartieListener(Agent a) {
			super(a);
		}

		public void action() {
			ACLMessage msg = myAgent.receive(template);
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.INFORM) {
					notifyPlayerAction(msg.getSender().getLocalName(), msg.getContent());
				}
			} else {
				block();
			}
		}
	} // END of inner class PartieListener

	@Override
	public String[] getPlayersNames() {
		String[] pp = new String[players.size()];
		Iterator it = players.iterator();
		int i = 0;
		while (it.hasNext()) {
			AID id = (AID) it.next();
			pp[i++] = id.getLocalName();
		}
		return pp;
	}

	@Override
	public void handleSpoken(String s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deconnexion() {
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
		message.addReceiver(new AID(CHAT_MANAGER_NAME, AID.ISLOCALNAME));
		message.setContent(DECONNEXION);
		this.send(message);
	}

}
