package com.deeme.types.config.ProfileChanger;

import com.deeme.types.gui.ConfigSupplier;
import com.github.manolo8.darkbot.config.ConfigManager;
import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.config.types.Options;
import com.github.manolo8.darkbot.gui.tree.components.JListField;

import eu.darkbot.api.config.types.Condition;

@Option("ProfileChanger")
public class ProfileChangerConfig {
    @Option(value = "Bot profile to set")
    @Editor(JListField.class)
    @Options(ConfigSupplier.class)
    public String BOT_PROFILE = ConfigManager.DEFAULT;

    @Option("Condition")
    public Condition condition;

    @Option("NPC Counter Condition")
    public NpcCounterCondition npcExtraCondition = new NpcCounterCondition();

    @Option("NPC Counter Condition 2")
    public NpcCounterCondition npcExtraCondition2 = new NpcCounterCondition();
}