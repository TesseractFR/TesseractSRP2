package onl.tesseract.srp.job.domain.model.talenttree;

public enum ArrowType {
    TopRight,
    TopLeft,
    Horizontal,
    Vertical,
    T,
    Cross,
    ReversedT,
    RightT,
    LeftT;

    public Arrow toArrow(){
        return new Arrow(this);
    }
}
