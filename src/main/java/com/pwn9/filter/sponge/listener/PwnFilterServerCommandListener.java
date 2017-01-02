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
import com.pwn9.filter.engine.rules.chain.InvalidChainException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.event.command.SendCommandEvent;

import java.util.Optional;

/**
 * Apply the filter to commands.
 *
 * @author Sage905
 * @version $Id: $Id
 */
public class PwnFilterServerCommandListener extends PwnFilterCommandListener {

    public PwnFilterServerCommandListener(PwnFilterSpongePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getShortName() {
        return "CONSOLE";
    }

    @Override
    public void handle(SendCommandEvent event) throws Exception {
        if (event.isCancelled()) return;
        Optional<ConsoleSource> console = event.getCause().first(ConsoleSource.class);
        if (console.isPresent())
            onCommand(event, plugin.getConsole());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Activate this listener.  This method can be called either by the owning plugin
     * or by PwnFilter.  PwnFilter will call the shutdown / activate methods when PwnFilter
     * is enabled / disabled and whenever it is reloading its config / rules.
     * <p>
     * These methods could either register / deregister the listener with Bukkit, or
     * they could just enable / disable the use of the filter.
     */
    @Override
    public void activate() {
        if (isActive()) return;
        if (!SpongeConfig.consoleFilterEnabled()) return;
        try {
            ruleChain = getCompiledChain(filterService.getConfig().getRuleFile("console.txt"));
            chatRuleChain = getCompiledChain(filterService.getConfig().getRuleFile("chat.txt"));
            Sponge.getEventManager().registerListener(PwnFilterSpongePlugin.getInstance(), SendCommandEvent.class, SpongeConfig.getCmdPriority(), this);
            setActive();
            plugin.getLogger().info("Activated ServerCommandListener with Priority Setting: " + SpongeConfig.getCmdPriority().toString()
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
            plugin.getLogger().error("Unable to activate ServerCommandListener.  Error: " + e.getMessage());
            setInactive();
        }
    }

}
