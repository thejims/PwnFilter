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
import com.pwn9.filter.sponge.config.SpongeConfig;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
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
public class pfmute implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource sender, CommandContext args) {
        if (SpongeConfig.globalMute()) {
            SpongeConfig.setGlobalMute(false);
            Sponge.getServer().getConsole().sendMessage(Text.builder("Global mute cancelled by " + sender.getName()).color(TextColors.RED).build());
            PwnFilterSpongePlugin.getLogger().info("global mute cancelled by " + sender.getName());
            sender.sendMessage(Text.builder("Global mute cancelled").color(TextColors.RED).build());
        } else {
            SpongeConfig.setGlobalMute(true);
            Sponge.getServer().getConsole().sendMessage(Text.builder("Global mute initiated by " + sender.getName()).color(TextColors.RED).build());
            PwnFilterSpongePlugin.getLogger().info("global mute initiated by " + sender.getName());
            sender.sendMessage(Text.builder("Global mute initiated").color(TextColors.RED).build());
        }
        return CommandResult.success();
    }

}