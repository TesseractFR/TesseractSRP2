package onl.tesseract.srp.job.domain.model.talenttree;

public enum ArrowType {
    TopRight(1),
    TopLeft(2),
    Horizontal(3),
    Vertical(4),
    T(5),
    Cross(6),
    ReversedT(7),
    RightT(8),
    LeftT(9);

    private final int customModelData;

    ArrowType(int customModelData) {
        this.customModelData = customModelData;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public Arrow toArrow(){
        return new Arrow(this);
    }
}
