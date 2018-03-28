import java.io.Serializable;

public class Input implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1008236880333943238L;
	private String id;
	private int index;

	public Input(String id, int index) {
		this.id = id;
		this.index = index;
	}

	public String getId() {
		return id;
	}

	public int getIndex() {
		return index;
	}

}
