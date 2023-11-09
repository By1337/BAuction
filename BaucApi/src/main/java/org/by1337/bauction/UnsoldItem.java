package org.by1337.bauction;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface UnsoldItem {
    @NotNull
    String getItem();
    long getExpired();
    long getDeleteVia();
    @NotNull
    UUID getOwner();
    @NotNull
    ItemStack getItemStack();

}
