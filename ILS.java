import java.util.*;

// Local search: Best-improvement bit-flip hill climbing
// Perturbation: Random double-bridge style (flip k random bits, where k = max(2, n/5))

public class ILS{

    private static final int MAX_ITERATIONs = 50;
    private static final int IMPROVE_LIMIT = 10;

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
        Solution current = greedyInit();
        current = localSearch(current);
        Solution bestSol = current.clone();
        double bestFitness = bestSol.fitness;

        int noImprove = 0;

        for(int i=0; i<MAX_ITERATIONs; i++){
            // Perturbation
            Solution perturbed = perturb(current, perturbK);
            // Local search from perturbed
            Solution localOpt = localSearch(perturbed);
            double localFitness = localOpt.fitness;

            // Acceptance criterion: non-worsening
            if(localFitness >= current.fitness){
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
                // Start from best, then apply a smaller perturbation to escape
                current = localSearch(perturb(bestSol, Math.max(1, perturbK / 2)));
                noImprove = 0;
                if(current.fitness > bestFitness){
                    bestFitness = current.fitness;
                    bestSol = current.clone();
                }
                current = bestSol.clone();
            }
        }
        return bestFitness;
    }

    // Greedy construction, sort items by value/weight ratio descending,
    private Solution greedyInit(){
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
        int[] genes = new int[n];
        double currentWeight = 0;
        for(int idx : order){
            if(currentWeight + inst.weights[idx] <= inst.capacity){
                genes[idx] = 1;
                currentWeight += inst.weights[idx];
            }
        }
        Solution sol = new Solution(genes);
        sol.fitness = inst.evaluate(sol.genes);
        return sol;
    }

    // Best-improvement bit-flip local search.
    private Solution localSearch(Solution start){
        int n = inst.n;
        Solution sol = start.clone();
        if(sol.fitness == Double.NEGATIVE_INFINITY){
            sol.fitness = inst.evaluate(sol.genes);
        }
        boolean improved = true;
        while(improved){
            improved = false;
            double currentFitness = sol.fitness;
            int bestIdx = -1;
            double bestGain = 0;
            double bestNewFitness = currentFitness;
            for(int i = 0; i < n; i++){
                sol.genes[i] ^= 1;
                double newFit = inst.evaluate(sol.genes);
                double gain = newFit - currentFitness;
                if(gain > bestGain){
                    bestGain = gain;
                    bestIdx = i;
                    bestNewFitness = newFit;
                }
                sol.genes[i] ^= 1;
            }
            if(bestIdx >= 0){
                sol.genes[bestIdx] ^= 1;
                sol.fitness = bestNewFitness;
                improved = true;
            }
        }
        return sol;
    }

    // Perturbation: randomly flip k bits 
    private Solution perturb(Solution sol, int k){
        int n = inst.n;
        int[] perturbedGenes = sol.genes.clone();
        Set<Integer> flipped = new HashSet<>();
        while(flipped.size() < Math.min(k, n)){
            int idx = luck.nextInt(n);
            if(flipped.add(idx)) perturbedGenes[idx] ^= 1;
        }
        // Repair if infeasible
        repair(perturbedGenes);
        Solution out = new Solution(perturbedGenes);
        out.fitness = inst.evaluate(out.genes);
        return out;
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
