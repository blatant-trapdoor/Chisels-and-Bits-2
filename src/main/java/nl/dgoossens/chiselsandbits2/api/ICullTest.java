package nl.dgoossens.chiselsandbits2.api;

public interface ICullTest {
	/**
	 * Test if a bit of id otherId can be seen through
	 * myId.
	 */
	boolean isVisible(int myId, int otherId);
}
