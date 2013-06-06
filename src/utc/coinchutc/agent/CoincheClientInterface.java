package utc.coinchutc.agent;

public interface CoincheClientInterface {
	public void handleSpoken(String s);
	public String[] getPlayersNames();
	public void deconnexion();
}
