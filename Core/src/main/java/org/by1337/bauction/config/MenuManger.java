package org.by1337.bauction.config;

import org.by1337.bauction.menu.MenuFactory;
import org.by1337.bauction.menu.MenuSetting;

import java.util.List;

public class MenuManger {
    private final MenuSetting menuBuyCount;
    private final MenuSetting menuConfirm;
    private final MenuSetting mainMenu;
    private final MenuSetting menuPlayerItemsView;
    private final MenuSetting itemsForSaleMenu;
    private final MenuSetting unsoldItems;
    private final List<Integer> mainMenuItemSlots;
    private final List<Integer> playerItemsViewSlots;
    private final List<Integer> itemsForSaleSlots;
    private final List<Integer> unsoldItemsSlots;
    private final int confirmMenuItemSlot;

    public MenuManger(Config config) {
        menuBuyCount = MenuFactory.create(config.getMenuBuyCount());
        menuConfirm = MenuFactory.create(config.getMenuConfirm());
        confirmMenuItemSlot = config.getMenuConfirm().getAsInteger("item-slot");
        mainMenu = MenuFactory.create(config.getMenu());
        menuPlayerItemsView = MenuFactory.create(config.getMenuPlayerItemsView());
        unsoldItems = MenuFactory.create(config.getMenuUnsoldItems());
        itemsForSaleMenu = MenuFactory.create(config.getMenuItemsForSale());
        mainMenuItemSlots = MenuFactory.getSlots(config.getMenu().getList("items-slots", String.class));
        playerItemsViewSlots = MenuFactory.getSlots(config.getMenu().getList("items-slots", String.class));
        unsoldItemsSlots = MenuFactory.getSlots(config.getMenuUnsoldItems().getList("items-slots", String.class));
        itemsForSaleSlots = MenuFactory.getSlots(config.getMenuItemsForSale().getList("items-slots", String.class));
    }

    public MenuSetting getUnsoldItems() {
        return unsoldItems;
    }

    public List<Integer> getUnsoldItemsSlots() {
        return unsoldItemsSlots;
    }

    public List<Integer> getItemsForSaleSlots() {
        return itemsForSaleSlots;
    }

    public MenuSetting getItemsForSaleMenu() {
        return itemsForSaleMenu;
    }

    public MenuSetting getMenuBuyCount() {
        return menuBuyCount;
    }

    public MenuSetting getMenuConfirm() {
        return menuConfirm;
    }

    public int getConfirmMenuItemSlot() {
        return confirmMenuItemSlot;
    }

    public MenuSetting getMainMenu() {
        return mainMenu;
    }

    public List<Integer> getMainMenuItemSlots() {
        return mainMenuItemSlots;
    }

    public MenuSetting getMenuPlayerItemsView() {
        return menuPlayerItemsView;
    }

    public List<Integer> getPlayerItemsViewSlots() {
        return playerItemsViewSlots;
    }
}
