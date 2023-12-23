package org.by1337.bauction.db.kernel;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.by1337.api.util.NameKey;
import org.by1337.bauc.util.SyncDetectorManager;
import org.by1337.bauction.Main;
import org.by1337.bauction.auc.SellItem;
import org.by1337.bauction.auc.UnsoldItem;
import org.by1337.bauction.auc.User;
import org.by1337.bauction.db.event.*;
import org.by1337.bauction.event.*;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.serialize.FileUtil;
import org.by1337.bauction.util.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileDataBase extends DataBaseCore implements Listener {

    protected final long removeTime;
    protected final boolean removeExpiredItems;

    private BukkitTask boostTask;

    public FileDataBase(Map<NameKey, Category> categoryMap, Map<NameKey, Sorting> sortingMap) {
        super(categoryMap, sortingMap);
        removeExpiredItems = Main.getCfg().getConfig().getAsBoolean("remove-expired-items.enable");
        removeTime = NumberUtil.getTime(Main.getCfg().getConfig().getAsString("remove-expired-items.time"));

        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
        boostTask = new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> boostCheck(player.getUniqueId()));
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 20 * 5, 20 * 5);

        expiredChecker();
        unsoldItemRemover();
    }

    protected Runnable sellItemRemChecker;
    protected BukkitTask sellItemRemCheckerTask;
    protected Runnable unsoldItemRemChecker;
    protected BukkitTask unsoldItemRemCheckerTask;


    protected void expiredChecker() {
        sellItemRemChecker = () -> {
            long time = System.currentTimeMillis();
            try {
                long sleep = 50L * 5;
                int removed = 0;
                while (getSellItemsSize() > 0) {
                    SellItem sellItem = getFirstSellItem();
                    if (sellItem.getRemovalDate() < time) {
                        expiredItem(sellItem);
                        removed++;
                        if (removed >= 30)
                            break;
                    } else {
                        sleep = Math.min((sellItem.getRemovalDate() - time) + 50, 50L * 100); // 100 ticks
                        break;
                    }
                }
                if (sellItemRemCheckerTask.isCancelled()) return;
                sellItemRemCheckerTask = Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), sellItemRemChecker, sleep / 50);
            } catch (Exception e) {
                Main.getMessage().error(e);
            }
        };
        sellItemRemCheckerTask = Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), sellItemRemChecker, 0);
    }

    protected void unsoldItemRemover() {
        if (removeExpiredItems) {
            unsoldItemRemChecker = () -> {
                long time = System.currentTimeMillis();
                try {
                    long sleep = 50L * 5;
                    int removed = 0;
                    while (getUnsoldItemsSize() > 0) {
                        UnsoldItem unsoldItem = getFirstUnsoldItem();
                        if (unsoldItem.getDeleteVia() < time) {
                            removeUnsoldItem(unsoldItem.getUniqueName());
                            removed++;
                            if (removed >= 30)
                                break;
                        } else {
                            sleep = Math.min((unsoldItem.getDeleteVia() - time) + 50, 50L * 100); // 100 ticks
                            break;
                        }
                    }
                    if (unsoldItemRemCheckerTask.isCancelled()) return;
                    unsoldItemRemCheckerTask = Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), unsoldItemRemChecker, sleep / 50);
                } catch (Exception e) {
                    Main.getMessage().error(e);
                }
            };
            unsoldItemRemCheckerTask = Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), unsoldItemRemChecker, 0);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        boostCheck(event.getPlayer().getUniqueId());
    }

    @Override
    protected void expiredItem(SellItem item) {
        removeSellItem(item.getUniqueName());
        CUnsoldItem unsoldItem = new CUnsoldItem(item.getItem(), item.getSellerUuid(), item.getRemovalDate(), item.getRemovalDate() + removeTime);
        addUnsoldItem(unsoldItem);
    }

    public void validateAndAddItem(SellItemEvent event) {
        try {
            update();
            SellItem sellItem = event.getSellItem();
            if (!hasUser(event.getUser().getUuid())) {
                throw new IllegalStateException("user non-exist: " + event.getUser());
            }
            if (hasSellItem(sellItem.getUniqueName())) {
                throw new IllegalStateException("sell item non-exist: " + event.getSellItem());
            }
            CUser user = (CUser) getUser(event.getUser().getUuid());

            SellItemProcess sellItemProcess = new SellItemProcess(!SyncDetectorManager.isSync(), user, sellItem);
            Bukkit.getPluginManager().callEvent(sellItemProcess);
            if (sellItemProcess.isCancelled()) {
                event.setValid(false);
                event.setReason(sellItemProcess.getReason());
                return;
            }

            if (Main.getCfg().getMaxSlots() <= (sellItemsCountByUser(user.uuid) - user.getExternalSlots())) {
                event.setValid(false);
                event.setReason(Lang.getMessages("auction_item_limit_reached"));
                return;
            }
            addSellItem(sellItem);
            event.setValid(true);
            Bukkit.getPluginManager().callEvent(new EventSellItem(!SyncDetectorManager.isSync(), user, sellItem));
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason(Lang.getMessages("error_occurred"));
        }
    }

    public void validateAndRemoveItem(TakeItemEvent event) {
        update();
        if (!hasUser(event.getUser().getUuid())) {
            throw new IllegalStateException("user non-exist: " + event.getUser());
        }
        if (!hasSellItem(event.getSellItem().getUniqueName())) {
            event.setValid(false);
            event.setReason(Lang.getMessages("item_already_sold_or_removed"));
            return;
        }
        User user = getUser(event.getUser().getUuid());
        SellItem sellItem = getSellItem(event.getSellItem().getUniqueName());

        if (!user.getUuid().equals(sellItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason(Lang.getMessages("not_item_owner"));
            return;
        }

        TakeItemProcess event1 = new TakeItemProcess(!SyncDetectorManager.isSync(), user, sellItem);
        Bukkit.getPluginManager().callEvent(event1);

        if (event1.isCancelled()) {
            event.setValid(false);
            event.setReason(event1.getReason());
            return;
        }
        try {
            removeSellItem(sellItem.getUniqueName());
            Bukkit.getPluginManager().callEvent(new EventTakeItem(!SyncDetectorManager.isSync(), user, sellItem));
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason(Lang.getMessages("error_occurred"));
            return;
        }
        event.setValid(true);
    }

    public void validateAndRemoveItem(TakeUnsoldItemEvent event) {
        update();
        try {
            if (!hasUser(event.getUser().getUuid())) {
                throw new IllegalStateException("user non-exist");
            }
            if (!hasUnsoldItem(event.getUnsoldItem().getUniqueName())) {
                throw new IllegalStateException("unsold item non-exist");
            }
            User user = getUser(event.getUser().getUuid());
            UnsoldItem unsoldItem = getUnsoldItem(event.getUnsoldItem().getUniqueName());

            TakeUnsoldItemProcess event1 = new TakeUnsoldItemProcess(!SyncDetectorManager.isSync(), user, unsoldItem);
            Bukkit.getPluginManager().callEvent(event1);

            if (event1.isCancelled()) {
                event.setValid(false);
                event.setReason(event1.getReason());
                return;
            }

            if (!user.getUuid().equals(unsoldItem.getSellerUuid())) {
                event.setValid(false);
                event.setReason(Lang.getMessages("not_item_owner"));
                return;
            } else if (!hasUnsoldItem(unsoldItem.getUniqueName())) {
                event.setValid(false);
                event.setReason(Lang.getMessages("item_not_found"));
                return;
            }

            removeUnsoldItem(unsoldItem.getUniqueName());
            event.setValid(true);
            Bukkit.getPluginManager().callEvent(new EventTakeUnsoldItem(!SyncDetectorManager.isSync(), user, unsoldItem));
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason(Lang.getMessages("error_occurred"));
        }
    }

    public void validateAndRemoveItem(BuyItemEvent event) {
        update();
        if (!hasUser(event.getUser().getUuid())) {
            event.setValid(false);
            event.setReason(Lang.getMessages("error_occurred"));
            return;
        }
        CUser user = (CUser) getUser(event.getUser().getUuid());
        SellItem sellItem = getSellItem(event.getSellItem().getUniqueName());

        if (sellItem == null) {
            event.setValid(false);
            event.setReason(Lang.getMessages("item_no_longer_exists"));
            return;
        }

        if (user.getUuid().equals(sellItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason(Lang.getMessages("item_owner"));
            return;
        }
        BuyItemProcess event1 = new BuyItemProcess(!SyncDetectorManager.isSync(), user, sellItem);
        Bukkit.getPluginManager().callEvent(event1);

        if (event1.isCancelled()) {
            event.setValid(false);
            event.setReason(event1.getReason());
            return;
        }
        try {
            removeSellItem(sellItem.getUniqueName());
            if (hasUser(sellItem.getSellerUuid())) {
                CUser owner = (CUser) getUser(sellItem.getSellerUuid());
                owner.dealSum += sellItem.getPrice();
                owner.dealCount++;
            }
            user.dealCount++;
            user.dealSum += sellItem.getPrice();
            Bukkit.getPluginManager().callEvent(new EventBuyItem(!SyncDetectorManager.isSync(), user, sellItem));
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason(Lang.getMessages("error_occurred"));
            return;
        }
        event.setValid(true);
    }

    public void validateAndRemoveItem(BuyItemCountEvent event) {
        try {
            update();
            if (!hasUser(event.getUser().getUuid())) {
                throw new IllegalStateException("user non-exist: " + event.getUser());
            }
            CUser buyer = (CUser) getUser(event.getUser().getUuid());
            CSellItem sellItem = (CSellItem) getSellItem(event.getSellItem().getUniqueName());

            if (buyer.getUuid().equals(sellItem.getSellerUuid())) {
                event.setValid(false);
                event.setReason(Lang.getMessages("item_owner"));
                return;
            }

            if (!hasSellItem(sellItem.getUniqueName())) {
                event.setValid(false);
                event.setReason(Lang.getMessages("item_already_sold_or_removed"));
                return;
            }
            CSellItem updated = (CSellItem) getSellItem(sellItem.getUniqueName());

            if (updated.getAmount() < event.getCount()) {
                event.setValid(false);
                event.setReason(Lang.getMessages("quantity_limit_exceeded"));
                return;
            }
            BuyItemCountProcess event1 = new BuyItemCountProcess(!SyncDetectorManager.isSync(), buyer, sellItem, event.getCount());
            Bukkit.getPluginManager().callEvent(event1);
            if (event1.isCancelled()) {
                event.setValid(false);
                event.setReason(event1.getReason());
                return;
            }

            removeSellItem(sellItem.getUniqueName());
            buyer.dealCount++;
            buyer.dealSum += updated.priceForOne * event.getCount();
            CUser owner = (CUser) getUser(updated.sellerUuid);
            if (owner != null){
                owner.dealCount++;
                owner.dealSum += updated.priceForOne * event.getCount();
            }
            int newCount = updated.getAmount() - event.getCount();
            ItemStack itemStack = updated.getItemStack().clone();
            itemStack.setAmount(newCount);

            if (newCount != 0) {
                CSellItem newItem = CSellItem.builder()
                        .sellerName(updated.getSellerName())
                        .sellerUuid(updated.getSellerUuid())
                        .price(updated.priceForOne * newCount)
                        .saleByThePiece(true)
                        .tags(updated.getTags())
                        .timeListedForSale(updated.getTimeListedForSale())
                        .removalDate(updated.getRemovalDate())
                        .uniqueName(updated.getUniqueName())
                        .material(updated.getMaterial())
                        .amount(newCount)
                        .priceForOne(updated.priceForOne)
                        .itemStack(itemStack)
                        .build();

                addSellItem(newItem);
            }
            Bukkit.getPluginManager().callEvent(new EventBuyItemCount(!SyncDetectorManager.isSync(), buyer, sellItem, event.getCount()));
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason(Lang.getMessages("error_occurred"));
            return;
        }
        event.setValid(true);
    }

    public void load() throws IOException {
        File home = new File(Main.getInstance().getDataFolder() + "/data");
        if (!home.exists()) {
            home.mkdir();
        }
        List<CSellItem> items;
        List<CUser> users;
        List<CUnsoldItem> unsoldItems;

        File fItems = new File(home + "/items.bauc");
        File fUsers = new File(home + "/users.bauc");
        File fUnsoldItems = new File(home + "/unsoldItems.bauc");

        if (fItems.exists()) {
            items = FileUtil.read(fItems, CSellItem::fromBytes);
        } else {
            items = new ArrayList<>();
        }
        if (fUsers.exists()) {
            users = FileUtil.read(fUsers, CUser::fromBytes);
        } else {
            users = new ArrayList<>();
        }
        if (fUnsoldItems.exists()) {
            unsoldItems = FileUtil.read(fUnsoldItems, CUnsoldItem::fromBytes);
        } else {
            unsoldItems = new ArrayList<>();
        }

        if (!items.isEmpty() || !users.isEmpty() || !unsoldItems.isEmpty()) {
            load(items, users, unsoldItems);
        }
    }

    public void save() throws IOException {
        TimeCounter timeCounter = new TimeCounter();
        File home = new File(Main.getInstance().getDataFolder() + "/data");
        if (!home.exists()) {
            home.mkdir();
        }
        File fItems = new File(home + "/items.bauc");
        File fUsers = new File(home + "/users.bauc");
        File fUnsoldItems = new File(home + "/unsoldItems.bauc");

        Collection<? extends SellItem> items = getAllSellItems();
        Collection<? extends User> users = getAllUsers();
        Collection<? extends UnsoldItem> unsoldItems = getAllUnsoldItems();

        for (File file : List.of(fItems, fUsers, fUnsoldItems)) {
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
        }

        if (!items.isEmpty()) {
            FileUtil.write(fItems, items);
        }
        if (!users.isEmpty()) {
            FileUtil.write(fUsers, users);
        }
        if (!unsoldItems.isEmpty()) {
            FileUtil.write(fUnsoldItems, unsoldItems);
        }
        Main.getMessage().logger(
                "saved %s items, %s users and %s unsold items in %s ms",
                items.size(),
                users.size(),
                unsoldItems.size(),
                timeCounter.getTime()

        );
    }

    @Override
    public void close() {
        boostTask.cancel();
        sellItemRemCheckerTask.cancel();
        if (unsoldItemRemChecker != null)
            unsoldItemRemCheckerTask.cancel();
        HandlerList.unregisterAll(this);
    }

    protected void update() {
    }
}
