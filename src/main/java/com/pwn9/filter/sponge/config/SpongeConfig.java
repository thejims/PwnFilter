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

package com.pwn9.filter.sponge.config;


import com.pwn9.filter.sponge.PwnFilterSpongePlugin;
import org.spongepowered.api.event.Order;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;


/**
 * A largely static object, which serves as an interface to the PwnFilter Sponge configuration.
 * Created by Sage905 on 15-09-10.
 */
public class SpongeConfig {

	private static ConfigurationNode rootNode;

    public static void loadConfiguration() throws IOException {
        rootNode = PwnFilterSpongePlugin.getConfigLoader().load();
    }

    public static boolean decolor() {
        if (rootNode.getNode("decolor").isVirtual())
            return false;
        return rootNode.getNode("decolor").getBoolean();
    }

    public static boolean globalMute() {
        if (rootNode.getNode("globalmute").isVirtual())
            return false;
        return rootNode.getNode("globalmute").getBoolean();
    }

    public static void setGlobalMute(boolean globalMute) {
        rootNode.getNode("globalmute").setValue(globalMute);
        try {
             PwnFilterSpongePlugin.getConfigLoader().save(rootNode);
        } catch (IOException e) {
            // We'll log this once we get the Logger all sorted
        }
    }

    public static List<String> getCmdWhitelist() {
        List<String> defaultValue = Collections.emptyList();
        if (rootNode.getNode("cmdlist").isVirtual())
            return defaultValue;
        try {
			return rootNode.getNode("cmdlist").getList(TypeToken.of(String.class));
		} catch (ObjectMappingException e) {
			return defaultValue;
		}
    }

    public static List<String> getCmdBlacklist() {
        List<String> defaultValue = Collections.emptyList();
        if (rootNode.getNode("cmdblist").isVirtual())
            return defaultValue;
        try {
			return rootNode.getNode("cmdblist").getList(TypeToken.of(String.class));
		} catch (ObjectMappingException e) {
			return defaultValue;
		}
	}

    public static List<String> getChatCommands() {
        List<String> defaultValue = Arrays.asList("me", "nick", "m", "mail", "msg", "nick", "r", "say", "t", "tell", "whisper");
        if (rootNode.getNode("cmdchat").isVirtual())
            return defaultValue;
        try {
			return rootNode.getNode("cmdchat").getList(TypeToken.of(String.class));
		} catch (ObjectMappingException e) {
			return defaultValue;
		}
	}

    public static Order getCmdPriority() {
        if (rootNode.getNode("cmdpriority").isVirtual())
            return Order.DEFAULT;
        return Order.valueOf(rootNode.getNode("cmdpriority").getString().toUpperCase());
    }

    public static Order getChatPriority() {
        if (rootNode.getNode("chatpriority").isVirtual())
            return Order.DEFAULT;
        return Order.valueOf(rootNode.getNode("chatpriority").getString().toUpperCase());
    }

    public static boolean cmdFilterEnabled() {
        if (rootNode.getNode("commandfilter").isVirtual())
            return false;
        return rootNode.getNode("commandfilter").getBoolean();
    }

    public static boolean cmdSpamFilterEnabled() {
        if (rootNode.getNode("commandspamfilter").isVirtual())
            return false;
        return rootNode.getNode("commandspamfilter").getBoolean();
    }

    public static boolean spamFilterEnabled() {
        if (rootNode.getNode("spamfilter").isVirtual())
            return false;
        return rootNode.getNode("spamfilter").getBoolean();
    }

    public static Order getBookPriority() {
        if (rootNode.getNode("bookpriority").isVirtual())
            return Order.EARLY;
        return Order.valueOf(rootNode.getNode("bookpriority").getString().toUpperCase());
    }

    public static boolean bookFilterEnabled() {
        if (rootNode.getNode("bookfilter").isVirtual())
            return false;
        return rootNode.getNode("bookfilter").getBoolean();
    }

    public static boolean itemFilterEnabled() {
        if (rootNode.getNode("itemfilter").isVirtual())
            return false;
        return rootNode.getNode("itemfilter").getBoolean();
    }

    public static Order getItemPriority() {
        if (rootNode.getNode("itempriority").isVirtual())
            return Order.EARLY;
        return Order.valueOf(rootNode.getNode("itempriority").getString().toUpperCase());
    }

    public static boolean consoleFilterEnabled() {
        if (rootNode.getNode("consolefilter").isVirtual())
            return false;
        return rootNode.getNode("consolefilter").getBoolean();
    }

    public static Order getSignPriority() {
        if (rootNode.getNode("signpriority").isVirtual())
            return Order.EARLY;
        return Order.valueOf(rootNode.getNode("signpriority").getString().toUpperCase());
    }

    public static boolean signFilterEnabled() {
        if (rootNode.getNode("signfilter").isVirtual())
            return false;
        return rootNode.getNode("signfilter").getBoolean();
    }

    public static String getRulesDirectory() {
        if (rootNode.getNode("ruledirectory").isVirtual())
            return "rules";
        return rootNode.getNode("ruledirectory").getString();
    }

    public static String getTextDirectory() {
        if (rootNode.getNode("textdir").isVirtual())
            return "textfiles";
        return rootNode.getNode("textdir").getString();
    }

    public static ConfigurationNode getPoints() {
        return rootNode.getNode("points");
    }
}