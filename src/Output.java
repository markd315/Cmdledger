
public class Output {
	private String name;
	private int amount;
	private String id;

	public Output(String name, int amount, Entry entry) {
		this.name = name;
		this.amount = amount;
		this.id = entry.getId();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public void setEntry(Entry e) {
		this.id = e.getId();
	}
}
