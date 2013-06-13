package utc.coinchutc.agent;

public class Points {
	private int valeur;
	private int carte;
	private boolean atout;
	
	public Points(int card, int val, boolean at)
	{
		valeur = val;
		carte = card;
		atout = at;
	}

	public int getValeur() {
		return valeur;
	}

	public void setValeur(int valeur) {
		this.valeur = valeur;
	}

	public int getCarte() {
		return carte;
	}

	public void setCarte(int carte) {
		this.carte = carte;
	}

	public boolean isAtout() {
		return atout;
	}

	public void setAtout(boolean atout) {
		this.atout = atout;
	}
}
