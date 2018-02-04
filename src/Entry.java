import java.util.List;

public class Entry {
	private String id;
	private Ledger parentLedger;
	private List<String> inputIDs;
	private List<Integer> inputIndices;
	private List<String> outputNames;
	private List<Integer> outputAmounts;

	public Entry() {

	}

	public String toString() {
		String ret = id + "; ";
		for (int i = 0; i < inputIDs.size(); i++) {
			ret += "(" + inputIDs.get(i) + ", " + inputIndices.get(i) + ")";
		}
		ret += "; " + outputNames.size();
		for (int i = 0; i < outputNames.size(); i++) {
			ret += "(" + outputNames.get(i) + ", " + outputAmounts.get(i) + ")";
		}
		return ret;
	}

	public int sumOfIns() {//TODO I WROTE THIS DRUNK AND IT NEEDS A UNIT TEST
		int ret =0;
		for(int inputIndexIndex=0; inputIndexIndex<inputIndices.size(); inputIndexIndex++){
		for(int i=0; i<parentLedger.getBlockchain().size(); i++) {//This runs in constant time with the number of inputs.
			if(this.getInputIDs().contains(parentLedger.getBlockchain().get(i).getId())) {//
				//We found an input to THIS transaction entry.
				//So now what we do is sum up the INDEX OUTPUT of those valid transactions that input here.
				//We need to use the input index to know WHICH one is the input.
				ret+= parentLedger.getBlockchain().get(i).getOutputAmounts().get(inputIndices.get(inputIndexIndex));//We need to find the 
			}
			}
		}
		return ret;
	}

	private List<Integer> getOutputAmounts() {
		return outputAmounts;
	}

	private List<String> getInputIDs() {
		return inputIDs;
	}

	public String getId() {
		return id;
	}

	public int sumOfOuts() {
		// TODO Auto-generated method stub
		return 0;
	}

}
