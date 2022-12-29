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
            coeff[c] = (int) (sum >> 10);
        }
        return coeff;
    }
    int[][] reverse(int[] coeff) {
        int txW = 4 << txSize;
        int txH = 4 << txSize;
        int[][] reconst = new int[txH][txW];
        for (int c = 0; c < patrn.length; c++) {
            for (int i = 0; i < txH; i++) {
                for (int j = 0; j < txW; j++) {
                    reconst[i][j] += coeff[c] * ((patrn[c][i][j] + 256) / 2);
                }
            }
        }
        for (int i = 0; i < txH; i++) {
            for (int j = 0; j < txW; j++) {
                reconst[i][j] >>= 10;
            }
        }
        return reconst;
    }
}
