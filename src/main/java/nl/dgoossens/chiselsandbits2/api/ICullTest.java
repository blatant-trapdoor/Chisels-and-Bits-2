package nl.dgoossens.chiselsandbits2.api;

public interface ICullTest {
	/**
	 * Test the visibility of a second spot
	 */
	boolean isVisible(
            int mySpot,
            int secondSpot);
}
