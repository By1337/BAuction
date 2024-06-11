package org.by1337.bauction.menu;

import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.by1337.bauction.api.auc.ItemHolder;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.blib.command.CommandException;
import org.by1337.bmenu.menu.*;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class ViewShulkerMenu extends Menu {
    private ItemStack itemStack;

    public ViewShulkerMenu(MenuSetting setting, Player player, @Nullable Menu previousMenu, MenuLoader menuLoader) {
        super(setting, player, previousMenu, menuLoader);
        if (previousMenu != null) {
            var item = previousMenu.getLastClickedItem();
            if (item != null && item.getData() instanceof ItemHolder itemHolder) {
                var data = item.getData();
                itemStack = itemHolder.getItemStack();
                if (data instanceof Placeholder placeholder) {
                    registerPlaceholders(placeholder);
                }
            } else {
                Menu m = this;
                while (!(m instanceof ItemHolder) && m != null) {
                    m = m.getPreviousMenu();
                }
                if (m instanceof ItemHolder itemHolder) {
                    itemStack = itemHolder.getItemStack();
                }
                if (m != null && m != this) {
                    registerPlaceholders(m);
                }
            }
        }
    }

    @Override
    protected void generate() {
        customItems.clear();
        if (itemStack != null && itemStack.getItemMeta() instanceof BlockStateMeta blockStateMeta){
            if (blockStateMeta.hasBlockState() && blockStateMeta.getBlockState() instanceof ShulkerBox shulkerBox){
                for (int i = 0; i < 27; i++) {
                    var item = shulkerBox.getInventory().getItem(i);
                    if (item == null) continue;
                    MenuItem menuItem = new MenuItem(new int[]{i}, item, Collections.emptyMap(), this, null);
                    customItems.add(menuItem);
                }
            }
        }
    }

    @Override
    protected boolean runCommand(String[] cmd) throws CommandException {
        return false;
    }
}
