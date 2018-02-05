import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Entry {
	private String id;
	private Ledger parentLedger;
	private List<Input> inputs;
	private List<Output> outputs;

	public Entry(Ledger parent, List<Input> ins, List<Output> outs) {
		this.id = txIDgen();
		this.inputs = ins;
		this.outputs = outs;
	}

	public Entry(Ledger parent, List<Input> ins, Output... outs) {
		this.id = txIDgen();
		this.inputs = ins;
		this.outputs = new ArrayList<Output>();

		for (Output o : outs)
			this.outputs.add(o);
	}

	public Entry(Ledger parent, Input ins, Output... outs) {
		this.id = txIDgen();
		this.inputs = new ArrayList<Input>();
		this.inputs.add(ins);
		this.outputs = new ArrayList<Output>();

		for (Output o : outs)
			this.outputs.add(o);
	}

	public Entry() {
		//We must call a genesis set on this constructor or add valid inputs or outputs for it to be accepted.
		
	}

	public void setParentLedger(Ledger l) {
		this.parentLedger = l;
	}

	public void setSingleGenesis(Ledger l, Output output) {
		if(l.getBlockchain().get(0) != this) {
			System.err.println("This is not the root transaction!");
			return;
		}
		this.id = "root";
		this.inputs = new ArrayList<Input>();
		this.outputs = new ArrayList<Output>(); // Empty lists for the inputs.
		this.addOutput(output);
		for(Output o : this.outputs) {
			o.setId(this.id);
		}
	}

	private void addOutput(Output op) {
		this.outputs.add(op);

	}

	private String txIDgen() {
		Random rng = new Random();
		String str = "";
		for (int i = 0; i < 8; i++) {
			char toAdd = (char) rng.nextInt(122 - 97); // 65-122 inclusive
			toAdd += 97;
			str += toAdd;
		}
		return str;
	}

	public String toString() {
		String ret = id + "; ";
		ret += inputs.size() + "; ";
		for (int i = 0; i < inputs.size(); i++) {
			ret += "(" + inputs.get(i).getId() + ", " + inputs.get(i).getIndex() + ")";
		}
		ret += "; " + outputs.size() + "; ";
		for (int i = 0; i < outputs.size(); i++) {
			ret += "(" + outputs.get(i).getName() + ", " + outputs.get(i).getAmount() + ")";
		}
		return ret;
	}

	public int sumOfIns() {// TODO
		int ret = 0;
		for(Input in : this.inputs) {
			//For each, lookup the location of the output.
			//Safely break if DNE.
			for(Entry e : parentLedger.getBlockchain()) {
				if (e.id.equals(in.getId())) {
					try {
					ret +=e.getOutputs().get(in.getIndex()).getAmount();
					}catch(Exception ex) {
						System.err.println("Couldn't find referenced previous output to use as an input.");
						return -1;
					}
				}
			}
		}
		return ret;
	}

	public List<Output> getOutputs() {
		return outputs;
	}

	public List<Input> getInputs() {
		return inputs;
	}

	public String getId() {
		return id;
	}

	public int sumOfOuts() {
		int sum = 0;
		for (Output k : this.outputs) {
			sum += k.getAmount();
		}
		return sum;
	}

	public void setTxID(String string) {
		this.id = string;
	}
}
