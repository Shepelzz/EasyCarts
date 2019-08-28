package com.github.xericore.easycarts;

import com.github.xericore.easycarts.utilities.RailTracer;
import com.github.xericore.easycarts.utilities.RailUtils;
import com.github.xericore.easycarts.utilities.Utils;
import org.bukkit.Material;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class DebugClickListener implements Listener
{
    public static EasyCarts easyCartsPlugin;

    public DebugClickListener(EasyCarts theInstance)
    {
        easyCartsPlugin = theInstance;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMyPlayerInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            easyCartsPlugin.logger.info("");
            easyCartsPlugin.logger.info("BlockData: " + event.getClickedBlock().getBlockData());
            easyCartsPlugin.logger.info("Material: " + event.getClickedBlock().getBlockData().getMaterial());

            switch (event.getClickedBlock().getBlockData().getMaterial())
            {
                case RAIL:
                    break;
                case ACTIVATOR_RAIL:
                    break;
                case POWERED_RAIL:
                    break;
                case DETECTOR_RAIL:
                    break;
                default:
                    return;
            }

            Rail railData = (Rail) event.getClickedBlock().getBlockData();
            easyCartsPlugin.logger.info("Shape: " + railData.getShape());

            if(player.getInventory().getItemInMainHand().getType() == Material.RAIL)
            {
                LogTracedRails(event, player);
            }
        }
    }

    private void LogTracedRails(PlayerInteractEvent event, Player player)
    {
        int traceLength = 6;

        RailTracer railTracer = new RailTracer();

        List<Rail.Shape> tracedRails = railTracer.traceRails(
                event.getClickedBlock(),
                Utils.getStraightBlockFaceFromYaw(player.getLocation().getYaw()),
                traceLength);

        easyCartsPlugin.logger.info("");
        easyCartsPlugin.logger.info("Traced Rails:");

        for (Rail.Shape railShape : tracedRails)
            easyCartsPlugin.logger.info("   " + railShape);

        boolean areAllRailsConnectedStraight = areAllRailsConnectedStraight(tracedRails);

        easyCartsPlugin.logger.info("Is Safe For Speedup: " + areAllRailsConnectedStraight);
    }

    public static boolean areAllRailsConnectedStraight(List<Rail.Shape> tracedRails)
    {
        int tracedRailsCount = 0;

        for (int i = 0; i < tracedRails.size()-1; i++)
        {
            tracedRailsCount++;

            if(RailUtils.areRailsConnectedStraight(tracedRails.get(i), tracedRails.get(i+1)) == false)
                return false;
        }

        return tracedRailsCount >= tracedRails.size() - 1;
    }
}
