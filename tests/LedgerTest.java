import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class LedgerTest {

	Ledger l;

	@Before
	public void callBefore() {
		Ledger.destroy();
		l = Ledger.getInstance();
	}

	@Test
	public void canLoadAndDumpTxFromFile() throws Exception {
		CmdLedger.loadFromFile(l, "testinputs.txt");
		assertTrue(Entry.getMempool().size() == 3);
		// Alice and Sam need to sign txs.
		//TODO Alice and Sam transactions weirdly have no inputs. Double-spend?
		Identity alice=null, sam=null;//Just yank their references from the static list.
		alice = Identity.lookupWithName("Alice");
		sam = Identity.lookupWithName("Sam");
		
		assertNotNull(alice);
		assertNotNull(sam);
		alice.loadKeyPair("Alice_keypair.ser");
		sam.loadKeyPair("Sam_keypair.ser");//fine for testing, ignore the semantics of this.
		List<Entry> mempool = Entry.getMempool();
		Entry aliceToSign = mempool.get(1);
		Entry samToSign = mempool.get(2);
		// sign txs and create a block.
		l.createBlock();//Alice cannot sign for a UTXO (genesis) that is still in the mempool, so we need to make a block first.
		assertTrue(Entry.getMempool().size() == 2);
		alice.sign(aliceToSign);
		l.createBlock();//Sam cannot sign for a UTXO that is still in the mempool.
		assertTrue(Entry.getMempool().size() == 1);
		sam.sign(samToSign);
		// have to sign all but the genesis.
		l.createBlock();
		assertTrue(l.getBlockchain().size() == 3);
		assertTrue(Entry.getMempool().size() == 0);
		assertTrue(l.calcBalance("Bob") == 3000);
		assertTrue(l.calcBalance("Milo") == 1500);
		assertTrue(l.calcBalance("Band") == 1500);
		assertTrue(l.calcBalance("Alice") == 0);
		assertTrue(l.calcBalance("Sam") == 0);
		l.addTransaction("dump; 1; (newtt, 0); 2; (Alice, 1000)(Milo, 500)");
		CmdLedger.dumpFile(l, "testoutputs.txt");
		Ledger.destroy();
		Ledger.getInstance();
		CmdLedger.loadFromFile(l, "testoutputs.txt");
		assertTrue(l.getBlockchain().size() == 4);
		assertTrue(l.calcBalance("Bob") == 3000);
		assertTrue(l.calcBalance("Milo") == 500);
		assertTrue(l.calcBalance("Band") == 1500);
		assertTrue(l.calcBalance("Alice") == 1000);
		assertTrue(l.calcBalance("Sam") == 0);
	}

	@Test
	public void testWontAddBadlyFormattedTx() {
		l.addTransaction("root; 0; 0; 0; 0; 0");
		l.addTransaction("root; 0; ; 0; (Mark, 1000)");
		l.addTransaction("new; 2; (root, 0); 0; (Mark, 1000)");
		l.addTransaction("root; 0; ; 3; (Mark, 1000)");
		assertTrue(Entry.getMempool().size() == 0);

	}

	@Test
	public void testWillAddWellFormattedTx() throws Exception {
		l.addTransaction("root; 0; ; 1; (Mark, 1000)");
		l.addTransaction("new; 1; (root, 0); 1; (Matt, 1000)");
		l.addTransaction("split; 1; (new, 0); 2; (Mark, 500)(Mateo, 500)");
		assertTrue(Entry.getMempool().size() == 3);
	}

	//TODO suspended for further semantic consideration
	/*@Test
	public void testWontAddDoubleSpendTx() throws Exception {
		l.addTransaction("root; 0; ; 1; (Mark, 1000)");
		l.addTransaction("new; 1; (root, 0); 1; (Matt, 1000)");
		l.addTransaction("split; 1; (root, 0); 2; (Mark, 500)(Mateo, 500)");
		assertTrue(Entry.getMempool().size() == 2);
	}

	@Test
	public void testWontAddBadlyNamedTx() throws Exception {
		l.addTransaction("root; 0; ; 1; (Mark, 1000)");
		l.addTransaction("new; 1; (root, 0); 1; (Matt, 1000)");
		l.addTransaction("new; 1; (new, 0); 2; (Mark, 500)(Mateo, 500)");
		assertTrue(l.getBlockchain().size() == 2);
		assertTrue(l.calcBalance("Mark") == 0);
		assertTrue(l.calcBalance("Matt") == 1000);
		assertTrue(l.calcBalance("Mateo") == 0);
	}*/

	@Test
	public void testWontAddUnbalancedTx() throws Exception {
		l.addTransaction("root; 0; ; 1; (Mark, 1000)");
		l.addTransaction("new; 1; (root, 0); 1; (Matt, 1000)");
		l.addTransaction("split; 1; (new, 0); 2; (Mark, 1000)(Mateo, 1000)");
		l.addTransaction("splitter; 1; (new, 0); 2; (Mark, 499)(Mateo, 499)");
		assertTrue(Entry.getMempool().size() == 2);
	}

}
