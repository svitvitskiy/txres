import jdk.incubator.vector.*;

import static jdk.incubator.vector.VectorOperators.ADD;
import static jdk.incubator.vector.VectorOperators.ASHR;

public class Main {
    static final VectorSpecies<Byte> SPECIES = ByteVector.SPECIES_PREFERRED;
    static long sadAvx256(byte[] src, byte[] ref) {
        long result = 0;
        for (int i = 0; i < (src.length & ~0x1f); i += 32) {
            var va = ByteVector.fromArray(SPECIES, src, i).lanewise(ASHR, 1).add((byte)64);
            var vb = ByteVector.fromArray(SPECIES, ref, i).lanewise(ASHR, 1).add((byte)64);
            var vc = va.sub(vb).abs();
            var b0_2 = vc.and((byte)0x7);
            var b3_5 = vc.and((byte)0x38).lanewise(ASHR, 3);
            var b6_7 = vc.and((byte)0xc0).lanewise(ASHR, 6);
            result += b0_2.reduceLanes(ADD) + (b3_5.reduceLanes(ADD) << 3) + (b6_7.reduceLanes(ADD) << 6);
        }
        return result;
    }

    static long sadSW(byte[] src, byte[] ref) {
        long result = 0;
        for (int i = 0; i < src.length; ++ i) {
            int res = ((src[i] + 128) >> 1) - ((ref[i] + 128) >> 1);
            res = res > 0 ? res : -res;
            result += res;
        }
        return result;
    }

    public static void main1(String[] args) {
        byte[] a = new byte[1 << 20];
        byte[] b = new byte[1 << 20];

        for (int i = 0; i < (1 << 20); i++) {
            a[i] = (byte) i;
            b[i] = (byte) (i*2);
        }

        for (int i = 0; i < 128; i++) {
            long time = System.currentTimeMillis();
            long value;
            if (SPECIES.length() == 32)
              value = sadAvx256(a, b);
            else
              value = sadSW(a, b);
            long done = System.currentTimeMillis();

            System.out.println("Done:" + (done - time));
            System.out.println(value);
        }
    }
    
    public static void print1D(int[] arr, String format, boolean clamp) {
        for (int i = 0; i < arr.length; i++) {
            int val = (arr[i] < 0 && clamp) ? -arr[i] : arr[i];
            System.out.print(String.format(format, val) + ", ");
        }
        System.out.println();
    }
    public static void print2D(int[][] arr, String format, boolean clamp) {
        for (int i = 0; i < arr.length; i++)
            print1D(arr[i], format, clamp);
    }
    public static void main(String[] args) {
        int[][] patch0 = {
            {0, 0, 0xff, 0xff},
            {0, 0, 0xff, 0xff},
            {0, 0, 0xff, 0xff},
            {0, 0, 0xff, 0xff}
        };
        int[][] patch1 = {
            {0xff, 0xff, 0, 0},
            {0xff, 0xff, 0, 0},
            {0xff, 0xff, 0, 0},
            {0xff, 0xff, 0, 0}
        };
        int[][] patch2 = {
            {0xff, 0xff, 0xff, 0xff},
            {0xff, 0xff, 0xff, 0xff},
            {0xff, 0xff, 0xff, 0xff},
            {0xff, 0xff, 0xff, 0xff}
        };
        int[][] patch3 = {
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0}
        };
        int[][] patch5 = {
            {0xff, 0xff, 0x0,  0x0 },
            {0xff, 0xff, 0x0,  0x0 },
            {0xff, 0xff, 0xff, 0xff},
            {0xff, 0xff, 0xff, 0xff}
        };

        int[][] patch6 = {
                {0x00, 0x10, 0x20, 0x30, 0x00, 0x10, 0x20, 0x30},
                {0x40, 0x50, 0x60, 0x70, 0x40, 0x50, 0x60, 0x70},
                {0x80, 0x90, 0xa0, 0xb0, 0x80, 0x90, 0xa0, 0xb0},
                {0xc0, 0xd0, 0xe0, 0xf0, 0xc0, 0xd0, 0xe0, 0xf0},
                {0x00, 0x10, 0x20, 0x30, 0x00, 0x10, 0x20, 0x30},
                {0x40, 0x50, 0x60, 0x70, 0x40, 0x50, 0x60, 0x70},
                {0x80, 0x90, 0xa0, 0xb0, 0x80, 0x90, 0xa0, 0xb0},
                {0xc0, 0xd0, 0xe0, 0xf0, 0xc0, 0xd0, 0xe0, 0xf0}
        };

        int[][] patch43 = {
                {162, 71, 1, 39, 138, 176, 106, 15},
                {9,	4, 0, 2, 8,	10,	6, 1},
                {225, 99, 2, 55, 191, 244, 147, 21},
                {72, 32, 1,	18,	61,	78,	47,	7},
                {72, 32, 1,	18,	61,	78,	47,	7},
                {225, 99, 2, 55, 191, 244, 147, 21},
                {9,	4, 0, 2, 8,	10,	6, 1},
                {162, 71, 1, 39, 138, 176, 106, 15}
        };

        int[][] patch42 = {
                {0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20},
                {0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20},
                {0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20},
                {0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20},
                {0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20},
                {0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20},
                {0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20},
                {0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20}
        };
        int[][] patch41 = {
                {0xF6, 0xB1, 0x4F, 0x0A, 0x0A, 0x4F, 0xB1, 0xF6},
                {0xF6, 0xB1, 0x4F, 0x0A, 0x0A, 0x4F, 0xB1, 0xF6},
                {0xF6, 0xB1, 0x4F, 0x0A, 0x0A, 0x4F, 0xB1, 0xF6},
                {0xF6, 0xB1, 0x4F, 0x0A, 0x0A, 0x4F, 0xB1, 0xF6},
                {0xF6, 0xB1, 0x4F, 0x0A, 0x0A, 0x4F, 0xB1, 0xF6},
                {0xF6, 0xB1, 0x4F, 0x0A, 0x0A, 0x4F, 0xB1, 0xF6},
                {0xF6, 0xB1, 0x4F, 0x0A, 0x0A, 0x4F, 0xB1, 0xF6},
                {0xF6, 0xB1, 0x4F, 0x0A, 0x0A, 0x4F, 0xB1, 0xF6}
        };
        int[][] patch44 = {
                {251, 233, 198, 153, 103, 58, 23, 5},
                {233, 217, 187, 149, 107, 69, 39, 23},
                {198, 187, 168, 142, 114, 88, 69, 58},
                {153, 149, 142, 133, 123, 114, 107, 103},
                {103, 107, 114, 123, 133, 142, 149, 153},
                {58, 69, 88, 114, 142, 168, 187, 198},
                {23, 39, 69, 107, 149, 187, 217, 233},
                {5, 23, 58, 103, 153, 198, 233, 251}
        };
        int[][] patch4 = {
                {176, 169, 155, 138, 118, 101, 87, 80},
                {12, 29, 62, 105, 151, 194, 227, 244},
                {244, 227, 194, 151, 105, 62, 29, 12},
                {80, 87, 101, 118, 138, 155, 169, 176},
                {80, 87, 101, 118, 138, 155, 169, 176},
                {244, 227, 194, 151, 105, 62, 29, 12},
                {12, 29, 62, 105, 151, 194, 227, 244},
                {176, 169, 155, 138, 118, 101, 87, 80}
        };

        AnyTx tx = new AnyTx(1, AnyTx.TxType.DCT);
        int[] coeff = tx.forward(patch4);
        print1D(coeff, "%d", false);
        int[][] reconst = tx.reverse(coeff);
        print2D(reconst, "%02x", true);
    }
}