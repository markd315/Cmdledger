
public class Output {
	private String name;
	private int amount;
	private String id;

	public Output(String name, int amount, Entry entry) {
		this.name = name;
		this.amount = amount;
		if(entry != null) {
			this.id = entry.getId();
		}
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	public int getAmount() {
		return amount;
	}

	public void setEntry(Entry e) {
		this.id = e.getId();
	}

	public void setId(String id2) {
		this.id = id2;
	}

	public int indexInChain(Ledger ledger) {
		for(Entry e : ledger.getBlockchain()) {
			for(int i=0; i<e.getOutputs().size(); i++) {
				if(this.equals(e.getOutputs().get(i))) {
					return i;
				}
			}
		}
		return -1;
	}
}
