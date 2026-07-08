package onl.tesseract.srp.customitem.adapter.userside;

import onl.tesseract.srp.customitem.domain.port.serverside.CustumMaterialRepository;
import onl.tesseract.srp.customitem.domain.port.serverside.InventoryRepository;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomItemServiceProvider {
    @Bean
    public CustomItemService customItemService2(CustumMaterialRepository custumMaterialRepository, InventoryRepository inventoryRepository) {
        return new CustomItemService(custumMaterialRepository, inventoryRepository);
    }
}
