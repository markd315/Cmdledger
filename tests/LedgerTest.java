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
		Entry.getMempool().clear();
	}

	@Test
	public void canLoadAndDumpTxFromFile() throws Exception {
		Identity alice=null, sam=null, milo=null;//Just yank their references from the static list.
		alice = Identity.lookupWithName("Alice");
		sam = Identity.lookupWithName("Sam");
		milo = Identity.lookupWithName("Milo");
		assertNotNull(alice);
		assertNotNull(sam);
		assertNotNull(milo);
		alice.loadKeyPair("Alice_keypair.ser");
		sam.loadKeyPair("Sam_keypair.ser");//fine for testing, ignore the semantics of this.
		milo.generateAndDumpKeys();//Not going to load one for him to prove that we can generate a new one.
		CmdLedger.loadFromFile(l, "testinputs.txt");
		assertTrue(Entry.getMempool().size() == 0);
		assertTrue(l.getBlockchain().size() == 3);
		// Alice and Sam need to sign txs.
		//TODO Alice and Sam transactions weirdly have no inputs. Double-spend?
		assertTrue(l.calcBalance("Bob") == 3000);
		assertTrue(l.calcBalance("Milo") == 1500);
		assertTrue(l.calcBalance("Band") == 1500);
		assertTrue(l.calcBalance("Alice") == 0);
		assertTrue(l.calcBalance("Sam") == 0);
		l.addTransaction("dump; 1; (newtt, 0); 2; (Alice, 1000)(Milo, 500)");
		Entry miloToSign = Entry.getMempool().get(0);
		milo.sign(miloToSign);
		l.createBlock();
		CmdLedger.dumpFile(l, "testoutputs.txt");
		Ledger.destroy();
		Ledger.getInstance();
		CmdLedger.loadFromFile(l, "testoutputs.txt");
		l.createBlock();//Genesis block
		l.createBlock();//All newer transactions.
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

	/*@Test
	public void testWontAddUnbalancedTx() throws Exception {
		l.addTransaction("root; 0; ; 1; (Mark, 1000)");
		l.createBlock();
		l.addTransaction("split; 1; (root, 0); 2; (Mark, 1000)(Mateo, 1000)");
		assertTrue(Entry.getMempool().size() == 0);
		l.addTransaction("splitter; 1; (root, 0); 2; (Mark, 499)(Mateo, 499)");
		assertTrue(Entry.getMempool().size() == 0);
	}*/

}
