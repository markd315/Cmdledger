import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

public class LedgerTest {

	Ledger l;
	@Before
	public void callBefore(){
		l = new Ledger();
	}
	
	@Test
	public void canLoadAndDumpTxFromFile() throws Exception {
		CmdLedger.loadFromFile(l, "testinputs.txt");
		assertTrue(l.getBlockchain().size() == 3);
		assertTrue(l.calcBalance("Bob") == 3000);
		assertTrue(l.calcBalance("Milo") == 1500);
		assertTrue(l.calcBalance("Band") == 1500);
		assertTrue(l.calcBalance("Alice") == 0);
		assertTrue(l.calcBalance("Sam") == 0);
		l.addTransaction("dump; 1; (newtt, 0); 2; (Alice, 1000)(Milo, 500)");
		CmdLedger.dumpFile(l, "testoutputs.txt");
		l = new Ledger();
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
		assertTrue(l.getBlockchain().size() == 0);
		
	}
	@Test
	public void testWillAddWellFormattedTx() throws Exception {
		l.addTransaction("root; 0; ; 1; (Mark, 1000)");
		l.addTransaction("new; 1; (root, 0); 1; (Matt, 1000)");
		l.addTransaction("split; 1; (new, 0); 2; (Mark, 500)(Mateo, 500)");
		assertTrue(l.getBlockchain().size() == 3);
		assertTrue(l.calcBalance("Matt") == 0);
		assertTrue(l.calcBalance("Mateo") == 500);
		assertTrue(l.calcBalance("Mark") == 500);
	}
	@Test
	public void testWontAddDoubleSpendTx() throws Exception {
		l.addTransaction("root; 0; ; 1; (Mark, 1000)");
		l.addTransaction("new; 1; (root, 0); 1; (Matt, 1000)");
		l.addTransaction("split; 1; (root, 0); 2; (Mark, 500)(Mateo, 500)");
		assertTrue(l.getBlockchain().size() == 2);
		assertTrue(l.calcBalance("Mark") == 0);
		assertTrue(l.calcBalance("Matt") == 1000);
		assertTrue(l.calcBalance("Mateo") == 0);
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
	}
	@Test
	public void testWontAddUnbalancedTx() throws Exception {
		l.addTransaction("root; 0; ; 1; (Mark, 1000)");
		l.addTransaction("new; 1; (root, 0); 1; (Matt, 1000)");
		l.addTransaction("split; 1; (new, 0); 2; (Mark, 1000)(Mateo, 1000)");
		l.addTransaction("splitter; 1; (new, 0); 2; (Mark, 499)(Mateo, 499)");
		assertTrue(l.getBlockchain().size() == 2);
		assertTrue(l.calcBalance("Mark") == 0);
		assertTrue(l.calcBalance("Matt") == 1000);
		assertTrue(l.calcBalance("Mateo") == 0);
	}
	
}
