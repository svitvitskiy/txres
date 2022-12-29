//            0, 0        1, 0        0, 1      1, 1
//         |  0 0 0 0  |  0 0 1 1  |  0 1 0 1 | 0 1 1 0
// 0 0 0 0 |  0        | 1         |        4 | 8
// 0 0 1 1 |  2        | 3         |        7 | 12
// 0 1 0 1 |  5        | 6         |       11 | 14
// 1 0 0 1 |  9        | 10        |       13 | 15

// 0, 0, 0           1, 0, 0           0, 1, 0           1, 1, 0           0, 0, 1           1, 0, 1           0, 1, 1           1, 1, 1
// 0 0 0 0 0 0 0 0   0 0 0 0 1 1 1 1   0 0 0 0 0 0 0 0   0 0 0 0 1 1 1 1   0 0 0 0 0 0 0 0   0 0 0 0 1 1 1 1   0 0 0 0 0 0 0 0   0 0 0 0 1 1 1 1
// 0, 4, 2, 6, 1, 5, 3, 7
public class HaarTx {
    public int generateScan(int sz, int[] zigzag, int count, int off, int stride) {
        if (sz == 0) {
            zigzag[off + 0] = count++;
            zigzag[off + 1] = count++;
            zigzag[off + stride + 0] = count++;
            zigzag[off + stride + 1] = count++;
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
     * @param txSize 0: 4x4, 1: 8x8 ... etc
     * @return
     */
    public boolean[][][] generatePatrn(int txSize) {
        int txW = 4 << txSize;
        int txH = 4 << txSize;
        int nPat = txW * txH;
        boolean[][][] out = new boolean[nPat][txH][txW];
        int sizes = txSize + 2;
        for (int pi = 0; pi < nPat; pi++) {
            int p = scan[pi];
            for (int i = 0; i < txH; i++) {
                for (int j = 0; j < txW; j++) {
                    out[p][i][j] = false;
                    for (int sz = 0; sz < sizes; sz++) {
                        int horPat = (p >> sz) & 1;
                        int verPat = (p >> (sizes + sz)) & 1;
                        int x      = (j >> (sizes - sz - 1)) & 1;
                        int y      = (i >> (sizes - sz - 1)) & 1;
                        out[p][i][j] ^= horPat!= 0 && x != 0;
                        out[p][i][j] ^= verPat != 0 && y != 0;
                    }
                }
            }
        }
        return out;
    }

    private int txSize;
    private boolean[][][] patrn;
    private int[] scan;

    /**
     *
     * @param txSize 0: 4x4, 1: 8x8 ... etc
     */
    HaarTx(int txSize) {
        this.txSize = txSize;
        int txW = 4 << txSize;
        int txH = 4 << txSize;
        int nPat = txW * txH;
        scan = new int[nPat];
        generateScan(txSize + 1, scan, 0,0, txW);
        patrn = generatePatrn(txSize);
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
                    sum += (!patrn[c][i][j] ? 0xff : -0xff) * patch[i][j];
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
                    reconst[i][j] += coeff[c] * (!patrn[c][i][j] ? 0xff : -0xff);
                }
            }
        }
        for (int i = 0; i < txH; i++) {
            for (int j = 0; j < txW; j++) {
                reconst[i][j] >>= 12;
            }
        }
        return reconst;
    }
}
