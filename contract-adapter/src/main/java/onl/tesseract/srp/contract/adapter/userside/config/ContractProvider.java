package onl.tesseract.srp.contract.adapter.userside.config;

import onl.tesseract.lib.service.ServiceContainer;
import onl.tesseract.srp.contract.adapter.userside.controller.commands.args.ContractJobCatalog;
import onl.tesseract.srp.contract.adapter.serverside.task.ContractExpirationTask;
import onl.tesseract.srp.contract.domain.port.serverside.ContractConfigRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractMessagingRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ItemInfoRepository;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerInfoRepository;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerInventoryRepository;
import onl.tesseract.srp.contract.domain.port.userside.ContractGenerator;
import onl.tesseract.srp.contract.domain.port.userside.ContractService;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class ContractProvider {

    @Bean
    public ContractGenerator contractGenerator(ContractConfigRepository contractConfigRepository, ItemInfoRepository itemInfoRepository) {
        return new ContractGenerator(contractConfigRepository, itemInfoRepository, new Random());
    }

    @Bean
    public ContractService contractService(ContractRepository contractRepository, PlayerInventoryRepository playerInventoryRepository, ContractGenerator contractGenerator,
                                           PlayerInfoRepository playerInfoRepository, ContractMessagingRepository contractMessagingRepository) {
        return new ContractService(contractRepository, playerInventoryRepository, contractGenerator, playerInfoRepository, contractMessagingRepository);
    }

    @Bean
    public SmartInitializingSingleton contractStartup(ContractJobCatalog contractJobCatalog, ContractExpirationTask contractExpirationTask) {
        return () -> {
            ServiceContainer.getInstance().registerService(ContractJobCatalog.class, contractJobCatalog);
            contractExpirationTask.start();
        };
    }
}