package org.by1337.bauction.menu.click;

import org.bukkit.event.inventory.InventoryClickEvent;

public enum ClickType {
    LEFT(org.bukkit.event.inventory.ClickType.LEFT, "left_click_commands", "left_click_requirement"),
    RIGHT(org.bukkit.event.inventory.ClickType.RIGHT, "right_click_commands", "right_click_requirement"),
    SHIFT_LIFT(org.bukkit.event.inventory.ClickType.SHIFT_LEFT, "shift_left_click_commands", "shift_left_click_requirement"),
    SHIFT_RIGHT(org.bukkit.event.inventory.ClickType.SHIFT_RIGHT, "shift_right_click_commands", "shift_right_click_requirement"),
    MIDDLE(org.bukkit.event.inventory.ClickType.MIDDLE, "middle_click_commands", "middle_click_requirement"),
    DROP(org.bukkit.event.inventory.ClickType.DROP, "drop_click_commands", "drop_click_requirement"),
    ANY_CLICK(null, "click_commands", "click_requirement"),
    NUMBER_KEY_0(null, "number_key_0_click_commands", "number_key_0_click_requirement"),
    NUMBER_KEY_1(null, "number_key_1_click_commands", "number_key_1_click_requirement"),
    NUMBER_KEY_2(null, "number_key_2_click_commands", "number_key_2_click_requirement"),
    NUMBER_KEY_3(null, "number_key_3_click_commands", "number_key_3_click_requirement"),
    NUMBER_KEY_4(null, "number_key_4_click_commands", "number_key_4_click_requirement"),
    NUMBER_KEY_5(null, "number_key_5_click_commands", "number_key_5_click_requirement"),
    NUMBER_KEY_6(null, "number_key_6_click_commands", "number_key_6_click_requirement"),
    NUMBER_KEY_7(null, "number_key_7_click_commands", "number_key_7_click_requirement"),
    NUMBER_KEY_8(null, "number_key_8_click_commands", "number_key_8_click_requirement"),
    SWAP_OFFHAND(org.bukkit.event.inventory.ClickType.SWAP_OFFHAND, "swap_offhand_click_commands", "swap_offhand_click_requirement"),
    CONTROL_DROP(org.bukkit.event.inventory.ClickType.CONTROL_DROP, "control_drop_click_commands", "control_drop_click_requirement");

    private final org.bukkit.event.inventory.ClickType clickType;
    private final String configKeyClick;
    private final String configKeyRequirement;

    ClickType(org.bukkit.event.inventory.ClickType clickType, String configKeyClick, String configKeyRequirement) {
        this.clickType = clickType;
        this.configKeyClick = configKeyClick;
        this.configKeyRequirement = configKeyRequirement;
    }

    public static ClickType adapter(InventoryClickEvent e){
        if (e.getClick() == org.bukkit.event.inventory.ClickType.NUMBER_KEY){
            return ClickType.valueOf("NUMBER_KEY_" + e.getHotbarButton());
        }
        for (ClickType clickType1 : ClickType.values()){
            if (clickType1.getClickType() == e.getClick()){
                return clickType1;
            }
        }
        return ANY_CLICK;
    }

    public org.bukkit.event.inventory.ClickType getClickType() {
        return clickType;
    }

    public String getConfigKeyClick() {
        return configKeyClick;
    }

    public String getConfigKeyRequirement() {
        return configKeyRequirement;
    }
}
