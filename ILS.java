import java.util.*;

// Local search: Best-improvement bit-flip hill climbing
// Perturbation: Random double-bridge style (flip k random bits, where k = max(2, n/5))

public class ILS{

    private static final int MAX_ITERATIONs = 1000;
    private static final int IMPROVE_LIMIT = 100;

    private final KnapsackInstance inst;
    private final Random luck;

    public ILS(KnapsackInstance inst, long seed){
        this.inst = inst;
        this.luck = new Random(seed);
    }

    public double run(){
        int n = inst.n;
        int perturbK = Math.max(2, n/5);

        //Build greedy initial solution
        int[] current = greedyInit();
        current = localSearch(current);
        double bestFitness = inst.evaluate(current);
        int[] bestSol = current.clone();

        int noImprove = 0;

        for(int i=0; i<MAX_ITERATIONs; i++){
            // Perturbation
            int[] perturbed = perturb(current, perturbK);
            // Local search from perturbed
            int[] localOpt = localSearch(perturbed);
            double localFitness = inst.evaluate(localOpt);

            // Acceptance criterion: non-worsening
            if(localFitness >= inst.evaluate(current)){
                current = localOpt;
                noImprove = (localFitness > bestFitness) ? 0 : noImprove + 1;
                if(localFitness > bestFitness){
                    bestFitness = localFitness;
                    bestSol = localOpt.clone();
                }
            }else{
                noImprove++;
            }

            // Restart from best if stuck
            if(noImprove >= IMPROVE_LIMIT){
                current = bestSol.clone();
                // Small random perturbation to escape
                current = perturb(current, Math.max(1, perturbK / 2));
                current = localSearch(current);
                noImprove = 0;
                double newFit = inst.evaluate(current);
                if(newFit > bestFitness){ 
                    bestFitness = newFit; bestSol = current.clone(); 
                }
                current = bestSol.clone();
            }
        }
        return bestFitness;
    }

    // Greedy construction, sort items by value/weight ratio descending,
    private int[] greedyInit(){
        int n = inst.n;
        Integer[] order = new Integer[n];
        for(int i = 0; i < n; i++){
            order[i] = i;
        }
        Arrays.sort(order, (a, b) ->{
            double ratioA = (inst.weights[a] > 0) ? inst.values[a] / inst.weights[a] : Double.MAX_VALUE;
            double ratioB = (inst.weights[b] > 0) ? inst.values[b] / inst.weights[b] : Double.MAX_VALUE;
            return Double.compare(ratioB, ratioA);
        });
        int[] sol = new int[n];
        double currentWeight = 0;
        for(int idx : order){
            if(currentWeight + inst.weights[idx] <= inst.capacity){
                sol[idx] = 1;
                currentWeight += inst.weights[idx];
            }
        }
        return sol;
    }

    // Best-improvement bit-flip local search.
    private int[] localSearch(int[] sol){
        int n = inst.n;
        sol = sol.clone();
        boolean improved = true;
        while(improved){
            improved = false;
            double currentFitness = inst.evaluate(sol);
            int bestIdx = -1;
            double bestGain = 0;
            for(int i = 0; i < n; i++){
                sol[i] ^= 1;
                double newFit = inst.evaluate(sol);
                double gain = newFit - currentFitness;
                if(gain > bestGain){
                    bestGain = gain; bestIdx = i;
                }
                sol[i] ^= 1; 
            }
            if(bestIdx >= 0){
                sol[bestIdx] ^= 1;
                improved = true;
            }
        }
        return sol;
    }

    // Perturbation: randomly flip k bits 
    private int[] perturb(int[] sol, int k){
        int n = inst.n;
        int[] perturbed = sol.clone();
        Set<Integer> flipped = new HashSet<>();
        while(flipped.size() < Math.min(k, n)){
            int idx = luck.nextInt(n);
            if(flipped.add(idx)) perturbed[idx] ^= 1;
        }
        // Repair if infeasible
        repair(perturbed);
        return perturbed;
    }

    // Remove lowest value/weight ratio items until feasible
    private void repair(int[] sol){
        int numItems = inst.n;
        while(inst.totalWeight(sol) > inst.capacity){
            double worstRatio = Double.MAX_VALUE;
            int worstIdx = -1;
            for(int i = 0; i < numItems; i++){
                if(sol[i] == 1){
                    double ratio = (inst.weights[i] > 0) ? inst.values[i] / inst.weights[i] : inst.values[i];
                    if(ratio < worstRatio){
                        worstRatio = ratio; 
                        worstIdx = i; 
                    }
                }
            }
            if(worstIdx == -1) break;
            sol[worstIdx] = 0;
        }
    }
}
