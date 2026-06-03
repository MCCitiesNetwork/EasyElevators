package com.minecraftcitiesnetwork.easyelevators.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public final class Texts {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private Texts() {
    }

    public static Component parse(String template, TagResolver... resolvers) {
        if (template == null || template.isEmpty()) {
            return Component.empty();
        }
        return resolvers.length == 0
                ? MINI.deserialize(template)
                : MINI.deserialize(template, TagResolver.resolver(resolvers));
    }
}
