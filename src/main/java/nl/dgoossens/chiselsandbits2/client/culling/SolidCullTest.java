package nl.dgoossens.chiselsandbits2.client.culling;


import nl.dgoossens.chiselsandbits2.api.ICullTest;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;

/**
 * Basic Solid Culling, culls almost all the faces but this works fine for solid
 * things.
 */
public class SolidCullTest implements ICullTest {
	@Override
	public boolean isVisible(final int mySpot, final int secondSpot) {
		return ChiselUtil.getTypeFromStateID(mySpot).shouldShow(ChiselUtil.getTypeFromStateID(secondSpot));
	}
}
