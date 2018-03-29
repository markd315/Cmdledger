import java.util.ArrayList;
import java.util.List;

public class Block {//contains multiple Entry, included in a Ledger blockchain.
	private List<Entry> txs = new ArrayList<Entry>();
	private Ledger parent;
	public Block(List<Entry> correctlySignedInMempool) {
		for(Entry tx : correctlySignedInMempool) {
			txs.add(tx);
		}
	}
	public List<Entry> getAllEntries() {
		return txs;
	}
	public void setParentLedger(Ledger ledger) {
		this.parent = ledger;
	}
	public Ledger getParentLedger() {
		return this.parent;
	}
	public String toString() {
		String ret = "";
		for(Entry e : this.txs) {
			ret+=e.toStringNoSig();
		}
		return ret;
	}
	
}
