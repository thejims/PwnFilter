package com.pwn9.filter.util;

import com.pwn9.filter.engine.FilterService;
import com.pwn9.filter.engine.rules.chain.RuleChain;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatType;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Created by thejims on 12/20/2016.
 */
public class MessageFilterChannel implements MessageChannel {

    private final FilterService filterService;
    private final RuleChain ruleChain;

    public MessageFilterChannel(FilterService filterService, RuleChain ruleChain) {
        this.filterService = filterService;
        this.ruleChain = ruleChain;
    }

    @Override public Optional<Text> transformMessage(Object sender, MessageReceiver recipient, Text original, ChatType type) {
        Text text = original;


        return Optional.of(text);
    }

    @Override public Collection<MessageReceiver> getMembers() {
        return Collections.emptyList();
    }
}
