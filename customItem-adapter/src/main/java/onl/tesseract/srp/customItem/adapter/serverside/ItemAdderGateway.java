package onl.tesseract.srp.customItem.adapter.serverside;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;
import org.springframework.stereotype.Component;

@Component
public class ItemAdderGateway {

    public ItemStack getCustomItem(String namespaceId){
        CustomStack customStack = CustomStack.getInstance(namespaceId);
        if(customStack == null){
            throw new IllegalArgumentException("Impossible de charger " + namespaceId);
        }
        return customStack.getItemStack();
    }

    public Boolean isCustomItem(ItemStack itemStack){
        return CustomStack.byItemStack(itemStack) != null;
    }
}
