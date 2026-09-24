package onl.tesseract.srp.config;

import onl.tesseract.srp.domain.job.JobSkill;

public class JobSkillMenuConfig {

    private final CellType[][] cells;

    public JobSkillMenuConfig(CellType[][] cells) {
        this.cells = cells;
    }

    public void forEach(int startLine, int height, TriConsumer<Integer, Integer, CellType> action) {
        int maxLineIndex = Math.min(startLine + height, cells.length);
        for (int lineIndex = startLine; lineIndex < maxLineIndex; lineIndex++) {
            for (int colIndex = 0; colIndex < cells[lineIndex].length; colIndex++) {
                action.accept(lineIndex, colIndex, cells[lineIndex][colIndex]);
            }
        }
    }

    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (getClass() != other.getClass()) return false;
        JobSkillMenuConfig that = (JobSkillMenuConfig) other;
        return java.util.Arrays.deepEquals(cells, that.cells);
    }

    @Override
    public int hashCode() {
        return java.util.Arrays.deepHashCode(cells);
    }

    // --- CellType hierarchy ---
    public sealed interface CellType permits EmptyCell, Arrow, RootCell, SkillCell {}

    public static final class EmptyCell implements CellType {
        public static final EmptyCell INSTANCE = new EmptyCell();
        private EmptyCell() {}
    }

    public static final class Arrow implements CellType {
        private final ArrowType type;
        public Arrow(ArrowType type) { this.type = type; }
        public ArrowType getType() { return type; }
    }

    public static final class RootCell implements CellType {
        public static final RootCell INSTANCE = new RootCell();
        private RootCell() {}
    }

    public static final class SkillCell implements CellType {
        private final JobSkill skill;
        public SkillCell(JobSkill skill) { this.skill = skill; }
        public JobSkill getSkill() { return skill; }
    }

    public enum ArrowType{
        TopRight(1), TopLeft(2), Horizontal(3), Vertical(4), T(5), Cross(6), ReversedT(7), RightT(8), LeftT(9);

        private final int customModelData;

        ArrowType(int customModelData){
            this.customModelData = customModelData;
        }

        public int customModelData(){
            return customModelData;
        }

    }
}

