package org.by1337.bauction.db.action;

public enum ActionType {
    USER_ADD_SELL_ITEM,
    USER_REMOVE_SELL_ITEM,
    USER_ADD_UNSOLD_ITEM,
    USER_REMOVE_UNSOLD_ITEM,
    USER_CREATE,
    USER_UPDATE_STATISTIC,

    AUCTION_ADD_SELL_ITEM,
    AUCTION_REMOVE_SELL_ITEM;
}