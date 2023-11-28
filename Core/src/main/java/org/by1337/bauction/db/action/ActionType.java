package org.by1337.bauction.db.action;

public enum ActionType {
    REMOVE_UNSOLD_ITEM, // uuid owner, uuid item
    REMOVE_SELL_ITEM, // uuid owner, uuid item

    ADD_SELL_ITEM, // uuid owner, uuid item
    ADD_UNSOLD_ITEM, // uuid owner, uuid item

    UPDATE_USER, // uuid owner
}
