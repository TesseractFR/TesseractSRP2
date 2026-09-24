package onl.tesseract.srp.domain.job;

import java.util.Random;

public class QualityDistribution {
    private final int expectation;
    private final float stddev;

    public QualityDistribution(int expectation, float stddev) {
        this.expectation = expectation;
        this.stddev = stddev;
    }

    public int getExpectation() { return expectation; }
    public float getStddev() { return stddev; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualityDistribution that = (QualityDistribution) o;
        return expectation == that.expectation && Float.compare(stddev, that.stddev) == 0;
    }

    @Override
    public int hashCode() {
        int result = expectation;
        result = 31 * result + Float.floatToIntBits(stddev);
        return result;
    }
}

