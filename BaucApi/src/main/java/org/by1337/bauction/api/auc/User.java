package org.by1337.bauction.api.auc;

import org.by1337.blib.chat.Placeholderable;
import org.by1337.bauction.api.serialize.SerializableToByteArray;

import java.util.UUID;

/**
 * Interface representing the auction user.
 */
public interface User extends Placeholderable, SerializableToByteArray {

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

