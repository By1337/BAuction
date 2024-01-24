package org.by1337.bauction.menu.click;

import org.by1337.blib.chat.Placeholderable;
import org.by1337.bauction.menu.Menu;

public interface IClick {
    ClickType getClickType();
    void run(Menu menu, Placeholderable holder);
}