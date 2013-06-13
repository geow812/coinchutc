package utc.coinchutc.agent;

public class Carte {
	private int valeur; 
	private String couleur;
	private String image;
	
	public Carte(){
		
	}
	
	public Carte(int val, String col, String img)
	{
		valeur = val;
		couleur = col; 
		image = img;
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
}
