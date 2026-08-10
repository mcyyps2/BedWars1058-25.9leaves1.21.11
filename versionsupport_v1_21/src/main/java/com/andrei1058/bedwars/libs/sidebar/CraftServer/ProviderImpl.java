package com.andrei1058.bedwars.libs.sidebar.CraftServer;

import com.andrei1058.bedwars.libs.sidebar.PlaceholderProvider;
import com.andrei1058.bedwars.libs.sidebar.PlayerTab;
import com.andrei1058.bedwars.libs.sidebar.ScoreLine;
import com.andrei1058.bedwars.libs.sidebar.Sidebar;
import com.andrei1058.bedwars.libs.sidebar.SidebarLine;
import com.andrei1058.bedwars.libs.sidebar.SidebarObjective;
import com.andrei1058.bedwars.libs.sidebar.SidebarProvider;
import com.andrei1058.bedwars.libs.sidebar.VersionedTabGroup;
import com.andrei1058.bedwars.libs.sidebar.WrappedSidebar;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

/**
 * Sidebar provider for leaves-1.21.11 (mojang mappings).
 * <p>
 * This class is loaded reflectively by the shaded SidebarManager which resolves
 * "CraftServer" as the version segment and loads
 * {@code com.andrei1058.bedwars.libs.sidebar.CraftServer.ProviderImpl}.
 */
@SuppressWarnings("unused")
public class ProviderImpl extends SidebarProvider {

    private static SidebarProvider instance;

    @Override
    public Sidebar createSidebar(SidebarLine title, Collection<SidebarLine> lines, Collection<PlaceholderProvider> placeholderProviders) {
        return new SidebarImpl(title, lines, placeholderProviders);
    }

    @Override
    public SidebarObjective createObjective(@NotNull WrappedSidebar sidebar, String name, boolean health, SidebarLine title, int type) {
        return ((SidebarImpl) sidebar).createObjective(name, health ? ObjectiveCriteria.HEALTH : ObjectiveCriteria.DUMMY, title, type);
    }

    @Override
    public ScoreLine createScoreLine(WrappedSidebar sidebar, SidebarLine line, int score, String color) {
        return ((SidebarImpl) sidebar).createScore(line, score, color);
    }

    public void sendScore(@NotNull WrappedSidebar sidebar, String playerName, int score) {
        if (sidebar.getHealthObjective() == null) return;
        ClientboundSetScorePacket packetPlayOutScoreboardScore = new ClientboundSetScorePacket(
                playerName,
                sidebar.getHealthObjective().getName(),
                score,
                Optional.empty(),
                Optional.empty()
        );
        for (Player player : sidebar.getReceivers()) {
            sendPacket(player, packetPlayOutScoreboardScore);
        }
    }

    @Override
    public VersionedTabGroup createPlayerTab(WrappedSidebar sidebar, String identifier, SidebarLine prefix, SidebarLine suffix,
                                             PlayerTab.PushingRule pushingRule, PlayerTab.NameTagVisibility tagVisibility,
                                             @Nullable Collection<PlaceholderProvider> placeholders) {
        return new PlayerListImpl(sidebar, identifier, prefix, suffix, pushingRule, tagVisibility, placeholders);
    }

    @Override
    public void sendHeaderFooter(Player player, String header, String footer) {
        ClientboundTabListPacket packet = new ClientboundTabListPacket(Component.literal(header), Component.literal(footer));
        sendPacket(player, packet);
    }

    public static void sendPacket(@NotNull Player player, net.minecraft.network.protocol.Packet<?> packet) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        serverPlayer.connection.send(packet);
    }

    public static SidebarProvider getInstance() {
        return null == instance ? instance = new ProviderImpl() : instance;
    }
}
