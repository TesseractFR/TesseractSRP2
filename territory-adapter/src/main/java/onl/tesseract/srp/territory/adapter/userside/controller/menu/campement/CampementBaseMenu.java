package onl.tesseract.srp.territory.adapter.userside.controller.menu.campement;

import net.kyori.adventure.text.Component;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.territory.adapter.userside.controller.menu.DecoratedMenu;

import java.util.List;

public abstract class CampementBaseMenu extends DecoratedMenu {

    protected CampementBaseMenu(MenuSize size, Component title, Menu previous) {
        super(size, title, previous);
    }

    @Override protected List<Integer> blueSlots()      { return List.of(0, 8, 18, 26); }
    @Override protected List<Integer> cyanSlots()      { return List.of(1, 7, 9, 17, 19, 25); }
    @Override protected List<Integer> lightBlueSlots() { return List.of(2, 6, 20, 24, 3, 5, 21, 23); }
    @Override protected List<Integer> graySlots()      { return List.of(4, 22, 11, 13, 15); }
}