import java.io.*;
import java.util.*;

/**
 * 0/1 Knapsack Problem Solver
 * Implements Genetic Algorithm (GA) and Iterated Local Search (ILS)
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter seed value: ");
        long seed = scanner.nextLong();

        // Instance files are expected to be in the current working directory.
        File dir = new File(System.getProperty("user.dir"));
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("Working directory not found: " + dir.getAbsolutePath());
            System.exit(1);
        }

        // Known optimums (for display only, not used in algorithm)
        Map<String, Double> knownOptimums = new LinkedHashMap<>();
        knownOptimums.put("f1_l-d_kp_10_269", 295.0);
        knownOptimums.put("f2_l-d_kp_20_878", 1024.0);
        knownOptimums.put("f3_l-d_kp_4_20", 35.0);
        knownOptimums.put("f4_l-d_kp_4_11", 23.0);
        knownOptimums.put("f5_l-d_kp_15_375", 481.0694);
        knownOptimums.put("f6_l-d_kp_10_60", 52.0);
        knownOptimums.put("f7_l-d_kp_7_50", 107.0);
        knownOptimums.put("f8_l-d_kp_23_10000", 9767.0);
        knownOptimums.put("f9_l-d_kp_5_80", 130.0);
        knownOptimums.put("f10_l-d_kp_20_879", 1025.0);
        knownOptimums.put("knapPI_1_100_1000_1", 9147.0);

        // Instance file order
        String[] instanceNames = {
            "f1_l-d_kp_10_269", "f2_l-d_kp_20_878", "f3_l-d_kp_4_20",
            "f4_l-d_kp_4_11", "f5_l-d_kp_15_375", "f6_l-d_kp_10_60",
            "f7_l-d_kp_7_50", "f8_l-d_kp_23_10000", "f9_l-d_kp_5_80",
            "f10_l-d_kp_20_879", "knapPI_1_100_1000_1"
        };

        // Results storage for Wilcoxon test
        List<Double> ilsResults = new ArrayList<>();
        List<Double> gaResults = new ArrayList<>();

        System.out.println("\n" + "=".repeat(100));
        System.out.printf("%-25s %-6s %15s %15s %15s%n","Problem Instance", "Algo", "Best Solution", "Known Optimum", "Runtime (s)");
        System.out.println("=".repeat(100));

        int numRuns = 30 ;
        Random seedRandom = new Random(seed);

        for (String name : instanceNames) {
            File f = new File(dir, name);
            if (!f.exists()) {
                // System.out.println("File not found, skipping: " + name);
                continue;
            }
            KnapsackInstance instance = KnapsackInstance.load(f);

            double knownOpt = knownOptimums.getOrDefault(name, -1.0);
            double bestILS = Double.NEGATIVE_INFINITY;
            double bestGA = Double.NEGATIVE_INFINITY;
            double bestILSTime = 0;
            double bestGATime = 0;

            for (int run = 0; run < numRuns; run++) {
                long ilsSeed = seedRandom.nextLong();
                long startILS = System.currentTimeMillis();
                ILS ils = new ILS(instance, ilsSeed);
                double ilsResult = ils.run();
                double ilsTime = (System.currentTimeMillis()-startILS)/1000.0;
                if (ilsResult > bestILS){
                    bestILS = ilsResult;
                    bestILSTime = ilsTime;
                } 

                long gaSeed = seedRandom.nextLong();
                long startGa = System.currentTimeMillis(); 
                GeneticAlgorithm ga = new GeneticAlgorithm(instance, gaSeed);
                double gaResult = ga.run();
                double gaTime = (System.currentTimeMillis()- startGa) / 1000.0;
                if (gaResult > bestGA){
                    bestGA = gaResult;
                    bestGATime = gaTime;
                }
            }
                ilsResults.add(bestILS);
                gaResults.add(bestGA);
    

                String optStr = (knownOpt < 0) ? "N/A" : String.format("%.4f", knownOpt);
                System.out.printf("%-25s %-6s %15.4f %15s %15.4f%n",
                    name, "ILS", bestILS, optStr, bestILSTime);
                System.out.printf("%-25s %-6s %15.4f %15s %15.4f%n",
                    "", "GA", bestGA, optStr, bestGATime);
                System.out.println("-".repeat(100));
        }

        // Wilcoxon signed-rank test (one-tailed, 5% level)
        System.out.println("\n" + "=".repeat(60));
        System.out.println("STATISTICAL ANALYSIS - One-tailed Wilcoxon Signed-Rank Test");
        System.out.println("H0: ILS and GA perform equivalently");
        System.out.println("H1: ILS performs better than GA (one-tailed, alpha=0.05)");
        System.out.println("=".repeat(60));
        WilcoxonTest.run(ilsResults, gaResults);
    }
}
