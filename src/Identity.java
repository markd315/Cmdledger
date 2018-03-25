import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class Identity {
	private KeyPair keys;
	private String name;
	private static List<Identity> allPeople = new ArrayList<Identity>();
	
	public static List<Identity> getPeople(){
		return allPeople;
	}
	
	public Identity(String name) {
		this.name = name;
		allPeople.add(this);
	}
	public void loadKeyPair(String filename) {
		
	}
	public PrivateKey getPrivateKey() {
		return keys.getPrivate();
	}
	public PublicKey getPublicKey() {
		return keys.getPublic();
	}

	public String getName() {
		return name;
	}
}
