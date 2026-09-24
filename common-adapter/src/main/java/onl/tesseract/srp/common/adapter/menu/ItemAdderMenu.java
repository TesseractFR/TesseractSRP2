package onl.tesseract.srp.common.adapter.menu;

import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper;
import dev.lone.itemsadder.api.CustomStack;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.common.domain.model.ItemTag;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class ItemAdderMenu extends Menu {

    private final String backgroundNamespaceId;
    private final String titleS;
    private final int textureOffset;

    public ItemAdderMenu(MenuSize size, String backgroundNamespaceId, String titleS, Menu previous, int textureOffset){
        super(size,titleS,previous);

        this.backgroundNamespaceId = backgroundNamespaceId;
        this.titleS = titleS;
        this.textureOffset = textureOffset;
    }

    public void open(Player viewer){
        super.open(viewer);
        TexturedInventoryWrapper.setPlayerInventoryTexture(viewer, new FontImageWrapper(backgroundNamespaceId),titleS,10,textureOffset);
    }

    protected ItemStack getCustomButtonItemStack(CustomMenuButton customMenuButton){
        return CustomStack.getInstance(customMenuButton.getItemTag().value()).getItemStack();
    }
}
