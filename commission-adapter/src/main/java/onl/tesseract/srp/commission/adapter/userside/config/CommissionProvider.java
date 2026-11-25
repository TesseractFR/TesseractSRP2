package onl.tesseract.srp.commission.adapter.userside.config;

import onl.tesseract.srp.commission.domain.port.serverside.CommissionBoardRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionCatalogRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionConfigRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionRepository;
import onl.tesseract.srp.commission.domain.port.serverside.GuildMembershipPort;
import onl.tesseract.srp.commission.domain.port.serverside.GuildBankPort;
import onl.tesseract.srp.commission.domain.port.serverside.GuildIncomePort;
import onl.tesseract.srp.commission.domain.port.serverside.PlayerMaterialPort;
import onl.tesseract.srp.commission.domain.port.serverside.PlayerPaymentPort;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardAccessService;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardManagementService;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardRefillService;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardService;
import onl.tesseract.srp.commission.domain.port.userside.CommissionDeliveryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

/** Déclare les services du domaine des commissions, qui n'ont aucune annotation Spring. */
@Configuration
public class CommissionProvider {

    @Bean
    public CommissionBoardRefillService commissionBoardRefillService(CommissionRepository commissionRepository,
                                                                     CommissionConfigRepository commissionConfigRepository,
                                                                     CommissionCatalogRepository commissionCatalogRepository) {
        return new CommissionBoardRefillService(commissionRepository, commissionConfigRepository, commissionCatalogRepository,
                new Random());
    }

    @Bean
    public CommissionBoardService commissionBoardService(CommissionBoardRepository commissionBoardRepository,
                                                         CommissionRepository commissionRepository,
                                                         CommissionCatalogRepository commissionCatalogRepository,
                                                         CommissionConfigRepository commissionConfigRepository,
                                                         CommissionBoardAccessService commissionBoardAccessService,
                                                         CommissionBoardRefillService commissionBoardRefillService) {
        return new CommissionBoardService(commissionBoardRepository, commissionRepository, commissionCatalogRepository,
                commissionConfigRepository, commissionBoardAccessService, commissionBoardRefillService);
    }

    @Bean
    public CommissionBoardAccessService commissionBoardAccessService(GuildMembershipPort guildMembershipPort) {
        return new CommissionBoardAccessService(guildMembershipPort);
    }

    @Bean
    public CommissionBoardManagementService commissionBoardManagementService(CommissionBoardRepository commissionBoardRepository,
                                                                             CommissionConfigRepository commissionConfigRepository,
                                                                             CommissionRepository commissionRepository,
                                                                             GuildBankPort guildBankPort,
                                                                             GuildMembershipPort guildMembershipPort,
                                                                             CommissionBoardService commissionBoardService,
                                                                             CommissionBoardRefillService commissionBoardRefillService) {
        return new CommissionBoardManagementService(commissionBoardRepository, commissionConfigRepository, commissionRepository,
                guildBankPort, guildMembershipPort, commissionBoardService, commissionBoardRefillService);
    }

    @Bean
    public CommissionDeliveryService commissionDeliveryService(CommissionRepository commissionRepository,
                                                               PlayerMaterialPort playerMaterialPort,
                                                               PlayerPaymentPort playerPaymentPort, GuildIncomePort guildIncomePort,
                                                               CommissionConfigRepository commissionConfigRepository,
                                                               CommissionBoardService commissionBoardService,
                                                               CommissionBoardAccessService commissionBoardAccessService,
                                                               CommissionBoardRefillService commissionBoardRefillService) {
        return new CommissionDeliveryService(commissionRepository, playerMaterialPort, playerPaymentPort, guildIncomePort,
                commissionConfigRepository, commissionBoardService, commissionBoardAccessService, commissionBoardRefillService);
    }
}
