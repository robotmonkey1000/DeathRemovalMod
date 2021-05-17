package studio.robotmonkey1000.DeathRemoval.net;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import studio.robotmonkey1000.DeathRemoval.Deathremoval;

import java.util.function.Supplier;

public class DeathpointCreatedPacket {
    private final double x, y, z;
    DeathpointCreatedPacket(final PacketBuffer packetBuffer) {
        this.x = packetBuffer.readDouble();
        this.y = packetBuffer.readDouble();
        this.z = packetBuffer.readDouble();
    }

    public DeathpointCreatedPacket(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    void encode(final PacketBuffer packetBuffer) {
        packetBuffer.writeDouble(this.x);
        packetBuffer.writeDouble(this.y);
        packetBuffer.writeDouble(this.z);
    }

    public static void handle(DeathpointCreatedPacket msg, Supplier<NetworkEvent.Context> ctx) {

        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide() == LogicalSide.SERVER) {
            Deathremoval.LogMessage(ctx.get().getSender().getName().getString() + " has created a death waypoint!");
            ctx.get().enqueueWork(() -> {
                if( Deathremoval.recentDeathForPlayer.containsKey(context.getSender().getUniqueID())) {
                    Deathremoval.recentDeathForPlayer.get(ctx.get().getSender().getUniqueID()).setNoGravity(true);
                    Deathremoval.recentDeathForPlayer.get(ctx.get().getSender().getUniqueID()).setPosition(msg.x, msg.y, msg.z);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
