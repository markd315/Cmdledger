import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class LedgerTest {

	Ledger l;
	@Before
	public void callBefore() {
		l = new Ledger();
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
