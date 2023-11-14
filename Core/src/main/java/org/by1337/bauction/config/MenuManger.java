package org.by1337.bauction.config;

import org.by1337.bauction.Main;
import org.by1337.bauction.menu.MenuFactory;
import org.by1337.bauction.menu.MenuSetting;

import java.util.List;

public class MenuManger {
    private final MenuSetting menuBuyCount;
    private final MenuSetting menuConfirm;
    private final MenuSetting mainMenu;
    private final MenuSetting itemsForSaleMenu;
    private final List<Integer> mainMenuItemSlots;
    private final List<Integer> itemsForSaleSlots;
    private final int confirmMenuItemSlot;

    public MenuManger(Config config) {
        menuBuyCount = MenuFactory.create(config.getMenuBuyCount());
        menuConfirm = MenuFactory.create(config.getMenuConfirm());
        confirmMenuItemSlot = config.getMenuConfirm().getAsInteger("item-slot");
        mainMenu = MenuFactory.create(config.getMenu());
        itemsForSaleMenu = MenuFactory.create(config.getMenuItemsForSale());
        mainMenuItemSlots = MenuFactory.getSlots(config.getMenu().getList("items-slots", String.class));
        itemsForSaleSlots = MenuFactory.getSlots(config.getMenuItemsForSale().getList("items-slots", String.class));
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
}
