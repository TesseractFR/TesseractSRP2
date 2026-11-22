package onl.tesseract.srp.job.adapter.userside.controller.menu;

import onl.tesseract.srp.job.domain.model.talent.Bonus;

/**
 * Libellés des bonus de talent affichés au joueur dans les menus.
 * <p>
 * Cette classe vit dans l'adapter parce que le domaine ne formate jamais de message pour le joueur.
 */
public final class JobBonusDescription {

    private JobBonusDescription() {
    }

    /**
     * Donne le libellé d'un bonus.
     *
     * @param bonus le bonus
     * @return le texte affiché au joueur
     */
    public static String of(Bonus bonus) {
        return switch (bonus) {
            case QUALITY -> "Augmente la chance de qualité";
            case LOOT_CHANCE -> "Augmente la chance de butin";
            case MONEY -> "Augmente la quantité d'argent gagnée";
        };
    }
}
