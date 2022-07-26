package com.jackpapel.critical_shears;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.Optional;

public class CriticalShears implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "critical_shears";
    public static final Logger LOG = LogUtils.getLogger();

    public static final Identifier SHEEP_CRIT_PARTICLE_ID = new Identifier(MOD_ID, "sheep_crit_particle");

    @Override
    public void onInitialize() {
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SHEEP_CRIT_PARTICLE_ID, (client, handler, buf, responseSender) -> {
            Entity entity = client.world.getEntityById(buf.readInt());
            if (entity == null) {
                LOG.error("No entity provided to crit packet");
                return;
            }
            client.execute(() -> client.particleManager.addEmitter(entity, ParticleTypes.CRIT));
        });
    }
}
