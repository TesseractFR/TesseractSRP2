package onl.tesseract.srp.job.adapter.userside.controller.command;

import onl.tesseract.srp.job.domain.model.talenttree.ArrowType;

/**
 * Custom model data des flèches de l'arbre de talents, utilisé pour leur rendu dans le menu.
 * <p>
 * Cette classe vit dans l'adapter parce que le rendu Bukkit est un détail technique
 * que le domaine ne doit pas connaître.
 */
public final class JobArrowModelData {

    private JobArrowModelData() {
    }

    /**
     * Donne le custom model data d'un type de flèche.
     *
     * @param arrowType le type de flèche
     * @return la valeur de custom model data de l'item affiché
     */
    public static int of(ArrowType arrowType) {
        return switch (arrowType) {
            case TopRight -> 1;
            case TopLeft -> 2;
            case Horizontal -> 3;
            case Vertical -> 4;
            case T -> 5;
            case Cross -> 6;
            case ReversedT -> 7;
            case RightT -> 8;
            case LeftT -> 9;
        };
    }
}
