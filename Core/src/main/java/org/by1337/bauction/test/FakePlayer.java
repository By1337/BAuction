package org.by1337.bauction.test;

import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.User;
import org.by1337.bauction.db.kernel.MemoryDatabase;
import org.by1337.bauction.db.kernel.event.AddSellItemEvent;
import org.by1337.bauction.db.kernel.event.BuyItemEvent;

import java.util.Random;
import java.util.UUID;

public class FakePlayer {
    private final Random random = new Random();
    private final MemoryDatabase storage;
    private UUID uuid;
    private String nickName;
    private int ahLimit;

    public FakePlayer(MemoryDatabase storage) {
        this(storage, Integer.MAX_VALUE);
    }

    public FakePlayer(MemoryDatabase core, int ahLimit) {
        this.ahLimit = ahLimit;
        this.storage = core;
        nickName = UUID.randomUUID().toString().substring(30);
        uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + nickName).getBytes(Charsets.UTF_8));
    }

    public void randomAction() {
        if (storage.getSellItemsSize() >= ahLimit) {
            buyItem();
            return;
        }
        if (random.nextBoolean()) {
            buyItem();
        } else {
            sellItem();
        }
    }

    private void buyItem() {
        if (storage.getSellItemsSize() == 0) return;

        SellItem item = storage.getFirstSellItem();

        User user = storage.getUserOrCreate(nickName, uuid);

        BuyItemEvent event = new BuyItemEvent(user, item);
        Main.getStorage().onEvent(event).whenComplete((e, t) -> {
            OfflinePlayer seller = Bukkit.getOfflinePlayer(item.getSellerUuid());
            if (e.isValid()) {
                Main.getEcon().depositPlayer(seller, item.getPrice());
            }
        });
    }

    private void sellItem() {
        User user = storage.getUserOrCreate(nickName, uuid);
        user.setExternalSlots(9999);
        ItemStack itemStack = new ItemStack(Material.values()[random.nextInt(50) + 1]);
        itemStack.setAmount(random.nextInt(itemStack.getType().getMaxStackSize() - 1) + 1);
        SellItem sellItem = new SellItem(nickName, uuid, itemStack, random.nextInt(200) + 200, Main.getCfg().getDefaultSellTime() + user.getExternalSellTime(), true);
        AddSellItemEvent event = new AddSellItemEvent(sellItem, user);
        storage.onEvent(event).whenComplete((e, t) -> {
        });
    }


}
