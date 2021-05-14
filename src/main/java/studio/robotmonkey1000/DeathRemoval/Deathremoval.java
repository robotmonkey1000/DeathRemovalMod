package studio.robotmonkey1000.DeathRemoval;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.fixes.PlayerUUID;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import studio.robotmonkey1000.DeathRemoval.net.DeathRemovalPacketHandler;
import studio.robotmonkey1000.DeathRemoval.net.RemoveWayPointPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("deathremoval")
public class Deathremoval {

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public static HashMap<UUID, HashMap<ResourceLocation, ArrayList<Vector3d>>> waypoints = new HashMap<UUID, HashMap<ResourceLocation, ArrayList<Vector3d>>>();

    public Deathremoval() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
        DeathRemovalPacketHandler.register();
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("deathremoval", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    public static void AddRemovalPoint(UUID uuid, ResourceLocation dimName, Vector3d position) {

        //Already contains some queued for the player
        if(waypoints.containsKey(uuid)) {
            HashMap<ResourceLocation, ArrayList<Vector3d>> playerWayPoints = waypoints.get(uuid);

            //Already Contains a Queue for this world
            if(playerWayPoints.containsKey(dimName)) {
                //This already contains a list of positions for this dimension so we just add it.
                playerWayPoints.get(dimName).add(position);
            }else {
                //Create a new list for this dimension and then add the position
                playerWayPoints.put(dimName, new ArrayList<Vector3d>());
                playerWayPoints.get(dimName).add(position);
            }
        } else {
            HashMap<ResourceLocation, ArrayList<Vector3d>> playerWayPoints = new HashMap<>();
            playerWayPoints.put(dimName, new ArrayList<Vector3d>());
            playerWayPoints.get(dimName).add(position);
            waypoints.put(uuid, playerWayPoints);
        }
    }



    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m -> m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LogMessage("HELLO from server starting");
    }


//    @SubscribeEvent
//    public void onJoinWorldEvent(final EntityJoinWorldEvent event) {
//        if(event.getEntity() instanceof PlayerEntity && !event.getWorld().isRemote) {
//
//            Deathremoval.LogMessage("Player Changing Worlds!");
//            PlayerEntity player = (PlayerEntity) event.getEntity();
//            if(waypoints.containsKey(player.getUniqueID())) {
//                HashMap<ResourceLocation, ArrayList<Vector3d>> playerWayPoints = waypoints.get(player.getUniqueID());
//                if(playerWayPoints.containsKey(event.getWorld().getDimensionKey().getRegistryName())) {
//                    ArrayList<Vector3d> points = playerWayPoints.get(event.getWorld().getDimensionKey().getRegistryName());
//                    for(Vector3d p: points) {
//                        Deathremoval.LogMessage(event.getWorld().toString());
//                        Deathremoval.LogMessage(event.getEntity().world.toString());
//
//                        Deathremoval.LogMessage("Sending Corpse de-spawn packet to: " + player.getName().getString());
//                        Deathremoval.LogMessage("Corpse Position on Server: X: " + p.getX() + " Y: " + p.getY() + " Z: " + p.getZ());
//                        DeathRemovalPacketHandler.INSTANCE.sendTo(new RemoveWayPointPacket(p.getX(), p.getY(), p.getZ()), ((ServerPlayerEntity)player).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
//                    }
//                    playerWayPoints.remove(event.getWorld().getDimensionKey().getRegistryName());
//                }
//            }
//        }
//    }

//    @SubscribeEvent
//    public void onPlayerClone(final PlayerEvent.Clone event) {
//        Deathremoval.LogMessage("Player Cloning!");
//        PlayerEntity player = event.getPlayer();
//        if(waypoints.containsKey(player.getUniqueID())) {
//            HashMap<ResourceLocation, ArrayList<Vector3d>> playerWayPoints = waypoints.get(player.getUniqueID());
//            if(playerWayPoints.containsKey(player.world.getDimensionKey().getRegistryName())) {
//                ArrayList<Vector3d> points = playerWayPoints.get(player.world.getDimensionKey().getRegistryName());
//                for(Vector3d p: points) {
//                    Deathremoval.LogMessage("Sending Corpse de-spawn packet to: " + player.getName().getString());
//                    Deathremoval.LogMessage("Corpse Position on Server: X: " + p.getX() + " Y: " + p.getY() + " Z: " + p.getZ());
//                    DeathRemovalPacketHandler.INSTANCE.sendTo(new RemoveWayPointPacket(p.getX(), p.getY(), p.getZ()), ((ServerPlayerEntity)player).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
//                }
//                playerWayPoints.remove(player.world.getDimensionKey().getRegistryName());
//            }
//        }
//    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }

    }



    public static void LogMessage(String message) {
        LOGGER.info("[deathremoval] " + message);
    }
}
