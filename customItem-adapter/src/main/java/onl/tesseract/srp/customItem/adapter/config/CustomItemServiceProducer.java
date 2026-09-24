package onl.tesseract.srp.customItem.adapter.config;

import onl.tesseract.srp.customItem.adapter.serverside.BukkitInventoryRepository;
import onl.tesseract.srp.customItem.adapter.serverside.CustomItemstackMapper;
import onl.tesseract.srp.customItem.adapter.serverside.FileSystemCustumMaterialRepository;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Component
public class CustomItemServiceProducer {

    @Bean
    public CustomItemService customItemService(FileSystemCustumMaterialRepository fscmr,
                                               BukkitInventoryRepository inventoryRepository,
                                               CustomItemstackMapper customItemstackMapper
    ) {
        return new CustomItemService(fscmr, inventoryRepository, customItemstackMapper);
    }
}