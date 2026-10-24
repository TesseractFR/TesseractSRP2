package onl.tesseract.srp.contract.domain.port.serverside;

import java.util.Random;

public interface RandomSource {
    double nextDouble();
    int nextInt(int bound);

    static RandomSource of(Random random) {
        return new RandomSource() {
            @Override
            public double nextDouble() {
                return random.nextDouble();
            }

            @Override
            public int nextInt(int bound) {
                return random.nextInt(bound);
            }
        };
    }
}
