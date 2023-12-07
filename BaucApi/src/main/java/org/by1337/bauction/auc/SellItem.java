package org.by1337.bauction.auc;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.serialize.SerializableToByteArray;
import org.by1337.bauction.util.UniqueName;

import java.util.Set;
import java.util.UUID;

/**
 * Interface defining properties and methods for items listed on an auction.
 */
public interface SellItem extends Placeholderable, SerializableToByteArray {

    /**
     * Get the item available for sale as an ItemStack.
     *
     * @return ItemStack representing the item for sale.
     */
    ItemStack getItemStack();

    /**
     * Get the item available for sale as Base64-encoded NBT tags.
     *
     * @return Base64-encoded NBT tags representing the item for sale.
     */
    String getItem();

    /**
     * Get the name of the seller.
     *
     * @return The name of the seller.
     */
    String getSellerName();

    /**
     * Get the UUID of the seller.
     *
     * @return The UUID of the seller.
     */
    UUID getSellerUuid();

    /**
     * Get the price of the item.
     *
     * @return The price of the item.
     */
    double getPrice();

    /**
     * Check if the item can be purchased in pieces.
     *
     * @return True if the item can be purchased in pieces, false otherwise.
     */
    boolean isSaleByThePiece();

    /**
     * Get the tags associated with the item.
     *
     * @return Set of tags associated with the item.
     */
    Set<String> getTags();

    /**
     * Get the date when the item was listed for sale.
     *
     * @return The date when the item was listed for sale.
     */
    long getTimeListedForSale();

    /**
     * Get the date after which the item will become an UnsoldItem.
     *
     * @return The date after which the item will become an UnsoldItem.
     */
    long getRemovalDate();

    /**
     * Get the UUID of the item.
     *
     * @return The UUID of the item.
     */
    UniqueName getUniqueName();

    /**
     * Get the material of the item.
     *
     * @return The material of the item.
     */
    Material getMaterial();

    /**
     * Get the quantity of the item.
     *
     * @return The quantity of the item.
     */
    int getAmount();

    /**
     * Get the price per unit of the item if it can be sold in pieces, otherwise, get the total price.
     *
     * @return The price per unit of the item if sold in pieces, otherwise, the total price.
     */
    double getPriceForOne();

    String toSql(String table);
}
