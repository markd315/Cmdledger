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

	public double calcBalance(String username){
		Set<Vout> unspent = new HashSet<Vout>();
		for(Entry e : blockchain) {
			int sum = 0;
			List<String> outputs = e.getOutputNames();
			for(int i=0; i< outputs.size(); i++) {
			if(outputs.get(i).equals(username)) { //We found one output of this transaction.
				Vout temp = new Vout(e.getId(), e.getOutputAmounts().get(i), i);
				unspent.add(temp);
				sum+= e.getOutputAmounts().get(i);
			}
			}
		}//Locate all vout relations (amount, transID, inputIndex) WHERE name=name 
		
		
		//Subtract them from the set when they are used.
		for(Entry e : blockchain) {
			for(Vout output : unspent) {
				if(e.getInputIDs().equals(output.getId()) && e.getInputIndices().get(output.getIndex())) {//Match the 
					
				}
			}
			
			/*if(unspentcontains()) {
				//spends from this transaction
				for() {//every possible index
					if() {//match{
						
					}
						
				}
			}*/
			
				
		}
		
		
		//Iterate across the remaining set and sum the balance.
		
		//Return this value and drop the refs with a gc.
		return 0;
		
		//TODO Return all of the vouts minus the cases where those are used as inputs (index and id both match)
		
		
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

}
