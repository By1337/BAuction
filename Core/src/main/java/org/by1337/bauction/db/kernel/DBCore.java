package org.by1337.bauction.db.kernel;

import org.by1337.bauction.auc.SellItem;
import org.by1337.bauction.db.StorageException;

import java.util.List;
import java.util.UUID;

public interface DBCore {
    List<CSellItem> getAllSellItems() throws StorageException;
    List<CUser> getAllUsers() throws StorageException;
    CUser getUser(UUID uuid) throws StorageException;
    boolean hasUser(UUID uuid) throws StorageException;
    boolean hasSellItem(UUID uuid) throws StorageException;
    CUser createNewAndSave(UUID uuid, String name) throws StorageException;
    void addItem(SellItem sellItem, UUID owner) throws StorageException;
    void tryRemoveUnsoldItem(UUID owner, UUID item) throws StorageException;
    void tryRemoveItem(UUID itemUuid) throws StorageException;
    void save();
    void load();
    List<CUnsoldItem> getAddUnsoldItems() throws StorageException;
}
