package studio.robotmonkey1000.DeathRemoval.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkDirection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.robotmonkey1000.DeathRemoval.Deathremoval;
import studio.robotmonkey1000.DeathRemoval.net.DeathRemovalPacketHandler;
import studio.robotmonkey1000.DeathRemoval.net.RemoveWayPointPacket;
import xaero.common.events.FMLEventHandler;

import java.util.ArrayList;
import java.util.HashMap;

@Mixin(FMLEventHandler.class)
public class MixinWayPointPlayerTickPost {


//    @Inject(at = @At(value = "TAIL"), method="Lxaero/common/events/FMLEventHandler;playerTickPostOverridable(Lxaero/common/XaeroMinimapSession;)V", cancellable = true, remap=false)
//    protected void playerTickPostOverridable(XaeroMinimapSession minimapSession, CallbackInfo ci) {
////        Deathremoval.LogMessage("WayPoints Updating!!!!");
//    }

    @Inject(at = @At(value = "TAIL"), method="Lxaero/common/events/FMLEventHandler;handlePlayerTickEvent(Lnet/minecraftforge/event/TickEvent$PlayerTickEvent;)V", cancellable = true, remap=false)
    public void handlePlayerTickEvent(TickEvent.PlayerTickEvent event, CallbackInfo ci) {
        if(event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.START) {
            PlayerEntity player = event.player;
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
        }
    }
}
