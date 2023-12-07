package org.by1337.bauction.db.kernel;

public class MysqlDbOld {/*
        extends FileDataBase {

    private final PacketConnection connection;

    private final String host;
    private final String name;
    private final String user;
    private final String password;
    private final int port;

    private long lastUpdate;
    private long lastDelete;
    private BukkitTask updateTask;
    private UUID server =   UUID.randomUUID();

    public MysqlDbOld(Map<NameKey, Category> categoryMap, Map<NameKey, Sorting> sortingMap, String host, String name, String user, String password, int port) throws SQLException {
        super(categoryMap, sortingMap);
        this.host = host;
        this.name = name;
        this.user = user;
        this.password = password;
        this.port = port;

        connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + name + "?useUnicode=true&characterEncoding=utf8&autoReconnect=true", user, password);


        String[] createTableStatements = {
                "CREATE TABLE IF NOT EXISTS unsold_items ( uuid VARCHAR(36) NOT NULL PRIMARY KEY, seller_uuid VARCHAR(36) NOT NULL, item TEXT NOT NULL, delete_via BIGINT NOT NULL, expired BIGINT NOT NULL)",
                "CREATE TABLE IF NOT EXISTS sell_items ( uuid VARCHAR(36) NOT NULL PRIMARY KEY, seller_uuid VARCHAR(36) NOT NULL, item TEXT NOT NULL, seller_name VARCHAR(50) NOT NULL, price DOUBLE NOT NULL, sale_by_the_piece BOOLEAN NOT NULL, tags TEXT NOT NULL, time_listed_for_sale BIGINT NOT NULL, removal_date BIGINT NOT NULL, material VARCHAR(50) NOT NULL, amount TINYINT NOT NULL, price_for_one DOUBLE NOT NULL, sell_for TEXT NOT NULL)",
                "CREATE TABLE IF NOT EXISTS users ( uuid VARCHAR(36) NOT NULL PRIMARY KEY, name VARCHAR(50) NOT NULL, deal_count INT NOT NULL, deal_sum DOUBLE NOT NULL)",
                "CREATE TABLE IF NOT EXISTS logs ( time BIGINT NOT NULL, type VARCHAR(20) NOT NULL, owner VARCHAR(36) NOT NULL, server VARCHAR(36) NOT NULL, uuid VARCHAR(36), INDEX idx_time (time))"
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

        String query = "SELECT type, owner, uuid, server FROM logs WHERE time > " + lastUpdate;
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
            if (action.getServer().equals(server)) continue;
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
                                    sellItemsMap.put(sellItem.uniqueName, sellItem);
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
                                    unsoldItemsMap.put(unsoldItem.uniqueName, unsoldItem);
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
                            sellItemsMap.remove(sellItem.uniqueName);
                            sortedSellItems.remove(sellItem);
                            sellItemsByOwner.computeIfAbsent(sellItem.sellerUuid, k -> new ArrayList<>()).remove(sellItem);
                            removeIf(i -> i.uniqueName.equals(sellItem.getUniqueName()));
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
                            unsoldItemsMap.remove(unsoldItem.uniqueName);
                            sortedUnsoldItems.remove(unsoldItem);
                            execute(String.format("DELETE FROM unsold_items WHERE uuid = '%s';", unsoldItem.uniqueName));
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
    public CUser createNewAndSave(UUID uuid, String name) {
        return writeLock(() -> {
            CUser user = new CUser(name, uuid);
            users.put(uuid, user);
            execute(user.toSql("users"));
            log(new Action(ActionType.UPDATE_USER, uuid, null, server));
            return user;
        });
    }

    @Override
    protected void addSellItem(CSellItem sellItem) {
        isWriteLock();
        sellItemsMap.put(sellItem.uniqueName, sellItem);
        int insertIndex = Collections.binarySearch(sortedSellItems, sellItem, sellItemComparator);
        if (insertIndex < 0) {
            insertIndex = -insertIndex - 1;
        }
        sortedSellItems.add(insertIndex, sellItem);

        sellItemsByOwner.computeIfAbsent(sellItem.sellerUuid, k -> new ArrayList<>()).add(sellItem);
     //   users.get(sellItem.sellerUuid).itemForSale.add(sellItem.uuid);
        for (Category value : categoryMap.values()) {
            if (TagUtil.matchesCategory(value, sellItem)) {
                sortedItems.get(value.nameKey()).forEach(list -> list.addItem(sellItem));
            }
        }

        CUser user = users.get(sellItem.sellerUuid);
        if (user != null && user.hasChanges()){
            user.updateHash();
            execute(user.toSqlUpdate("users"));
        }

        execute(sellItem.toSql("sell_items"));
        log(new Action(ActionType.ADD_SELL_ITEM, sellItem.sellerUuid, sellItem.uniqueName, server));
        log(new Action(ActionType.UPDATE_USER, sellItem.sellerUuid, null, server));
    }

    @Override
    protected void removeSellItem(CSellItem sellItem) {
        isWriteLock();
        sellItemsMap.remove(sellItem.uniqueName);

        sortedSellItems.remove(sellItem);

        sellItemsByOwner.computeIfAbsent(sellItem.sellerUuid, k -> new ArrayList<>()).remove(sellItem);
       // users.get(sellItem.sellerUuid).itemForSale.remove(sellItem.uuid);
        removeIf(i -> i.uniqueName.equals(sellItem.getUniqueName()));

        CUser user = users.get(sellItem.sellerUuid);
        if (user != null && user.hasChanges()){
            user.updateHash();
            execute(user.toSqlUpdate("users"));
        }

        execute(String.format("DELETE FROM sell_items WHERE uuid = '%s';", sellItem.uniqueName));
        log(new Action(ActionType.REMOVE_SELL_ITEM, sellItem.sellerUuid, sellItem.uniqueName, server));
        log(new Action(ActionType.UPDATE_USER, sellItem.sellerUuid, null, server));
    }

    @Override
    protected void addUnsoldItem(CUnsoldItem unsoldItem) {
        isWriteLock();
      //  CUser user = users.get(unsoldItem.sellerUuid);
        unsoldItemsMap.put(unsoldItem.uniqueName, unsoldItem);
        int insertIndex = Collections.binarySearch(sortedUnsoldItems, unsoldItem, unsoldItemComparator);
        if (insertIndex < 0) {
            insertIndex = -insertIndex - 1;
        }
        sortedUnsoldItems.add(insertIndex, unsoldItem);

        unsoldItemsByOwner.computeIfAbsent(unsoldItem.sellerUuid, k -> new ArrayList<>()).add(unsoldItem);
      //  user.unsoldItems.add(unsoldItem.uuid);

        CUser user = users.get(unsoldItem.sellerUuid);
        if (user != null && user.hasChanges()){
            user.updateHash();
            execute(user.toSqlUpdate("users"));
        }
        execute(unsoldItem.toSql("unsold_items"));
        log(new Action(ActionType.ADD_UNSOLD_ITEM, unsoldItem.sellerUuid, unsoldItem.uniqueName, server));
        log(new Action(ActionType.UPDATE_USER, unsoldItem.sellerUuid, null, server));
    }

    @Override
    protected void removeUnsoldItem(CUnsoldItem unsoldItem) {
        isWriteLock();
        unsoldItemsMap.remove(unsoldItem.uniqueName);
        sortedUnsoldItems.remove(unsoldItem);

        execute(String.format("DELETE FROM unsold_items WHERE uuid = '%s';", unsoldItem.uniqueName));
        unsoldItemsByOwner.computeIfAbsent(unsoldItem.sellerUuid, k -> new ArrayList<>()).remove(unsoldItem);

        CUser user = users.get(unsoldItem.sellerUuid);
        if (user != null && user.hasChanges()){
            user.updateHash();
            execute(user.toSqlUpdate("users"));
        }
        log(new Action(ActionType.REMOVE_UNSOLD_ITEM, user.uuid, unsoldItem.uniqueName, server));
        log(new Action(ActionType.UPDATE_USER, user.uuid, null, server));
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

                TimeCounter timeCounter = new TimeCounter();
                Message message = Main.getMessage();
                message.logger("[DB] load items fom mysql # step 1");

                List<CSellItem> items = parseSellItems();
                List<CUser> users = parseUsers();
                List<CUnsoldItem> unsoldItems = parseUnsoldItems();

                message.logger("[DB] # step 1 completed in %s ms.", timeCounter.getTime());
                timeCounter.reset();

                message.logger("[DB] init users # step 2");
                users.forEach(user -> {
                    user.updateHash();
                    this.users.put(user.getUuid(), user);
                });


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

                        //    CUser user = this.users.get(item.sellerUuid);
                        //    user.itemForSale.remove(item.uuid);
                       //     user.unsoldItems.add(unsoldItem.uuid);

                            sqls.add(String.format("DELETE FROM sell_items WHERE uuid = '%s';", item.uniqueName));
                         //   toUpdate.add(user);
                            //execute(String.format("DELETE FROM sell_items WHERE uuid = '%s';", item.uuid));
                            // execute(user.toSqlUpdate("users"));
                            toLog.add(new Action(ActionType.REMOVE_SELL_ITEM, item.sellerUuid, item.uniqueName, server));
                            toLog.add(new Action(ActionType.ADD_UNSOLD_ITEM, item.sellerUuid, unsoldItem.uniqueName, server));
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
                         //   user.unsoldItems.remove(item.uuid);

                         //   toUpdate.add(user);
                         //   execute(user.toSqlUpdate("users"));
                            toLog.add(new Action(ActionType.REMOVE_UNSOLD_ITEM, user.uuid, item.uniqueName, server));
                         //   toLog.add(new Action(ActionType.UPDATE_USER, user.uuid, null));

                            return true;
                        }
                        return false;
                    });
                }
                unsoldItems.addAll(toAdd);

                message.logger("[DB] # step 3 completed in %s ms.", timeCounter.getTime());
                timeCounter.reset();

                message.logger("[DB] validate users # step 4");

                message.logger("[DB] # step 4 completed in %s ms.", timeCounter.getTime());
                timeCounter.reset();

                message.logger("[DB] final initialization # step 5");
                items.forEach(item -> {
                    sellItemsMap.put(item.uniqueName, item);
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
                    unsoldItemsMap.put(unsoldItem.uniqueName, unsoldItem);
                    sortedUnsoldItems.add(unsoldItem);
                    unsoldItemsByOwner.computeIfAbsent(unsoldItem.sellerUuid, k -> new ArrayList<>()).add(unsoldItem);
                });
                message.logger("[DB] # step 5 completed in %s ms.", timeCounter.getTime());
                timeCounter.reset();

                message.logger("[DB] validate users 2 # step 6");

                message.logger("[DB] # step 6 completed in %s ms.", timeCounter.getTime());

                if (!sqls.isEmpty()) {
                    message.logger("[DB] detected updates # sqls: %s", sqls.size());
                    timeCounter.reset();
                    for (String sql : sqls) {
                        execute(sql);
                    }
                    message.logger("[DB] detected updates # sqls completed in %s ms.", timeCounter.getTime());
                }

//                if (!toUpdate.isEmpty()) {
//                    message.logger("[DB] detected updates # users: %s", sqls.size());
//                    timeCounter.reset();
//                    for (CUser user : toUpdate) {
//                        execute(user.toSqlUpdate("users"));
//                    }
//                    message.logger("[DB] detected updates # users completed in %s ms.", timeCounter.getTime());
//                }

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
    }*/
}
