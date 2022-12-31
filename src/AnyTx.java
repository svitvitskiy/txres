public class AnyTx {
    public static enum TxType {
        LINEAR(true), DCT(true), SCALABLE(false);
        public boolean periodic;

        private TxType(boolean periodic) {
            this.periodic = periodic;
        }
    };
    public int generateScan(int sz, int[] zigzag, int count, int off, int stride) {
        if (sz == 0) {
            zigzag[count++] = off + 0;
            zigzag[count++] = off + 1;
            zigzag[count++] = off + stride + 0;
            zigzag[count++] = off + stride + 1;
        } else {
            count = generateScan(sz - 1, zigzag, count, off, stride);
            count = generateScan(sz - 1, zigzag, count, off + (1 << sz), stride);
            count = generateScan(sz - 1, zigzag, count, off + (stride << sz), stride);
            count = generateScan(sz - 1, zigzag, count, off + (stride << sz) + (1 << sz), stride);
        }
        return count;
    }
    /**
     *
     * @return
     */
    // 0 0 0 0 0 0 0 0   0 0 0 0 0 0 0 0   0 0 0 0 0 0 0 0   0 0 0 0 0 0 0 0   0 1 0 1 0 1 0 1   0 1 0 1 0 1 0 1   0 1 0 1 0 1 0 1   0 1 0 1 0 1 0 1
    // 0 0 0 0 0 0 0 0   0 0 0 0 0 0 0 0   0 0 1 1 0 0 1 1   0 0 1 1 0 0 1 1   0 0 0 0 0 0 0 0   0 0 0 0 0 0 0 0   0 0 1 1 0 0 1 1   0 0 1 1 0 0 1 1
    // 0 0 0 0 0 0 0 0   0 0 0 0 1 1 1 1   0 0 0 0 0 0 0 0   0 0 0 0 1 1 1 1   0 0 0 0 0 0 0 0   0 0 0 0 1 1 1 1   0 0 0 0 0 0 0 0   0 0 0 0 1 1 1 1
    // ---------------   ---------------   ---------------   ---------------   ---------------   ---------------   ---------------   ---------------
    // 0 0 0 0 0 0 0 0   0 0 0 0 1 1 1 1   0 0 1 1 0 0 1 1   0 0 1 1 1 1 0 0   0 1 0 1 0 1 0 1   0 1 0 1 1 0 1 0   0 1 1 0 0 1 1 0   0 1 1 0 1 0 0 1
    //
    // 0 0 0 0 0 0 0 0   0 1 2 3 4 5 6 8   0 2 5 8 0 2 5 8   0 2 5 8 8 5 2 0   0 8 0 8 0 8 0 8   0 8 0 8 8 0 8 0   0 8 8 0 0 8 8 0   0 8 8 0 8 0 0 8
    // 0 0 0 0 0 0 0 0   0 1 2 4 5 7 8 0   0 6 3 0 0 6 3 0   0 6 3 0 0 3 6 0   0 0 0 0 0 0 0 0   0 0 0 0 0 0 0 0   0 0 0 0 0 0 0 0   0 0 0 0 0 0 0 0
    public static int msb(int n) {
        if (n >= 128) return 7;
        else if (n >= 64) return 6;
        else if (n >= 32) return 5;
        else if (n >= 16) return 4;
        else if (n >= 8) return 3;
        else if (n >= 4) return 2;
        else if (n >= 2) return 1;
        else return 0;
    }

    private int[][][] generateWithPeriodic(int txSize, TxType txType) {
        int txW = 4 << txSize;
        int txH = 4 << txSize;
        int nPat = txW * txH;
        int[][][] out = new int[nPat][txH][txW];
        int sizes = txSize + 2;
        for (int pi = 0; pi < nPat; pi++) {
            int p = scan[pi];
            int patX = p % txW;
            int patY = p / txH;
            int scaleX = patX;
            int scaleY = patY;
            double stepX = (double)scaleX / txW;
            double stepY = (double)scaleY / txH;
            System.out.println("// " + p + " patX:" + patX + " patY:" + patY + " stepX:" + stepX + " stepY:" + stepY);
            for (int i = 0; i < txH; i++) {
                for (int j = 0; j < txW; j++) {
                    out[pi][i][j] = func( (j + 0.5) * stepX, (i + 0.5) * stepY, txType);
                    System.out.print(String.format("%4d ", out[pi][i][j]));
                }
                System.out.println();
            }
        }
        return out;
    }
    public int[][][] generatePatrn(int txSize, TxType txType) {
        if (txType.periodic) {
            return generateWithPeriodic(txSize, txType);
        } else {
            throw new IllegalArgumentException("Unsupported pattern");
        }
    }

    // Linear
    private int func(double x, double y, TxType txType) {
        if (txType == TxType.LINEAR) {
            double xx = x - (((int) x) & ~1);
            double x0 = xx < 1 ? 1 - 2 * xx : -3 + 2 * xx;
            double yy = y - (((int) y) & ~1);
            double y0 = yy < 1 ? 1 - 2 * yy : -3 + 2 * yy;
            return (int) (x0 * y0 * 0xff);
            //0:1  1:-1
            //1:2  -1:1
        } else {
            double x0 = Math.cos(x * Math.PI);
            double y0 = Math.cos(y * Math.PI);
            return (int) (x0 * y0 * 0xff);
        }
    }

    private int txSize;
    private int[][][] patrn;
    private int[] scan;

    /**
     *
     * @param txSize 0: 4x4, 1: 8x8 ... etc
     */
    AnyTx(int txSize, TxType txType) {
        this.txSize = txSize;
        int txW = 4 << txSize;
        int txH = 4 << txSize;
        int nPat = txW * txH;
        scan = new int[nPat];
        generateScan(txSize + 1, scan, 0,0, txW);
        patrn = generatePatrn(txSize, txType);
        if(!check()) {
            System.out.println("CRAP");
            System.exit(-1);
        }
    }
    int[] forward(int[][] patch) {
        int txW = 4 << txSize;
        int txH = 4 << txSize;
        if (patch.length != txH)
            throw new IllegalArgumentException("Wrong input size: " + patch.length + " vs " + txH);
        for (int i = 0; i < patch.length; i++) {
            if (patch[i].length != txW)
                throw new IllegalArgumentException("Wrong input size["+i+"]: " + patch[i].length + " vs " + txW);
        }
        int[] coeff = new int[patrn.length];
        for (int c = 0; c < patrn.length; c++) {
            long sum = 0;
            for (int i = 0; i < patch.length; i++) {
                for (int j = 0; j < patch[i].length; j++) {
                    sum += patrn[c][i][j] * (patch[i][j] - 128)*2;
                }
            }
            coeff[c] = (int) (sum >> 8);
        }
        return coeff;
    }
    int[][] reverse(int[] coeff) {
        int txW = 4 << txSize;
        int txH = 4 << txSize;
        int[][] reconst = new int[txH][txW];
        for (int c = 0; c < patrn.length; c++) {
            int p = scan[c];
            int px = p % txW;
            int py = p / txW;
            for (int i = 0; i < txH; i++) {
                for (int j = 0; j < txW; j++) {
                    reconst[i][j] += coeff[c] * patrn[c][i][j] * (px == 0 ? 1 : 2) * (py == 0 ? 1 : 2);
                }
            }
        }
        for (int i = 0; i < txH; i++) {
            for (int j = 0; j < txW; j++) {
                int val = reconst[i][j] >> 8;
                val /= txW * txH;
                reconst[i][j] = (val + 256) / 2;
            }
        }
        return reconst;
    }
    double angle(int[] v0, int[] v1) {
        // angle through dotproduct
        long sum0 = 0, sum1 = 0, sum2 = 0;
        for (int i = 0; i < v0.length; i++) {
            sum0 += v0[i] * v1[i];
            sum1 += v0[i] * v0[i];
            sum2 += v1[i] * v1[i];
        }
        double acos = Math.acos(sum0 / Math.sqrt(sum1*sum2));
        return acos;
    }
    boolean orthogonal(int[] v0, int[] v1) {
        return angle(v0, v1) == Math.PI / 2;
    }

    int[] flatten2D(int[][] arr) {
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            count += arr[i].length;
        }
        int[] result = new int[count];
        for (int i = 0, cnt = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                result[cnt++] = arr[i][j];
            }
        }
        return result;
    }

    boolean check() {
        double epsilon = Math.PI / 180; // +-1 degree is fine
        // checks if this transform is orthogonal by checking each pair of vectors
        // to make sure they are orthogonal
        boolean a = true;
        for (int i = 0; i < patrn.length; i++) {
            for (int j = i + 1; j < patrn.length; j++) {
                double angle = angle(flatten2D(patrn[i]), flatten2D(patrn[j]));
//                if (angle > (Math.PI/2 + epsilon) || angle < (Math.PI/2 - epsilon)) {
//                    System.out.println("" + (i % 8) + "," + (i / 8) + " vs " + (j % 8) + "," + (j / 8) + ": " + (Math.PI / 2 - angle) * 180 / Math.PI);
//                }
                a = a && ((angle < Math.PI/2 + epsilon) && (angle > Math.PI/2 - epsilon));
            }
        }
        return a;
    }
}
