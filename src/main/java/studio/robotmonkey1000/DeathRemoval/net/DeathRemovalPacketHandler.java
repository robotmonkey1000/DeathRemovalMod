package studio.robotmonkey1000.DeathRemoval.net;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class DeathRemovalPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("deathremoval", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int messageID = 0;
        INSTANCE.registerMessage(++messageID, RemoveWayPointPacket.class, RemoveWayPointPacket::encode, RemoveWayPointPacket::new, RemoveWayPointPacket::handle);
        INSTANCE.registerMessage(++messageID, MinimapUpdatedPacket.class, MinimapUpdatedPacket::encode, MinimapUpdatedPacket::new, MinimapUpdatedPacket::handle);
        INSTANCE.registerMessage(++messageID, DeathpointCreatedPacket.class, DeathpointCreatedPacket::encode, DeathpointCreatedPacket::new, DeathpointCreatedPacket::handle);
    }
}

