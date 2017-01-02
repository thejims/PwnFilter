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

package com.pwn9.filter.sponge.listener;

import com.pwn9.filter.sponge.PwnFilterSpongePlugin;
import com.pwn9.filter.sponge.config.SpongeConfig;
import com.pwn9.filter.engine.api.FilterContext;
import com.pwn9.filter.engine.api.MessageAuthor;
import com.pwn9.filter.engine.rules.chain.InvalidChainException;
import com.pwn9.filter.engine.rules.chain.RuleChain;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;


/**
 * Apply the filter to commands.
 *
 * @author Sage905
 * @version $Id: $Id
 */
public class PwnFilterCommandListener extends BaseListener implements EventListener<SendCommandEvent> {
    volatile PwnFilterSpongePlugin plugin;
    volatile RuleChain chatRuleChain;

    public PwnFilterCommandListener(PwnFilterSpongePlugin plugin) {
        super(plugin.getFilterService());
        this.plugin = plugin;
    }

    public String getShortName() {
        return "COMMAND";
    }

    /**
     * <p>eventProcessor.</p>
     *
     * @param event a {@link org.spongepowered.api.event.command.SendCommandEvent} object.
     */
	 @Override
     public void handle(SendCommandEvent event) throws Exception {
         if (event.isCancelled()) return;
         Optional<Player> player = event.getCause().first(Player.class);
         if (player.isPresent())
             onCommand(event, filterService.getAuthor(player.get().getUniqueId()));
     }
    //@Listener(order = Order.EARLY)
    public void onCommand(SendCommandEvent event, MessageAuthor minecraftPlayer) {
        if (event.isCancelled()) return;

        if (minecraftPlayer.hasPermission("pwnfilter.bypass.commands")) return;

        String args = event.getArguments();
        String cmd = event.getCommand();
        String fullCmd = cmd + " " + args;

        // If it's blacklisted or it isn't white listed then don't do anything
        if ((!SpongeConfig.getCmdBlacklist().isEmpty() && !SpongeConfig.getCmdBlacklist().contains(cmd)) ||
                (!SpongeConfig.getCmdWhitelist().isEmpty() && SpongeConfig.getCmdWhitelist().contains(cmd)))
            return;

        // Ignore if this command was triggered by a previous event to avoid recursion!
        if (PwnFilterSpongePlugin.triggeredCommand.containsKey(minecraftPlayer.getId()) && PwnFilterSpongePlugin.triggeredCommand.get(minecraftPlayer.getId()).equals(fullCmd)) {
            PwnFilterSpongePlugin.triggeredCommand.remove(minecraftPlayer.getId());
            return;
        }

        FilterContext filterTask = new FilterContext(args, cmd, minecraftPlayer, this);

        // Check to see if we should treat this command as chat (eg: /tell)
        if (SpongeConfig.getChatCommands().contains(cmd)) {

            // Global mute
            if (SpongeConfig.globalMute() && !minecraftPlayer.hasPermission("pwnfilter.bypass.mute")) {
                event.setCancelled(true);
                return;
            }

            // Simple Spam filter
            if (SpongeConfig.cmdSpamFilterEnabled() && !minecraftPlayer.hasPermission("pwnfilter.bypass.spam")) {
                // Keep a log of the last message sent by this player.  If it's the same as the current message, cancel.
                if (PwnFilterSpongePlugin.lastMessage.containsKey(minecraftPlayer.getId()) && PwnFilterSpongePlugin.lastMessage.get(minecraftPlayer.getId()).equals(args)) {
                    event.setCancelled(true);
                    minecraftPlayer.sendMessage(TextColors.DARK_RED + "[PwnFilter]" + TextColors.RED + " Repeated command blocked by spam filter.");
                    return;
                }
                PwnFilterSpongePlugin.lastMessage.put(minecraftPlayer.getId(), args);
            }

            chatRuleChain.execute(filterTask, filterService);
        } else {
            // Take the message from the Command Event and send it through the filter.
            ruleChain.execute(filterTask, filterService);
        }

        // Only update the message if it has been changed.
        if (filterTask.isCancelled() || filterTask.getModifiedMessage().toString().isEmpty()) {
            event.setCancelled(true);
            return;
        }

        if (filterTask.messageChanged())
            event.setArguments(filterTask.getModifiedMessage().getRaw().trim());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Activate this listener.  This method can be called either by the owning plugin
     * or by PwnFilter.  PwnFilter will call the shutdown / activate methods when PwnFilter
     * is enabled / disabled and whenever it is reloading its config / rules.
     * <p>
     * These methods could either register / deregister the listener with Sponge, or
     * they could just enable / disable the use of the filter.
     */
    @Override
    public void activate() {
        if (isActive()) return;
        if (!SpongeConfig.cmdFilterEnabled()) return;
        try {
            ruleChain = getCompiledChain(filterService.getConfig().getRuleFile("command.txt"));
            chatRuleChain = getCompiledChain(filterService.getConfig().getRuleFile("chat.txt"));

            // Now register the listener with the appropriate priority
            Sponge.getEventManager().registerListener(PwnFilterSpongePlugin.getInstance(), SendCommandEvent.class, SpongeConfig.getCmdPriority(), this);
            setActive();
            plugin.getLogger().info("Activated CommandListener with Priority Setting: " + SpongeConfig.getCmdPriority().toString()
                    + " Rule Count: " + ruleChain.ruleCount() + " Chat Rule Count: " + chatRuleChain.ruleCount());

            StringBuilder sb = new StringBuilder("Commands handled as chat: ");
            for (String command : SpongeConfig.getChatCommands())
                sb.append(command).append(" ");
            plugin.getLogger().info(sb.toString().trim());

            sb = new StringBuilder("Commands to filter: ");
            for (String command : SpongeConfig.getCmdWhitelist())
                sb.append(command).append(" ");
            plugin.getLogger().info(sb.toString().trim());

            sb = new StringBuilder("Commands to never filter: ");
            for (String command : SpongeConfig.getCmdBlacklist())
                sb.append(command).append(" ");
            plugin.getLogger().info(sb.toString().trim());
        } catch (InvalidChainException e) {
            plugin.getLogger().error("Unable to activate CommandListener.  Error: " + e.getMessage());
            setInactive();
        }
    }

}