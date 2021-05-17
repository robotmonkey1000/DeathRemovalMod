package studio.robotmonkey1000.DeathRemoval.mixin;

import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.corelib.death.Death;
import de.maxhenkel.corpse.entities.CorpseBoundingBoxBase;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.robotmonkey1000.DeathRemoval.Deathremoval;
import studio.robotmonkey1000.DeathRemoval.net.DeathRemovalPacketHandler;
import studio.robotmonkey1000.DeathRemoval.net.RemoveWayPointPacket;

@Mixin(CorpseEntity.class)
public abstract class MixinCorpseEntity extends CorpseBoundingBoxBase {

    @Shadow public abstract String getCorpseName();
    @Shadow protected Death death;

    @Shadow private int emptyAge;
    @Shadow private int age;

    private boolean clearWaypoint = false;
    private MixinCorpseEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Inject(at = @At(value = "TAIL"), method="Lde/maxhenkel/corpse/entities/CorpseEntity;func_70071_h_()V", cancellable = true, remap=false)
//    @Inject(at = @At("HEAD"), method="Lde/maxhenkel/corpse/entities/CorpseEntity;func_70071_h_()V", cancellable = true, remap=false)
    public void corpseDespawningCheck(CallbackInfo info) {
        if ((Integer)Main.SERVER_CONFIG.corpseForceDespawnTime.get() > 0 && this.age > (Integer)Main.SERVER_CONFIG.corpseForceDespawnTime.get()) {
            ServerPlayerEntity player = (ServerPlayerEntity) this.world.getPlayerByUuid(this.death.getPlayerUUID());
            if(player != null) {
                sendDespawnPacket(player);
            } else {
                Deathremoval.LogMessage("Adding Point for when player changes world " + this.world.getDimensionKey().getLocation());
                Deathremoval.AddRemovalPoint(this.death.getPlayerUUID(),  this.world.getDimensionKey().getLocation(), new Vector3d(this.getPosX(), this.getPosY(), this.getPosZ()));
            }
        } else {
            boolean empty = this.isEmpty();
            if (empty && this.emptyAge < 0) {
                this.emptyAge = this.age;
            } else if (empty && this.age - this.emptyAge >= (Integer)Main.SERVER_CONFIG.corpseDespawnTime.get()) {
                ServerPlayerEntity player = (ServerPlayerEntity) this.world.getPlayerByUuid(this.death.getPlayerUUID());
                if(player != null) {
                    sendDespawnPacket(player);
                } else {
                    Deathremoval.LogMessage("Adding Point for when player changes world " + this.world.getDimensionKey().getLocation());
                    Deathremoval.AddRemovalPoint(this.death.getPlayerUUID(),  this.world.getDimensionKey().getLocation(), new Vector3d(this.getPosX(), this.getPosY(), this.getPosZ()));
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
    public boolean isEmpty() {
        throw new IllegalStateException("Mixin failed to shadow isEmpty()");
    }



}
