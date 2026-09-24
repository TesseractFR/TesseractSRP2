package onl.tesseract.srp.domain.equipment.elytra;

import onl.tesseract.lib.event.equipment.invocable.ElytraUpgrade;

/**
 * Stats for an elytra upgrade display.
 */
public record ElytraUpgradeStats(double currentValue, Double nextValue, ElytraUpgrade type) {
}

