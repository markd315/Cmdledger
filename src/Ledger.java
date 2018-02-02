
public class Ledger {
/*
 * Format of Transactions:
<TransID>; M; (<TransID>, <vout>)^M; N; (<AcctID>, <amount>)^N 
Items in angle brackets are parameters, M and N are whole numbers, and caret M (or N) indicates M (or N) repetitions of the parenthesized pairs. 

Example Transaction:
4787df35; 1;(f2cea539, 0);3; (Bob, 150)(Alice, 845)(Gopesh, 5)
 */
	private boolean isInteractive, isVerbose;
	public String toString() {
		//TODO
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
