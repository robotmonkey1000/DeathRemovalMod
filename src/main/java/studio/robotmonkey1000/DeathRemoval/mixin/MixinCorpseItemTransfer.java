package studio.robotmonkey1000.DeathRemoval.mixin;

import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.entities.CorpseInventoryBaseEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.datafix.fixes.PlayerUUID;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.robotmonkey1000.DeathRemoval.Deathremoval;
import studio.robotmonkey1000.DeathRemoval.net.DeathRemovalPacketHandler;
import studio.robotmonkey1000.DeathRemoval.net.RemoveWayPointPacket;
import xaero.common.XaeroMinimapSession;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointsManager;
import xaero.minimap.XaeroMinimap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Supplier;

@Mixin(CorpseEntity.class)
public abstract class MixinCorpseItemTransfer extends CorpseInventoryBaseEntity implements IInventory {
    @Shadow public abstract UUID getCorpseUUID();

    @Shadow public abstract String getCorpseName();

    @Shadow private int emptyAge;
    private boolean clearWaypoint = false;
    private MixinCorpseItemTransfer(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Inject(at = @At(value = "TAIL"), method="Lde/maxhenkel/corpse/entities/CorpseEntity;func_70071_h_()V", cancellable = true, remap=false)
//    @Inject(at = @At("HEAD"), method="Lde/maxhenkel/corpse/entities/CorpseEntity;func_70071_h_()V", cancellable = true, remap=false)
    public void func_70106_y(CallbackInfo info) {
        if ((Integer) Main.SERVER_CONFIG.corpseForceDespawnTime.get() > 0 && this.getCorpseAge() > (Integer)Main.SERVER_CONFIG.corpseForceDespawnTime.get()) {
            ServerPlayerEntity player = (ServerPlayerEntity) this.world.getPlayerByUuid(this.getCorpseUUID());
            if(player != null) {
                sendDespawnPacket(player);
            }
        } else {
            if (this.func_191420_l() && this.emptyAge < 0) {
                this.emptyAge = this.getCorpseAge();
            } else if (this.func_191420_l() && this.getCorpseAge() - this.emptyAge >= (Integer)Main.SERVER_CONFIG.corpseDespawnTime.get()) {
                ServerPlayerEntity player = (ServerPlayerEntity) this.world.getPlayerByUuid(this.getCorpseUUID());
                if(player != null) {
                    sendDespawnPacket(player);
                } else {
                    Deathremoval.LogMessage("Adding Point for when player changes world " + this.world.getDimensionKey().getLocation());
                    Deathremoval.AddRemovalPoint(this.getCorpseUUID(),  this.world.getDimensionKey().getLocation(), new Vector3d(this.getPosX(), this.getPosY(), this.getPosZ()));
                }
            }
        }

    }
    void sendDespawnPacket(ServerPlayerEntity player) {
        Deathremoval.LogMessage("Sending Corpse de-spawn packet to: " + player.getName().getString());
        Deathremoval.LogMessage("Corpse Position on Server: X: " + this.getPosX() + " Y: " + this.getPosY() + " Z: " + this.getPosZ());
        DeathRemovalPacketHandler.INSTANCE.sendTo(new RemoveWayPointPacket(this.getPosX(), this.getPosY(), this.getPosZ()), player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    }
    @Shadow
    public int getCorpseAge() {
        throw new IllegalStateException("Mixin failed to shadow getCorpseAge()");
    }


}
