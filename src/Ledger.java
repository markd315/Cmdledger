import java.util.ArrayList;
import java.util.List;

public class Ledger {
/*
 * Format of Transactions:
<TransID>; M; (<TransID>, <vout>)^M; N; (<AcctID>, <amount>)^N 
Items in angle brackets are parameters, M and N are whole numbers, and caret M (or N) indicates M (or N) repetitions of the parenthesized pairs. 

Example Transaction:
4787df35; 1;(f2cea539, 0);3; (Bob, 150)(Alice, 845)(Gopesh, 5)
 */
	private boolean isInteractive, isVerbose;
	public List<Entry> getBlockchain() {
		return blockchain;
	}
	private List<Entry> blockchain;
	public Ledger(Entry genesis) {
		blockchain = new ArrayList<Entry>();
		blockchain.add(genesis);
	}
	public void addTx(Entry e) {
		if(e.sumOfIns() != e.sumOfOuts()) {
			System.out.println("Invalid transaction sum");
			return;
		}
		blockchain.add(e);
	}
	public double calcBalance(String userkey){
		//TODO
	}
	
	public String toString() {
		String ret = "";
		for(Entry e : blockchain) {
			ret+=e;
			ret+="\n";
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
