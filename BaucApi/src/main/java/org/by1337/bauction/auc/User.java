package org.by1337.bauction.auc;

import org.by1337.api.chat.Placeholderable;

import java.util.List;
import java.util.UUID;

/**
 * Interface representing the auction user.
 */
public interface User extends Placeholderable {

    /**
     * Get the nickname of the user.
     *
     * @return The nickname of the user.
     */
    String getNickName();

    /**
     * Get the UUID of the user.
     *
     * @return The UUID of the user.
     */
    UUID getUuid();

    /**
     * Get a list of UUIDs representing unsold items associated with the user.
     *
     * @return List of UUIDs of unsold items.
     */
    List<UUID> getUnsoldItems();

    /**
     * Get a list of UUIDs representing items currently for sale by the user.
     *
     * @return List of UUIDs of items for sale.
     */
    List<UUID> getItemForSale();

    /**
     * Get the total count of completed deals involving the user.
     *
     * @return The total count of completed deals.
     */
    int getDealCount();

    /**
     * Get the total sum of deals completed by the user.
     *
     * @return The total sum of completed deals.
     */
    double getDealSum();

    /**
     * Get the number of external slots available to the user.
     *
     * @return The number of external slots available.
     */
    int getExternalSlots();

    /**
     * Get the extended time during which the user's items remain available for sale
     * before being transformed into UnsoldItems.
     *
     * @return The additional time for which items will be available for sale.
     */
    long getExternalSellTime();
}

