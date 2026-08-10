package com.andrei1058.bedwars.support.version.CraftServer.despawnable;

import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.server.VersionSupport;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class DespawnableProvider<T> {

    abstract DespawnableType getType();

    abstract String getDisplayName(DespawnableAttributes attr, ITeam team);

    abstract T spawn(@NotNull DespawnableAttributes attr, @NotNull Location location, @NotNull ITeam team, VersionSupport api);

    protected boolean notSameTeam(@NotNull Entity entity, ITeam team, @NotNull VersionSupport api) {
        var despawnable = api.getDespawnablesList().getOrDefault(entity.getBukkitEntity().getUniqueId(), null);
        return null == despawnable || despawnable.getTeam() != team;
    }

    protected GoalSelector getTargetSelector(@NotNull PathfinderMob entityLiving) {
        return entityLiving.targetSelector;
    }

    protected GoalSelector getGoalSelector(@NotNull PathfinderMob entityLiving) {
        return entityLiving.goalSelector;
    }

    protected void clearSelectors(@NotNull PathfinderMob entityLiving) {
        entityLiving.goalSelector.removeAllGoals(goal -> true);
        entityLiving.targetSelector.removeAllGoals(goal -> true);
    }

    protected Goal getTargetGoal(Mob entity, ITeam team, VersionSupport api) {
        return new NearestAttackableTargetGoal<LivingEntity>(entity, LivingEntity.class, 20, true, false,
                (entityLiving, serverLevel) -> {
                    if (entityLiving instanceof Player) {
                        return !((Player) entityLiving).getBukkitEntity().isDead() &&
                                !team.wasMember(((Player) entityLiving).getBukkitEntity().getUniqueId()) &&
                                !team.getArena().isReSpawning(((Player) entityLiving).getBukkitEntity().getUniqueId())
                                && !team.getArena().isSpectator(((Player) entityLiving).getBukkitEntity().getUniqueId());
                    }
                    return notSameTeam(entityLiving, team, api);
                });
    }

    protected void applyDefaultSettings(org.bukkit.entity.@NotNull LivingEntity bukkitEntity, DespawnableAttributes attr,
                                        ITeam team) {
        bukkitEntity.setRemoveWhenFarAway(false);
        bukkitEntity.setPersistent(true);
        bukkitEntity.setCustomNameVisible(true);
        bukkitEntity.setCustomName(getDisplayName(attr, team));

        var entity = (Mob) ((CraftEntity) bukkitEntity).getHandle();
        Objects.requireNonNull(entity.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(attr.health());
        Objects.requireNonNull(entity.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(attr.speed());
        Objects.requireNonNull(entity.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(attr.damage());
    }
}
