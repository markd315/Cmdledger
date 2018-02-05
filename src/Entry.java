import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Entry {
	private String id;
	private Ledger parentLedger;
	private List<Input> inputs;
	private List<Output> outputs;

	public Entry() {
		this.id = txIDgen();
	}
	public void setParentLedger(Ledger l) {
		this.parentLedger= l;
	}
	
	public void setSingleGenesis(Output output) {
		this.inputs = new ArrayList<Input>();
		this.outputs = new ArrayList<Output>(); //Empty lists for the inputs.
		this.addOutput(output);
	}
	
	private void addOutput(Output op) {
		this.outputs.add(op);
		
	}
	private String txIDgen() {
		Random rng = new Random();
		String str = "";
		for(int i=0; i<16; i++) {
			char toAdd = (char) rng.nextInt(122-65); //65-122 inclusive
			toAdd += 65;
			str+=toAdd;
		}
		return str;
	}
	
	public String toString() {
		String ret = id + "; ";
		for (int i = 0; i < inputs.size(); i++) {
			ret += "(" + inputs.get(i).getId() + ", " + inputs.get(i).getIndex() + ")";
		}
		ret += "; " + outputs.size();
		for (int i = 0; i < outputs.size(); i++) {
			ret += "(" + outputs.get(i).getName() + ", " + outputs.get(i).getAmount() + ")";
		}
		return ret;
	}

	public int sumOfIns() {//TODO I WROTE THIS DRUNK AND IT NEEDS A UNIT TEST
		int ret =0;
		for(int inputIndexIndex=0; inputIndexIndex<inputs.size(); inputIndexIndex++){
		for(int i=0; i<parentLedger.getBlockchain().size(); i++) {//This runs in constant time with the number of inputs.
			if(this.getInputs().contains(parentLedger.getBlockchain().get(i))) {//
				//We found an input to THIS transaction entry.
				//So now what we do is sum up the INDEX OUTPUT of those valid transactions that input here.
				//We need to use the input index to know WHICH one is the input.
				ret+= parentLedger.getBlockchain().get(i).getOutputs().get(inputIndexIndex).getAmount();//We need to find the 
				//TODO
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
		int sum=0;
		for(Output k : this.outputs) {
			sum+=k.getAmount();
		}
		return sum;
	}
}
