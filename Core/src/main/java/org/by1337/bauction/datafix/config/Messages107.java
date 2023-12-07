package org.by1337.bauction.datafix.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.by1337.api.configuration.YamlConfig;
import org.by1337.api.configuration.YamlContext;
import org.by1337.bauction.Main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Messages107 {

    public void update() throws IOException, InvalidConfigurationException {
        File messages = new File(Main.getInstance().getDataFolder() + "/message.yml");

        if (!messages.exists()) return;

        Map<String, String> map;

        try (InputStream in = Main.getInstance().getResource("message.yml")) {
            File tempFile = File.createTempFile("message", ".yml");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(tempFile);

            YamlContext context = new YamlContext(yamlConfiguration);

            map = context.getMap("messages", String.class, new HashMap<>());
            tempFile.delete();
        }


        YamlConfig config = new YamlConfig(messages);

        map.put("successful_loading", config.getContext().getAsString("successful_loading", map.get("successful_loading")));
        map.put("plugin_reload", config.getContext().getAsString("plugin_reload", map.get("successful_loading")));
        map.put("must_be_player", config.getContext().getAsString("must_be_player", map.get("plugin_reload")));
        map.put("item_in_hand_required", config.getContext().getAsString("item_in_hand_required", map.get("must_be_player")));
        map.put("price_tag", config.getContext().getAsString("price_tag", map.get("item_in_hand_required")));
        map.put("quantity_tag", config.getContext().getAsString("quantity_tag", map.get("price_tag")));
        map.put("sale_time_tag", config.getContext().getAsString("sale_time_tag", map.get("quantity_tag")));
        map.put("successful_listing", config.getContext().getAsString("successful_listing", map.get("sale_time_tag")));
        map.put("cannot_trade_air", config.getContext().getAsString("cannot_trade_air", map.get("successful_listing")));
        map.put("successful_single_listing", config.getContext().getAsString("successful_single_listing", map.get("cannot_trade_air")));
        map.put("tags_required", config.getContext().getAsString("tags_required", map.get("successful_single_listing")));
        map.put("start_entering_item_name", config.getContext().getAsString("start_entering_item_name", map.get("tags_required")));
        map.put("auction_item_limit_reached", config.getContext().getAsString("auction_item_limit_reached", map.get("start_entering_item_name")));
        map.put("error_occurred", config.getContext().getAsString("error_occurred", map.get("auction_item_limit_reached")));
        map.put("not_item_owner", config.getContext().getAsString("not_item_owner", map.get("error_occurred")));
        map.put("item_already_sold_or_removed", config.getContext().getAsString("item_already_sold_or_removed", map.get("not_item_owner")));
        map.put("item_not_found", config.getContext().getAsString("item_not_found", map.get("item_already_sold_or_removed")));
        map.put("item_owner", config.getContext().getAsString("item_owner", map.get("item_not_found")));
        map.put("quantity_limit_exceeded", config.getContext().getAsString("quantity_limit_exceeded", map.get("item_owner")));
        map.put("successful_item_retrieval", config.getContext().getAsString("successful_item_retrieval", map.get("quantity_limit_exceeded")));
        map.put("insufficient_balance", config.getContext().getAsString("insufficient_balance", map.get("successful_item_retrieval")));
        map.put("item_sold_to_buyer", config.getContext().getAsString("item_sold_to_buyer", map.get("insufficient_balance")));
        map.put("successful_purchase", config.getContext().getAsString("successful_purchase", map.get("item_sold_to_buyer")));
        map.put("something_went_wrong", config.getContext().getAsString("something_went_wrong", map.get("successful_purchase")));
        map.put("price_not_specified", config.getContext().getAsString("price_not_specified", map.get("something_went_wrong")));
        map.put("insufficient_balance_for_purchase", config.getContext().getAsString("insufficient_balance_for_purchase", map.get("price_not_specified")));
        map.put("item_no_longer_exists", config.getContext().getAsString("item_no_longer_exists", map.get("insufficient_balance_for_purchase")));
        map.put("has_no_external_time", config.getContext().getAsString("has_no_external_time", map.get("item_no_longer_exists")));


        config.getContext().getHandle().set("successful_loading", null);
        config.getContext().getHandle().set("plugin_reload", null);
        config.getContext().getHandle().set("must_be_player", null);
        config.getContext().getHandle().set("item_in_hand_required", null);
        config.getContext().getHandle().set("price_tag", null);
        config.getContext().getHandle().set("quantity_tag", null);
        config.getContext().getHandle().set("sale_time_tag", null);
        config.getContext().getHandle().set("successful_listing", null);
        config.getContext().getHandle().set("cannot_trade_air", null);
        config.getContext().getHandle().set("successful_single_listing", null);
        config.getContext().getHandle().set("tags_required", null);
        config.getContext().getHandle().set("start_entering_item_name", null);
        config.getContext().getHandle().set("auction_item_limit_reached", null);
        config.getContext().getHandle().set("error_occurred", null);
        config.getContext().getHandle().set("not_item_owner", null);
        config.getContext().getHandle().set("item_already_sold_or_removed", null);
        config.getContext().getHandle().set("item_not_found", null);
        config.getContext().getHandle().set("item_owner", null);
        config.getContext().getHandle().set("quantity_limit_exceeded", null);
        config.getContext().getHandle().set("successful_item_retrieval", null);
        config.getContext().getHandle().set("insufficient_balance", null);
        config.getContext().getHandle().set("item_sold_to_buyer", null);
        config.getContext().getHandle().set("successful_purchase", null);
        config.getContext().getHandle().set("something_went_wrong", null);
        config.getContext().getHandle().set("price_not_specified", null);
        config.getContext().getHandle().set("insufficient_balance_for_purchase", null);
        config.getContext().getHandle().set("item_no_longer_exists", null);
        config.getContext().getHandle().set("has_no_external_time", null);

        config.getContext().set("messages", map);

        config.save();
    }
}
