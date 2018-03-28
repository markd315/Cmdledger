import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;

public class Entry {
	private String id;
	private Ledger parentLedger;
	private List<Input> inputs;
	private List<Output> outputs;
	private byte[] signature;
	private static List<Entry> mempool = new ArrayList<Entry>();// These transactions have no block yet.

	public static List<Entry> getMempool() {
		return mempool;
	}

	public Entry(Ledger parent, List<Input> ins, List<Output> outs) {
		this.signature = null; // to be signed later.
		if(ins == null) {
			ins = new ArrayList<Input>();
		}
		if(outs == null) {
			outs = new ArrayList<Output>();
		}
		this.inputs = ins;
		this.parentLedger = parent;
		// verify that all inputs come from the same user.
		List<Output> referencedOutputs = new ArrayList<Output>();
		for (Input i : inputs) {
			referencedOutputs.add(parentLedger.lookupOutput(i));
		}
		if ((referencedOutputs.size() == 0 || referencedOutputs.get(0) == null)
				&& parentLedger.getBlockchain().size() == 0) {
			// If we are at the genesis transaction.
			this.outputs = outs;
			return;
		}
		String nameForAllInputs = referencedOutputs.get(0).getName();
		for (Output o : referencedOutputs) {
			if (!o.getName().equals(nameForAllInputs)) {
				throw new IllegalArgumentException("Not all inputs are owned by the same user!");
			}
		}
		// Create identities if they do not exist.
		List<Identity> ppl = Identity.getPeople();
		for (Identity i : ppl) {
			if (i.getName().equals(nameForAllInputs)) {
				this.outputs = outs;
				return;// We found the matching identity.
			}
		}
		// If we reach this point, we need to make a new identity for signing
		// transactions.
		new Identity(nameForAllInputs);
		this.outputs = outs;
	}

	public Entry() {
		// We must call a genesis set on this constructor or add valid inputs or outputs
		// for it to be accepted.

	}

	public void setSignature(PrivateKey RSAPrivateKey) throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchProviderException, IOException, SignatureException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream writer = new ObjectOutputStream(baos);
		writer.writeObject(this.inputs);
		writer.writeObject(this.outputs);
		writer.writeObject(this.id);
		byte[] dataToSign = baos.toByteArray();
		Signature signature = Signature.getInstance("SHA256withRSA", "BC");
		signature.initSign(RSAPrivateKey, new SecureRandom());
		signature.update(dataToSign);
		this.signature = signature.sign();
	}

	public boolean verifySignature(PublicKey publickey) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream writer = new ObjectOutputStream(baos);
			writer.writeObject(this.inputs);
			writer.writeObject(this.outputs);
			writer.writeObject(this.id);
			byte[] dataToVerify = baos.toByteArray();
			Signature localSignature = Signature.getInstance("SHA256withRSA", "BC");
			localSignature.initVerify(publickey);
			localSignature.update(dataToVerify);
			return localSignature.verify(this.signature);
		} catch (Exception e) {
			e.printStackTrace();
		}

		throw new UnsupportedOperationException();
	}

	public void setParentLedger(Ledger l) {
		this.parentLedger = l;
	}

	public String toString() {
		String ret = id + "; ";
		ret += inputs.size() + "; ";
		for (int i = 0; i < inputs.size(); i++) {
			ret += "(" + inputs.get(i).getId() + ", " + inputs.get(i).getIndex() + ")";
		}
		ret += "; " + outputs.size() + "; ";
		for (int i = 0; i < outputs.size(); i++) {
			ret += "(" + outputs.get(i).getName() + ", " + outputs.get(i).getAmount() + ")";
		}
		return ret;
	}

	public int sumOfIns() {
		int ret = 0;
		for (Input in : this.inputs) {
			// For each, lookup the location of the output.
			// Safely break if DNE.
			for (Entry e : parentLedger.getBlockchain()) {
				if (e.id.equals(in.getId())) {
					try {
						ret += e.getOutputs().get(in.getIndex()).getAmount();
					} catch (Exception ex) {
						System.err.println("Couldn't find referenced previous output to use as an input.");
						return -1;
					}
				}
			}
		}
		return ret;
	}

	public List<Output> getOutputs() {
		return outputs;
	}

	public List<Input> getInputs() {
		return inputs;
	}

	public String getId() {
		return id;
	}

	public int sumOfOuts() {
		int sum = 0;
		for (Output k : this.outputs) {
			sum += k.getAmount();
		}
		return sum;
	}

	public void setTxID(String string) {
		this.id = string;
	}

	public static void addToMempool(Entry e) {
		mempool.add(e);
	}

	public byte[] getSignature() {
		return signature;
	}
}
