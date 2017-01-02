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
import com.pwn9.filter.minecraft.util.ColoredString;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.text.serializer.TextSerializers;
import java.util.Optional;


/**
 * Listen for Chat events and apply the filter.
 */
public class PwnFilterPlayerListener extends BaseListener implements EventListener<MessageChannelEvent.Chat> {

    public PwnFilterPlayerListener(PwnFilterSpongePlugin p) { super(p.getFilterService()); }

    public String getShortName() {
        return "CHAT";
    }

    @Override
    public void handle(MessageChannelEvent.Chat event) throws Exception {
        if (event.isCancelled())
            return;
        Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent())
            onPlayerChat(event, player.get());
    }
    //@Listener(order = SpongeConfig.getChatPriority())
    public void onPlayerChat(MessageChannelEvent.Chat event, Player player) {
        if (event.isCancelled())
            return;

        MessageAuthor minecraftPlayer = filterService.getAuthor(player.getUniqueId());

        // Permissions Check, if player has bypass permissions, then skip everything.
        if (minecraftPlayer.hasPermission("pwnfilter.bypass.chat"))
            return;

        // Global mute
        if (SpongeConfig.globalMute() && !minecraftPlayer.hasPermission("pwnfilter.bypass.mute")) {
            event.setMessageCancelled(true);
            return;
        }

        // Spam check
        if (SpongeConfig.spamFilterEnabled() && !minecraftPlayer.hasPermission("pwnfilter.bypass.spam")) {
            String rawMessage = TextSerializers.PLAIN.serialize(event.getRawMessage());
            if (PwnFilterSpongePlugin.lastMessage.containsKey(minecraftPlayer.getId()) && PwnFilterSpongePlugin.lastMessage.get(minecraftPlayer.getId()).equals(rawMessage)) {
                event.setMessageCancelled(true);
                minecraftPlayer.sendMessage(TextColors.DARK_RED + "[PwnFilter]" + TextColors.RED + " Repeated command blocked by spam filter.");
                return;
            }
            PwnFilterSpongePlugin.lastMessage.put(minecraftPlayer.getId(), rawMessage);
        }

        MessageChannelEvent.MessageFormatter msgFormatter = event.getFormatter();
        Text textMessage = msgFormatter.getBody().toText();
        String message = TextSerializers.FORMATTING_CODE.serialize(textMessage);

        // Global decolor
        if (SpongeConfig.decolor() && !minecraftPlayer.hasPermission("pwnfilter.color"))
            message = TextSerializers.PLAIN.serialize(textMessage);
        FilterContext state = new FilterContext(new ColoredString(message), minecraftPlayer, this);

        // Take the message from the ChatEvent and send it through the filter.
        PwnFilterSpongePlugin.getLogger().debug("Applying '" + ruleChain.getConfigName() + "' to message: " + state.getModifiedMessage());
        ruleChain.execute(state, filterService);

        // Only update the message if it has been changed.
        if (state.isCancelled() || state.getModifiedMessage().toString().isEmpty()) {
            event.setMessageCancelled(true);
            return;
        }
        if (state.messageChanged()) {
            msgFormatter.setBody(TextSerializers.FORMATTING_CODE.deserialize(state.getModifiedMessage().getRaw()));
            event.setMessage(msgFormatter.toText());
        }
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

        try {
            ruleChain = getCompiledChain(filterService.getConfig().getRuleFile("chat.txt"));
            Sponge.getEventManager().registerListener(PwnFilterSpongePlugin.getInstance(), MessageChannelEvent.Chat.class, SpongeConfig.getChatPriority(), this);
            setActive();
            PwnFilterSpongePlugin.getLogger().info("Activated PlayerListener with Priority Setting: " + SpongeConfig.getChatPriority().toString()
                    + " Rule Count: " + getRuleChain().ruleCount());

        } catch ( InvalidChainException e) {
            PwnFilterSpongePlugin.getLogger().error("Unable to activate PlayerListener.  Error: " + e.getMessage());
            setInactive();
        }
    }
}