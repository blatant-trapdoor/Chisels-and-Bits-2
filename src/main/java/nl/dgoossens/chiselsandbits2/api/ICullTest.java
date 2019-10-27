package nl.dgoossens.chiselsandbits2.api;

/**
 * A test designed to establish whether or not a given bit type should be visible
 * through another type.
 */
public interface ICullTest {
    /**
     * Test if a bit of id otherId can be seen through
     * myId.
     */
    boolean isVisible(int myId, int otherId);
}
