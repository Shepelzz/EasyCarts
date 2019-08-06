package com.github.xericore.easycarts.utilities;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

public class Utils
{

	public static RideableMinecart getValidMineCart(Vehicle vehicle, boolean mustHavePassenger)
	{
		RideableMinecart cart = null;

		if (!(vehicle instanceof RideableMinecart))
			return null;
		cart = (RideableMinecart) vehicle;

		Entity firstPassenger = GetFirstPassenger(cart);
		if (firstPassenger == null)
			return null;

		if (mustHavePassenger && (cart.isEmpty() || !(firstPassenger instanceof Player)))
			return null;

		return cart;
	}

	public static Entity GetFirstPassenger(Minecart toCart)
	{
		List<Entity> passengers = toCart.getPassengers();

		if (passengers.isEmpty())
			return null;

		return passengers.get(0);
	}

	public static Vector getStraightUnitVectorFromYaw(float yaw)
	{
		BlockFace facing = getStraightBlockFaceFromYaw(yaw);
		switch (facing)
		{
		case SOUTH:
			return new Vector(0, 0, 1);
		case WEST:
			return new Vector(-1, 0, 0);
		case NORTH:
			return new Vector(0, 0, -1);
		default: // EAST
			return new Vector(1, 0, 0);
		}
	}

	public static BlockFace getStraightBlockFaceFromYaw(float yaw)
	{
		if (yaw < 0)
		{ // Map all negative values to positives. E.g. -45° = +315°
			yaw = yaw + 360;
		}
		yaw = yaw % 360; // crop value, e.g. if it's 460° --> 100°

		float straightAngle = 90;

		if ((yaw >= 0) && (yaw < (straightAngle / 2)) || (yaw >= (360 - (straightAngle / 2))))
		{
			return BlockFace.SOUTH;
		} else if ((yaw >= (straightAngle / 2)) && (yaw < 135))
		{
			return BlockFace.WEST;
		} else if ((yaw >= 135) && (yaw < (360 - (straightAngle * 1.5))))
		{
			return BlockFace.NORTH;
		} else
		{
			return BlockFace.EAST;
		}
	}

    public static BlockFace getCartBlockFaceDirection(RideableMinecart cart)
    {
        Vector velocity = cart.getVelocity();
        float yaw = cart.getLocation().getYaw();

        if(approximatelyEquals(yaw, 0) || approximatelyEquals(yaw, 180))
        {
            // We are either facing east or west
        }
        else if(approximatelyEquals(yaw, 90) || approximatelyEquals(yaw, -90) || approximatelyEquals(yaw, 270))
        {
            // We are either facing north or south
            if(approximatelyEquals(velocity.getX(), 0) && approximatelyEquals(velocity.getZ(), -1))
                return BlockFace.NORTH;
            if(approximatelyEquals(velocity.getX(), 0) && approximatelyEquals(velocity.getZ(), 1))
                return BlockFace.SOUTH;
        }
        else
        {

        }

        return null;
    }

    public static boolean approximatelyEquals(double a, double b){
        return Math.abs(a-b)<0.0001d;
    }

	public static boolean isMovingUp(VehicleMoveEvent event)
	{
		return event.getTo().getY() - event.getFrom().getY() > 0;
	}

	public static boolean isMovingDown(VehicleMoveEvent event)
	{
		return event.getTo().getY() - event.getFrom().getY() < 0;
	}

	public static void pushNearbyEntities(RideableMinecart cart, Location cartLocation)
	{
		// To avoid collision, the entity must be located at least 1.0 block away from the cart.
		// The entities will be moved to this distance if they are within the search box when the cart is moving.
		// We actually move the entity a little bit further, to avoid it moving right back into the search box.

		// We cannot use cart.getVelocity() because on diagonal rails, this returns +x,0,0 then 0,0,+z and then diagonal
		// (depending on movement direction).
		// Then it looks like we are moving e.g. left, then right, then diagonal and we cannot distinguish between this
		// and a real straight movement.
		// Luckily, the getLocation().getDirection() is unaffected by this. However, for some unknown reason we have to
		// rotate that vector 90° clockwise to get the correct direction.
		Vector cartVector = (new Vector(-cart.getLocation().getDirection().getZ(), 0, cart.getLocation().getDirection().getX()))
				.normalize();

		Vector velocityNormalRight = new Vector(-cartVector.getZ(), 0, cartVector.getX());
		Vector velocityNormalLeft = new Vector(cartVector.getZ(), 0, -cartVector.getX());

		// Adjust size of box to be between 1-2, depending on movement direction
		List<Entity> nearbyEntities = cart.getNearbyEntities(1 + Math.abs(cartVector.getX()), 1, 1 + Math.abs(cartVector.getZ()));

		for (Entity entity : nearbyEntities)
		{
			// Remove empty minecarts still on track
			if ((entity instanceof Minecart) && entity.isEmpty())
			{
				entity.remove();
				continue;
			}

			// Only move monsters, animals and NPCs, not players
			if ((entity instanceof Monster) || (entity instanceof Animals) || (entity instanceof NPC) || (entity instanceof Player))
			{
				// Entity is not in a minecart, thus we can move it
				if (entity.isInsideVehicle())
					continue;

				Location entityLocation = entity.getLocation();

				// The vector between the current cart location and the entity location, needed to determine which direction to
				// move the entity to.
				Vector cartToEntity = new Vector(entityLocation.getX() - cartLocation.getX(), 0,
						entityLocation.getZ() - cartLocation.getZ());

				// The cross product vector will point up- or downwards depending on the location of the second vector
				if (cartVector.crossProduct(cartToEntity).getY() > 0)
				{
					entity.teleport(entityLocation.add(velocityNormalLeft.multiply(0.5)));
				} else
				{
					entity.teleport(entityLocation.add(velocityNormalRight.multiply(0.5)));
				}
			}
		}
	}
}
