package org.by1337.bauction.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.lang.Lang;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHook extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "BAuction";
    }

    @Override
    public @NotNull String getAuthor() {
        return "By1337";
    }

    @Override
    public @NotNull String getVersion() {
        return Main.getInstance().getDescription().getVersion();
    }

    private static final Placeholder placeholder;

    @Override
    @Nullable
    public String onPlaceholderRequest(final Player player, @NotNull final String params) {// %bairdrop_test% = test %bairdrop_time_to_open_<air id>%
        return placeholder.process(player, params.split("_"));
    }

    static {
        placeholder = new Placeholder(null)
                .addSubPlaceholder(new Placeholder("player")
                        .addSubPlaceholder(new Placeholder("deal")
                                .addSubPlaceholder(new Placeholder("sum")
                                        .executor(player -> {
                                            if (player == null) return "player is null!";
                                            return String.valueOf(Main.getStorage().getUser(player.getUniqueId()).getDealSum());
                                        })// player_deal_sum
                                )
                                .addSubPlaceholder(new Placeholder("count")
                                        .executor(player -> {
                                            if (player == null) return "player is null!";
                                            return String.valueOf(Main.getStorage().getUserOrCreate(player).getDealCount());
                                        })// player_deal_count
                                )
                        )
                        .addSubPlaceholder(new Placeholder("selling")
                                .addSubPlaceholder(new Placeholder("item")
                                        .addSubPlaceholder(new Placeholder("count")
                                                .executor(player -> {
                                                    if (player == null) return "player is null!";
                                                    return String.valueOf(Main.getStorage().sellItemsCountByUser(player.getUniqueId()));
                                                })//selling_item_count
                                        )
                                )
                        )
                        .addSubPlaceholder(new Placeholder("not")
                                .addSubPlaceholder(new Placeholder("sold")
                                        .addSubPlaceholder(new Placeholder("item")
                                                .addSubPlaceholder(new Placeholder("count")
                                                        .executor(player -> {
                                                            if (player == null) return "player is null!";
                                                            return String.valueOf(Main.getStorage().unsoldItemsCountByUser(player.getUniqueId()));
                                                        })//not_sold_item_count
                                                )
                                        )
                                )
                        )
                        .addSubPlaceholder(new Placeholder("external")
                                .addSubPlaceholder(new Placeholder("slots")
                                        .addSubPlaceholder(new Placeholder("count")
                                                .executor(player -> {
                                                    if (player == null) return "player is null!";
                                                    return String.valueOf(Main.getStorage().getUserOrCreate(player).getExternalSlots());
                                                })//external_slots_count
                                        )
                                )
                        )
                        .addSubPlaceholder(new Placeholder("external")
                                .addSubPlaceholder(new Placeholder("sell")
                                        .addSubPlaceholder(new Placeholder("time")
                                                .executor(player -> {
                                                    if (player == null) return "player is null!";
                                                    long time = Main.getStorage().getUserOrCreate(player).getExternalSellTime();
                                                    if (time == 0) return Lang.getMessage("has_no_external_time");
                                                    return Main.getTimeUtil().getFormat(Main.getStorage().getUserOrCreate(player).getExternalSellTime() + System.currentTimeMillis(), false);
                                                })//external_sell_time
                                        )
                                )
                        )

                )
        ;
    }

}
