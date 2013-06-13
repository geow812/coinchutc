package utc.coinchutc.agent;

import jade.core.AID;

public class Annonce {
	
	private int valeur;
	private String couleur;
	private AID[] maitre;
	private int ind;
	
	public Annonce(int val, String col)
	{
		ind=0;
		maitre = new AID[2];
		valeur = val;
		couleur = col;
	}
	
	public Annonce()
	{
		
	}

	public int getValeur() {
		return valeur;
	}

	public void setValeur(int valeur) {
		this.valeur = valeur;
	}

	public String getCouleur() {
		return couleur;
	}

	public void setCouleur(String couleur) {
		this.couleur = couleur;
	}

	public AID[] getMaitre() {
		return maitre;
	}

	public void setMaitre(AID[] maitre) {
		this.maitre = maitre;
	}
}
