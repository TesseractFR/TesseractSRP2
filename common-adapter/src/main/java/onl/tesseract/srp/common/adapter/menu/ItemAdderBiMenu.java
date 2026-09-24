package onl.tesseract.srp.common.adapter.menu;

import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper;
import net.kyori.adventure.text.Component;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public abstract class ItemAdderBiMenu extends BiMenu {

    private final String backgroundNamespaceId;
    private final String titleS;
    private final int titleOffset;

    public ItemAdderBiMenu(MenuSize size, String backgroundNamespaceId, String titleS, Menu previous, int titleOffset){
        super(size, Component.text(titleS), previous);
        this.backgroundNamespaceId = backgroundNamespaceId;
        this.titleS = titleS;
        this.titleOffset = titleOffset;
    }

    public void open(@NonNull Player viewer){
        super.open(viewer);
        TexturedInventoryWrapper.setPlayerInventoryTexture(viewer,
                new FontImageWrapper(backgroundNamespaceId),titleS,titleOffset,-8);
    }
    protected ItemStack getCustomButtonItemStack(CustomMenuButton customMenuButton){
        return CustomStack.getInstance(customMenuButton.getItemTag().value()).getItemStack();
    }
}
