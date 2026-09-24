package onl.tesseract.srp.domain.equipment.elytra;

import onl.tesseract.lib.event.equipment.invocable.ElytraUpgrade;

/**
 * Entry for an elytra upgrade display showing current and next level info.
 */
public record ElytraUpgradeEntry(ElytraUpgrade upgrade, int currentLevel, Integer nextLevel, int maxLevel, Integer price, boolean canAfford) {
}

