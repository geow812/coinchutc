package utc.coinchutc.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;

import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

@SuppressWarnings("serial")
public class JoueurAgent extends Agent implements JoueurInterface{
	protected static final int ANNONCE_EVENT = 0;
	protected static final int JEU_EVENT = 1;
	protected static final int REJOINDRE_EVENT = 2;
	protected static final int CHAT_EVENT = 3;
	private static final String CHAT_ID = "__chat__";
	private static final String NOTIF = "__notif__";
	private Carte[] main;
	private String main2;
	private String nom;
	private String prenom;
	private String identifiant = "";
	private Context context;

	private AID[] receivers = new AID[3];
	private String[] joueurs = new String[3];
	private boolean rejoindre = false;
	
	protected void setup()
	{

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			if (args[0] instanceof Context) {
				context = (Context) args[0];
			}
			if (args.length > 1) {
				identifiant = (String) args[1];
			}
		}
		
		main = new Carte[8];
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Chat");
		sd.setName(getLocalName());
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
			System.out.println(" "+getAID().getLocalName()+" is registered as '"+sd.getType()+"'");
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		// notify others players especially android players
		addBehaviour(new NotifyOthersBehaviour());
		addBehaviour(new ReceiveNotifBehaviour());
		
		SequentialBehaviour comportementSequentiel = new SequentialBehaviour();
		comportementSequentiel.addSubBehaviour(new ChatBehaviour());
		comportementSequentiel.addSubBehaviour(new RecupBehaviour());
		comportementSequentiel.addSubBehaviour(new AnnonceBehaviour());
		
		/*SequentialBehaviour jouerTour = new SequentialBehaviour();
		jouerTour.addSubBehaviour(new JouerBehaviour());
		jouerTour.addSubBehaviour(new JouerBehaviour());
		jouerTour.addSubBehaviour(new JouerBehaviour());
		jouerTour.addSubBehaviour(new JouerBehaviour());
		jouerTour.addSubBehaviour(new JouerBehaviour());
		jouerTour.addSubBehaviour(new JouerBehaviour());
		jouerTour.addSubBehaviour(new JouerBehaviour());
		jouerTour.addSubBehaviour(new JouerBehaviour());*/
		
		//comportementSequentiel.addSubBehaviour(jouerTour);
		
		addBehaviour(comportementSequentiel);
		registerO2AInterface(JoueurInterface.class, this);
		
		// initialiser la liste de joueurs connectes
		setReceiver();
		Intent broadcast = new Intent();
		broadcast.setAction("coinchutc.JOUEURS_CONNECTES");
		broadcast.putExtra("moi", identifiant);
		broadcast.putExtra("joueurs", joueurs);
		Log.d("JoueurAgent", "Sending broadcast " + broadcast.getAction());
		context.sendBroadcast(broadcast);
	}
	
	private void setReceiver() {
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
						receivers[ind] = result[i].getName();
						joueurs[ind] = result[i].getName().getLocalName();
						ind++;
						Log.d("JoueurAgent", "Add receiver " + result[i].getName());
					}
				}

			}
			else Log.d("JoueurAgent", "Erreur lors de la creation des receivers");
		}
		catch(FIPAException fe) {  }
		ind=0;
	}
	
	public class NotifyOthersBehaviour extends OneShotBehaviour {

		@Override
		public void action() {
			ACLMessage notif = new ACLMessage(ACLMessage.INFORM);
			notif.setConversationId(NOTIF);
			notif.addReceiver(receivers[0]);
			notif.addReceiver(receivers[1]);
			notif.addReceiver(receivers[2]);
			myAgent.send(notif);
		}
	}
	
	public class ReceiveNotifBehaviour extends CyclicBehaviour {

		@Override
		public void action() {
			MessageTemplate rcvNotifTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchConversationId(NOTIF));
			ACLMessage notif = myAgent.receive(rcvNotifTemplate);
			if (notif != null) {
				Intent broadcast = new Intent();
				broadcast.setAction("coinchutc.REFRESH_JOUEURS");
				broadcast.putExtra("sender", notif.getSender().getLocalName());
				Log.d("JoueurAgent", "Sending broadcast " + broadcast.getAction());
				context.sendBroadcast(broadcast);
			}
			else
				block();
		}
	}

	public class ChatBehaviour extends Behaviour{
		private boolean fini = false;
		@Override
		public void action() {
			MessageTemplate chatTemplate = MessageTemplate.MatchConversationId(CHAT_ID);

			ACLMessage msg = myAgent.receive(chatTemplate);
			
			if (msg!=null && msg.getPerformative() == ACLMessage.INFORM)
			{
				//changes.firePropertyChange("chat", msg.getSender().getLocalName(), msg.getContent());
				Intent broadcast = new Intent();
				broadcast.setAction("coinchutc.REFRESH_CHAT");
				broadcast.putExtra("sender", msg.getSender().getLocalName());
				broadcast.putExtra("chat", msg.getContent());
				Log.d("JoueurAgent", "Sending broadcast " + broadcast.getAction());
				context.sendBroadcast(broadcast);
			}
			else if (msg!=null && msg.getPerformative()==ACLMessage.CONFIRM)
			{
				fini=true;
			}
		}

		@Override
		public boolean done() {
			return fini;
		}
		
	}
	
	public class RecupBehaviour extends Behaviour{
		private boolean fini = false;
		private int ind=0;
		@Override
		public void action() {
			ACLMessage msg = myAgent.receive();
			if(msg!=null && msg.getPerformative()==ACLMessage.INFORM)
			{
				Log.d("JoueurAgent", "message reçu : "+msg.getContent()+" par : "+myAgent.getLocalName());
				//main2 = msg.getContent();
				String s = msg.getContent(); // chaine JSON
				ObjectMapper mapper = new ObjectMapper();
				try {
					Carte card = mapper.readValue(s, Carte.class);
					main[ind]=card;

					//changes.firePropertyChange("new card", null, card);
					//TODO: broadcast the message to PartieActivity

					Intent broadcast = new Intent();
					broadcast.setAction("coinchutc.RECUP");
					broadcast.putExtra("new card", msg.getContent());
					Log.d("JoueurAgent", "Sending broadcast " + broadcast.getAction());
					context.sendBroadcast(broadcast);
					
					ind++;
				}
				catch(Exception ex) {}
			}
			if (ind==8)
			{
				fini = true;
			}
		}

		@Override
		public boolean done() {
			return fini;
		}

	}

	public class AnnonceBehaviour extends Behaviour{
		private boolean fini = false;
		@Override
		public void action() {
			// TODO Auto-generated method stub
			ACLMessage msg = myAgent.receive();

			if(msg!=null && msg.getPerformative()==ACLMessage.CONFIRM)
			{

				//changes.firePropertyChange("fini", null, "fini");
				//TODO:broadcast to PartieActivity
				fini = true; 

			}
		}

		@Override
		public boolean done() {
			return fini;
		}

	}

	public class JouerBehaviour extends Behaviour{
		private boolean fini=false;
		private String annonce;
		private boolean atout=false;
		private int compteur = 0;
		@Override
		public void action() {
			//System.out.println("je suis dans jouer");
			// TODO Auto-generated method stub
			ACLMessage msg = myAgent.receive();
			//System.out.println(msg.getContent());
			ArrayList<Carte> aJouer = new ArrayList<Carte>();

			if (msg!=null && msg.getPerformative()==ACLMessage.INFORM)
			{		
				annonce = PartieAgent.annonce.getCouleur();
				System.out.println("couleur annoncé : "+annonce);
				if (PartieAgent.tapis[0].getValeur()!=0)
				{
					if (PartieAgent.tapis[0].getCouleur().equals(annonce))
						atout=true;
					
					if(atout==true)
					{
						for(int i=0;i<8;i++)
						{
							if (main[i].getCouleur().equals(annonce))
								aJouer.add(main[i]);
						}
						if (aJouer.size()==0)
						{
							for (int i=0;i<8;i++)
								aJouer.add(main[i]);
						}
					}
					else 
					{
						for(int i=0;i<8;i++)
						{
							if (main[i].getCouleur().equals(PartieAgent.tapis[0].getCouleur()))
								aJouer.add(main[i]);
						}
						if (aJouer.size()==0)
						{
							for (int i=0;i<8;i++)
							{
								if(main[i].getCouleur().equals(annonce))
									aJouer.add(main[i]);
							}
						}
						if (aJouer.size()==0)
						{
							for (int i=0;i<8;i++)
							{
								aJouer.add(main[i]);
							}
						}
					}
				}
				//TODO:broadcast to PartieActivity
				//changes.firePropertyChange("cartesDispo", null, aJouer);
				//changes.firePropertyChange("jouer",null,"go");
				//changes.firePropertyChange("finJeu",null,"fin");
				
				//fini=true;
			}

		}
		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return fini;
		}


	}


	public Carte[] getMain() {
		return main;
	}



	public void setMain(Carte[] main) {
		this.main = main;
	}



	public String getNom() {
		return nom;
	}



	public void setNom(String nom) {
		this.nom = nom;
	}



	public String getPrenom() {
		return prenom;
	}



	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	@Override
	public void sendMessage(String message) {
		setReceiver();
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setConversationId(CHAT_ID);
		msg.setContent(message);
		msg.addReceiver(receivers[0]);
		msg.addReceiver(receivers[1]);
		msg.addReceiver(receivers[2]);
		send(msg);
	}


	//TODO: agent est cree par RejoindrePartieActivty et reference par PartieActivity

//	@Override
//	protected void onGuiEvent(GuiEvent arg0) {
//		// TODO Auto-generated method stub
//		if (arg0.getType() == ANNONCE_EVENT) {
//			ObjectMapper mapper = new ObjectMapper();
//			StringWriter sw = new StringWriter();
//			try {
//
//				int valeur = Integer.parseInt(arg0.getParameter(0).toString());
//
//				Annonce ann = new Annonce(valeur,arg0.getParameter(1).toString());
//				mapper.writeValue(sw, ann);
//				String s = sw.toString();
//				ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
//				msg.setContent(s);
//				msg.addReceiver(new AID("Partie1", AID.ISLOCALNAME));
//
//				send(msg);
//
//			}
//			catch(Exception ex) {}
//		}
//		else if(arg0.getType()==JEU_EVENT)
//		{
//			changes.firePropertyChange("finJeu",null,"fin");
//			String s = (String) arg0.getParameter(0); // cha�e JSON
//
//			ObjectMapper mapper = new ObjectMapper();
//			StringWriter sw = new StringWriter();
//			try {
//				Carte card = mapper.readValue(s, Carte.class);
//				System.out.println("image carte : "+card.getImage());
//				//System.out.println("carte jou� gui ok : "+card.getValeur()+" "+card.getCouleur());
//				mapper.writeValue(sw, card);
//				String s1 = sw.toString();
//				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
//				msg.setContent(s1);
//				msg.addReceiver(new AID("Partie1", AID.ISLOCALNAME));
//
//				send(msg);
//
//
//			}
//			catch(Exception ex) {}
//		}
//		else if(arg0.getType()==REJOINDRE_EVENT)
//		{
//			//rejoindre = true;
//			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
//			msg.setContent("subscribe");
//			msg.addReceiver(new AID("Partie1", AID.ISLOCALNAME));
//			send(msg);
//		}
//		else if(arg0.getType()==CHAT_EVENT)
//		{
//
//			setReceiver();
//
//			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
//			msg.setContent((String) arg0.getParameter(0));
//			msg.addReceiver(receivers[0]);
//			send(msg);
//
//			ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
//			msg1.setContent((String) arg0.getParameter(0));
//			msg1.addReceiver(receivers[1]);
//			send(msg1);
//
//			ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
//			msg2.setContent((String) arg0.getParameter(0));
//			msg2.addReceiver(receivers[2]);
//			send(msg2);
//
//			changes.firePropertyChange("envoi", this.getLocalName(), (String) arg0.getParameter(0));
//		}
//	}
}

