package onl.tesseract.srp.territory.adapter.userside.controller.menu.guild;

import net.kyori.adventure.text.Component;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.territory.adapter.userside.controller.menu.DecoratedMenu;

import java.util.List;

public abstract class GuildBaseMenu extends DecoratedMenu {

    protected GuildBaseMenu(MenuSize size, Component title, Menu previous) {
        super(size, title, previous);
    }

    @Override protected List<Integer> limeSlots()  { return List.of(2, 4, 6, 10, 16, 18, 26, 28, 34, 38, 40, 42); }
    @Override protected List<Integer> greenSlots() { return List.of(3, 5, 9, 12, 14, 17, 19, 20, 22, 24, 25, 27, 30, 32, 35, 39, 41); }
    @Override protected List<Integer> graySlots()  { return List.of(0, 1, 7, 8, 11, 13, 15, 21, 23, 29, 31, 33, 36, 37, 43, 44); }
}