import java.util.Locale;

import java.math.BigInteger;
import static java.math.BigInteger.*;

public class Simple {
    private static BigInteger exact(int n, int k) {
        BigInteger sum = ZERO;
        BigInteger count = ONE;
        BigInteger depth = ONE;
        BigInteger total = ONE.shiftLeft(n);
        BigInteger bigK = BigInteger.valueOf(k);
        while (total.signum() > 0) {
            BigInteger mtc = total.min(count);
            sum = sum.add(mtc.multiply(depth));
            depth = depth.add(ONE);
            total = total.subtract(mtc);
            count = count.multiply(bigK);
        }
        return sum;
    }

    private static void singleType(int n, int k) {
        int[][] a = {{k}};
        BigInteger[] b = {ONE.shiftLeft(n)};
        MatrixTheorem th2 = new MatrixTheorem();
        long t1 = System.nanoTime();
        BigInteger rv2 = th2.sumDepths(a, b, b[0]);
        long t2 = System.nanoTime();
        System.out.printf(
            "n = %d, k = %d: theorem2 = %d (in %f sec), simple: %d%n",
            n, k, rv2, (t2 - t1) / 1e9, exact(n, k)
        );
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        for (int i = 5; i <= 15; ++i) {
            for (int j = 1; j <= 3; ++j) {
                singleType(i, j);
            }
        }
    }
}
