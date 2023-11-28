package org.by1337.bauction.db.kernel;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.by1337.api.chat.util.Message;
import org.by1337.api.util.NameKey;
import org.by1337.bauction.Main;
import org.by1337.bauction.auc.SellItem;
import org.by1337.bauction.auc.User;
import org.by1337.bauction.db.action.Action;
import org.by1337.bauction.db.action.ActionType;
import org.by1337.bauction.db.event.SellItemEvent;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.Sorting;
import org.by1337.bauction.util.TagUtil;
import org.by1337.bauction.util.TimeCounter;

import java.sql.*;
import java.util.*;

public class MysqlDb extends JsonDBCore {

    private final Connection connection;

    private final String host;
    private final String name;
    private final String user;
    private final String password;
    private final int port;

    private long lastUpdate;
    private long lastDelete;
    private BukkitTask updateTask;

    public MysqlDb(Map<NameKey, Category> categoryMap, Map<NameKey, Sorting> sortingMap, String host, String name, String user, String password, int port) throws SQLException {
        super(categoryMap, sortingMap);
        this.host = host;
        this.name = name;
        this.user = user;
        this.password = password;
        this.port = port;

        connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + name + "?useUnicode=true&characterEncoding=utf8&autoReconnect=true", user, password);


        String[] createTableStatements = {
                "CREATE TABLE IF NOT EXISTS unsold_items (uuid VARCHAR(36) NOT NULL PRIMARY KEY,seller_uuid VARCHAR(36) NOT NULL, item TEXT NOT NULL,delete_via BIGINT NOT NULL,expired BIGINT NOT NULL)",
                "CREATE TABLE IF NOT EXISTS sell_items (uuid VARCHAR(36) NOT NULL PRIMARY KEY,seller_uuid VARCHAR(36) NOT NULL,item TEXT NOT NULL,seller_name VARCHAR(50) NOT NULL,price DOUBLE NOT NULL,sale_by_the_piece BOOLEAN NOT NULL,tags TEXT NOT NULL,time_listed_for_sale BIGINT NOT NULL,removal_date BIGINT NOT NULL,material VARCHAR(50) NOT NULL,amount TINYINT NOT NULL,price_for_one DOUBLE NOT NULL,sell_for TEXT NOT NULL)",
                "CREATE TABLE IF NOT EXISTS users (uuid VARCHAR(36) NOT NULL PRIMARY KEY,name VARCHAR(50) NOT NULL,unsold_items TEXT NOT NULL,item_for_sale TEXT NOT NULL,deal_count INT NOT NULL,deal_sum DOUBLE NOT NULL)",
                "CREATE TABLE IF NOT EXISTS logs (time BIGINT NOT NULL,type VARCHAR(20) NOT NULL,owner VARCHAR(36) NOT NULL,uuid VARCHAR(36),INDEX idx_time (time))"
        };

        for (String sql : createTableStatements) {
            try (PreparedStatement stat = connection.prepareStatement(sql)) {
                stat.execute();
            }
        }
    }

    private void setUpdateTask() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                long x = System.currentTimeMillis();
                if (x - lastUpdate > 500) {
                    update();
                }
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 2, 2);
    }

    @Override
    protected void update() {
        List<Action> actions = new ArrayList<>();

        String query = "SELECT type, owner, uuid FROM logs WHERE time > " + lastUpdate;
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                actions.add(Action.fromResultSet(resultSet));
            }
        } catch (SQLException e) {
            Main.getMessage().error(e);
        }

        if ((System.currentTimeMillis() - lastDelete) > 3600000L) {
            lastDelete = System.currentTimeMillis();
            execute("DELETE FROM logs WHERE time < " + (System.currentTimeMillis() - (3600000L * 2)));
        }
        lastUpdate = System.currentTimeMillis();

        for (Action action : actions) {
            try {
                switch (action.getType()) {
                    case ADD_SELL_ITEM -> {
                        if (readLock(() -> sellItemsMap.containsKey(action.getItem()))) {
                            break;
                        }
                        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM sell_items WHERE uuid = '%s'", action.getItem()));
                             ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (resultSet.next()) {
                                CSellItem sellItem = CSellItem.fromResultSet(resultSet);
                                writeLock(() -> {
                                    sellItemsMap.put(sellItem.uuid, sellItem);
                                    int insertIndex = Collections.binarySearch(sortedSellItems, sellItem, sellItemComparator);
                                    if (insertIndex < 0) {
                                        insertIndex = -insertIndex - 1;
                                    }
                                    sortedSellItems.add(insertIndex, sellItem);

                                    sellItemsByOwner.computeIfAbsent(sellItem.sellerUuid, k -> new ArrayList<>()).add(sellItem);
                                    //users.get(sellItem.sellerUuid).itemForSale.add(sellItem.uuid);
                                    for (Category value : categoryMap.values()) {
                                        if (TagUtil.matchesCategory(value, sellItem)) {
                                            sortedItems.get(value.nameKey()).forEach(list -> list.addItem(sellItem));
                                        }
                                    }
                                    return null;
                                });
                            }
                        }
                    }
                    case UPDATE_USER -> {
                        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM users WHERE uuid = '%s'", action.getOwner()));
                             ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (resultSet.next()) {
                                CUser user = CUser.fromResultSet(resultSet);
                                writeLock(() -> {
                                    users.put(user.uuid, user);
                                    return null;
                                });
                            }
                        }
                    }

                    case ADD_UNSOLD_ITEM -> {
                        if (readLock(() -> unsoldItemsMap.containsKey(action.getItem()))) {
                            break;
                        }
                        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM unsold_items WHERE uuid = '%s'", action.getItem()));
                             ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (resultSet.next()) {
                                CUnsoldItem unsoldItem = CUnsoldItem.fromResultSet(resultSet);
                                writeLock(() -> {
                                    unsoldItemsMap.put(unsoldItem.uuid, unsoldItem);
                                    int insertIndex = Collections.binarySearch(sortedUnsoldItems, unsoldItem, unsoldItemComparator);
                                    if (insertIndex < 0) {
                                        insertIndex = -insertIndex - 1;
                                    }
                                    sortedUnsoldItems.add(insertIndex, unsoldItem);
                                    unsoldItemsByOwner.computeIfAbsent(unsoldItem.sellerUuid, k -> new ArrayList<>()).add(unsoldItem);
                                    return null;
                                });
                            }
                        }

                    }
                    case REMOVE_SELL_ITEM -> {
                        if (!readLock(() -> sellItemsMap.containsKey(action.getItem()))) {
                            break;
                        }
                        writeLock(() -> {
                            CSellItem sellItem = sellItemsMap.get(action.getItem());
                            if (sellItem == null) {
                                return null;
                            }
                            sellItemsMap.remove(sellItem.uuid);
                            sortedSellItems.remove(sellItem);
                            sellItemsByOwner.computeIfAbsent(sellItem.sellerUuid, k -> new ArrayList<>()).remove(sellItem);
                            removeIf(i -> i.uuid.equals(sellItem.getUuid()));
                            return null;
                        });

                    }
                    case REMOVE_UNSOLD_ITEM -> {
                        if (!readLock(() -> unsoldItemsMap.containsKey(action.getItem()))) {
                            break;
                        }
                        writeLock(() -> {
                            CUnsoldItem unsoldItem = unsoldItemsMap.get(action.getItem());
                            if (unsoldItem == null) {
                                return null;
                            }
                            unsoldItemsMap.remove(unsoldItem.uuid);
                            sortedUnsoldItems.remove(unsoldItem);
                            execute(String.format("DELETE FROM unsold_items WHERE uuid = '%s';", unsoldItem.uuid));
                            unsoldItemsByOwner.computeIfAbsent(unsoldItem.sellerUuid, k -> new ArrayList<>()).remove(unsoldItem);
                            return null;
                        });
                    }
                }
            } catch (Exception e) {
                Main.getMessage().error(e);

            }
        }
    }

    @Override
    public void validateAndAddItem(SellItemEvent event) {
        super.validateAndAddItem(event);
//        CUser user = getUser(event.getUser().getUuid());
//        if (user.itemForSale.size() >= 1500) { // todo remove limit
//            event.setValid(false);
//            event.setReason(Lang.getMessages("mysql_limit_items"));
//        } else {
//            super.validateAndAddItem(event);
//        }
    }

    @Override
    public CUser createNewAndSave(UUID uuid, String name) {
        return writeLock(() -> {
            CUser user = new CUser(name, uuid);
            users.put(uuid, user);
            execute(user.toSql("users"));
            log(new Action(ActionType.UPDATE_USER, uuid, null));
            return user;
        });
    }

    @Override
    protected void addSellItem(CSellItem sellItem) {
        isWriteLock();
        sellItemsMap.put(sellItem.uuid, sellItem);
        int insertIndex = Collections.binarySearch(sortedSellItems, sellItem, sellItemComparator);
        if (insertIndex < 0) {
            insertIndex = -insertIndex - 1;
        }
        sortedSellItems.add(insertIndex, sellItem);

        sellItemsByOwner.computeIfAbsent(sellItem.sellerUuid, k -> new ArrayList<>()).add(sellItem);
        users.get(sellItem.sellerUuid).itemForSale.add(sellItem.uuid);
        for (Category value : categoryMap.values()) {
            if (TagUtil.matchesCategory(value, sellItem)) {
                sortedItems.get(value.nameKey()).forEach(list -> list.addItem(sellItem));
            }
        }

        execute(sellItem.toSql("sell_items"));
        execute(users.get(sellItem.sellerUuid).toSqlUpdate("users"));
        log(new Action(ActionType.ADD_SELL_ITEM, sellItem.sellerUuid, sellItem.uuid));
        log(new Action(ActionType.UPDATE_USER, sellItem.sellerUuid, null));
    }

    @Override
    protected void removeSellItem(CSellItem sellItem) {
        isWriteLock();
        sellItemsMap.remove(sellItem.uuid);

        sortedSellItems.remove(sellItem);

        sellItemsByOwner.computeIfAbsent(sellItem.sellerUuid, k -> new ArrayList<>()).remove(sellItem);
        users.get(sellItem.sellerUuid).itemForSale.remove(sellItem.uuid);
        removeIf(i -> i.uuid.equals(sellItem.getUuid()));

        execute(String.format("DELETE FROM sell_items WHERE uuid = '%s';", sellItem.uuid));
        execute(users.get(sellItem.sellerUuid).toSqlUpdate("users"));
        log(new Action(ActionType.REMOVE_SELL_ITEM, sellItem.sellerUuid, sellItem.uuid));
        log(new Action(ActionType.UPDATE_USER, sellItem.sellerUuid, null));
    }

    @Override
    protected void addUnsoldItem(CUnsoldItem unsoldItem) {
        isWriteLock();
        CUser user = users.get(unsoldItem.sellerUuid);
        if (user.unsoldItems.size() >= 1500) {
            Main.getMessage().error("The user %s has %s unsold items with a limit of 1500!", user.uuid, user.unsoldItems.size());
            return;
        }
        unsoldItemsMap.put(unsoldItem.uuid, unsoldItem);
        int insertIndex = Collections.binarySearch(sortedUnsoldItems, unsoldItem, unsoldItemComparator);
        if (insertIndex < 0) {
            insertIndex = -insertIndex - 1;
        }
        sortedUnsoldItems.add(insertIndex, unsoldItem);

        unsoldItemsByOwner.computeIfAbsent(unsoldItem.sellerUuid, k -> new ArrayList<>()).add(unsoldItem);
        user.unsoldItems.add(unsoldItem.uuid);

        execute(user.toSqlUpdate("users"));
        execute(unsoldItem.toSql("unsold_items"));
        log(new Action(ActionType.ADD_UNSOLD_ITEM, unsoldItem.sellerUuid, unsoldItem.uuid));
        log(new Action(ActionType.UPDATE_USER, unsoldItem.sellerUuid, null));
    }

    @Override
    protected void removeUnsoldItem(CUnsoldItem unsoldItem) {
        isWriteLock();
        unsoldItemsMap.remove(unsoldItem.uuid);
        sortedUnsoldItems.remove(unsoldItem);

        execute(String.format("DELETE FROM unsold_items WHERE uuid = '%s';", unsoldItem.uuid));
        unsoldItemsByOwner.computeIfAbsent(unsoldItem.sellerUuid, k -> new ArrayList<>()).remove(unsoldItem);
        CUser user = users.get(unsoldItem.sellerUuid);
        user.unsoldItems.remove(unsoldItem.uuid);

        execute(user.toSqlUpdate("users"));
        log(new Action(ActionType.REMOVE_UNSOLD_ITEM, user.uuid, unsoldItem.uuid));
        log(new Action(ActionType.UPDATE_USER, user.uuid, null));
    }

    protected void execute(String sql) {
        try (PreparedStatement stat = connection.prepareStatement(sql)) {
            stat.execute();
        } catch (SQLException e) {
            Main.getMessage().error(e);
        }
    }

    protected void log(Action action) {
        String sql = action.toSql("logs");
        try (PreparedStatement stat = connection.prepareStatement(sql)) {
            stat.execute();
        } catch (SQLException e) {
            Main.getMessage().error(e);
        }
    }

    private List<CSellItem> parseSellItems() {
        List<CSellItem> sellItems = new ArrayList<>();

        String query = "SELECT * FROM sell_items";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                sellItems.add(CSellItem.fromResultSet(resultSet));
            }
        } catch (SQLException e) {
            Main.getMessage().error(e);
        }
        return sellItems;
    }

    private List<CUser> parseUsers() {
        List<CUser> userList = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                userList.add(CUser.fromResultSet(resultSet));
            }
        } catch (SQLException e) {
            Main.getMessage().error(e);
        }
        return userList;
    }

    private List<CUnsoldItem> parseUnsoldItems() {
        List<CUnsoldItem> userList = new ArrayList<>();
        String query = "SELECT * FROM unsold_items";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                userList.add(CUnsoldItem.fromResultSet(resultSet));
            }
        } catch (SQLException e) {
            Main.getMessage().error(e);
        }
        return userList;
    }


    @Override
    public void load() {
        try {
            writeLock(() -> {
                lastUpdate = System.currentTimeMillis();
                Set<String> sqls = new HashSet<>();
                Set<Action> toLog = new HashSet<>();
                Set<CUser> toUpdate = new HashSet<>();

                TimeCounter timeCounter = new TimeCounter();
                Message message = Main.getMessage();
                message.logger("[DB] load items fom mysql # step 1");

                List<CSellItem> items = parseSellItems();
                List<CUser> users = parseUsers();
                List<CUnsoldItem> unsoldItems = parseUnsoldItems();

                message.logger("[DB] # step 1 completed in %s ms.", timeCounter.getTime());
                timeCounter.reset();

                message.logger("[DB] init users # step 2");
                users.forEach(user -> this.users.put(user.getUuid(), user));


                message.logger("[DB] # step 2 completed in %s ms.", timeCounter.getTime());
                timeCounter.reset();

                message.logger("[DB] validate items and unsold items # step 3");
                List<CUnsoldItem> toAdd = new ArrayList<>();
                long time = System.currentTimeMillis();
                {
                    items.removeIf(item -> {
                        if (item.removalDate <= time) {
                            CUnsoldItem unsoldItem = new CUnsoldItem(item.item, item.sellerUuid, item.removalDate, item.removalDate + removeTime);
                            toAdd.add(unsoldItem);

                            CUser user = this.users.get(item.sellerUuid);
                            user.itemForSale.remove(item.uuid);
                            user.unsoldItems.add(unsoldItem.uuid);

                            sqls.add(String.format("DELETE FROM sell_items WHERE uuid = '%s';", item.uuid));
                            toUpdate.add(user);
                            //execute(String.format("DELETE FROM sell_items WHERE uuid = '%s';", item.uuid));
                            // execute(user.toSqlUpdate("users"));
                            toLog.add(new Action(ActionType.REMOVE_SELL_ITEM, item.sellerUuid, item.uuid));
                            toLog.add(new Action(ActionType.ADD_UNSOLD_ITEM, item.sellerUuid, unsoldItem.uuid));
                            //   log(new Action(ActionType.UPDATE_USER, item.sellerUuid, null));

                            return true;
                        }
                        return false;
                    });
                }
                {
                    unsoldItems.removeIf(item -> {
                        if (item.deleteVia <= removeTime) {
                            CUser user = this.users.get(item.sellerUuid);
                            user.unsoldItems.remove(item.uuid);

                            toUpdate.add(user);
                            execute(user.toSqlUpdate("users"));
                            toLog.add(new Action(ActionType.REMOVE_UNSOLD_ITEM, user.uuid, item.uuid));
                            toLog.add(new Action(ActionType.UPDATE_USER, user.uuid, null));

                            return true;
                        }
                        return false;
                    });
                }
                unsoldItems.addAll(toAdd);

                message.logger("[DB] # step 3 completed in %s ms.", timeCounter.getTime());
                timeCounter.reset();

                message.logger("[DB] validate users # step 4");
                { // validate 1

                    items.forEach(item -> {
                        CUser user = this.users.get(item.sellerUuid);
                        if (!user.itemForSale.contains(item.uuid)) {
                            Main.getMessage().error("user %s has no item %s!", user.uuid, item.uuid);
                            user.itemForSale.add(item.uuid);

                            toUpdate.add(user);
                            // execute(user.toSqlUpdate("users"));
                            toLog.add(new Action(ActionType.UPDATE_USER, user.uuid, null));
                        }
                    });

                    unsoldItems.forEach(item -> {
                        CUser user = this.users.get(item.sellerUuid);
                        if (!user.unsoldItems.contains(item.uuid)) {
                            Main.getMessage().error("user %s has no unsold item %s!", user.uuid, item.uuid);
                            user.unsoldItems.add(item.uuid);

                            toUpdate.add(user);
                            //  execute(user.toSqlUpdate("users"));
                            toLog.add(new Action(ActionType.UPDATE_USER, user.uuid, null));
                        }
                    });
                }

                message.logger("[DB] # step 4 completed in %s ms.", timeCounter.getTime());
                timeCounter.reset();

                message.logger("[DB] final initialization # step 5");
                items.forEach(item -> {
                    sellItemsMap.put(item.uuid, item);
                    sortedSellItems.add(item);
                    sellItemsByOwner.computeIfAbsent(item.sellerUuid, k -> new ArrayList<>()).add(item);

                    categoryMap.values().forEach(value -> {
                        if (TagUtil.matchesCategory(value, item)) {
                            sortedItems.get(value.nameKey()).forEach(list -> list.addItem(item));
                        }
                    });
                });
                sortedSellItems.sort(sellItemComparator);
                unsoldItems.forEach(unsoldItem -> {
                    unsoldItemsMap.put(unsoldItem.uuid, unsoldItem);
                    sortedUnsoldItems.add(unsoldItem);
                    unsoldItemsByOwner.computeIfAbsent(unsoldItem.sellerUuid, k -> new ArrayList<>()).add(unsoldItem);
                });
                message.logger("[DB] # step 5 completed in %s ms.", timeCounter.getTime());
                timeCounter.reset();

                message.logger("[DB] validate users 2 # step 6");
                this.users.values().forEach(user -> {
                    user.unsoldItems.removeIf(uuid -> {
                        if (!unsoldItemsMap.containsKey(uuid)) {
                            message.error("user %s has non-existent item %s", user.uuid, uuid);
                            toUpdate.add(user);
                            return true;
                        }
                        return false;
                    });

                    user.itemForSale.removeIf(uuid -> {
                        if (!sellItemsMap.containsKey(uuid)) {
                            message.error("user %s has non-existent item %s", user.uuid, uuid);
                            toUpdate.add(user);
                            return true;
                        }
                        return false;
                    });
                });

                message.logger("[DB] # step 6 completed in %s ms.", timeCounter.getTime());

                if (!sqls.isEmpty()) {
                    message.logger("[DB] detected updates # sqls: %s", sqls.size());
                    timeCounter.reset();
                    for (String sql : sqls) {
                        execute(sql);
                    }
                    message.logger("[DB] detected updates # sqls completed in %s ms.", timeCounter.getTime());
                }

                if (!toUpdate.isEmpty()) {
                    message.logger("[DB] detected updates # users: %s", sqls.size());
                    timeCounter.reset();
                    for (CUser user : toUpdate) {
                        execute(user.toSqlUpdate("users"));
                    }
                    message.logger("[DB] detected updates # users completed in %s ms.", timeCounter.getTime());
                }

                if (!toLog.isEmpty()) {
                    message.logger("[DB] detected updates # logs: %s", toLog.size());
                    timeCounter.reset();
                    for (Action action : toLog) {
                        log(action);
                    }
                    message.logger("[DB] detected updates # logs completed in %s ms.", timeCounter.getTime());
                }

                message.logger("[DB] total time %s ms.", timeCounter.getTotalTime());
                setUpdateTask();
                return null;
            });
        } catch (Exception e) {
            Main.getMessage().error(e);
        }
    }

    @Override
    public void save() {
        try {
            connection.close();
        } catch (SQLException e) {
            Main.getMessage().error(e);
        }
    }
}
