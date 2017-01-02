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

package com.pwn9.filter.sponge.minecraft.command;

import com.pwn9.filter.sponge.PwnFilterSpongePlugin;
import com.pwn9.filter.engine.FilterService;

import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;

/**
 * Reload the PwnFilter config.
 * User: Sage905
 * Date: 13-08-10
 * Time: 9:23 AM
 *
 * @author Sage905
 * @version $Id: $Id
 */
public class pfreload implements CommandExecutor {
    private final FilterService filterService;
    private final PwnFilterSpongePlugin plugin;

    public pfreload(FilterService filterService, PwnFilterSpongePlugin plugin) {
        this.filterService = filterService;
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext args) {
        sender.sendMessage(Text.builder("Reloading config and rules/*.txt files.").color(TextColors.RED).build());

        PwnFilterSpongePlugin.getLogger().info("Disabling all listeners");
        filterService.disableClients();

        plugin.configurePlugin();

        PwnFilterSpongePlugin.getLogger().info("Reloaded config as requested by " + sender.getName());
        PwnFilterSpongePlugin.getLogger().info("All rules reloaded by " + sender.getName());

        // Re-register our listeners
        filterService.enableClients();
        PwnFilterSpongePlugin.getLogger().info("All listeners re-enabled");

        return CommandResult.success();

    }

}