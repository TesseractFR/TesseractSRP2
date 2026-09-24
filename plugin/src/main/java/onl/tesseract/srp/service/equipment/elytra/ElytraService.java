package onl.tesseract.srp.service.equipment.elytra;

import onl.tesseract.lib.equipment.EquipmentService;
import onl.tesseract.lib.event.equipment.invocable.Elytra;
import onl.tesseract.lib.event.equipment.invocable.ElytraUpgrade;
import onl.tesseract.srp.domain.equipment.elytra.ElytraInvocationResult;
import onl.tesseract.srp.domain.equipment.elytra.ElytraUpgradeEntry;
import onl.tesseract.srp.domain.equipment.elytra.ElytraUpgradeResult;
import onl.tesseract.srp.domain.equipment.elytra.ElytraUpgradeStats;
import onl.tesseract.srp.service.player.SrpPlayerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing elytra equipment and upgrades.
 */
@Service
public class ElytraService {
    private final SrpPlayerService srpPlayerService;
    private final EquipmentService equipmentService;

    public ElytraService(SrpPlayerService srpPlayerService, EquipmentService equipmentService) {
        this.srpPlayerService = srpPlayerService;
        this.equipmentService = equipmentService;
    }

    @Transactional
    public void createElytra(UUID playerId) {
        var equipment = equipmentService.getEquipment(playerId);
        var existing = equipment.get(Elytra.class);
        if (existing != null) {
            return;
        }
        var elytra = new Elytra(
                playerId,
                false,
                -1
        );
        equipmentService.add(playerId, elytra);
    }

    @Transactional
    public ElytraInvocationResult getInvocationResult(UUID playerId) {
        var equipment = equipmentService.getEquipment(playerId);
        var elytra = equipment.get(Elytra.class);
        if (elytra == null) {
            return ElytraInvocationResult.NO_ELYTRA;
        }
        return elytra.isInvoked() ? ElytraInvocationResult.ALREADY_INVOKED : ElytraInvocationResult.READY_TO_INVOKE;
    }

    @Transactional
    public void toggleAutoGlide(UUID playerId) {
        var equipment = equipmentService.getEquipment(playerId);
        var elytra = equipment.get(Elytra.class);
        if (elytra == null) {
            return;
        }
        elytra.toggleAutoGlideEnabled(!elytra.getAutoGlide());
    }

    @Transactional
    public boolean requestPropulsion(UUID playerId) {
        var equipment = equipmentService.getEquipment(playerId);
        var elytra = equipment.get(Elytra.class);
        if (elytra == null) {
            return false;
        }
        return elytra.isInvoked();
    }

    @Transactional
    public List<ElytraUpgradeEntry> getUpgradeEntries(UUID playerId) {
        var equipment = equipmentService.getEquipment(playerId);
        var elytra = equipment.get(Elytra.class);
        if (elytra == null) {
            return List.of();
        }

        var player = srpPlayerService.getPlayer(playerId);

        return List.of(ElytraUpgrade.values()).stream()
                .map(upgrade -> {
                    int currentLevel = elytra.getLevel(upgrade);
                    Integer nextLevel = currentLevel < Constants.MAX_UPGRADE_LEVEL ? currentLevel + 1 : null;
                    Integer price = currentLevel < Constants.MAX_UPGRADE_LEVEL ? getPriceForLevel(currentLevel) : null;
                    boolean canAfford = price != null && player.getIlluminationPoints() >= price;
                    return new ElytraUpgradeEntry(
                            upgrade,
                            currentLevel,
                            nextLevel,
                            Constants.MAX_UPGRADE_LEVEL,
                            price,
                            canAfford
                    );
                })
                .toList();
    }

    @Transactional
    public ElytraUpgradeResult tryBuyNextUpgrade(UUID playerId, ElytraUpgrade upgrade) {
        var equipment = equipmentService.getEquipment(playerId);
        var elytra = equipment.get(Elytra.class);
        if (elytra == null) {
            return ElytraUpgradeResult.NO_ELYTRA;
        }
        int currentLevel = elytra.getLevel(upgrade);
        if (currentLevel >= Constants.MAX_UPGRADE_LEVEL) {
            return ElytraUpgradeResult.MAX_LEVEL_REACHED;
        }
        Integer price = getPriceForLevel(currentLevel);
        if (price == null) {
            return ElytraUpgradeResult.MAX_LEVEL_REACHED;
        }
        boolean success = srpPlayerService.giveIlluminationPoints(playerId, -price);
        if (!success) {
            return ElytraUpgradeResult.NOT_ENOUGH_POINTS;
        }
        elytra.upgradeLevel(upgrade);
        if (upgrade == ElytraUpgrade.SPEED) {
            elytra.enableSpeedUpgrade();
        }
        equipmentService.saveEquipment(equipment);
        return ElytraUpgradeResult.SUCCESS;
    }

    @Transactional
    public boolean setUpgradeLevel(UUID playerId, ElytraUpgrade upgrade, int level) {
        var equipment = equipmentService.getEquipment(playerId);
        var elytra = equipment.get(Elytra.class);
        if (elytra == null) {
            return false;
        }
        elytra.setLevel(upgrade, level);
        equipmentService.saveEquipment(equipment);
        return true;
    }

    public ElytraUpgradeStats getUpgradeStats(ElytraUpgrade upgrade, int level) {
        return switch (upgrade) {
            case SPEED -> {
                double current = Constants.SPEED_MULTIPLIER * (level + 1) * Constants.PERCENT_CONVERSION;
                double next = Constants.SPEED_MULTIPLIER * (level + 2) * Constants.PERCENT_CONVERSION;
                yield new ElytraUpgradeStats(current, next, upgrade);
            }
            case PROTECTION -> {
                double current = Constants.PROTECTION_MULTIPLIER * level;
                double next = Constants.PROTECTION_MULTIPLIER * (level + 1);
                yield new ElytraUpgradeStats(current, next, upgrade);
            }
            case BOOST_NUMBER -> {
                double current = Elytra.getBoostCount(level);
                double next = Elytra.getBoostCount(level + 1);
                yield new ElytraUpgradeStats(current, next, upgrade);
            }
            case RECOVERY -> {
                double current = (double) Elytra.getBaseRecoveryTime(level) / Constants.MS_TO_SECONDS;
                double next = (double) Elytra.getBaseRecoveryTime(level + 1) / Constants.MS_TO_SECONDS;
                yield new ElytraUpgradeStats(current, next, upgrade);
            }
        };
    }

    private Integer getPriceForLevel(int level) {
        if (level >= 0 && level < Constants.MAX_UPGRADE_LEVEL) {
            return Constants.BASE_PRICE * (level + 1);
        }
        return null;
    }
}

