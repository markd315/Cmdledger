import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Identity {
	private KeyPair keys;
	private String name;
	private static List<Identity> allPeople = new ArrayList<Identity>();

	public static List<Identity> getPeople() {
		return allPeople;
	}

	public Identity(String name) {
		this.name = name;
		allPeople.add(this);
	}

	public void generateAndDumpKeys() throws NoSuchAlgorithmException, IOException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(512);
		this.keys = keyGen.generateKeyPair();
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(this.name + "_keypair.ser"));
		out.writeObject(this.keys);
		out.close();

	}

	public static Identity lookupWithName(String n) {
		for (Identity i : allPeople) {

			if (i.getName().equalsIgnoreCase(n)) {
				return i;
			}
		}
		return new Identity(n);
	}

	public void sign(Entry e) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException,
			SignatureException, IOException {
		// TODO throw exception if signing an entry that isn't ours.
		e.setSignature(this.getPrivateKey());
	}

	public boolean verify(Entry e) {
		// TODO throw exception if verifying an entry that isn't ours.
		return e.verifySignature(this.getPublicKey());
	}

	public void loadKeyPair(String keypairFile) {
		try {
			FileInputStream door = new FileInputStream(keypairFile);
			ObjectInputStream reader = new ObjectInputStream(door);
			this.keys = (KeyPair) reader.readObject();
			reader.close();
		} catch (Exception e) {
			System.err.println("Error reading in key serialization");
		}
	}

	private byte valFromHexChar(char hex) {
		switch (hex) {
		case '0':
			return 0;
		case '1':
			return 1;
		case '2':
			return 2;
		case '3':
			return 3;
		case '4':
			return 4;
		case '5':
			return 5;
		case '6':
			return 6;
		case '7':
			return 7;
		case '8':
			return 8;
		case '9':
			return 9;
		case 'a':
			return 10;
		case 'b':
			return 11;
		case 'c':
			return 12;
		case 'd':
			return 13;
		case 'e':
			return 14;
		case 'f':
			return 15;
		}
		return 0;
	}

	private byte[] bytesFromHex(String secondLine) {
		secondLine = secondLine.toLowerCase();
		List<Byte> bytes = new ArrayList<Byte>();
		for (int i = 0; i < secondLine.length() - 1; i++) {
			char a = secondLine.charAt(i);
			char b = secondLine.charAt(i + 1); // Big endian, each a is worth 16, each b is worth 1.
			byte toAdd = (byte) (valFromHexChar(a) * 0x10 + valFromHexChar(b));
			bytes.add(Byte.valueOf(toAdd));
		}
		byte[] ret = new byte[bytes.size()];
		for (int i = 0; i < bytes.size(); i++) {
			ret[i] = bytes.get(i).byteValue();
		}
		return ret;
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
