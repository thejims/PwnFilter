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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;

/**
 * Listeners for the DataCache
 * User: Sage905
 * Date: 13-11-13
 * Time: 10:34 AM
 *
 * @author Sage905
 * @version $Id: $Id
 */
public class PlayerCacheListener {

    /**
     * <p>onPlayerQuit.</p>
     *
     * @param event a {@link org.bukkit.event.player.PlayerQuitEvent} object.
     */
    @Listener(order = Order.EARLY)
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event, @Root Player player) {
        // Cleanup player messages on quit
        if (PwnFilterSpongePlugin.lastMessage.containsKey(player.getUniqueId())) {
            PwnFilterSpongePlugin.lastMessage.remove(player.getUniqueId());
        }
    }

//    /**
//     * <p>onPlayerJoin.</p>
//     *
//     * @param event a {@link org.bukkit.event.player.PlayerJoinEvent} object.
//     */
//    @EventHandler(priority = EventPriority.LOWEST)
//    public void onPlayerJoin(PlayerJoinEvent event) {
//        // In future, might want to load points data?
//    }

}
