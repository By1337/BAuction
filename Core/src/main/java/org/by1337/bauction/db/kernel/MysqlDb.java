
package org.by1337.bauction.db.kernel;

import org.by1337.bauction.network.Packet;
import org.by1337.bauction.network.PacketConnection;
import org.by1337.bauction.network.PacketListener;
import org.by1337.bauction.util.auction.Category;
import org.by1337.bauction.util.auction.Sorting;
import org.by1337.bauction.util.player.MoneyGiver;
import org.by1337.blib.util.NameKey;

import java.util.Map;

public class MysqlDb extends FileDataBase implements PacketListener {
    public MysqlDb(Map<NameKey, Category> categoryMap, Map<NameKey, Sorting> sortingMap) {
        super(categoryMap, sortingMap);
    }

    @Override
    public void update(Packet packetIn) {

    }

    @Override
    public void connectionLost() {

    }

    @Override
    public void connectionRestored() {

    }
    public PacketConnection getPacketConnection() {
        return null;
    }

    public MoneyGiver getMoneyGiver() {
        return null;
    }
    public void addSqlToQueue(String sql) {
    }

    public void addSqlToQueue(String sql, Object... objects) {
    }
/*
    private final Connection connection;
    private final PacketConnection packetConnection;
    private final UUID server = UUID.randomUUID();
    private final ConcurrentLinkedQueue<String> sqlQueue = new ConcurrentLinkedQueue<>();
    private final BukkitTask sqlExecuteTask;
    @Nullable
    private final BukkitTask logClearTask;
    private final BukkitTask updateTask;
    private final boolean isHead;
    private long lastLogCheck;
    private final MoneyGiver moneyGiver;

    public MysqlDb(Map<NameKey, Category> categoryMap, Map<NameKey, Sorting> sortingMap, DbCfg dbCfg) throws SQLException {
        super(categoryMap, sortingMap);
        isHead = dbCfg.isHead();
        packetConnection = new PacketConnection(this);
        moneyGiver = new MoneyGiver(this);
        connection = DriverManager.getConnection(
                "jdbc:mysql://" + dbCfg.getHost() + ":" + dbCfg.getPort() + "/" + dbCfg.getDbName() + "?useUnicode=true&characterEncoding=utf8&autoReconnect=true",
                dbCfg.getUser(), dbCfg.getPassword()
        );

        String[] createTableStatements = {
                //<editor-fold desc="create tables sqls" defaultstate="collapsed">
                """
CREATE TABLE IF NOT EXISTS give_money (server VARBINARY(36) NOT NULL, uuid VARCHAR(36) NOT NULL, count DOUBLE NOT NULL)
""",
                """
CREATE TABLE IF NOT EXISTS unsold_items (
  uuid VARBINARY(36) NOT NULL PRIMARY KEY,
  seller_uuid VARCHAR(36) NOT NULL,
  item TEXT NOT NULL,
  delete_via BIGINT NOT NULL,
  expired BIGINT NOT NULL,
  compressed BOOLEAN NOT NULL
)
""",
                """
CREATE TABLE IF NOT EXISTS sell_items (
  uuid VARBINARY(36) NOT NULL PRIMARY KEY,
  seller_uuid VARCHAR(36) NOT NULL,
  item TEXT NOT NULL,
  seller_name VARCHAR(50) NOT NULL,
  price DOUBLE NOT NULL,
  sale_by_the_piece BOOLEAN NOT NULL,
  tags TEXT NOT NULL,
  time_listed_for_sale BIGINT NOT NULL,
  removal_date BIGINT NOT NULL,
  material VARCHAR(50) NOT NULL,
  amount TINYINT NOT NULL,
  price_for_one DOUBLE NOT NULL,
  sell_for TEXT NOT NULL,
  server VARBINARY(36) NOT NULL,
  compressed BOOLEAN NOT NULL
)
""",
                """
CREATE TABLE IF NOT EXISTS users (
  uuid VARBINARY(36) NOT NULL PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  deal_count INT NOT NULL,
  deal_sum DOUBLE NOT NULL
)
""",
                """
CREATE TABLE IF NOT EXISTS logs (
  time BIGINT NOT NULL,
  type VARCHAR(20) NOT NULL,
  owner VARCHAR(36) NOT NULL,
  server VARCHAR(36) NOT NULL,
  uuid VARCHAR(36),
  INDEX idx_time (time)
)
"""
                //</editor-fold>
        };
        for (String sql : createTableStatements) {
            try (PreparedStatement stat = connection.prepareStatement(sql.replace("\n", ""))) {
                stat.execute();
            }
        }

        sqlExecuteTask = //<editor-fold desc="sql execute task" defaultstate="collapsed">
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        String sql = null;
                        for (int i = 0; i < 200; i++) {
                            sql = sqlQueue.poll();
                            if (sql == null) {
                                break;
                            }
                            try (PreparedStatement stat = connection.prepareStatement(sql)) {

                                stat.execute();
                            } catch (SQLException e) {
                                Main.getMessage().error(sql, e);
                            }
                        }
                        if (sql != null) {
                            Main.getMessage().warning("the number of sql requests is more than 200!");
                        }
                    }

                }.runTaskTimerAsynchronously(Main.getInstance(), 10, 1);
        //</editor-fold>


        if (isHead) {
            logClearTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                    Main.getInstance(),
                    () -> sqlQueue.offer("DELETE FROM logs WHERE time < " + (System.currentTimeMillis() - 3600000L / 2)),
                    500, 3600000L / 2
            );
        } else {
            logClearTask = null;
        }
        updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> update(), 40, 40);
    }

    @Override
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
                            if (isHead) {
                                removeUnsoldItem(unsoldItem.getUniqueName());
                            } else {
                                super.removeUnsoldItem(unsoldItem.getUniqueName());
                            }
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

    @Override
    protected void expiredItem(SellItem item) {
        if (isHead) {
            super.expiredItem(item);
        } else {
            super.removeSellItem(item.getUniqueName());
        }
    }

    @Override
    public void addSellItem(@NotNull SellItem sellItem) {
        super.addSellItem(sellItem);
        execute(sellItem.toSql("sell_items"));
        log(new Action(ActionType.ADD_SELL_ITEM, sellItem.getSellerUuid(), sellItem.getUniqueName(), server));
        packetConnection.saveSend(new PacketAddSellItem(sellItem));
    }

    @Override
    protected void replaceUser(User user) {
        super.replaceUser(user);
        execute(user.toSqlUpdate("users"));
        log(new Action(ActionType.UPDATE_USER, user.getUuid(), null, server));
        packetConnection.saveSend(new PacketUpdateUser(user));
    }

    @Override
    protected User addUser(User user) {
        super.addUser(user);
        execute(user.toSql("users"));
        log(new Action(ActionType.UPDATE_USER, user.getUuid(), null, server));
        packetConnection.saveSend(new PacketUpdateUser(user));
        return user;
    }

    private void updateUsers(UUID user, UUID user1) {
        User buyer = getUser(user);
        User owner = getUser(user1);

        if (buyer != null) {
            execute(buyer.toSqlUpdate("users"));
            log(new Action(ActionType.UPDATE_USER, buyer.getUuid(), null, server));
            packetConnection.saveSend(new PacketUpdateUser(buyer));
        }

        if (owner != null) {
            execute(owner.toSqlUpdate("users"));
            log(new Action(ActionType.UPDATE_USER, owner.getUuid(), null, server));
            packetConnection.saveSend(new PacketUpdateUser(owner));
        }
    }

    @Override
    public void validateAndRemoveItem(BuyItemEvent event) {// hook
        super.validateAndRemoveItem(event);
        if (event.isValid()) {
            updateUsers(event.getUser().getUuid(), event.getSellItem().getSellerUuid());
        }
    }

    @Override
    public void validateAndRemoveItem(BuyItemCountEvent event) { // hook
        super.validateAndRemoveItem(event);
        if (event.isValid()) {
            updateUsers(event.getUser().getUuid(), event.getSellItem().getSellerUuid());
        }
    }

    @Override
    public UnsoldItem removeUnsoldItem(UniqueName name) {
        UnsoldItem unsoldItem = super.removeUnsoldItem(name);
        execute("DELETE FROM unsold_items WHERE uuid = '%s';", name.getKey());
        log(new Action(ActionType.REMOVE_UNSOLD_ITEM, unsoldItem.getSellerUuid(), name, server));
        packetConnection.saveSend(new PacketRemoveUnsoldItem(name));
        return unsoldItem;
    }

    @Override
    public void addUnsoldItem(UnsoldItem unsoldItem) {
        super.addUnsoldItem(unsoldItem);
        execute(unsoldItem.toSql("unsold_items"));
        log(new Action(ActionType.ADD_UNSOLD_ITEM, unsoldItem.getSellerUuid(), unsoldItem.uniqueName, server));
        packetConnection.saveSend(new PacketAddUnsoldItem(unsoldItem));
    }

    @Override
    public SellItem removeSellItem(UniqueName name) {
        SellItem item = super.removeSellItem(name);
        execute("DELETE FROM sell_items WHERE uuid = '%s';", name.getKey());
        log(new Action(ActionType.REMOVE_SELL_ITEM, item.getSellerUuid(), item.getUniqueName(), server));
        packetConnection.saveSend(new PacketRemoveSellItem(name));
        return item;
    }

    protected void execute(String sql) {
        sqlQueue.offer(sql);
    }

    protected void execute(String sql, Object... objects) {
        execute(String.format(sql, objects));
    }

    public void addSqlToQueue(String sql) {
        execute(sql);
    }

    public void addSqlToQueue(String sql, Object... objects) {
        execute(sql, objects);
    }

    protected void log(Action action) {
        sqlQueue.offer(action.toSql("logs"));
    }

    @Override
    public void load() {
        writeLock(() -> {
            List<SellItem> items = parseSellItems();
            List<User> users = parseUsers();
            List<UnsoldItem> unsoldItems = parseUnsoldItems();
            lastLogCheck = System.currentTimeMillis();

            if (!items.isEmpty() || !users.isEmpty() || !unsoldItems.isEmpty()) {
                load(items, users, unsoldItems);
            }
        });
    }

    private List<SellItem> parseSellItems() {
        List<SellItem> sellItems = new ArrayList<>();

        String query = "SELECT * FROM sell_items";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                sellItems.add(SellItem.fromResultSet(resultSet));
            }
        } catch (SQLException e) {
            Main.getMessage().error(e);
        }
        return sellItems;
    }

    private List<User> parseUsers() {
        List<User> userList = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                userList.add(User.fromResultSet(resultSet));
            }
        } catch (SQLException e) {
            Main.getMessage().error(e);
        }
        return userList;
    }

    private List<UnsoldItem> parseUnsoldItems() {
        List<UnsoldItem> userList = new ArrayList<>();
        String query = "SELECT * FROM unsold_items";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                userList.add(UnsoldItem.fromResultSet(resultSet));
            }
        } catch (SQLException e) {
            Main.getMessage().error(e);
        }
        return userList;
    }

    private List<Action> parseLogs() {
        List<Action> actions = new ArrayList<>();
        String query = String.format("SELECT type, owner, uuid FROM `logs` WHERE time > %s AND server != '%s'", lastLogCheck, server);
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                actions.add(Action.fromResultSet(resultSet));
            }
        } catch (SQLException e) {
            Main.getMessage().error(e);
        }
        lastLogCheck = System.currentTimeMillis();
        return actions;
    }

    private void applyLogs(List<Action> actions) {
        for (Action action : actions) {
            try {
                switch (action.getType()) {
                    case ADD_SELL_ITEM -> {
                        if (hasSellItem(action.getItem())) {
                            break;
                        }
                        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM sell_items WHERE uuid = '%s'", action.getItem().getKey()));
                             ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (!resultSet.next()) break;
                            SellItem sellItem = SellItem.fromResultSet(resultSet);
                            writeLock(() -> addSellItem0(sellItem));
                        }
                    }
                    case UPDATE_USER -> {
                        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM users WHERE uuid = '%s'", action.getOwner()));
                             ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (!resultSet.next()) break;
                            User user = User.fromResultSet(resultSet);
                            if (hasUser(user.uuid)) {
                                writeLock(() -> super.replaceUser(user));
                            } else {
                                writeLock(() -> super.addUser(user));
                            }
                            boostCheck(user.uuid);
                        }
                    }
                    case ADD_UNSOLD_ITEM -> {
                        if (hasUnsoldItem(action.getItem())) break;
                        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM unsold_items WHERE uuid = '%s'", action.getItem().getKey()));
                             ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (!resultSet.next()) break;
                            UnsoldItem unsoldItem = UnsoldItem.fromResultSet(resultSet);
                            writeLock(() -> addUnsoldItem0(unsoldItem));
                        }
                    }
                    case REMOVE_SELL_ITEM -> {
                        if (!hasSellItem(action.getItem())) break;
                        super.removeSellItem(action.getItem());
                    }
                    case REMOVE_UNSOLD_ITEM -> {
                        if (!hasUnsoldItem(action.getItem())) break;
                        super.removeUnsoldItem(action.getItem());
                    }
                }
            } catch (SQLException e) {
                Main.getMessage().error("failed to apply log '%s'", e, action);
            }
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            connection.close();
        } catch (SQLException e) {
            Main.getMessage().error(e);
        }
        packetConnection.close();
        sqlExecuteTask.cancel();
        if (logClearTask != null)
            logClearTask.cancel();
        updateTask.cancel();
        moneyGiver.close();
    }

    @Override
    public void save() {
    }

    @Override
    protected void update() {
        if ((System.currentTimeMillis() - lastLogCheck) > 1500) {
            applyLogs(parseLogs());
            applyGiveMoney();
        }
    }

    private void applyGiveMoney() {
        List<ActionGiveMoney> actions = new ArrayList<>();
        String query = String.format("SELECT uuid, count FROM `give_money` WHERE server = '%s'", Main.getServerId());
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                actions.add(ActionGiveMoney.fromResultSet(resultSet, false));
            }
            try (PreparedStatement p = connection.prepareStatement(String.format("DELETE FROM give_money WHERE server = '%s'", Main.getServerId()))) {
                p.execute();
            }
        } catch (SQLException e) {
            Main.getMessage().error(e);
        }
        for (ActionGiveMoney action : actions) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(action.getUuid());
            Main.getEcon().depositPlayer(player, action.getCount());
        }
    }

    @Override
    public void update(Packet packetIn) {
        int id = packetIn.getType().getId();
        if (id == PacketType.ADD_SELL_ITEM.getId()) {
            SellItem sellItem = ((PacketAddSellItem) packetIn).getSellItem();
            if (hasSellItem(sellItem.getUniqueName())) return;
            writeLock(() -> addSellItem0(sellItem));
        } else if (id == PacketType.UPDATE_USER.getId()) {
            User user = ((PacketUpdateUser) packetIn).getUser();
            if (hasUser(user.uuid)) {
                writeLock(() -> super.replaceUser(user));
            } else {
                writeLock(() -> super.addUser(user));
            }
            boostCheck(user.uuid);
        } else if (id == PacketType.ADD_UNSOLD_ITEM.getId()) {
            UnsoldItem unsoldItem = ((PacketAddUnsoldItem) packetIn).getUnsoldItem();
            if (hasUnsoldItem(unsoldItem.uniqueName)) return;
            writeLock(() -> addUnsoldItem0(unsoldItem));
        } else if (id == PacketType.REMOVE_SELL_ITEM.getId()) {
            UniqueName name = ((PacketRemoveSellItem) packetIn).getName();
            if (!hasSellItem(name)) return;
            super.removeSellItem(name);
        } else if (id == PacketType.REMOVE_UNSOLD_ITEM.getId()) {
            UniqueName name = ((PacketRemoveUnsoldItem) packetIn).getName();
            if (!hasUnsoldItem(name)) return;
            super.removeUnsoldItem(name);
        }
    }

    @Override
    public void connectionLost() {
    }

    @Override
    public void connectionRestored() {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> applyLogs(parseLogs()), 0);
    }

    public PacketConnection getPacketConnection() {
        return packetConnection;
    }

    public MoneyGiver getMoneyGiver() {
        return moneyGiver;
    }
    */
}

