package org.by1337.bauction.db.kernel;

import org.by1337.bauction.db.MemorySellItem;
import org.by1337.bauction.db.MemoryUnsoldItem;
import org.by1337.bauction.db.MemoryUser;
import org.by1337.bauction.db.StorageException;

import java.util.List;
import java.util.UUID;

public interface DBCore {
    List<MemorySellItem> getAllSellItems() throws StorageException;
    List<MemoryUser> getAllUsers() throws StorageException;
    MemoryUser getUser(UUID uuid) throws StorageException;
    boolean hasUser(UUID uuid) throws StorageException;
    boolean hasSellItem(UUID uuid) throws StorageException;
    MemoryUser createNew(UUID uuid, String name) throws StorageException;
    void addItem(MemorySellItem memorySellItem, UUID owner) throws StorageException;
    void tryRemoveUnsoldItem(UUID owner, UUID item) throws StorageException;
    void tryRemoveItem(UUID itemUuid) throws StorageException;
    void save();
    void load();
    List<MemoryUnsoldItem> getAddUnsoldItems() throws StorageException;
}
