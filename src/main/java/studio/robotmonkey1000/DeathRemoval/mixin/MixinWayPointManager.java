package studio.robotmonkey1000.DeathRemoval.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkDirection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.robotmonkey1000.DeathRemoval.net.DeathRemovalPacketHandler;
import studio.robotmonkey1000.DeathRemoval.net.DeathpointCreatedPacket;
import studio.robotmonkey1000.DeathRemoval.net.MinimapUpdatedPacket;
import studio.robotmonkey1000.DeathRemoval.net.RemoveWayPointPacket;
import xaero.common.minimap.waypoints.WaypointWorld;
import xaero.common.minimap.waypoints.WaypointsManager;
import xaero.common.misc.OptimizedMath;

@Mixin(WaypointsManager.class)
public class MixinWayPointManager {

    @Inject(at = @At(value = "TAIL"), method="Lxaero/common/minimap/waypoints/WaypointsManager;onServerLevelId(I)V", cancellable = true, remap=false)
    public void onServerLevelId(int id, CallbackInfo ci) {
        DeathRemovalPacketHandler.INSTANCE.sendToServer(new MinimapUpdatedPacket());
    }

    @Inject(at = @At(value = "TAIL"), method="Lxaero/common/minimap/waypoints/WaypointsManager;createDeathpoint(Lnet/minecraft/entity/player/PlayerEntity;Lxaero/common/minimap/waypoints/WaypointWorld;Z)V", cancellable = true, remap=false)
    private void createDeathpoint(PlayerEntity p, WaypointWorld wpw, boolean temp, CallbackInfo ci) {
        double dimDiv = this.getDimensionDivision(wpw.getContainer().getKey());
        DeathRemovalPacketHandler.INSTANCE.sendToServer(new DeathpointCreatedPacket(OptimizedMath.myFloor((double)OptimizedMath.myFloor(p.getPosX()) * dimDiv), OptimizedMath.myFloor(p.getPosY()), OptimizedMath.myFloor((double)OptimizedMath.myFloor(p.getPosZ()) * dimDiv)));
    }

    @Shadow
    public double getDimensionDivision(String worldContainerID) {
        throw new IllegalStateException("Mixin failed to shadow isEmpty()");
    }
}
