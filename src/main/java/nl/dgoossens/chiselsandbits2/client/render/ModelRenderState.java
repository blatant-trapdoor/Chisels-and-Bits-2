package nl.dgoossens.chiselsandbits2.client.render;

import net.minecraft.util.Direction;
import nl.dgoossens.chiselsandbits2.api.IStateRef;

/**
 * An object storing references to all neighbouring blocks.
 */
public class ModelRenderState {
	// less objects/garbage to clean up, and less memory usage.
	private IStateRef north, south, east, west, up, down;

	public IStateRef get(final Direction side) {
		switch(side) {
			case DOWN:
				return down;
			case EAST:
				return east;
			case NORTH:
				return north;
			case SOUTH:
				return south;
			case UP:
				return up;
			case WEST:
				return west;
			default:
				return null;
		}
	}

	public void put(final Direction side, final IStateRef value) {
		switch(side) {
			case DOWN:
				down = value;
				break;
			case EAST:
				east = value;
				break;
			case NORTH:
				north = value;
				break;
			case SOUTH:
				south = value;
				break;
			case UP:
				up = value;
				break;
			case WEST:
				west = value;
				break;
			default:
		}
	}

	public ModelRenderState() {}
	public ModelRenderState(final ModelRenderState sides) {
		if(sides != null) {
			north = sides.north;
			south = sides.south;
			east = sides.east;
			west = sides.west;
			up = sides.up;
			down = sides.down;
		}
	}
}
