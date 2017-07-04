import java.util.*;
import java.math.*;
import static java.math.BigInteger.*;

public class Jump {
    private static final MathContext CTX = new MathContext(100);

    private static BigInteger choose(int n, int k) {
        BigInteger rv = ONE;
        for (int i = 0; i < k; ++i) {
            rv = rv.multiply(BigInteger.valueOf(n - i));
            rv = rv.divide(BigInteger.valueOf(i + 1));
        }
        return rv;
    }

    private static double upperBound(int n) {
        BigInteger num = ONE.shiftLeft(n);
        BigInteger den = ONE.add(choose(n, n / 2).shiftLeft(n % 2)).shiftLeft(1);
        return new BigDecimal(num).divide(new BigDecimal(den), CTX).doubleValue() + n;
    }

    // copied from http://stackoverflow.com/a/25414163
    private static double log2(BigInteger val) {
        int blex = val.bitLength() - 1000;
        if (blex > 0) {
            val = val.shiftRight(blex);
        }
        double rv = Math.log(val.doubleValue()) / Math.log(2);
        return rv + (blex > 0 ? blex : 0);
    }

    private static double complexLowerBound(int n) {
        BigDecimal s = new BigDecimal(ONE.shiftLeft(n));
        BigInteger ch = choose(n, n / 2).shiftLeft(n % 2);
        BigDecimal q = new BigDecimal(ch);
        BigDecimal log2q = new BigDecimal(log2(ch));
        BigDecimal BD1 = new BigDecimal(1);
        BigDecimal BD2 = new BigDecimal(2);
        BigDecimal BD3 = new BigDecimal(3);

        BigDecimal add1 = q.multiply(log2q).multiply(s.add(BD1).subtract(log2q)).divide(BD1.add(q), CTX).divide(s, CTX);
        BigDecimal add2 = s.subtract(BD2.multiply(q)).multiply(s.add(q)).add(s).subtract(BD3.multiply(q)).divide(BD2.multiply(s).multiply(BD1.add(q)), CTX);
        return add1.add(add2).subtract(BD2).doubleValue();
    }

    private static double extremeJumpQ(double q, double s) {
        double log2q = Math.log(q) / Math.log(2);
        return q * log2q * (s + 1 - log2q) / (1 + q) / s + ((s - 2 * q) * (s + q) + s - 3 * q) / (2 * s * (1 + q)) - 2;
    }

    private static void extremeJump(int n) {
        int[][] a = {{2, 0}, {1 + n % 2, 1}};
        BigInteger[] b = {choose(n, n / 2), ONE.shiftLeft(n)};
        System.out.printf(
            "n = %5d: simple: %5d, complex: %12.6f, upper: %12.6f",
            n, n - 1 - n % 2, complexLowerBound(n), upperBound(n)
        );
        long t1 = System.nanoTime();
        double rv = MatrixTheorem.averageDepth(a, b, ONE.shiftLeft(n));
        long t2 = System.nanoTime();
        System.out.printf("; theorem: %12.6f (in %12.6f sec)%n", rv, (t2 - t1) / 1e9);
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        for (int i = 1; i <= 100; ++i) {
            extremeJump(i);
        }
        for (int i = 200; i < 1000; i += 200) {
            extremeJump(i);
            extremeJump(i + 1);
        }
        for (int i = 1000; i < 10000; i += 1000) {
            extremeJump(i);
            extremeJump(i + 1);
        }
    }
}
