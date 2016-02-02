import java.math.BigInteger;
import java.util.*;

import static java.math.BigInteger.*;

public class MatrixTheorem {
    protected boolean verbose;
    public void setVerbosity(boolean verbose) {
        this.verbose = verbose;
    }

    static class Implementation {
        private final BigInteger[][] a;
        private final BigInteger[] b;
        private final int nTypes;
        private final RealNode root;
        private final IdealNode[] ideals;

        class RealNode {
            final int type;
            BigInteger singleCapacity;
            BigInteger multiplicity;
            int callCount;

            public RealNode(int type, BigInteger multiplicity, BigInteger singleCapacity) {
                this.type = type;
                this.singleCapacity = singleCapacity;
                this.multiplicity = multiplicity;
            }

            public BigInteger advanceLevel(BigInteger remains) {
                if (remains.signum() == 0 || singleCapacity.signum() == 0) {
                    return ZERO;
                }
                BigInteger idealValue = ideals[type].getLevelSize(callCount++);
                BigInteger realValue = idealValue.min(singleCapacity);
                singleCapacity = singleCapacity.subtract(realValue);
                return remains.min(realValue.multiply(multiplicity));
            }
        }

        class IdealNode {
            final int type;
            RealNode[] children;
            BigInteger capacity;
            List<BigInteger> levels;

            public IdealNode(int type) {
                this.type = type;
                this.capacity = b[type].subtract(ONE);
                int childCount = 0;
                for (int i = 0; i < nTypes; ++i) {
                    if (a[type][i].signum() > 0) {
                        ++childCount;
                    }
                }
                children = new RealNode[childCount];
                for (int i = 0, j = 0; i < nTypes; ++i) {
                    if (a[type][i].signum() > 0) {
                        children[j++] = new RealNode(i, a[type][i], b[i]);
                    }
                }
                levels = new ArrayList<>();
                levels.add(ONE);
            }

            private void advance() {
                if (capacity.signum() == 0) {
                    throw new AssertionError("Type " + type + ": a non-existent level requested!");
                }
                BigInteger prevCapacity = capacity;
                for (RealNode child : children) {
                    BigInteger change = child.advanceLevel(capacity);
                    capacity = capacity.subtract(change);
                }
                levels.add(prevCapacity.subtract(capacity));
            }

            public BigInteger getLevelSize(int level) {
                while (levels.size() <= level) {
                    advance();
                }
                return levels.get(level);
            }
        }

        public Implementation(BigInteger[][] a, BigInteger[] b, int rootType) {
            this.a = a;
            this.b = b;
            this.nTypes = a.length;
            this.ideals = new IdealNode[nTypes];
            for (int i = 0; i < nTypes; ++i) {
                this.ideals[i] = new IdealNode(i);
            }
            this.root = new RealNode(rootType, ONE, b[rootType]);
        }
    }

    public BigInteger sumDepths(BigInteger[][] a, BigInteger[] b, BigInteger maxSize) {
        Implementation impl = new Implementation(a, b, a.length - 1);
        BigInteger remains = maxSize;
        long depth = 1;
        BigInteger rv = ZERO;
        while (remains.signum() > 0) {
            BigInteger nextLayerSize = impl.root.advanceLevel(remains);
            if (nextLayerSize.equals(ZERO)) {
                throw new IllegalArgumentException("Can not terminate");
            }
            remains = remains.subtract(nextLayerSize);
            rv = rv.add(nextLayerSize.multiply(BigInteger.valueOf(depth)));
            ++depth;
        }
        return rv;
    }

    public BigInteger sumDepths(int[][] a, BigInteger[] b, BigInteger maxSize) {
        return sumDepths(wrap(a), b, maxSize);
    }

    public double averageDepth(int[][] a, BigInteger[] b, BigInteger maxSize) {
        return averageDepth(wrap(a), b, maxSize);
    }

    public double averageDepth(BigInteger[][] a, BigInteger[] b, BigInteger maxSize) {
        BigInteger sumDepths = sumDepths(a, b, maxSize);
        BigInteger[] divrem = sumDepths.divideAndRemainder(maxSize);
        BigInteger tail = new BigInteger("1000000000000000").multiply(divrem[1]).divide(maxSize);
        return divrem[0].doubleValue() + tail.doubleValue() / 1e15;
    }

    private static BigInteger[][] wrap(int[][] a) {
        BigInteger[][] A = new BigInteger[a.length][];
        for (int i = 0; i < A.length; ++i) {
            A[i] = new BigInteger[a[i].length];
            for (int j = 0; j < A[i].length; ++j) {
                A[i][j] = BigInteger.valueOf(a[i][j]);
            }
        }
        return A;
    }
}
