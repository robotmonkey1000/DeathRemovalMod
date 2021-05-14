package studio.robotmonkey1000.DeathRemoval.mixin;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkDirection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.robotmonkey1000.DeathRemoval.net.DeathRemovalPacketHandler;
import studio.robotmonkey1000.DeathRemoval.net.MinimapUpdatedPacket;
import studio.robotmonkey1000.DeathRemoval.net.RemoveWayPointPacket;
import xaero.common.minimap.waypoints.WaypointsManager;

@Mixin(WaypointsManager.class)
public class MixinWayPointManager {

    @Inject(at = @At(value = "TAIL"), method="Lxaero/common/minimap/waypoints/WaypointsManager;onServerLevelId(I)V", cancellable = true, remap=false)
    public void onServerLevelId(int id, CallbackInfo ci) {
        DeathRemovalPacketHandler.INSTANCE.sendToServer(new MinimapUpdatedPacket());
    }
}
