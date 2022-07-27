package com.jackpapel.critical_shears;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.util.logging.Logger;

public class CriticalShears implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "critical_shears";
    public static final Logger LOG = Logger.getLogger("CriticalShears");

    public static final Identifier SHEEP_CRIT_PARTICLE_ID = new Identifier(MOD_ID, "sheep_crit_particle");

    @Override
    public void onInitialize(ModContainer mod) {
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient(ModContainer mod) {
        ClientPlayNetworking.registerGlobalReceiver(SHEEP_CRIT_PARTICLE_ID, (client, handler, buf, responseSender) -> {
            Entity entity = client.world.getEntityById(buf.readInt());
            if (entity == null) {
                LOG.warning("No entity provided to crit packet");
                return;
            }
            client.execute(() -> client.particleManager.addEmitter(entity, ParticleTypes.CRIT));
        });
    }
}
