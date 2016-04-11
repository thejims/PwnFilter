/*
 * PwnFilter -- Regex-based User Filter Plugin for Bukkit-based Minecraft servers.
 * Copyright (c) 2013 Pwn9.com. Tremor77 <admin@pwn9.com> & Sage905 <patrick@toal.ca>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.pwn9.filter.minecraft.command;

import com.pwn9.filter.minecraft.api.MinecraftConsole;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Reload the PwnFilter config.
 * User: Sage905
 * Date: 13-08-10
 * Time: 9:23 AM
 *
 * @author Sage905
 * @version $Id: $Id
 */
public class pfcls implements CommandExecutor {
    private final Logger logger;
    private final MinecraftConsole console;
    /**
     * <p>Constructor for pfcls.</p>
     */
    public pfcls(Logger logger, MinecraftConsole console) {
        this.console = console;
        this.logger = logger;
    }

    /** {@inheritDoc} */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.RED + "Clearing chat screen");
        logger.info("chat screen cleared by " + sender.getName());
        int i = 0;
        List<String> messages = new ArrayList<>();
        while (i <= 120) {
            messages.add(" ");
            i++;
        }

        console.sendBroadcast(messages);

        return true;
    }

}