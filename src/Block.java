import java.util.List;

public class Block {//contains multiple Entry, included in a Ledger blockchain.
	private List<Entry> txs;
	private Ledger parent;
	public Block(List<Entry> correctlySignedInMempool) {
		txs.addAll(correctlySignedInMempool);
	}
	public List<Entry> getAllEntries() {
		return txs;
	}
	public void setParentLedger(Ledger ledger) {
		this.parent = ledger;
	}
	
	
	
}
