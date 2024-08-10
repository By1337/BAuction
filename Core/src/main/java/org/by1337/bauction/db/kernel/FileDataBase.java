package org.by1337.bauction.db.kernel;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.event.*;
import org.by1337.bauction.db.event.*;
import org.by1337.bauction.event.Event;
import org.by1337.bauction.event.EventType;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.util.auction.Category;
import org.by1337.bauction.util.auction.Sorting;
import org.by1337.bauction.util.common.NumberUtil;
import org.by1337.blib.BLib;
import org.by1337.blib.chat.placeholder.MultiPlaceholder;
import org.by1337.blib.util.NameKey;
import org.by1337.blib.util.Pair;

import java.io.IOException;
import java.util.Map;

public class FileDataBase extends DataBaseCore implements Listener {

    protected final long removeTime;
    protected final boolean removeExpiredItems;

    private final BukkitTask boostTask;

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
                            removeUnsoldItem(unsoldItem.id);
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
        removeSellItem(item.id);
        UnsoldItem unsoldItem = new UnsoldItem(item.getItem(), item.getSellerUuid(), item.getRemovalDate(), item.getRemovalDate() + removeTime, item.isCompressed());
        addUnsoldItem(unsoldItem);
        Player player = Bukkit.getPlayer(item.getSellerUuid());
        if (player != null){
            User user = getUserOrCreate(player);
            Event event1 = new Event(player, EventType.EXPIRED_ITEM, new MultiPlaceholder(user, item, unsoldItem));
            Main.getEventManager().onEvent(event1);
        }
    }

    public void validateAndAddItem(SellItemEvent event) {
        try {
            update();
            SellItem sellItem = event.getSellItem();
            if (!hasUser(event.getUser().getUuid())) {
                throw new IllegalStateException("user non-exist: " + event.getUser());
            }
            if (hasSellItem(sellItem.id)) {
                throw new IllegalStateException("sell item already exist: " + event.getSellItem());
            }
            User user = getUser(event.getUser().getUuid());

            SellItemProcess sellItemProcess = new SellItemProcess(!Bukkit.isPrimaryThread(), user, sellItem);
            Bukkit.getPluginManager().callEvent(sellItemProcess);
            if (sellItemProcess.isCancelled()) {
                event.setValid(false);
                event.setReason(sellItemProcess.getReason());
                return;
            }

            if (sellItem.getItem().getBytes().length > Main.getCfg().getItemMaxSize()) {
                event.setValid(false);
                event.setReason(Lang.getMessage("item-size-limit"));
                return;
            } else if (sellItem.isCompressed()) {
                int size = BLib.getApi().getItemStackSerialize().decompress(sellItem.getItem()).getBytes().length;
                if (size > Main.getCfg().getMaximumUncompressedItemSize()) {
                    event.setValid(false);
                    event.setReason(Lang.getMessage("item-size-limit"));
                    return;
                }
            }

            if (Main.getCfg().getMaxSlots() <= (sellItemsCountByUser(user.uuid) - user.getExternalSlots())) {
                event.setValid(false);
                event.setReason(Lang.getMessage("auction_item_limit_reached"));
                return;
            }
            addSellItem(sellItem);
            event.setValid(true);
            Bukkit.getPluginManager().callEvent(new EventSellItem(!Bukkit.isPrimaryThread(), user, sellItem));
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason(Lang.getMessage("error_occurred"));
        }
    }

    public void validateAndRemoveItem(TakeItemEvent event) {
        update();
        if (!hasUser(event.getUser().getUuid())) {
            throw new IllegalStateException("user non-exist: " + event.getUser());
        }
        if (!hasSellItem(event.getSellItem().id)) {
            event.setValid(false);
            event.setReason(Lang.getMessage("item_already_sold_or_removed"));
            return;
        }
        User user = getUser(event.getUser().getUuid());
        SellItem sellItem = getSellItem(event.getSellItem().id);

        if (!user.getUuid().equals(sellItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason(Lang.getMessage("not_item_owner"));
            return;
        }

        TakeItemProcess event1 = new TakeItemProcess(!Bukkit.isPrimaryThread(), user, sellItem);
        Bukkit.getPluginManager().callEvent(event1);

        if (event1.isCancelled()) {
            event.setValid(false);
            event.setReason(event1.getReason());
            return;
        }
        try {
            removeSellItem(sellItem.id);
            Bukkit.getPluginManager().callEvent(new EventTakeItem(!Bukkit.isPrimaryThread(), user, sellItem));
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason(Lang.getMessage("error_occurred"));
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
            if (!hasUnsoldItem(event.getUnsoldItem().id)) {
                throw new IllegalStateException("unsold item non-exist");
            }
            User user = getUser(event.getUser().getUuid());
            UnsoldItem unsoldItem = getUnsoldItem(event.getUnsoldItem().id);

            TakeUnsoldItemProcess event1 = new TakeUnsoldItemProcess(!Bukkit.isPrimaryThread(), user, unsoldItem);
            Bukkit.getPluginManager().callEvent(event1);

            if (event1.isCancelled()) {
                event.setValid(false);
                event.setReason(event1.getReason());
                return;
            }

            if (!user.getUuid().equals(unsoldItem.getSellerUuid())) {
                event.setValid(false);
                event.setReason(Lang.getMessage("not_item_owner"));
                return;
            } else if (!hasUnsoldItem(unsoldItem.id)) {
                event.setValid(false);
                event.setReason(Lang.getMessage("item_not_found"));
                return;
            }

            removeUnsoldItem(unsoldItem.id);
            event.setValid(true);
            Bukkit.getPluginManager().callEvent(new EventTakeUnsoldItem(!Bukkit.isPrimaryThread(), user, unsoldItem));
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason(Lang.getMessage("error_occurred"));
        }
    }

    public void validateAndRemoveItem(BuyItemEvent event) {
        update();
        if (!hasUser(event.getUser().getUuid())) {
            event.setValid(false);
            event.setReason(Lang.getMessage("error_occurred"));
            return;
        }
        User user = getUser(event.getUser().getUuid());
        SellItem sellItem = getSellItem(event.getSellItem().id);

        if (sellItem == null) {
            event.setValid(false);
            event.setReason(Lang.getMessage("item_no_longer_exists"));
            return;
        }

        if (user.getUuid().equals(sellItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason(Lang.getMessage("item_owner"));
            return;
        }
        BuyItemProcess event1 = new BuyItemProcess(!Bukkit.isPrimaryThread(), user, sellItem);
        Bukkit.getPluginManager().callEvent(event1);

        if (event1.isCancelled()) {
            event.setValid(false);
            event.setReason(event1.getReason());
            return;
        }
        try {
            removeSellItem(sellItem.id);
            if (hasUser(sellItem.getSellerUuid())) {
                User owner = getUser(sellItem.getSellerUuid());
                owner.dealSum += sellItem.getPrice();
                owner.dealCount++;
            }
            user.dealCount++;
            user.dealSum += sellItem.getPrice();
            Bukkit.getPluginManager().callEvent(new EventBuyItem(!Bukkit.isPrimaryThread(), user, sellItem));
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason(Lang.getMessage("error_occurred"));
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
            User buyer = getUser(event.getUser().getUuid());
            SellItem sellItem = getSellItem(event.getSellItem().id);

            if (buyer.getUuid().equals(sellItem.getSellerUuid())) {
                event.setValid(false);
                event.setReason(Lang.getMessage("item_owner"));
                return;
            }

            if (!hasSellItem(sellItem.id)) {
                event.setValid(false);
                event.setReason(Lang.getMessage("item_already_sold_or_removed"));
                return;
            }
            SellItem updated = getSellItem(sellItem.id);

            if (updated.getAmount() < event.getCount()) {
                event.setValid(false);
                event.setReason(Lang.getMessage("quantity_limit_exceeded"));
                return;
            }
            BuyItemCountProcess event1 = new BuyItemCountProcess(!Bukkit.isPrimaryThread(), buyer, sellItem, event.getCount());
            Bukkit.getPluginManager().callEvent(event1);
            if (event1.isCancelled()) {
                event.setValid(false);
                event.setReason(event1.getReason());
                return;
            }

            removeSellItem(sellItem.id);
            buyer.dealCount++;
            buyer.dealSum += updated.priceForOne * event.getCount();
            User owner = getUser(updated.sellerUuid);
            if (owner != null) {
                owner.dealCount++;
                owner.dealSum += updated.priceForOne * event.getCount();
            }
            int newCount = updated.getAmount() - event.getCount();
            ItemStack itemStack = updated.getItemStack();
            itemStack.setAmount(newCount);

            Pair<String, Boolean> item = SellItem.serializeItemStack(itemStack);
            if (newCount != 0) {
                SellItem newItem = SellItem.builder()
                        .item(item.getLeft())
                        .sellerName(updated.sellerName)
                        .sellerUuid(updated.sellerUuid)
                        .price(updated.priceForOne * newCount)
                        .saleByThePiece(updated.saleByThePiece)
                        .tags(updated.tags)
                        .timeListedForSale(updated.timeListedForSale)
                        .removalDate(updated.removalDate)
                        .id(Main.getUniqueIdGenerator().nextId())
                        .material(updated.material)
                        .amount(newCount)
                        .priceForOne(updated.priceForOne)
                        .itemStack(itemStack)
                        .server(updated.server)
                        .compressed(item.getRight())
                        .build();

                addSellItem(newItem);
            }
            Bukkit.getPluginManager().callEvent(new EventBuyItemCount(!Bukkit.isPrimaryThread(), buyer, sellItem, event.getCount()));
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason(Lang.getMessage("error_occurred"));
            return;
        }
        event.setValid(true);
    }

    public void load() throws IOException {
    /*    File home = new File(Main.getInstance().getDataFolder() + "/data");
        if (!home.exists()) {
            home.mkdir();
        }
        List<SellItem> items;
        List<User> users;
        List<UnsoldItem> unsoldItems;

        File fItems = new File(home + "/items.bauc");
        File fUsers = new File(home + "/users.bauc");
        File fUnsoldItems = new File(home + "/unsoldItems.bauc");

        if (fItems.exists()) {
            items = FileUtil.read(fItems, SellItem::fromBytes);
        } else {
            items = new ArrayList<>();
        }
        if (fUsers.exists()) {
            users = FileUtil.read(fUsers, User::fromBytes);
        } else {
            users = new ArrayList<>();
        }
        if (fUnsoldItems.exists()) {
            unsoldItems = FileUtil.read(fUnsoldItems, UnsoldItem::fromBytes);
        } else {
            unsoldItems = new ArrayList<>();
        }

        if (!items.isEmpty() || !users.isEmpty() || !unsoldItems.isEmpty()) {
            load(items, users, unsoldItems);
        }*/
    }

    public void save() throws IOException {
     /*   TimeCounter timeCounter = new TimeCounter();
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

        );*/
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
