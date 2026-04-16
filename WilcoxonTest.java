import java.util.*;

/**
 * One-tailed Wilcoxon Signed-Rank Test
 * H0: The medians (means) of the two algorithms are equivalent.
 * H1: ILS is better than GA (one-tailed).
 * Significance level: alpha = 0.05
 * Uses exact critical values for small n (n <= 25),
 * and normal approximation for larger n.
 */
public class WilcoxonTest {

    // Critical values W+ for one-tailed test at alpha=0.05
    // Index = n (number of non-zero differences), value = critical W+
    // Reject H0 if W+ >= criticalValue
    private static final int[] CRITICAL_W = {
        0,  // n=0 (unused)
        0,  // n=1
        0,  // n=2
        0,  // n=3
        0,  // n=4
        8,  // n=5
        11, // n=6
        13, // n=7
        16, // n=8
        19, // n=9
        21, // n=10
        24, // n=11
        28, // n=12
        29, // n=13
        33, // n=14
        37, // n=15
        41, // n=16
        45, // n=17
        49, // n=18
        54, // n=19
        59, // n=20
        64, // n=21
        69, // n=22
        75, // n=23
        80, // n=24
        86  // n=25
    };

    public static void run(List<Double> ilsScores, List<Double> gaScores) {
        int m = Math.min(ilsScores.size(), gaScores.size());
        System.out.println("Number of instances (n): " + m);

        // Compute differences d_i = ILS_i - GA_i
        double[] diffs = new double[m];
        for (int i = 0; i < m; i++) diffs[i] = ilsScores.get(i) - gaScores.get(i);

        // Remove ties (d_i == 0), collect absolute differences with sign
        List<double[]> nonZero = new ArrayList<>();
        for (double d : diffs){
            if (d != 0){
                nonZero.add(new double[]{Math.abs(d), Math.signum(d)});
            } 
        } 

        int n = nonZero.size();
        System.out.println("Non-zero differences: " + n);

        if (n == 0) {
            System.out.println("All differences are zero. Fail to reject H0.");
            return;
        }

        // Sort by absolute difference (for ranking)
        nonZero.sort(Comparator.comparingDouble(a -> a[0]));

        // Assign ranks (handle ties by average rank)
        double[] ranks = new double[n];
        int i = 0;
        while (i < n) {
            int j = i;
            while (j < n && nonZero.get(j)[0] == nonZero.get(i)[0]) j++;
            double avgRank = (i + 1 + j) / 2.0;
            for (int k = i; k < j; k++) ranks[k] = avgRank;
            i = j;
        }

        // Compute W+ (sum of ranks for positive differences)
        double wPlus  = 0, wMinus = 0;
        for (int k = 0; k < n; k++) {
            if (nonZero.get(k)[1] > 0){
                wPlus  += ranks[k];
            }
            else{
                wMinus += ranks[k];
            }                        
        }

        System.out.printf("W+ (ILS better) = %.1f%n", wPlus);
        System.out.printf("W- (GA better)  = %.1f%n", wMinus);

        boolean reject;
        String method;

        if (n <= 25) {
            int critical = CRITICAL_W[Math.min(n, 25)];
            System.out.printf("Critical value (n=%d, alpha=0.05, one-tailed): %d%n", n, critical);
            reject = wPlus >= critical;
            method = "exact table";
        } else {
            // Normal approximation
            double mean = n * (n + 1) / 4.0;
            double variance = n * (n + 1) * (2 * n + 1) / 24.0;
            double z = (wPlus - mean) / Math.sqrt(variance);
            System.out.printf("Z-score: %.4f (critical z for alpha=0.05 one-tailed: 1.6449)%n", z);
            reject = z >= 1.6449;
            method = "normal approximation";
        }

        System.out.println("Method: " + method);
        System.out.println();
        if (reject) {
            System.out.println("RESULT: Reject H0 at 5% significance level.");
            System.out.println("        ILS performs significantly better than GA.");
        } else {
            System.out.println("RESULT: Fail to reject H0 at 5% significance level.");
            System.out.println("        No statistically significant difference detected.");
        }

        // Print differences table
        System.out.println("\nDifferences per instance (ILS - GA):");
        System.out.printf("%-5s %12s %12s %12s%n", "Inst", "ILS", "GA", "Diff");
        for (int k = 0; k < m; k++) {
            System.out.printf("%-5d %12.4f %12.4f %12.4f%n",
                k+1, ilsScores.get(k), gaScores.get(k), diffs[k]);
        }
    }
}
