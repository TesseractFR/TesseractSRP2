package onl.tesseract.srp.common.adapter.userside.menu;

import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper;
import dev.lone.itemsadder.api.CustomStack;
import lombok.extern.slf4j.Slf4j;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Slf4j
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
        try {
            TexturedInventoryWrapper.setPlayerInventoryTexture(viewer, new FontImageWrapper(backgroundNamespaceId),titleS,10,textureOffset);
        } catch (IllegalArgumentException e) {
            log.warn("Menu opened without background: {}", e.getMessage());
        }
    }

    protected ItemStack getCustomButtonItemStack(CustomMenuButton customMenuButton){
        try{
            return CustomStack.getInstance(customMenuButton.getItemTag().value()).getItemStack();
        }catch(Exception e){
            throw new RuntimeException("Could not get item stack for custom menu button "+customMenuButton,e);
        }
    }
}
