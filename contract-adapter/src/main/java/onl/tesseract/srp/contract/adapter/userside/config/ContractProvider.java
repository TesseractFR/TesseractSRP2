package onl.tesseract.srp.contract.adapter.userside.config;

import onl.tesseract.lib.service.ServiceContainer;
import onl.tesseract.srp.contract.adapter.userside.controller.commands.args.ContractJobCatalog;
import onl.tesseract.srp.contract.adapter.userside.controller.task.ContractExpirationTask;
import onl.tesseract.srp.contract.domain.port.serverside.ContractCatalogRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractConfigRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractScrollPort;
import onl.tesseract.srp.contract.domain.port.serverside.ContractTicketRepository;
import onl.tesseract.srp.contract.domain.port.serverside.DailyTicketQuotaPort;
import onl.tesseract.srp.contract.domain.port.serverside.JobMoneyBonusPort;
import onl.tesseract.srp.contract.domain.port.serverside.PaymentPort;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerMaterialPort;
import onl.tesseract.srp.contract.domain.port.userside.ContractDeliveryService;
import onl.tesseract.srp.contract.domain.port.userside.ContractExpirationService;
import onl.tesseract.srp.contract.domain.port.userside.ContractGenerator;
import onl.tesseract.srp.contract.domain.port.userside.ContractPurchaseService;
import onl.tesseract.srp.contract.domain.port.userside.ContractTicketService;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.Random;

/** Assemble les services du domaine contrat, un par cas d'usage, et démarre les composants du module. */
@Configuration
public class ContractProvider {

    private static final Clock CLOCK = Clock.systemUTC();

    @Bean
    public ContractGenerator contractGenerator(ContractConfigRepository contractConfigRepository,
                                               ContractCatalogRepository contractCatalogRepository) {
        return new ContractGenerator(contractConfigRepository, contractCatalogRepository, new Random(), CLOCK);
    }

    @Bean
    public ContractTicketService contractTicketService(ContractTicketRepository contractTicketRepository,
                                                       DailyTicketQuotaPort dailyTicketQuotaPort) {
        return new ContractTicketService(contractTicketRepository, dailyTicketQuotaPort);
    }

    @Bean
    public ContractPurchaseService contractPurchaseService(ContractRepository contractRepository,
                                                           ContractGenerator contractGenerator,
                                                           ContractScrollPort contractScrollPort,
                                                           ContractTicketService contractTicketService) {
        return new ContractPurchaseService(contractRepository, contractGenerator, contractScrollPort,
                contractTicketService);
    }

    @Bean
    public ContractDeliveryService contractDeliveryService(ContractRepository contractRepository,
                                                           ContractScrollPort contractScrollPort,
                                                           PlayerMaterialPort playerMaterialPort,
                                                           JobMoneyBonusPort jobMoneyBonusPort, PaymentPort paymentPort) {
        return new ContractDeliveryService(contractRepository, contractScrollPort, playerMaterialPort, jobMoneyBonusPort,
                paymentPort, CLOCK);
    }

    @Bean
    public ContractExpirationService contractExpirationService(ContractRepository contractRepository) {
        return new ContractExpirationService(contractRepository, CLOCK);
    }

    @Bean
    public SmartInitializingSingleton contractStartup(ContractJobCatalog contractJobCatalog,
                                                      ContractExpirationTask contractExpirationTask) {
        return () -> {
            ServiceContainer.getInstance().registerService(ContractJobCatalog.class, contractJobCatalog);
            contractExpirationTask.start();
        };
    }
}
