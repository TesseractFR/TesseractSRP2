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
import onl.tesseract.srp.contract.domain.port.userside.ContractGenerator;
import onl.tesseract.srp.contract.domain.port.userside.ContractService;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.Random;

/**
 * Assemble le service du domaine contrat et démarre les composants du module. Le bean
 * {@link ContractService} répond aussi à l'injection de chacune de ses interfaces de cas d'usage.
 */
@Configuration
public class ContractProvider {

    @Bean
    public ContractGenerator contractGenerator(ContractConfigRepository contractConfigRepository,
                                               ContractCatalogRepository contractCatalogRepository) {
        return new ContractGenerator(contractConfigRepository, contractCatalogRepository, new Random(), Clock.systemUTC());
    }

    @Bean
    public ContractService contractService(ContractRepository contractRepository, ContractGenerator contractGenerator,
                                           ContractScrollPort contractScrollPort, PlayerMaterialPort playerMaterialPort,
                                           ContractTicketRepository contractTicketRepository,
                                           DailyTicketQuotaPort dailyTicketQuotaPort, JobMoneyBonusPort jobMoneyBonusPort,
                                           PaymentPort paymentPort) {
        return new ContractService(contractRepository, contractGenerator, contractScrollPort, playerMaterialPort,
                contractTicketRepository, dailyTicketQuotaPort, jobMoneyBonusPort, paymentPort, Clock.systemUTC());
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
