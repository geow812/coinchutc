package utc.coinchutc.agent;

public interface JoueurInterface {
	
	public void sendMessage(String message);
	public void sendRejoindreRequest(String message);
	public void annonce(int point, String couleur);
}
