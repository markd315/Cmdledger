import java.util.List;

public class Block {//contains multiple Entry, included in a Ledger blockchain.
	private List<Entry> txs;
	public Block(List<Entry> correctlySignedInMempool) {
		txs.addAll(correctlySignedInMempool);
	}
	public Entry getAllEntries() {
		// TODO Auto-generated method stub
		return null;
	}
	public void setParentLedger(Ledger ledger) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
