/*
 *  PwnFilter - Chat and user-input filter with the power of Regex
 *  Copyright (C) 2016 Pwn9.com / Sage905 <sage905@takeflight.ca>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.pwn9.filter.sponge;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.pwn9.filter.engine.api.AuthorService;
import com.pwn9.filter.engine.api.NotifyTarget;
import com.pwn9.filter.minecraft.DeathMessages;
import com.pwn9.filter.minecraft.api.MinecraftAPI;
import com.pwn9.filter.minecraft.api.MinecraftConsole;
//import net.milkbowl.vault.economy.EconomyResponse;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.data.key.Keys;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles keeping a cache of data that we need during Async event handling.
 * We can't get this data in our Async method, as Bukkit API calls are not threadsafe.
 * Also, we can't always schedule a task, because we might be running in the
 * main thread.
 * <p/>
 * This will cache data about players for 10s.
 */

class SpongeAPI implements MinecraftAPI, AuthorService, NotifyTarget {

    private final PwnFilterSpongePlugin plugin;
    private final MinecraftAPI playerAPI = this;
    private final Cache<UUID, PwnFilterPlayer> playerCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build();

    SpongeAPI(PwnFilterSpongePlugin p) {
        plugin = p;
    }

    public PwnFilterPlayer getAuthorById(final UUID u) {
        /*
        Not sure if this is the best way of doing this, but we need to make
        certain that we do not block the main server thread while waiting for a
        FutureTask in the same thread when the Cache loads a missing value.
         */
        PwnFilterPlayer player;
        player = playerCache.getIfPresent(u);
        if (player == null) {
            Player onlinePlayer = (Sponge.getServer().getPlayer(u)).get();
            if (onlinePlayer != null) playerCache.asMap().putIfAbsent(u, new PwnFilterPlayer(u, playerAPI));
        }
        // At this point, the player should be in the cache if they are online.
        // If player is offline, returns null
        return playerCache.getIfPresent(u);
    }

    public MinecraftConsole getConsole() {
        return plugin.getConsole();
    }

    @Override
    public synchronized void reset() {
        playerCache.invalidateAll();
    }

    /*
      **********
      Player API
      **********
    */
//    public Player getPlayerFromID(UUID u) { // No way this is right.. UUID.randomUUID???
//        return Sponge.getServer().getPlayer(UUID.randomUUID()).get();
//
//    }

    // Get the player's Name (Works even if they are offline)
    @Nullable
    @Override
    public String getPlayerName(final UUID u) {
        Player p = Sponge.getServer().getPlayer(u).get();
        if (p != null) return p.getName();
        return null;
    }

    // Get the player's current world
    @Nullable
    @Override
    public String getPlayerWorldName(final UUID u) {
        Player p = Sponge.getServer().getPlayer(u).get();
        if (p != null) return p.getWorld().getName();
        return null;
    }

    // Check if a player has a perm. (not cached)
    @Override
    public Boolean playerIdHasPermission(final UUID u, final String s) {
        Player p = Sponge.getServer().getPlayer(u).get();
        return p != null && p.hasPermission(s);
    }

    @Override
    public boolean burn(final UUID u, final int duration, final String s) {
        Player spongePlayer = Sponge.getServer().getPlayer(u).get();
        if (spongePlayer != null) {
            spongePlayer.offer(Keys.FIRE_TICKS, duration);
            spongePlayer.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(s));
        }
        return true;
    }

    @Override
    public void sendMessage(final UUID u, final String s) {
        Player spongePlayer = Sponge.getServer().getPlayer(u).get();
        if (spongePlayer != null)
            spongePlayer.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(s));
    }

    @Override
    public void sendMessages(final UUID u, final List<String> s) {
        s.forEach(msg -> { sendMessage(u, msg);});
    }

    @Override
    public void executePlayerCommand(final UUID u, final String command) {
        Optional<Player> spongePlayer = Sponge.getServer().getPlayer(u);
        Optional<? extends CommandMapping> commandMapping = Sponge.getCommandManager().get(command.substring(0,command.indexOf(' ')));
        if (!spongePlayer.isPresent() || !spongePlayer.get().getCommandSource().isPresent() || !commandMapping.isPresent())
            return;
        boolean hasPerms = commandMapping.get().getCallable().testPermission(spongePlayer.get().getCommandSource().get());
        PwnFilterSpongePlugin.getLogger().debug("Permissions check, does " + spongePlayer.get().getName() +
                " have permissions for " + command.substring(0,command.indexOf(' ')) + ":" + hasPerms);
        if (!hasPerms) return;
        Sponge.getCommandManager().process(spongePlayer.get().getCommandSource().get(), command);
    }

    /*@Override // WIP
    public boolean withdrawMoney(final UUID uuid, final Double amount, final String messageString) {
        if (PwnFilterSpongePlugin.economy != null) {
            Boolean result = safeSpongeAPICall(
                    () -> {
                        Player spongePlayer = Sponge.getServer().getPlayer(uuid).get();
                        if (spongePlayer != null) {
                            EconomyResponse resp = PwnFilterSpongePlugin.economy.withdrawPlayer(
                                    spongePlayer, amount);
                            spongePlayer.sendMessage(Text.of(messageString));
                            return resp.transactionSuccess();
                        }
                        return false;
                    });
            if (result != null) return result;
        }
        return false;
    }*/
	@Override public boolean withdrawMoney(final UUID u, final Double amount, final String messageString) { return false; }
	
	
    @Override
    public void kick(final UUID u, final String s) {
        Player spongePlayer = Sponge.getServer().getPlayer(u).get();
        if (spongePlayer != null)
            spongePlayer.kick(Text.of(s));
	}

    @Override
    public void kill(final UUID u, final String s) {
        Player spongePlayer = Sponge.getServer().getPlayer(u).get();
        if (spongePlayer != null)
            spongePlayer.offer(Keys.HEALTH, 0D);
        DeathMessages.addKilledPlayer(u, spongePlayer + " " + s);
    }

    /*
      ***********
      Console API
      ***********
     */
    @Override
    public void sendConsoleMessage(final String message) {
        Sponge.getServer().getConsole().sendMessage(TextSerializers.FORMATTING_CODE.deserialize(message));
    }

    @Override
    public void sendConsoleMessages(final List<String> messageList) {
        messageList.forEach(message -> Sponge.getServer().getConsole().sendMessage(TextSerializers.FORMATTING_CODE.deserialize(message)));
    }

    @Override
    public void sendBroadcast(final String message) {
        Sponge.getServer().getBroadcastChannel().send(TextSerializers.FORMATTING_CODE.deserialize(message));
    }

    @Override
    public void sendBroadcast(final List<String> messageList) {
        messageList.forEach(message -> Sponge.getServer().getBroadcastChannel().send(TextSerializers.FORMATTING_CODE.deserialize(message)));
    }

    @Override
    public void executeCommand(final String command) {
		Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command);
    }

    @Override
    public void notifyWithPerm(final String permissionString, final String sendString) {
        if (permissionString.equalsIgnoreCase("console")) {
            Sponge.getServer().getConsole().sendMessage(TextSerializers.FORMATTING_CODE.deserialize(sendString));
        } else {
            Sponge.getServer().getOnlinePlayers()
                    .stream()
                    .filter(p -> p.hasPermission(permissionString))
                    .forEach(p -> p.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(sendString)));
        }
    }

    public class PlayerNotFound extends Exception {
    }

}