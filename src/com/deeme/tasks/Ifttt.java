package com.deeme.tasks;

import com.deeme.types.backpage.Utils;
import com.deeme.types.gui.AdvertisingMessage;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.types.*;
import com.github.manolo8.darkbot.config.types.suppliers.OptionList;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.core.manager.StatsManager;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.tree.components.JLabel;
import com.github.manolo8.darkbot.gui.tree.components.JListField;
import com.github.manolo8.darkbot.utils.SystemUtils;
import com.github.manolo8.darkbot.utils.Time;

import javax.swing.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

@Feature(name = "Ifttt", description = "Used to send statistics to ifttt")
  public class Ifttt implements Task,Configurable<Ifttt.IftttConfig> {

    private IftttConfig iftttConfig;
    private Main main;
    private StatsManager statsManager;
    private long nextSend = 0;
    private final DecimalFormat formatter = new DecimalFormat("###,###,###");

    @Override
    public void install(Main main) {
        this.main = main;
        this.statsManager = main.statsManager;

        AdvertisingMessage.showAdverMessage();
    }

    @Override
    public void tick() {
        if (this.nextSend <= System.currentTimeMillis() && AdvertisingMessage.hasAccepted){
            nextSend = (60000*iftttConfig.intervalMessage) + System.currentTimeMillis();
            sendTrigger();
        }
    }

    public void setConfig(IftttConfig conf) {
        this.iftttConfig = conf;
    }

    public static class IftttConfig {

        @Option("")
        @Editor(value = jInstructions.class, shared = true)
        public Lazy<String> instruction;

        @Option(value = "Message Interval", description = "How often a message is sent in minutes")
        @Num(min = 10, max = 500)
        public int intervalMessage = 10;

        @Option(value = "Ifttt Trigger Name", description = "Name of your Ifttt trigger webhook")
        @Length(10)
        public String iftttTriggerName = "";

        @Option(value = "Ifttt Api Key", description = "Key of your Ifttt webhook")
        @Length(10)
        public String iftttApiKey = "";

        @Option("IFTTT Value 1")
        @Editor(JListField.class)
        @Options(ValueTypes.class)
        public String value1 = "totalUridium";

        @Option("IFTTT Value 2")
        @Editor(JListField.class)
        @Options(ValueTypes.class)
        public String value2 = "totalCredits";

        @Option("IFTTT Value 3")
        @Editor(JListField.class)
        @Options(ValueTypes.class)
        public String value3 = "runningTime";

    }

    public static class jInstructions extends JPanel implements OptionEditor {
        public jInstructions() {
            JLabel text = new JLabel("<html> With this task you can connect the bot with any application thanks to IFTTT.<br/>" +
                    "You have to create an account and fill in the data <br/></html>");
            add(text);
            JButton goLink = new JButton("Go IFTTT WebHooks");
            goLink.addActionListener(e -> {
                SystemUtils.openUrl("https://ifttt.com/maker_webhooks");
            });
            add(goLink);
        }

        public JComponent getComponent() { return this; }

        public void edit(ConfigField configField) {}
    }

    private void sendTrigger() {
        if (iftttConfig.iftttTriggerName == null || iftttConfig.iftttTriggerName.isEmpty() || iftttConfig.iftttApiKey == null || iftttConfig.iftttApiKey.isEmpty()) {
            return;
        }

        String message = "{\"value1\":\""+getSelectValue(iftttConfig.value1)+"\"," +
                "\"value2\":\""+getSelectValue(iftttConfig.value2)+"\"," +
                "\"value3\":\""+getSelectValue(iftttConfig.value3)+"\"}";

        Utils.sendMessage(message,"https://maker.ifttt.com/trigger/"+iftttConfig.iftttTriggerName+"/with/key/"+iftttConfig.iftttApiKey);
    }

    private String getSelectValue(String type) {

        switch (type) {
            case "totalUridium":
                return formatter.format(statsManager.uridium);
            case "totalCredits":
                return formatter.format(statsManager.credits);
            case "totalExp":
                return formatter.format(statsManager.experience);
            case "totalHonor":
                return formatter.format(statsManager.honor);
            case "runningTime":
                return Time.toString(statsManager.runningTime());
            case "ping":
                return String.valueOf(main.pingManager.ping);
            case "sidStatus":
                return main.backpage.sidStatus();
            case "sidLink":
                String sid = main.statsManager.sid, instance = main.statsManager.instance;
                if (sid == null || sid.isEmpty() || instance == null || instance.isEmpty()) return "No link";
                return instance + "?dosid=" + sid;
            case "map":
                return main.hero.map.name;
            case "uri/h":
                return formatter.format(statsManager.earnedUridium());
            case "cre/h":
                return formatter.format(statsManager.earnedCredits());
            case "exp/h":
                return formatter.format(statsManager.earnedExperience());
            case "hon/h":
                return formatter.format(statsManager.earnedHonor());
            case "deaths":
                return String.valueOf(main.guiManager.deaths);
            case "sessionUridium":
                return formatter.format(statsManager.earnedUridium);
            case "sessionCredits":
                return formatter.format(statsManager.earnedCredits);
            case "sessionExp":
                return formatter.format(statsManager.earnedExperience);
            case "sessionHonor":
                return formatter.format(statsManager.earnedHonor);
        }

        return "";
    }


    public static class ValueTypes extends OptionList<String> {
        private final List<String> VALUES_TYPES = Arrays.asList(
                "totalUridium", "totalCredits", "totalExp", "totalHonor",
                "runningTime", "ping", "sidStatus", "sidLink",
                "map", "uri/h", "cre/h", "exp/h", "hon/h", "deaths",
                "sessionUridium", "sessionCredits", "sessionExp", "sessionHonor");

        public String getValue(String text) {
            return text;
        }

        public String getText(String value) {
            return value;
        }

        public List<String> getOptions() {
            return VALUES_TYPES;
        }
    }
}