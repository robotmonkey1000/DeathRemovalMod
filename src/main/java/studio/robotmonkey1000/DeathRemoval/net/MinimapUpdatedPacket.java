package studio.robotmonkey1000.DeathRemoval.net;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import studio.robotmonkey1000.DeathRemoval.Deathremoval;
import xaero.common.XaeroMinimapSession;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointsManager;
import xaero.minimap.XaeroMinimap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

public class MinimapUpdatedPacket {

    MinimapUpdatedPacket(final PacketBuffer packetBuffer) {

    }

    public MinimapUpdatedPacket() {
    }

    void encode(final PacketBuffer packetBuffer) {
    }

    public static void handle(MinimapUpdatedPacket msg, Supplier<NetworkEvent.Context> ctx) {

        Deathremoval.LogMessage(ctx.get().getSender().getName().getString() + " has updated their minimap!");
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide() == LogicalSide.SERVER) {
            ctx.get().enqueueWork(() -> {
                PlayerEntity player = ctx.get().getSender();
                if(Deathremoval.waypoints.containsKey(player.getUniqueID())) {
                    HashMap<ResourceLocation, ArrayList<Vector3d>> playerWayPoints = Deathremoval.waypoints.get(player.getUniqueID());
                    if(playerWayPoints.containsKey(player.world.getDimensionKey().getLocation())) {
                        ArrayList<Vector3d> points = playerWayPoints.get(player.world.getDimensionKey().getLocation());
                        for(Vector3d p: points) {
                            Deathremoval.LogMessage("Sending Corpse de-spawn packet to: " + player.getName().getString());
                            Deathremoval.LogMessage("Corpse Position on Server: X: " + p.getX() + " Y: " + p.getY() + " Z: " + p.getZ());
                            DeathRemovalPacketHandler.INSTANCE.sendTo(new RemoveWayPointPacket(p.getX(), p.getY(), p.getZ()), ((ServerPlayerEntity)player).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
                        }
                        playerWayPoints.remove(player.world.getDimensionKey().getLocation());
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
