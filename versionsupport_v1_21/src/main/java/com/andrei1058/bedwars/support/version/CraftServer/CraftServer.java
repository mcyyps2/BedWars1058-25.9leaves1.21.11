package com.andrei1058.bedwars.support.version.CraftServer;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.shop.ShopHolo;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.arena.team.TeamColor;
import com.andrei1058.bedwars.api.entity.Despawnable;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.api.server.VersionSupport;
import com.andrei1058.bedwars.support.version.CraftServer.despawnable.DespawnableAttributes;
import com.andrei1058.bedwars.support.version.CraftServer.despawnable.DespawnableFactory;
import com.andrei1058.bedwars.support.version.CraftServer.despawnable.DespawnableType;
import com.andrei1058.bedwars.support.version.common.VersionCommon;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Bed;
import org.bukkit.block.data.type.Ladder;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftFireball;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.entity.CraftTNTPrimed;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * BedWars1058 version support for leaves-1.21.11.
 * <p>
 * CraftBukkit package has no version suffix since 1.20.5 (org.bukkit.craftbukkit.CraftServer),
 * and NMS uses mojang mappings (ServerPlayer, Component, Clientbound*Packet...).
 * <p>
 * BedWars loads this class by resolving the class name: {@code org.bukkit.craftbukkit.CraftServer}
 * split by "." index 3 = "CraftServer", so it loads
 * {@code com.andrei1058.bedwars.support.version.CraftServer.CraftServer}.
 */
public class CraftServer extends VersionSupport {

    private final DespawnableFactory despawnableFactory = new DespawnableFactory(this);

    public CraftServer(org.bukkit.plugin.Plugin plugin, String versionName) {
        super(plugin, versionName);
    }

    @Override
    public void registerCommand(String name, Command clasa) {
        ((org.bukkit.craftbukkit.CraftServer) getPlugin().getServer()).getCommandMap().register(name, clasa);
    }

    @Override
    public void sendTitle(@NotNull Player p, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        p.sendTitle(title == null ? " " : title, subtitle == null ? " " : subtitle, fadeIn, stay, fadeOut);
    }

    @Override
    public void spawnSilverfish(Location loc, ITeam bedWarsTeam, double speed, double health, int despawn, double damage) {
        var attr = new DespawnableAttributes(DespawnableType.SILVERFISH, speed, health, damage, despawn);
        var entity = despawnableFactory.spawn(attr, loc, bedWarsTeam);

        new Despawnable(
                entity,
                bedWarsTeam, despawn,
                Messages.SHOP_UTILITY_NPC_SILVERFISH_NAME,
                PlayerKillEvent.PlayerKillCause.SILVERFISH_FINAL_KILL,
                PlayerKillEvent.PlayerKillCause.SILVERFISH
        );
    }

    @Override
    public void spawnIronGolem(Location loc, ITeam bedWarsTeam, double speed, double health, int despawn) {
        var attr = new DespawnableAttributes(DespawnableType.IRON_GOLEM, speed, health, 4, despawn);
        var entity = despawnableFactory.spawn(attr, loc, bedWarsTeam);
        new Despawnable(
                entity,
                bedWarsTeam, despawn,
                Messages.SHOP_UTILITY_NPC_IRON_GOLEM_NAME,
                PlayerKillEvent.PlayerKillCause.IRON_GOLEM_FINAL_KILL,
                PlayerKillEvent.PlayerKillCause.IRON_GOLEM
        );
    }

    @Override
    public void playAction(@NotNull Player p, String text) {
        p.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent(org.bukkit.ChatColor.translateAlternateColorCodes('&', text)
                )
        );
    }

    @Override
    public boolean isBukkitCommandRegistered(String name) {
        return ((org.bukkit.craftbukkit.CraftServer) getPlugin().getServer()).getCommandMap().getCommand(name) != null;
    }

    @Override
    public org.bukkit.inventory.ItemStack getItemInHand(@NotNull Player p) {
        return p.getInventory().getItemInMainHand();
    }

    @Override
    public void hideEntity(@NotNull org.bukkit.entity.Entity e, Player p) {
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(e.getEntityId());
        this.sendPacket(p, packet);
    }

    @Override
    public void minusAmount(Player p, org.bukkit.inventory.@NotNull ItemStack i, int amount) {
        if (i.getAmount() - amount <= 0) {
            if (p.getInventory().getItemInOffHand().equals(i)) {
                p.getInventory().setItemInOffHand(null);
            } else {
                p.getInventory().removeItem(i);
            }
            return;
        }
        i.setAmount(i.getAmount() - amount);
        p.updateInventory();
    }

    @Override
    public void setSource(TNTPrimed tnt, Player owner) {
        net.minecraft.world.entity.LivingEntity nmsEntityLiving = (((CraftLivingEntity) owner).getHandle());
        PrimedTnt nmsTNT = (((CraftTNTPrimed) tnt).getHandle());
        try {
            nmsTNT.owner = net.minecraft.world.entity.EntityReference.of(nmsEntityLiving);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isArmor(org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack == null) return false;
        String type = itemStack.getType().name();
        return type.endsWith("_HELMET") || type.endsWith("_CHESTPLATE")
                || type.endsWith("_LEGGINGS") || type.endsWith("_BOOTS")
                || type.equals("ELYTRA");
    }

    @Override
    public boolean isTool(org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack == null) return false;
        String type = itemStack.getType().name();
        return type.endsWith("_PICKAXE") || type.endsWith("_SHOVEL")
                || type.endsWith("_AXE") || type.endsWith("_HOE");
    }

    @Override
    public boolean isSword(org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack == null) return false;
        String type = itemStack.getType().name();
        return type.endsWith("_SWORD");
    }

    @Override
    public boolean isAxe(org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack == null) return false;
        return itemStack.getType().name().endsWith("_AXE");
    }

    @Override
    public boolean isBow(org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack == null) return false;
        String type = itemStack.getType().name();
        return type.equals("BOW") || type.equals("CROSSBOW");
    }

    @Override
    public boolean isProjectile(org.bukkit.inventory.ItemStack itemStack) {
        var item = getItem(itemStack);
        if (null == item) return false;
        return item instanceof net.minecraft.world.item.ProjectileItem;
    }

    @Override
    public boolean isInvisibilityPotion(org.bukkit.inventory.@NotNull ItemStack itemStack) {
        if (!itemStack.getType().equals(org.bukkit.Material.POTION)) return false;

        org.bukkit.inventory.meta.PotionMeta pm = (org.bukkit.inventory.meta.PotionMeta) itemStack.getItemMeta();

        return pm != null && pm.hasCustomEffects() && pm.hasCustomEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY);
    }

    @Override
    public void registerEntities() {
    }

    @Override
    public void spawnShop(@NotNull Location loc, String name1, List<Player> players, IArena arena) {
        Location l = loc.clone();

        if (l.getWorld() == null) return;
        Villager vlg = (Villager) l.getWorld().spawnEntity(loc, EntityType.VILLAGER);
        vlg.setAI(false);
        vlg.setRemoveWhenFarAway(false);
        vlg.setCollidable(false);
        vlg.setInvulnerable(true);
        vlg.setSilent(true);

        for (Player p : players) {
            String[] name = Language.getMsg(p, name1).split(",");
            if (name.length == 1) {
                ArmorStand a = createArmorStand(name[0], l.clone().add(0, 1.85, 0));
                new ShopHolo(Language.getPlayerLanguage(p).getIso(), a, null, l, arena);
            } else {
                ArmorStand a = createArmorStand(name[0], l.clone().add(0, 2.1, 0));
                ArmorStand b = createArmorStand(name[1], l.clone().add(0, 1.85, 0));
                new ShopHolo(Language.getPlayerLanguage(p).getIso(), a, b, l, arena);
            }
        }
        for (ShopHolo sh : ShopHolo.getShopHolo()) {
            if (sh.getA() == arena) {
                sh.update();
            }
        }
    }

    @Override
    public double getDamage(org.bukkit.inventory.ItemStack i) {
        // 1.21 物品属性改用 DataComponents，这里直接用 Bukkit 属性 API 读取攻击伤害
        double damage = 1.0D;
        if (i != null && i.getType() != org.bukkit.Material.AIR) {
            for (var modifier : i.getType().getDefaultAttributeModifiers(org.bukkit.inventory.EquipmentSlot.HAND)
                    .get(org.bukkit.attribute.Attribute.ATTACK_DAMAGE)) {
                if (modifier.getOperation() == org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER) {
                    damage += modifier.getAmount();
                }
            }
        }
        return damage;
    }

    private static ArmorStand createArmorStand(String name, Location loc) {
        if (loc == null) return null;
        if (loc.getWorld() == null) return null;
        ArmorStand a = loc.getWorld().spawn(loc, ArmorStand.class);
        a.setGravity(false);
        a.setVisible(false);
        a.setCustomNameVisible(true);
        a.setCustomName(name);
        return a;
    }

    @Override
    public void voidKill(Player p) {
        ServerPlayer player = getPlayer(p);
        player.hurtServer((net.minecraft.server.level.ServerLevel) player.level(),
                player.damageSources().genericKill(), 1000);
    }

    @Override
    public void hideArmor(@NotNull Player victim, Player receiver) {
        List<Pair<EquipmentSlot, ItemStack>> items = new ArrayList<>();
        items.add(new Pair<>(EquipmentSlot.HEAD, ItemStack.EMPTY));
        items.add(new Pair<>(EquipmentSlot.CHEST, ItemStack.EMPTY));
        items.add(new Pair<>(EquipmentSlot.LEGS, ItemStack.EMPTY));
        items.add(new Pair<>(EquipmentSlot.FEET, ItemStack.EMPTY));
        ClientboundSetEquipmentPacket packet1 = new ClientboundSetEquipmentPacket(victim.getEntityId(), items);
        sendPacket(receiver, packet1);
    }

    @Override
    public void showArmor(@NotNull Player victim, Player receiver) {
        List<Pair<EquipmentSlot, ItemStack>> items = new ArrayList<>();
        items.add(new Pair<>(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(victim.getInventory().getHelmet())));
        items.add(new Pair<>(EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(victim.getInventory().getChestplate())));
        items.add(new Pair<>(EquipmentSlot.LEGS, CraftItemStack.asNMSCopy(victim.getInventory().getLeggings())));
        items.add(new Pair<>(EquipmentSlot.FEET, CraftItemStack.asNMSCopy(victim.getInventory().getBoots())));
        ClientboundSetEquipmentPacket packet1 = new ClientboundSetEquipmentPacket(victim.getEntityId(), items);
        sendPacket(receiver, packet1);
    }

    @Override
    public void spawnDragon(Location l, ITeam bwt) {
        if (l == null || l.getWorld() == null) {
            getPlugin().getLogger().log(Level.WARNING, "Could not spawn Dragon. Location is null");
            return;
        }
        EnderDragon ed = (EnderDragon) l.getWorld().spawnEntity(l, EntityType.ENDER_DRAGON);
        ed.setPhase(EnderDragon.Phase.CIRCLING);
    }

    @Override
    public void colorBed(ITeam bwt) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockState bed = bwt.getBed().clone().add(x, 0, z).getBlock().getState();
                if (bed instanceof Bed) {
                    bed.setType(bwt.getColor().bedMaterial());
                    bed.update();
                }
            }
        }
    }

    @Override
    public void registerTntWhitelist(float endStoneBlast, float glassBlast) {
        try {
            // 1.21 中爆炸抗性存放在 BlockBehaviour.Properties（BlockStateBase 内的 properties 字段）
            Field statePropField = BlockBehaviour.BlockStateBase.class.getDeclaredField("properties");
            statePropField.setAccessible(true);
            Field blastField = BlockBehaviour.Properties.class.getDeclaredField("explosionResistance");
            blastField.setAccessible(true);

            setBlastResistance(Blocks.END_STONE.defaultBlockState(), endStoneBlast, statePropField, blastField);
            setBlastResistance(Blocks.GLASS.defaultBlockState(), glassBlast, statePropField, blastField);

            var coloredGlass = new net.minecraft.world.level.block.Block[]{
                    Blocks.WHITE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS,
                    Blocks.YELLOW_STAINED_GLASS, Blocks.LIME_STAINED_GLASS, Blocks.PINK_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS,
                    Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS,
                    Blocks.BROWN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS, Blocks.RED_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS,
                    Blocks.TINTED_GLASS,
            };

            for (net.minecraft.world.level.block.Block glass : coloredGlass) {
                setBlastResistance(glass.defaultBlockState(), glassBlast, statePropField, blastField);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void setBlastResistance(net.minecraft.world.level.block.state.BlockState state, float value,
                                           Field statePropField, Field blastField) throws IllegalAccessException {
        Object props = statePropField.get(state);
        if (props != null) {
            blastField.setFloat(props, value);
        }
    }

    @Override
    public void setBlockTeamColor(@NotNull Block block, TeamColor teamColor) {
        if (block.getType().toString().contains("STAINED_GLASS") || block.getType().toString().equals("GLASS")) {
            block.setType(teamColor.glassMaterial());
        } else if (block.getType().toString().contains("_TERRACOTTA")) {
            block.setType(teamColor.glazedTerracottaMaterial());
        } else if (block.getType().toString().contains("_WOOL")) {
            block.setType(teamColor.woolMaterial());
        }
    }

    @Override
    public void setCollide(@NotNull Player p, IArena a, boolean value) {
        p.setCollidable(value);
        if (a == null) return;
        a.updateSpectatorCollideRule(p, value);
    }

    @Override
    public org.bukkit.inventory.ItemStack addCustomData(org.bukkit.inventory.ItemStack i, String data) {
        return pdcSet(i, VersionSupport.PLUGIN_TAG_GENERIC_KEY, data);
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack itemStack, String key, String value) {
        return pdcSet(itemStack, key, value);
    }

    @Override
    public String getTag(org.bukkit.inventory.ItemStack itemStack, String key) {
        return pdcGet(itemStack, key);
    }

    @Override
    public boolean isCustomBedWarsItem(org.bukkit.inventory.ItemStack i) {
        return pdcHas(i, VersionSupport.PLUGIN_TAG_GENERIC_KEY);
    }

    @Override
    public String getCustomData(org.bukkit.inventory.ItemStack i) {
        return pdcGet(i, VersionSupport.PLUGIN_TAG_GENERIC_KEY);
    }

    @Override
    public org.bukkit.inventory.ItemStack colourItem(org.bukkit.inventory.ItemStack itemStack, ITeam bedWarsTeam) {
        if (itemStack == null) return null;
        String type = itemStack.getType().toString();
        if (isBed(itemStack.getType())) {
            return new org.bukkit.inventory.ItemStack(bedWarsTeam.getColor().bedMaterial(), itemStack.getAmount());
        } else if (type.contains("_STAINED_GLASS_PANE")) {
            return new org.bukkit.inventory.ItemStack(bedWarsTeam.getColor().glassPaneMaterial(), itemStack.getAmount());
        } else if (type.contains("STAINED_GLASS") || type.equals("GLASS")) {
            return new org.bukkit.inventory.ItemStack(bedWarsTeam.getColor().glassMaterial(), itemStack.getAmount());
        } else if (type.contains("_TERRACOTTA")) {
            return new org.bukkit.inventory.ItemStack(bedWarsTeam.getColor().glazedTerracottaMaterial(), itemStack.getAmount());
        } else if (type.contains("_WOOL")) {
            return new org.bukkit.inventory.ItemStack(bedWarsTeam.getColor().woolMaterial(), itemStack.getAmount());
        }
        return itemStack;
    }

    @Override
    public org.bukkit.inventory.ItemStack createItemStack(String material, int amount, short data) {
        org.bukkit.inventory.ItemStack i;
        try {
            i = new org.bukkit.inventory.ItemStack(org.bukkit.Material.valueOf(material), amount);
        } catch (Exception ex) {
            getPlugin().getLogger().log(Level.WARNING, material + " is not a valid " + getName() + " material!");
            i = new org.bukkit.inventory.ItemStack(org.bukkit.Material.BEDROCK);
        }
        return i;
    }

    @Override
    public org.bukkit.Material materialFireball() {
        return org.bukkit.Material.FIRE_CHARGE;
    }

    @Override
    public org.bukkit.Material materialPlayerHead() {
        return org.bukkit.Material.PLAYER_HEAD;
    }

    @Override
    public org.bukkit.Material materialSnowball() {
        return org.bukkit.Material.SNOWBALL;
    }

    @Override
    public org.bukkit.Material materialGoldenHelmet() {
        return org.bukkit.Material.GOLDEN_HELMET;
    }

    @Override
    public org.bukkit.Material materialGoldenChestPlate() {
        return org.bukkit.Material.GOLDEN_CHESTPLATE;
    }

    @Override
    public org.bukkit.Material materialGoldenLeggings() {
        return org.bukkit.Material.GOLDEN_LEGGINGS;
    }

    @Override
    public org.bukkit.Material materialNetheriteHelmet() {
        return Material.NETHERITE_HELMET;
    }

    @Override
    public org.bukkit.Material materialNetheriteChestPlate() {
        return Material.NETHERITE_CHESTPLATE;
    }

    @Override
    public org.bukkit.Material materialNetheriteLeggings() {
        return Material.NETHERITE_LEGGINGS;
    }

    @Override
    public org.bukkit.Material materialElytra() {
        return Material.ELYTRA;
    }

    @Override
    public org.bukkit.Material materialCake() {
        return org.bukkit.Material.CAKE;
    }

    @Override
    public org.bukkit.Material materialCraftingTable() {
        return org.bukkit.Material.CRAFTING_TABLE;
    }

    @Override
    public org.bukkit.Material materialEnchantingTable() {
        return org.bukkit.Material.ENCHANTING_TABLE;
    }

    @Override
    public org.bukkit.Material woolMaterial() {
        return org.bukkit.Material.WHITE_WOOL;
    }

    @Override
    public String getShopUpgradeIdentifier(org.bukkit.inventory.ItemStack itemStack) {
        String value = pdcGet(itemStack, VersionSupport.PLUGIN_TAG_TIER_KEY);
        return value == null ? "null" : value;
    }

    @Override
    public org.bukkit.inventory.ItemStack setShopUpgradeIdentifier(org.bukkit.inventory.ItemStack itemStack, String identifier) {
        return pdcSet(itemStack, VersionSupport.PLUGIN_TAG_TIER_KEY, identifier);
    }

    @Override
    public org.bukkit.inventory.ItemStack getPlayerHead(Player player, org.bukkit.inventory.ItemStack copyTagFrom) {
        org.bukkit.inventory.ItemStack head = new org.bukkit.inventory.ItemStack(materialPlayerHead());

        if (copyTagFrom != null && copyTagFrom.hasItemMeta()) {
            var meta = copyTagFrom.getItemMeta();
            if (meta != null) {
                var headMeta = head.getItemMeta();
                if (headMeta != null) {
                    for (var key : meta.getPersistentDataContainer().getKeys()) {
                        var tag = meta.getPersistentDataContainer().get(key, org.bukkit.persistence.PersistentDataType.STRING);
                        if (tag != null) {
                            headMeta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.STRING, tag);
                        }
                    }
                    head.setItemMeta(headMeta);
                }
            }
        }

        var meta = head.getItemMeta();
        if (meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwnerProfile(player.getPlayerProfile());
        }
        head.setItemMeta(meta);
        return head;
    }

    @Override
    public void sendPlayerSpawnPackets(Player respawned, IArena arena) {
        if (respawned == null) return;
        if (arena == null) return;
        if (!arena.isPlayer(respawned)) return;

        // if method was used when the player was still in re-spawning screen
        if (arena.getRespawnSessions().containsKey(respawned)) return;

        // 1.20.5+ 服务端会在玩家重生时自动向所有玩家广播实体生成包，
        // 这里只需处理隐形玩家（在重生瞬间显示盔甲会泄露位置）的装备隐藏逻辑。
        for (Player p : arena.getPlayers()) {
            if (p == null) continue;
            if (p.equals(respawned)) continue;
            if (arena.getRespawnSessions().containsKey(p)) continue;

            if (p.getWorld().equals(respawned.getWorld())) {
                if (respawned.getLocation().distance(p.getLocation()) <= arena.getRenderDistance()) {
                    // 若该玩家有隐身效果，确保其盔甲不被其他玩家看到
                    if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        hideArmor(p, respawned);
                    } else {
                        showArmor(p, respawned);
                    }
                }
            }
        }

        for (Player spectator : arena.getSpectators()) {
            if (spectator == null) continue;
            if (spectator.equals(respawned)) continue;
            respawned.hidePlayer(getPlugin(), spectator);
        }
    }

    @Override
    public String getInventoryName(@NotNull InventoryEvent e) {
        return e.getView().getTitle();
    }

    @Override
    public void setUnbreakable(@NotNull ItemMeta itemMeta) {
        itemMeta.setUnbreakable(true);
    }

    @Override
    public String getMainLevel() {
        return MinecraftServer.getServer().getWorldData().getLevelName();
    }

    @Override
    public int getVersion() {
        // impacts on sidebar
        // experimental score placeholders
        return 10;
    }

    @Override
    public void setJoinSignBackground(@NotNull BlockState b, org.bukkit.Material material) {
        if (b.getBlockData() instanceof WallSign) {
            b.getBlock().getRelative(((WallSign) b.getBlockData()).getFacing().getOppositeFace()).setType(material);
        }
    }

    @Override
    public void spigotShowPlayer(Player victim, @NotNull Player receiver) {
        receiver.showPlayer(getPlugin(), victim);
    }

    @Override
    public void spigotHidePlayer(Player victim, @NotNull Player receiver) {
        receiver.hidePlayer(getPlugin(), victim);
    }

    @Override
    public org.bukkit.entity.Fireball setFireballDirection(org.bukkit.entity.Fireball fireball, @NotNull Vector vector) {
        net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile fb = ((CraftFireball) fireball).getHandle();
        fb.setDeltaMovement(new Vec3(vector.getX() * 0.1D, vector.getY() * 0.1D, vector.getZ() * 0.1D));
        return fireball;
    }

    @Override
    public void playRedStoneDot(@NotNull Player player) {
        Color color = Color.RED;
        ClientboundLevelParticlesPacket particlePacket = new ClientboundLevelParticlesPacket(
                new DustParticleOptions(color.asRGB(), 1.0F),
                true, true,
                player.getLocation().getX(),
                player.getLocation().getY() + 2.6,
                player.getLocation().getZ(),
                0.0F, 0.0F, 0.0F, 0.0F, 0
        );
        for (Player inWorld : player.getWorld().getPlayers()) {
            if (inWorld.equals(player)) continue;
            this.sendPacket(inWorld, particlePacket);
        }
    }

    @Override
    public void clearArrowsFromPlayerBody(Player player) {
        // minecraft clears them on death on newer version
    }

    /**
     * Gets the NMS Item from ItemStack
     */
    private @Nullable Item getItem(org.bukkit.inventory.ItemStack itemStack) {
        var i = CraftItemStack.asNMSCopy(itemStack);
        if (null == i) {
            return null;
        }
        return i.getItem();
    }



    // ------------------------------------------------------------------
    // PersistentDataContainer 辅助（替代已废除的 NBT ItemStack tag API）
    // ------------------------------------------------------------------

    /** 写入字符串到物品 PDC，返回更新后的物品。 */
    private org.bukkit.inventory.ItemStack pdcSet(org.bukkit.inventory.ItemStack itemStack, String key, String value) {
        if (itemStack == null) {
            return null;
        }
        org.bukkit.inventory.ItemStack copy = itemStack.clone();
        var meta = copy.getItemMeta();
        if (meta == null) {
            return itemStack;
        }
        meta.getPersistentDataContainer().set(key(key), org.bukkit.persistence.PersistentDataType.STRING, value);
        copy.setItemMeta(meta);
        return copy;
    }

    /** 读取物品 PDC 中的字符串，不存在返回 null。 */
    private String pdcGet(org.bukkit.inventory.ItemStack itemStack, String key) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return null;
        }
        var meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(key(key), org.bukkit.persistence.PersistentDataType.STRING);
    }

    /** 判断物品 PDC 中是否存在该键。 */
    private boolean pdcHas(org.bukkit.inventory.ItemStack itemStack, String key) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return false;
        }
        var meta = itemStack.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(key(key), org.bukkit.persistence.PersistentDataType.STRING);
    }

    private org.bukkit.NamespacedKey key(String key) {
        return new org.bukkit.NamespacedKey(getPlugin(), sanitize(key));
    }

    private String sanitize(String key) {
        String out = key.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");
        return out.isEmpty() ? "data" : out;
    }

    public ServerPlayer getPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    public List<Pair<EquipmentSlot, ItemStack>> getPlayerEquipment(@NotNull Player player) {
        return getPlayerEquipment(getPlayer(player));
    }

    public List<Pair<EquipmentSlot, ItemStack>> getPlayerEquipment(@NotNull ServerPlayer entityPlayer) {
        List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>();
        list.add(new Pair<>(EquipmentSlot.MAINHAND, entityPlayer.getItemBySlot(EquipmentSlot.MAINHAND)));
        list.add(new Pair<>(EquipmentSlot.OFFHAND, entityPlayer.getItemBySlot(EquipmentSlot.OFFHAND)));
        list.add(new Pair<>(EquipmentSlot.HEAD, entityPlayer.getItemBySlot(EquipmentSlot.HEAD)));
        list.add(new Pair<>(EquipmentSlot.CHEST, entityPlayer.getItemBySlot(EquipmentSlot.CHEST)));
        list.add(new Pair<>(EquipmentSlot.LEGS, entityPlayer.getItemBySlot(EquipmentSlot.LEGS)));
        list.add(new Pair<>(EquipmentSlot.FEET, entityPlayer.getItemBySlot(EquipmentSlot.FEET)));

        return list;
    }

    @Override
    public void placeTowerBlocks(@NotNull Block b, @NotNull IArena a, @NotNull TeamColor color, int x, int y, int z) {
        b.getRelative(x, y, z).setType(color.woolMaterial());
        a.addPlacedBlock(b.getRelative(x, y, z));
    }

    @Override
    public void placeLadder(@NotNull Block b, int x, int y, int z, @NotNull IArena a, int ladderData) {
        Block block = b.getRelative(x, y, z);  //ladder block
        block.setType(Material.LADDER);
        Ladder ladder = (Ladder) block.getBlockData();
        a.addPlacedBlock(block);
        switch (ladderData) {
            case 2 -> {
                ladder.setFacing(org.bukkit.block.BlockFace.NORTH);
                block.setBlockData(ladder);
            }
            case 3 -> {
                ladder.setFacing(org.bukkit.block.BlockFace.SOUTH);
                block.setBlockData(ladder);
            }
            case 4 -> {
                ladder.setFacing(org.bukkit.block.BlockFace.WEST);
                block.setBlockData(ladder);
            }
            case 5 -> {
                ladder.setFacing(org.bukkit.block.BlockFace.EAST);
                block.setBlockData(ladder);
            }
        }
    }

    @Override
    public void playVillagerEffect(@NotNull Player player, Location location) {
        player.spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, location, 1);
    }

    private void sendPacket(Player player, net.minecraft.network.protocol.Packet<?> packet) {
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    private void sendPackets(Player player, net.minecraft.network.protocol.Packet<?> @NotNull ... packets) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        for (net.minecraft.network.protocol.Packet<?> p : packets) {
            connection.send(p);
        }
    }

    @Override
    public void registerVersionListeners() {
        new VersionCommon(this);
    }
}
