package com.andrei1058.bedwars.libs.sidebar.CraftServer;

import com.andrei1058.bedwars.libs.sidebar.PlaceholderProvider;
import com.andrei1058.bedwars.libs.sidebar.PlayerTab;
import com.andrei1058.bedwars.libs.sidebar.SidebarLine;
import com.andrei1058.bedwars.libs.sidebar.VersionedTabGroup;
import com.andrei1058.bedwars.libs.sidebar.WrappedSidebar;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class PlayerListImpl extends PlayerTeam implements VersionedTabGroup {

    private Team.CollisionRule pushingRule;
    private final SidebarLine prefix;
    private MutableComponent prefixComp = Component.literal(" ");
    private final SidebarLine suffix;
    private MutableComponent suffixComp = Component.literal(" ");
    private final WrappedSidebar sidebar;
    private final String id;
    private Team.Visibility nameTagVisibility = Team.Visibility.ALWAYS;
    private Player papiSubject = null;
    private final Collection<PlaceholderProvider> placeholders;

    public PlayerListImpl(
            @NotNull WrappedSidebar sidebar,
            String identifier,
            SidebarLine prefix,
            SidebarLine suffix,
            PlayerTab.PushingRule pushingRule,
            PlayerTab.NameTagVisibility nameTagVisibility,
            @Nullable Collection<PlaceholderProvider> placeholders
    ) {
        super(null, identifier);
        this.suffix = suffix;
        this.prefix = prefix;
        this.sidebar = sidebar;
        this.setPushingRule(pushingRule);
        this.setNameTagVisibility(nameTagVisibility);
        this.id = identifier;
        this.placeholders = placeholders;
    }

    @Override
    public void setPlayerPrefix(@Nullable Component var0) {
    }

    @Override
    public Team.CollisionRule getCollisionRule() {
        return pushingRule;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal(id);
    }

    @Override
    public MutableComponent getFormattedName(Component var0) {
        return Component.literal(prefixComp.getString() + var0.getString() + suffixComp.getString());
    }

    public String getName() {
        return getIdentifier();
    }

    @Override
    public Component getPlayerPrefix() {
        return prefixComp;
    }

    @Override
    public void setPlayerSuffix(@Nullable Component var0) {
    }

    @Override
    public Component getPlayerSuffix() {
        return suffixComp;
    }

    @Override
    public void setAllowFriendlyFire(boolean b) {
    }

    @Override
    public void setSeeFriendlyInvisibles(boolean b) {
    }

    @Override
    public void setNameTagVisibility(Team.Visibility enumNameTagVisibility) {
        nameTagVisibility = enumNameTagVisibility;
    }

    @Override
    public Team.Visibility getNameTagVisibility() {
        return nameTagVisibility;
    }

    @Override
    public void add(@NotNull Player player) {
        ClientboundSetPlayerTeamPacket packetPlayOutScoreboardTeam = ClientboundSetPlayerTeamPacket.createPlayerPacket(
                this, player.getName(), ClientboundSetPlayerTeamPacket.Action.ADD
        );
        sidebar.getReceivers().forEach(r -> ProviderImpl.sendPacket(r, packetPlayOutScoreboardTeam));
    }

    @Override
    public void sendCreateToPlayer(Player player) {
        ClientboundSetPlayerTeamPacket packetPlayOutScoreboardTeam = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(this, true);
        ProviderImpl.sendPacket(player, packetPlayOutScoreboardTeam);
    }

    @Override
    public void remove(@NotNull Player player) {
        ClientboundSetPlayerTeamPacket packetPlayOutScoreboardTeam = ClientboundSetPlayerTeamPacket.createPlayerPacket(
                this, player.getName(), ClientboundSetPlayerTeamPacket.Action.REMOVE
        );
        sidebar.getReceivers().forEach(r -> ProviderImpl.sendPacket(r, packetPlayOutScoreboardTeam));
    }

    @Override
    public void sendUserCreateToReceivers(@NotNull Player player) {
        ClientboundSetPlayerTeamPacket packetPlayOutScoreboardTeam = ClientboundSetPlayerTeamPacket.createPlayerPacket(
                this, player.getName(), ClientboundSetPlayerTeamPacket.Action.ADD
        );
        sidebar.getReceivers().forEach(r -> ProviderImpl.sendPacket(r, packetPlayOutScoreboardTeam));
    }

    @Override
    public void sendUpdateToReceivers() {
        ClientboundSetPlayerTeamPacket packetPlayOutScoreboardTeam = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(this, false);
        sidebar.getReceivers().forEach(r -> ProviderImpl.sendPacket(r, packetPlayOutScoreboardTeam));
    }

    @Override
    public void sendRemoveToReceivers() {
        ClientboundSetPlayerTeamPacket packetPlayOutScoreboardTeam = ClientboundSetPlayerTeamPacket.createRemovePacket(this);
        sidebar.getReceivers().forEach(r -> ProviderImpl.sendPacket(r, packetPlayOutScoreboardTeam));
    }

    @Override
    public boolean refreshContent() {
        var newPrefix = prefix.getTrimReplacePlaceholders(getSubject(), 256, this.placeholders);
        var newSuffix = suffix.getTrimReplacePlaceholders(getSubject(), 256, this.placeholders);

        if (newPrefix.equals(prefixComp.getString()) && newSuffix.equals(suffixComp.getString())) {
            return false;
        }

        this.prefixComp = Component.literal(newPrefix);
        this.suffixComp = Component.literal(newSuffix);
        return true;
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public void setSubject(@Nullable Player papiSubject) {
        this.papiSubject = papiSubject;
    }

    @Override
    public @org.jetbrains.annotations.Nullable Player getSubject() {
        return papiSubject;
    }

    @Override
    public void setPushingRule(@NotNull PlayerTab.PushingRule rule) {
        switch (rule) {
            case NEVER -> this.pushingRule = Team.CollisionRule.NEVER;
            case ALWAYS -> this.pushingRule = Team.CollisionRule.ALWAYS;
            case PUSH_OTHER_TEAMS -> this.pushingRule = Team.CollisionRule.PUSH_OTHER_TEAMS;
            case PUSH_OWN_TEAM -> this.pushingRule = Team.CollisionRule.PUSH_OWN_TEAM;
        }
        if (null != this.id) {
            sendUpdateToReceivers();
        }
    }

    @Override
    public void setNameTagVisibility(@NotNull PlayerTab.NameTagVisibility nameTagVisibility) {
        switch (nameTagVisibility) {
            case NEVER -> this.nameTagVisibility = Team.Visibility.NEVER;
            case ALWAYS -> this.nameTagVisibility = Team.Visibility.ALWAYS;
            case HIDE_FOR_OTHER_TEAMS -> this.nameTagVisibility = Team.Visibility.HIDE_FOR_OTHER_TEAMS;
            case HIDE_FOR_OWN_TEAM -> this.nameTagVisibility = Team.Visibility.HIDE_FOR_OWN_TEAM;
        }
        if (null != id) {
            sendUpdateToReceivers();
        }
    }
}
