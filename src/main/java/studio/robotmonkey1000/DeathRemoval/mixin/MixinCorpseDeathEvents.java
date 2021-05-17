package studio.robotmonkey1000.DeathRemoval.mixin;

import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.corelib.death.PlayerDeathEvent;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.events.DeathEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.robotmonkey1000.DeathRemoval.Deathremoval;

import static de.maxhenkel.corpse.events.DeathEvents.deleteOldDeaths;

@Mixin(DeathEvents.class)
public class MixinCorpseDeathEvents {


    @Inject(at = @At(value = "HEAD"), method="Lde/maxhenkel/corpse/events/DeathEvents;playerDeath(Lde/maxhenkel/corpse/corelib/death/PlayerDeathEvent;)V", cancellable = true, remap=false)
    public void playerDeath(PlayerDeathEvent event, CallbackInfo ci) {
        if (Main.SERVER_CONFIG.maxDeathAge.get() != 0) {
            event.storeDeath();
        }

        event.removeDrops();
        CorpseEntity ent = CorpseEntity.createFromDeath(event.getPlayer(), event.getDeath());
        Deathremoval.AddDeath(event.getPlayer().getUniqueID(), ent);
        event.getPlayer().world.addEntity(ent);
        (new Thread(() -> {
            deleteOldDeaths(event.getPlayer().getServerWorld());
        })).start();
        ci.cancel();
    }
}
