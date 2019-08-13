package nl.dgoossens.chiselsandbits2.api;

/**
 * An entry representing one state and the quantity of said state in a chiseled tile.
 */
public class StateCount {
	final private int stateId;
	private int quantity;

	public StateCount(final int id, final int q) {
		stateId = id;
		quantity = q;
	}

	public void setQuantity(int quan) { quantity=quan; }
	public int getStateId() { return stateId; }
	public int getQuantity() { return quantity; }
}
