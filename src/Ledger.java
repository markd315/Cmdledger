import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Ledger {
	/*
	 * Format of Transactions: <TransID>; M; (<TransID>, <vout>)^M; N; (<AcctID>,
	 * <amount>)^N Items in angle brackets are parameters, M and N are whole
	 * numbers, and caret M (or N) indicates M (or N) repetitions of the
	 * parenthesized pairs.
	 * 
	 * Example Transaction: 4787df35; 1;(f2cea539, 0);3; (Bob, 150)(Alice,
	 * 845)(Gopesh, 5)
	 */
	private static Ledger instance;

	public static Ledger getInstance() {
		if (instance != null)
			return instance;
		else {
			instance = new Ledger();
			return instance;
		}
	}

	public static void destroy() {
		instance = new Ledger();
	}

	private Ledger(Block genesis) {

		blockchain = new ArrayList<Block>();
		blockchain.add(genesis);
		genesis.setParentLedger(this);
		instance = this;
	}

	private Ledger() {
		blockchain = new ArrayList<Block>();
		isInteractive = false;
		isVerbose = false;
		instance = this;
	}

	private boolean isInteractive, isVerbose;

	public List<Entry> getBlockchain() {
		List<Entry> ret = new ArrayList<Entry>();
		for (Block b : blockchain) {
			ret.addAll(b.getAllEntries());
		}
		return ret;
	}

	public List<Block> getBlockchainAsBlocks() {
		return blockchain;
	}

	private List<Block> blockchain;
	private List<Entry> addingInThisBlock;

	public Block createBlock() {
		addingInThisBlock = new ArrayList<Entry>();
		for (Entry e : Entry.getMempool()) {
			if (e.getInputs().size() == 0 && this.blockchain.size() == 0) {// Genesis.
				addingInThisBlock.add(e);
				break;
			} else {
				String person = this.lookupOutput(e.getInputs().get(0)).getName();
				PublicKey pkToVerifyWith = Identity.lookupWithName(person).getPublicKey();
				if(pkToVerifyWith == null) {
					//System.err.println("Keypair not loaded into memory!");
					continue;
				}
				if (e.verifySignature(pkToVerifyWith)) {
					addingInThisBlock.add(e);// If that's all I need to check...
				}
			}
		}
		for (Entry e : addingInThisBlock) {
			Entry.getMempool().remove(e);// These are no longer waiting to be added.
		}
		Block newBlock = new Block(addingInThisBlock);
		newBlock.setParentLedger(this);
		addingInThisBlock = new ArrayList<Entry>(); // Drop this reference and start over for next time this method is
													// called.
		this.blockchain.add(newBlock);
		return newBlock;

	}

	public void addTx(Entry e) {
		if (e.sumOfIns() != e.sumOfOuts()) {
			System.out.println("Invalid transaction sum");
			return;
		}
		e.setParentLedger(this);
		Entry.addToMempool(e);
	}

	public int calcBalance(String username) throws Exception {
		Set<Output> unspent = new HashSet<Output>();
		for (Entry e : getBlockchain()) {
			List<Output> outputs = e.getOutputs();
			for (int i = 0; i < outputs.size(); i++) {
				if (outputs.get(i).getName().equals(username)) { // We found one output of this transaction.
					Output temp = outputs.get(i);
					unspent.add(temp);
				}
			}
		} // Locate all vout relations (amount, transID, inputIndex) WHERE name=name
			// SAVE THE INDEX. SAVE THE TXID FROM THIS NAME SEARCH.
			// CHECK THE TXID for ALL TRANSACTIONS.
			// DOES THE UNSPENT SET CONTAIN THIS TXID?
			// Yes? CHECK ALL INPUTS FOR THIS TRANSACTION.
			// Does the output index location from the output list match the input.index?
			// MATCHES? REMOVE IT FROM THE UNSPENT SET.
		for (Iterator<Output> i = unspent.iterator(); i.hasNext();) {
			Output element = i.next();
			if (isSpentInAnyTransaction(element)) {
				i.remove();
			}
		}
		// Iterate across the remaining set and sum the balance.
		int sum = 0;
		for (Output o : unspent) {
			sum += o.getAmount();
		}
		// Return this value and drop the refs with a gc.
		System.gc();
		return sum;
	}

	private boolean isSpentInAnyTransaction(Output op) {
		for (Entry e : this.getBlockchain()) {
			for (Input in : e.getInputs()) {
				if (in.getId().equals(op.getId()) && in.getIndex() == op.indexInChain(this)) {
					return true;
				}
			}
		}
		return false;
	}

	public String toString() {
		String ret = "";
		for (Entry e : this.getBlockchain()) {
			ret += e;
			ret += "\n";
		}
		return ret.trim();

	}

	public boolean isVerbose() {
		return isVerbose;
	}

	public void setVerbose(boolean isVerbose) {
		this.isVerbose = isVerbose;
	}

	public boolean isInteractive() {
		return isInteractive;
	}

	public void setInteractive(boolean isInteractive) {
		this.isInteractive = isInteractive;
	}

	public void addTransaction(String remainingCmd) {
		// Break the String into 5 parts by split;
		String[] split = remainingCmd.split(";");
		if (split.length != 5) {
			System.err.println("Unable to parse into the 5 arguments");
			return;
		}
		for (int i = 0; i < split.length; i++) {
			if (split[i].length() > 0)
				split[i] = split[i].trim();
		}
		int inputSize = Integer.parseInt(split[1]);
		List<Input> ins = new ArrayList<Input>();
		if (inputSize > 0) {
			String temp = split[2].substring(1, split[2].length() - 1);
			String withoutParentheses = temp.replaceAll("\\)", ";");
			withoutParentheses = withoutParentheses.replaceAll("\\(", "");
			String[] inputArray = {};

			if (withoutParentheses.contains(";")) {
				inputArray = withoutParentheses.split(";");
			} else {
				inputArray = new String[1];
				inputArray[0] = withoutParentheses;
			}
			for (String s : inputArray) {
				String[] inSplit = s.split(",");
				String txid = inSplit[0].trim();
				int index = Integer.parseInt(inSplit[1].trim());
				ins.add(new Input(txid, index));
			}
		}
		if (ins.size() != inputSize) {
			System.err.println("Invalid inputs!");
			return;
		}

		int outputSize = Integer.parseInt(split[3]);
		List<Output> outs = new ArrayList<Output>();
		if (outputSize > 0) {
			String[] outputArray = {};
			String temp = split[4].substring(1, split[4].length() - 1);
			String withoutParentheses = temp.replaceAll("\\)", ";");
			withoutParentheses = withoutParentheses.replaceAll("\\(", "");

			if (withoutParentheses.contains(";")) {
				outputArray = withoutParentheses.split(";");
			} else {
				outputArray = new String[1];
				outputArray[0] = withoutParentheses;
			}
			// We want to remove the trailing ) to be able to split this string.
			for (String s : outputArray) {
				String[] outSplit = s.split(",");
				String name = outSplit[0].trim();
				int amount = Integer.parseInt(outSplit[1].trim());
				outs.add(new Output(name, amount, null)); // We need to update the entry from Null.
			}
		}
		if (outs.size() != outputSize) {
			System.err.println("Invalid outputs!");
			return;
		}
		Entry e = new Entry(this, ins, outs);
		for (Output o : e.getOutputs()) {
			o.setEntry(e);
		}
		e.setTxID(split[0]);
		this.addTransaction(e);
		return;
	}

	// This needs to handle geneses and check them to see if they are the first
	// transaction.
	// Exception for vin!=vout && not genesis.
	public void addTransaction(Entry e) {
		// We need to set all of the outputs to have the right txid.
		for (Output o : e.getOutputs()) {
			o.setId(e.getId());
		}

		// Verify sanity.
		e.setParentLedger(this);
		for (Entry old : this.getBlockchain()) {
			if (old.getId().equals(e.getId())) {
				System.err.println("Duplicate txid");
				return;
			}
		}
		for (Input in : e.getInputs()) {
			if (isSpentInAnyTransaction(lookupOutput(in))) {
				System.err.println("Duplicate spend");
				return;
			}
		}

		if (e.sumOfOuts() == 0) {
			System.out.print("Transaction rejected, reason: ");
			System.err.println("Empty output space.");
			return;
		} /*else if (e.sumOfIns() != e.sumOfOuts()) {
			if (blockchain.size() > 0 || Entry.getMempool().size() > 0) {
				System.out.print("Transaction rejected, reason: ");
				System.err.println("Invalid spend or destruct of funds.");
				return;
			}
		}*/
		//Uncomment previous line if you want the mempool to demand a transaction's inputs be in a block before it is added.
		Entry.addToMempool(e);
	}

	Output lookupOutput(Input in) {
		Output toReturn = null;
		for (Entry e : this.getBlockchain()) {
			if (e.getId().equals(in.getId())) {
				if (toReturn == null) {
					toReturn = e.getOutputs().get(in.getIndex());
				} else {
					System.err.println("Error: There are two entries with the same ID!");
				}
			}
		} // We also have to check inside of addingInThisBlock, but we will give the
			// established chain priority.
		if (this.addingInThisBlock != null) {
			for (Entry e : this.addingInThisBlock) {
				if (e.getId().equals(in.getId())) {
					if (toReturn == null) {
						toReturn = e.getOutputs().get(in.getIndex());
					} else {
						System.err.println("Error: There are two entries with the same ID!");
					}
				}
			}
		}
		return toReturn;
	}

	public void wipe() {
		this.blockchain = new ArrayList<Block>();
		System.gc();

	}

	public Entry lookupWithId(String txid) {
		for (Entry e : this.getBlockchain()) {
			if (e.getId().equals(txid)) {
				return e;
			}
		}
		return null;
	}

	public void addSignedTransaction(String nextLine, String nextLine2) {// For signed transactions.
		// Break the String into 5 parts by split;
		String[] split = nextLine.split(";");
		if (split.length != 5) {
			System.err.println("Unable to parse into the 5 arguments");
			return;
		}
		for (int i = 0; i < split.length; i++) {
			if (split[i].length() > 0)
				split[i] = split[i].trim();
		}
		int inputSize = Integer.parseInt(split[1]);
		List<Input> ins = new ArrayList<Input>();
		if (inputSize > 0) {
			String temp = split[2].substring(1, split[2].length() - 1);
			String withoutParentheses = temp.replaceAll("\\)", ";");
			withoutParentheses = withoutParentheses.replaceAll("\\(", "");
			String[] inputArray = {};

			if (withoutParentheses.contains(";")) {
				inputArray = withoutParentheses.split(";");
			} else {
				inputArray = new String[1];
				inputArray[0] = withoutParentheses;
			}
			// TESTCMD: t newt; 1; (root, 0); 2; (Sam, 2000)(Bob, 3000)
			// We want to remove the leading ( to be able to split this string.
			for (String s : inputArray) {
				String[] inSplit = s.split(",");
				String txid = inSplit[0].trim();
				int index = Integer.parseInt(inSplit[1].trim());
				ins.add(new Input(txid, index));
			}
		}
		if (ins.size() != inputSize) {
			System.err.println("Invalid inputs!");
			return;
		}

		int outputSize = Integer.parseInt(split[3]);
		List<Output> outs = new ArrayList<Output>();
		if (outputSize > 0) {
			String[] outputArray = {};
			String temp = split[4].substring(1, split[4].length() - 1);
			String withoutParentheses = temp.replaceAll("\\)", ";");
			withoutParentheses = withoutParentheses.replaceAll("\\(", "");

			if (withoutParentheses.contains(";")) {
				outputArray = withoutParentheses.split(";");
			} else {
				outputArray = new String[1];
				outputArray[0] = withoutParentheses;
			}
			// We want to remove the trailing ) to be able to split this string.
			for (String s : outputArray) {
				String[] outSplit = s.split(",");
				String name = outSplit[0].trim();
				int amount = Integer.parseInt(outSplit[1].trim());
				outs.add(new Output(name, amount, null)); // We need to update the entry from Null.
			}
		}
		if (outs.size() != outputSize) {
			System.err.println("Invalid outputs!");
			return;
		}
		Entry e = new Entry(this, ins, outs);
		for (Output o : e.getOutputs()) {
			o.setEntry(e);
		}
		e.setTxID(split[0]);
		byte[] signature;
		if (nextLine2.equalsIgnoreCase("null")) {
			signature = null;
		} else {
			String[] bytesToParse = nextLine2.split(" ");
			signature = new byte[bytesToParse.length];
			for (int i = 0; i < signature.length; i++) {
				signature[i] = Byte.valueOf(bytesToParse[i]);
			}
		}

		e.reloadSignature(signature);
		this.addTransaction(e);
		return;
	}

}
