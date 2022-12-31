import jdk.incubator.vector.*;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static void main7(String[] args) {
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
        int[][] patch46 = {
                {251, 233, 198, 153, 103, 58, 23, 5},
                {233, 217, 187, 149, 107, 69, 39, 23},
                {198, 187, 168, 142, 114, 88, 69, 58},
                {153, 149, 142, 133, 123, 114, 107, 103},
                {103, 107, 114, 123, 133, 142, 149, 153},
                {58, 69, 88, 114, 142, 168, 187, 198},
                {23, 39, 69, 107, 149, 187, 217, 233},
                {5, 23, 58, 103, 153, 198, 233, 251}
        };
        int[][] patch45 = {
                {176, 169, 155, 138, 118, 101, 87, 80},
                {12, 29, 62, 105, 151, 194, 227, 244},
                {244, 227, 194, 151, 105, 62, 29, 12},
                {80, 87, 101, 118, 138, 155, 169, 176},
                {80, 87, 101, 118, 138, 155, 169, 176},
                {244, 227, 194, 151, 105, 62, 29, 12},
                {12, 29, 62, 105, 151, 194, 227, 244},
                {176, 169, 155, 138, 118, 101, 87, 80}
        };
        int[][] patch47 = {
                {0x16, 0x1b, 0x1a, 0x1d, 0x1b, 0x20, 0x20, 0x1b},
                {0x1d, 0x19, 0x18, 0x1b, 0x1c, 0x1f, 0x1c, 0x2a},
                {0x1f, 0x18, 0x1b, 0x1a, 0x1c, 0x1a, 0x0e, 0x25},
                {0x1c, 0x1c, 0x1f, 0x1a, 0x19, 0x18, 0x07, 0x13},
                {0x1b, 0x20, 0x1e, 0x1b, 0x18, 0x1b, 0x11, 0x0e},
                {0x1c, 0x1e, 0x1b, 0x1e, 0x1b, 0x1c, 0x1a, 0x12},
                {0x1d, 0x1d, 0x1b, 0x21, 0x1d, 0x1d, 0x1c, 0x18},
                {0x20, 0x1f, 0x1c, 0x20, 0x1d, 0x1f, 0x1f, 0x22},
        };
        int[][] patch4 = {
                {0x0e, 0x5a, 0xcf, 0x99, 0x3e, 0x63, 0x95, 0xa5},
                {0x1a, 0x5c, 0xc0, 0xc1, 0x4c, 0x39, 0x8b, 0xa2},
                {0x89, 0x60, 0xb0, 0xbf, 0x97, 0x28, 0x48, 0x9f},
                {0x92, 0x5c, 0x8d, 0xbc, 0x9d, 0x2a, 0x4d, 0x99},
                {0x9c, 0x73, 0x7a, 0xb5, 0x87, 0x2e, 0x3f, 0x8e},
                {0x93, 0x80, 0x77, 0x9d, 0x74, 0x59, 0x8a, 0x95},
                {0xa7, 0x7f, 0x63, 0x9c, 0x81, 0x71, 0x94, 0x92},
                {0x9f, 0x8f, 0x69, 0x94, 0x8a, 0x74, 0x8e, 0x91},
        };

        AnyTx tx = new AnyTx(1, AnyTx.TxType.DCT);
        int[] coeff = tx.forward(patch4);
        print2D(patch4, "%02x", true);
        print1D(coeff, "%d", false);
        int[][] reconst = tx.reverse(coeff);
        print2D(reconst, "%02x", true);
        int[][] err = calcErr(reconst, patch4);
        System.out.println();
        print2D(err, "%3d", false);
        System.out.println("PSNR: " + calcPSNR(err));
    }

    public static void main5(String[] args) {
        AnyTx tx = new AnyTx(1, AnyTx.TxType.DCT);
        for (int i = 0; i < 64; i++) {
            for (int j = i; j < 64; j++) {
                for (int k = 0; k < 64; k++) {
                    for (int l = 0; l < 64; l++) {
                        int[] coeff0 = new int[64];
                        coeff0[i] = 4096;
                        coeff0[j] = 2048;
                        coeff0[k] = 1024;
                        coeff0[l] = 1024;
                        int[][] patch4 = tx.reverse(coeff0);
                        int[] coeff1 = tx.forward(patch4);
                        //print2D(patch4, "%02x", true);
                        //print1D(coeff1, "%d", false);
                        int[][] reconst = tx.reverse(coeff1);
                        //print2D(reconst, "%02x", true);
                        int[][] err = calcErr(reconst, patch4);
                        //System.out.println();
                        //print2D(err, "%3d", false);
                        double psnr = calcPSNR(err);
                        if (psnr < 40) {
                            print2D(patch4, "%02x", true);
                            System.out.println("[" + i + "] PSNR: " + psnr);
                            print1D(coeff0, "%d", false);
                            print1D(coeff1, "%d", false);
                        }
                    }
                }
            }
        }
    }

    private static double calcPSNR(int[][] err) {
        long sum = 0;
        for (int i = 0; i < err.length; i++) {
            for (int j = 0; j < err[i].length; j++) {
                sum += err[i][j] * err[i][j];
            }
        }
        return 10 * Math.log10((err.length * err[0].length * 255d * 255d) / sum);
    }

    private static int[][] calcErr(int[][] reconst, int[][] patch4) {
        int[][] result = new int[reconst.length][reconst[0].length];
        for (int i = 0; i < reconst.length; i++) {
            for (int j = 0; j < reconst[i].length; j++) {
                result[i][j] = reconst[i][j] - patch4[i][j];
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Please specify the file name");
            return;
        }
        String fileName = args[0];
        Pattern p = Pattern.compile(".*_(\\d+)x(\\d+)\\..*");
        Matcher m = p.matcher(fileName);
        if (!m.matches()) {
            System.out.println("Filename should be in <name>_WxH.<ext> format");
            return;
        }
        int w = Integer.parseInt(m.group(1));
        int h = Integer.parseInt(m.group(2));
        int cw = (w+1)>>1;
        int ch = (h+1)>>1;
        byte[] buf = new byte[w*h];
        byte[] bad = new byte[cw*ch];
        byte[] cool = new byte[cw*ch];
        File inpFile = new File(fileName);
        InputStream is = new FileInputStream(inpFile);
        int read = is.read(buf);
        is.close();
        System.out.println(read);

        double totalPsnr = 0;
        int psnrPts = 0;
        AnyTx tx0 = new AnyTx(1, AnyTx.TxType.DCT);
        HaarTx tx = new HaarTx(1);
        for (int i = 0; i < h; i += 8) {
            if (i + 8 >= h)
                continue;
            for (int j = 0; j < w; j += 8) {
                if (j + 8 >= w)
                    continue;
                int[][] patch = extract(buf, w, j, i, 8, 8);
                int[] coeff = tx.forward(patch);
                int[][] reconst = tx.reverse(coeff);
                int[][] err = calcErr(reconst, patch);
                double psnr = calcPSNR(err);
//                if (psnr < 36) {
//                    print2D(patch, "%02x", true);
//                    print1D(coeff, "%d", false);
//                    print2D(reconst, "%02x", true);
//                    System.out.println();
//                    print2D(err, "%2d", false);
//                }
                double tmp = 40 - psnr;
                tmp = tmp <  0 ?  0 : tmp;
                tmp = tmp > 20 ? 20 : tmp;
                fill(bad, cw, j/2, i/2, 4, 4, (int)(tmp * 6 + 128));
                fill(cool, cw, j/2, i/2, 4, 4, 128);
                System.out.println("[" + (j/8) + "," + (i/8) + "] PSNR: " + psnr);
                if (totalPsnr < 100) {
                    totalPsnr += psnr;
                    ++psnrPts;
                }
            }
        }
        System.out.println("Avg psnr: " + (totalPsnr / psnrPts));
        OutputStream out = new FileOutputStream(new File(inpFile.getParentFile(),"result.yuv"));
        out.write(buf);
        out.write(cool);
        out.write(bad);
        out.close();
    }

    private static void fill(byte[] buf, int stride, int x, int y, int w, int h, int val) {
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                buf[(y + i) * stride + x + j] = (byte) val;
            }
        }
    }

    private static int[][] extract(byte[] buf, int stride, int x, int y, int w, int h) {
        int[][] result = new int[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                result[i][j] = buf[(y + i)*stride + x + j] & 0xff;
            }
        }
        return result;
    }
}