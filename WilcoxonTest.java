import java.util.*;


// One-tailed Wilcoxon Signed-Rank Test
// H0: The medians (means) of the two algorithms are equivalent.
// H1: ILS is better than GA (one-tailed).

public class WilcoxonTest {

    private static final double ALPHA = 0.05;
    private static final double EPS = 1e-12;

    // Exact one-tailed p-value for W+ under H0.
    // Uses subset-sum DP over ranks scaled by 2.
    private static double exactOneTailedPValue(double[] ranks, double observedWPlus){
        int n = ranks.length;

        int[] scaled = new int[n];
        int observedSum;
        {
            double scaledObserved = observedWPlus * 2.0;
            observedSum = (int)Math.round(scaledObserved);
            if(Math.abs(scaledObserved - observedSum) > 1e-9){
                observedSum = (int)Math.ceil(scaledObserved - 1e-9);
            }
        }

        int total = 0;
        for(int i = 0; i < n; i++){
            double scaledRank = ranks[i] * 2.0;
            int r = (int)Math.round(scaledRank);
            if(Math.abs(scaledRank - r) > 1e-9){
                // Fallback: force upward so we don't under-estimate p-value.
                r = (int)Math.ceil(scaledRank - 1e-9);
            }
            scaled[i] = r;
            total += r;
        }

        long[] ways = new long[total + 1];
        ways[0] = 1L;
        for(int r : scaled){
            for(int s = total; s >= r; s--){
                ways[s] += ways[s - r];
            }
        }

        long favorable = 0L;
        for(int s = Math.max(0, observedSum); s <= total; s++){
            favorable += ways[s];
        }

        // Total sign patterns = 2^n
        double totalPatterns = Math.pow(2.0, n);
        return favorable / totalPatterns;
    }

    public static void run(List<Double> ilsScores, List<Double> gaScores) {
        int m = Math.min(ilsScores.size(), gaScores.size());
        System.out.println("Number of instances (n): " + m);

        double[] diffs = new double[m];
        for (int i = 0; i < m; i++) diffs[i] = ilsScores.get(i) - gaScores.get(i);

        // Remove ties (d_i == 0), collect absolute differences with sign
        List<double[]> nonZero = new ArrayList<>();
        for (double d : diffs){
            if (Math.abs(d) > EPS){
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
            while (j < n && Math.abs(nonZero.get(j)[0] - nonZero.get(i)[0]) <= EPS) j++;
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

        double pValue = Double.NaN;

        if (n <= 25) {
            pValue = exactOneTailedPValue(ranks, wPlus);
            System.out.printf("Exact one-tailed p-value: %.6f (alpha=%.2f)%n", pValue, ALPHA);
            reject = pValue <= ALPHA;
            method = "exact p-value";
        } else {
            // Normal approximation
            double mean = n * (n + 1) / 4.0;
            double variance = n * (n + 1) * (2 * n + 1) / 24.0;
            // Continuity correction for upper-tail test
            double z = (wPlus - mean - 0.5) / Math.sqrt(variance);
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
