import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
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

	public static Identity lookupWithName(String n) {
		try {
			for (Identity i : allPeople) {

				if (i.getName().equalsIgnoreCase(n)) {
					return i;
				}
			}
		} catch (Exception e) {
			System.err.println("Invalid person lookup!");

		}
		return null;
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

	public void loadKeyPair(String privfn, String pubfn)
			throws InvalidKeySpecException, NoSuchAlgorithmException, FileNotFoundException {
		try {
			File f = new File(privfn);
			FileInputStream fis = new FileInputStream(f);
			DataInputStream dis = new DataInputStream(fis);
			byte[] keyBytes = new byte[(int) f.length()];
			dis.readFully(keyBytes);
			dis.close();

			String temp = new String(keyBytes);
			String privKeyPEM = temp.replace("-----BEGIN PRIVATE KEY-----\n", "");
			privKeyPEM = privKeyPEM.replace("-----END PRIVATE KEY-----", "");
			// System.out.println("Private key\n"+privKeyPEM);

			byte[] decoded = Base64.getDecoder().decode(privKeyPEM);

			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey privkey = kf.generatePrivate(spec);

			f = new File(pubfn);
			fis = new FileInputStream(f);
			dis = new DataInputStream(fis);
			keyBytes = new byte[(int) f.length()];
			dis.readFully(keyBytes);
			dis.close();

			temp = new String(keyBytes);
			String publicKeyPEM = temp.replace("-----BEGIN PUBLIC KEY-----\n", "");
			publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");

			decoded = Base64.getDecoder().decode(publicKeyPEM);

			X509EncodedKeySpec spec2 = new X509EncodedKeySpec(decoded);
			kf = KeyFactory.getInstance("RSA");
			PublicKey pubkey = kf.generatePublic(spec2);

			KeyPair pair = new KeyPair(pubkey, privkey);
			this.keys = pair;
		} catch (Exception e) {
			System.err.println();
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
