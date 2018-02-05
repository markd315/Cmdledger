import java.util.ArrayList;
import java.util.HashSet;
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
	public Ledger(Entry genesis) {
		blockchain = new ArrayList<Entry>();
		blockchain.add(genesis);
		genesis.setParentLedger(this);
	}

	private boolean isInteractive, isVerbose;

	public List<Entry> getBlockchain() {
		return blockchain;
	}

	private List<Entry> blockchain;

	public void addTx(Entry e) {
		if (e.sumOfIns() != e.sumOfOuts()) {
			System.out.println("Invalid transaction sum");
			return;
		}
		e.setParentLedger(this);
		blockchain.add(e);
	}

	public double calcBalance(String username) throws Exception {
		Set<Output> unspent = new HashSet<Output>();
		for (Entry e : blockchain) {
			List<Output> outputs = e.getOutputs();
			for (int i = 0; i < outputs.size(); i++) {
				if (outputs.get(i).equals(username)) { // We found one output of this transaction.
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
		for (int i = 0; i < blockchain.size(); i++) {
			Entry e = blockchain.get(i);
			for (Output op : unspent) { // Does the unspent set contain this txid?
				if (op.getId().equals(e.getId())) {// Yes?
					for (Input in : e.getInputs()) {
						int index = in.getIndex();
						// TODO how do I find the output index location?
						int outputIndexLocation = lookupOutput(op);
						if (index == outputIndexLocation) {
							unspent.remove(op);
						}
					}

				}
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

	private int lookupOutput(Output op) throws Exception {
		for (Entry e : this.blockchain) {
			for (int i = 0; i < e.getOutputs().size(); i++) {
				if (op.equals(e.getOutputs().get(i))) {
					return i;
				}
			}
		}
		throw new Exception("Couldn't find output");
	}

	public String toString() {
		String ret = "";
		for (Entry e : blockchain) {
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

	// 4787df35; 1;(f2cea539, 0);3; (Bob, 150)(Alice, 845)(Gopesh, 5)
	public void addTransaction(String remainingCmd) {
		// Break the String into 5 parts by split;
		String[] split = remainingCmd.split(";");
		if (split.length != 5) {
			System.err.println("Unable to parse into the 5 arguments");
			return;
		}
		for (int i = 0; i < split.length; i++) {
			split[i] = split[i].trim();
		}
		int inputSize = Integer.parseInt(split[1]);
		List<Input> ins=  new ArrayList<Input>();
		String[] inputArray = split[2].split(")");
		for (String s : inputArray) {
			s.replace("(", "");
			String[] inSplit = s.split(",");
			String txid = inSplit[0].trim();
			int index = Integer.parseInt(inSplit[1].trim()); 
			ins.add(new Input(txid, index));
		}
		if(ins.size() != inputSize) {
			System.err.println("Invalid inputs!");
		}

		int outputSize = Integer.parseInt(split[3]);
		List<Output> outs=  new ArrayList<Output>();
		String[] outputArray = split[4].split(")");
		for (String s : outputArray) {
			s.replace("(", "");
			String[] outSplit = s.split(",");
			String name = outSplit[0].trim();
			int amount = Integer.parseInt(outSplit[1].trim()); 
			outs.add(new Output(name, amount, null)); //We need to update the entry from Null.
		}
		if(outs.size() != outputSize) {
			System.err.println("Invalid outputs!");
		}
		Entry e = new Entry(this, ins, outs);
		for(Output o : e.getOutputs()) {
			o.setEntry(e);
		}
		this.addTransaction(e);
		return;
	}

	// This needs to handle geneses and check them to see if they are the first
	// transaction.
	// Exception for vin!=vout && not genesis.
	public void addTransaction(Entry e) {
		// Verify sanity.
		if (e.sumOfIns() == 0) {
			System.out.print("Transaction rejected, reason: ");
			System.err.println("Empty input space");
		} else if (e.sumOfOuts() == 0) {
			System.out.print("Transaction rejected, reason: ");
			System.err.println("Empty output space.");
		} else if (e.sumOfIns() != e.sumOfOuts() && blockchain.size() > 0) {
			System.out.print("Transaction rejected, reason: ");
			System.err.println("Invalid spend or destruct of funds.");
		} else
			blockchain.add(e);
	}

}
