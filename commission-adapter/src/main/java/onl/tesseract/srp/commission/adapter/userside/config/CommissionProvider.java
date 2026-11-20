package onl.tesseract.srp.commission.adapter.userside.config;

import onl.tesseract.srp.commission.domain.port.serverside.BoardFurniturePort;
import onl.tesseract.srp.commission.domain.port.serverside.BoardPlacementSessionPort;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionBoardRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionCatalogRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionConfigRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionRepository;
import onl.tesseract.srp.commission.domain.port.serverside.GuildMembershipPort;
import onl.tesseract.srp.commission.domain.port.serverside.GuildProgressPort;
import onl.tesseract.srp.commission.domain.port.serverside.GuildTerritoryPort;
import onl.tesseract.srp.commission.domain.port.serverside.PlayerMaterialPort;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardManagementService;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardPlacementService;
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
        return new CommissionBoardRefillService(commissionRepository, commissionConfigRepository, commissionCatalogRepository, new Random());
    }

    @Bean
    public CommissionBoardService commissionBoardService(CommissionBoardRepository commissionBoardRepository,
                                                         GuildMembershipPort guildMembershipPort,
                                                         CommissionCatalogRepository commissionCatalogRepository,
                                                         BoardFurniturePort boardFurniturePort,
                                                         CommissionConfigRepository commissionConfigRepository,
                                                         CommissionBoardRefillService commissionBoardRefillService) {
        return new CommissionBoardService(commissionBoardRepository, guildMembershipPort, commissionCatalogRepository, boardFurniturePort,
                commissionConfigRepository, commissionBoardRefillService);
    }

    @Bean
    public CommissionBoardManagementService commissionBoardManagementService(CommissionBoardRepository commissionBoardRepository,
                                                                             CommissionConfigRepository commissionConfigRepository,
                                                                             GuildProgressPort guildProgressPort,
                                                                             CommissionBoardService commissionBoardService,
                                                                             CommissionBoardRefillService commissionBoardRefillService) {
        return new CommissionBoardManagementService(commissionBoardRepository, commissionConfigRepository, guildProgressPort,
                commissionBoardService, commissionBoardRefillService);
    }

    @Bean
    public CommissionBoardPlacementService commissionBoardPlacementService(CommissionBoardService commissionBoardService,
                                                                           CommissionBoardRepository commissionBoardRepository,
                                                                           BoardPlacementSessionPort boardPlacementSessionPort,
                                                                           BoardFurniturePort boardFurniturePort,
                                                                           GuildTerritoryPort guildTerritoryPort) {
        return new CommissionBoardPlacementService(commissionBoardService, commissionBoardRepository, boardPlacementSessionPort,
                boardFurniturePort, guildTerritoryPort);
    }

    @Bean
    public CommissionDeliveryService commissionDeliveryService(CommissionRepository commissionRepository, PlayerMaterialPort playerMaterialPort,
                                                               CommissionConfigRepository commissionConfigRepository,
                                                               CommissionBoardService commissionBoardService,
                                                               CommissionBoardRefillService commissionBoardRefillService) {
        return new CommissionDeliveryService(commissionRepository, playerMaterialPort, commissionConfigRepository, commissionBoardService,
                commissionBoardRefillService);
    }
}
