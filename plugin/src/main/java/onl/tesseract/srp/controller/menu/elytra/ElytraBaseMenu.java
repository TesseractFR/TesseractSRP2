package onl.tesseract.srp.controller.menu.elytra;

import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import org.bukkit.entity.Player;

public abstract class ElytraBaseMenu extends Menu {
    protected final Player player;

    public ElytraBaseMenu(MenuSize size, String title, Menu previous, Player player) {
        super(size, title, previous);
        this.player = player;
    }

    public Player getPlayer() { return player; }
}

