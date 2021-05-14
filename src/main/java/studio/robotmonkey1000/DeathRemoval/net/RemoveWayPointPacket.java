package studio.robotmonkey1000.DeathRemoval.net;


import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import studio.robotmonkey1000.DeathRemoval.Deathremoval;
import xaero.common.XaeroMinimapSession;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointsManager;
import xaero.minimap.XaeroMinimap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Supplier;

public class RemoveWayPointPacket {
    private final double x;
    private final double y;
    private final double z;
    RemoveWayPointPacket(final PacketBuffer packetBuffer) {
        this.x = packetBuffer.readDouble();
        this.y = packetBuffer.readDouble();
        this.z = packetBuffer.readDouble();
    }

    public RemoveWayPointPacket(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    void encode(final PacketBuffer packetBuffer) {
        packetBuffer.writeDouble(this.x);
        packetBuffer.writeDouble(this.y);
        packetBuffer.writeDouble(this.z);
    }

    public static void handle(RemoveWayPointPacket msg, Supplier<NetworkEvent.Context> ctx) {

        Deathremoval.LogMessage("Corpse De-spawn Packet Received");
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getOriginationSide() == LogicalSide.SERVER) {
            ctx.get().enqueueWork(() -> {

                Deathremoval.LogMessage("Corpse De-spawning");
                ArrayList<Waypoint> wayPoints = XaeroMinimapSession.getForPlayer(Minecraft.getInstance().player).getWaypointsManager().getWaypoints().getList();

                Deathremoval.LogMessage("WayPoint Count: " + wayPoints.size());
                WaypointsManager manager = XaeroMinimapSession.getForPlayer(Minecraft.getInstance().player).getWaypointsManager();

                Deathremoval.LogMessage("Corpse Position on Client: X: " + msg.x + " Y: " + msg.y + " Z: " + msg.z);
                for(Waypoint w: wayPoints) {
                    if(w.getDistanceSq(msg.x, msg.y, msg.z) <= 2) {
                        if(w.getName().equalsIgnoreCase("gui.xaero_deathpoint") || w.getName().equalsIgnoreCase("gui.xaero_deathpoint_old")) {

                            Deathremoval.LogMessage("Found Waypoint Removing");
                            wayPoints.remove(w);
                            break;
                        }
                    }
                }

                try {
                    XaeroMinimap.instance.getSettings().saveWaypoints(manager.getCurrentWorld(manager.getAutoContainerID(), manager.getAutoWorldID()));
                } catch (IOException var5) {
                    var5.printStackTrace();
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

}
