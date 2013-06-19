package utc.coinchutc.agent;

import java.io.Serializable;

public class Carte implements Serializable {
	private int valeur; 
	private String couleur;
	private String image;
	private String login;
	
	public Carte(){
		
	}
	
	public Carte(int val, String col, String img, String login)
	{
		valeur = val;
		couleur = col; 
		image = img;
		this.login = login;
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

	public String getImage(){
		return image;
	}
	
	public void setCouleur(String couleur) {
		this.couleur = couleur;
	}
	
	public void setImage(String img) {
		this.image = img;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}
	
	public String toString() {
		return "carte " + couleur + valeur;
	}
}
