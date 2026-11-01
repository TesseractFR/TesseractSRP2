package onl.tesseract.srp.customItem.adapter.config;

import onl.tesseract.lib.service.ServiceContainer;
import onl.tesseract.srp.customItem.adapter.serverside.BukkitInventoryRepository;
import onl.tesseract.srp.customItem.adapter.serverside.CustomItemstackMapper;
import onl.tesseract.srp.customItem.adapter.serverside.FileSystemCustumMaterialRepository;
import onl.tesseract.srp.customItem.adapter.serverside.YamlMoneyRepository;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Component
public class CustomItemServiceProducer {

    @Bean
    public CustomItemService customItemService(FileSystemCustumMaterialRepository fscmr,
                                               BukkitInventoryRepository inventoryRepository,
                                               CustomItemstackMapper customItemstackMapper,
                                               YamlMoneyRepository yamlMoneyRepository
    ) {
        return new CustomItemService(fscmr, inventoryRepository, customItemstackMapper, yamlMoneyRepository);
    }

    @Bean
    public SmartInitializingSingleton registerCustomItemServices(CustomItemService customItemService) {
        return () -> ServiceContainer.getInstance().registerService(CustomItemService.class, customItemService);
    }
}