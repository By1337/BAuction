package org.by1337.bauction.auc;

import org.bukkit.inventory.ItemStack;
import org.by1337.api.chat.Placeholderable;

import java.util.UUID;

/**
 * Interface representing an unsold item that has expired on the auction.
 */
public interface UnsoldItem extends Placeholderable {

    /**
     * Get the item that remains unsold as an ItemStack.
     *
     * @return ItemStack representing the unsold item.
     */
    ItemStack getItemStack();

    /**
     * Get the unsold item as Base64-encoded NBT tags.
     *
     * @return Base64-encoded NBT tags representing the unsold item.
     */
    String getItem();

    /**
     * Get the expiration date of the unsold item, marking it as an UnsoldItem.
     *
     * @return The date when the item expired and became an UnsoldItem.
     */
    long getExpired();

    /**
     * Get the UUID of the seller associated with the unsold item.
     *
     * @return The UUID of the seller.
     */
    UUID getSellerUuid();

    /**
     * Get the UUID of the unsold item.
     *
     * @return The UUID of the unsold item.
     */
    UUID getUuid();

    /**
     * Get the date when the unsold item will be permanently deleted.
     *
     * @return The date of the final deletion of the unsold item.
     */
    long getDeleteVia();
}
