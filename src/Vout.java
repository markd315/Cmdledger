
public class Vout {
	private String id;
	private Integer amount;
	private int index;
	
	public Vout(String id, Integer amount, int index) {
		this.id = id;
		this.amount = amount;
		this.index = index;
	}
	public boolean matches(String testID, int testIndex) {
		return testID.equals(this.id) && index==testIndex;
	}

}
