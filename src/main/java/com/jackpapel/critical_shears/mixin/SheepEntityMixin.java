package com.jackpapel.critical_shears.mixin;

import com.jackpapel.critical_shears.CriticalShears;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(SheepEntity.class)
public class SheepEntityMixin extends MobEntity {
    @Unique
    private boolean criticallySheared;

    protected SheepEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
        throw new AssertionError();
    }

    @Inject(
            method = "interactMob(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/passive/SheepEntity;sheared(Lnet/minecraft/sound/SoundCategory;)V"
            )
    )
    private void determineCriticality(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        // Long ass condition. See ClientPlayerEntity for origin
        if (player.getAttackCooldownProgress(0.5F) > 0.9F && player.fallDistance > 0.0F && !player.isOnGround() && !player.isClimbing() && !player.isTouchingWater() && !player.hasStatusEffect(StatusEffects.BLINDNESS) && !player.hasVehicle()) {
            this.criticallySheared = true;
        }
    }

    @Redirect(
            method = "sheared(Lnet/minecraft/sound/SoundCategory;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Random;nextInt(I)I"
            )
    )
    private int criticallyShear(Random instance, int i) {
        if (this.criticallySheared) {
            this.criticallySheared = false;
            this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, this.getSoundCategory(), 1.0F, 1.0F);
            if (!this.world.isClient()) {
                PacketByteBuf packet = PacketByteBufs.create();
                packet.writeInt(this.getId());
                for (ServerPlayerEntity player : PlayerLookup.tracking(this)) {
                    ServerPlayNetworking.send(player, CriticalShears.SHEEP_CRIT_PARTICLE_ID, packet);
                }
            }
            return 2; // + 1 = 3
        }
        return random.nextInt(3);
    }
}
