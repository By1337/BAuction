package org.by1337.bauction.menu.click;

import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;

import java.util.List;

public interface IClick {
    ClickType getClickType();
    List<String> run(Placeholderable placeholderable, Player clicker);
}