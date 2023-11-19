package org.by1337.bauction.lang;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.by1337.api.configuration.YamlContext;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class Lang {
    private static final HashMap<String, String> messages = new HashMap<>();

    public static void load(Plugin plugin) {
        messages.clear();

        YamlContext context;
        File file;
        file = new File(plugin.getDataFolder().getPath() + "/message.yml");
        if (!file.exists()) {
            plugin.saveResource("message.yml", true);
        }
        context = new YamlContext(YamlConfiguration.loadConfiguration(file));

        // Load messages into the map
        messages.put("successful_loading", context.getAsString("successful_loading"));
        messages.put("plugin_reload", context.getAsString("plugin_reload"));
        messages.put("must_be_player", context.getAsString("must_be_player"));
        messages.put("item_in_hand_required", context.getAsString("item_in_hand_required"));
        messages.put("price_tag", context.getAsString("price_tag"));
        messages.put("quantity_tag", context.getAsString("quantity_tag"));
        messages.put("sale_time_tag", context.getAsString("sale_time_tag"));
        messages.put("successful_listing", context.getAsString("successful_listing"));
        messages.put("cannot_trade_air", context.getAsString("cannot_trade_air"));
        messages.put("successful_single_listing", context.getAsString("successful_single_listing"));
        messages.put("tags_required", context.getAsString("tags_required"));
        messages.put("start_entering_item_name", context.getAsString("start_entering_item_name"));
        messages.put("auction_item_limit_reached", context.getAsString("auction_item_limit_reached"));
        messages.put("error_occurred", context.getAsString("error_occurred"));
        messages.put("not_item_owner", context.getAsString("not_item_owner"));
        messages.put("item_already_sold_or_removed", context.getAsString("item_already_sold_or_removed"));
        messages.put("item_not_found", context.getAsString("item_not_found"));
        messages.put("item_owner", context.getAsString("item_owner"));
        messages.put("quantity_limit_exceeded", context.getAsString("quantity_limit_exceeded"));
        messages.put("successful_item_retrieval", context.getAsString("successful_item_retrieval"));
        messages.put("insufficient_balance", context.getAsString("insufficient_balance"));
        messages.put("item_sold_to_buyer", context.getAsString("item_sold_to_buyer"));
        messages.put("successful_purchase", context.getAsString("successful_purchase"));
        messages.put("something_went_wrong", context.getAsString("something_went_wrong"));
        messages.put("price_not_specified", context.getAsString("price_not_specified"));
        messages.put("insufficient_balance_for_purchase", context.getAsString("insufficient_balance_for_purchase"));
        messages.put("item_no_longer_exists", context.getAsString("item_no_longer_exists"));
        messages.put("has_no_external_time", context.getAsString("has_no_external_time"));

        messages.putAll(context.getMap("items", String.class));
    }

    public static String getMessages(String s) {
        String str = messages.get(s);
        if (str == null) return "please check the message file";
        return str;
    }
}
