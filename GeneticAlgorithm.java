
import java.util.*;


public class GeneticAlgorithm{

    private static final int POP_SIZE=100;
    private static final int MAX_GEN=500;
    private static final double CROSSOVER_RATE=0.85;
    private static final int ELITES=2;
    private static final int TOURNAMENT_K=3;

    private final KnapsackInstance instance;
    private final Random rnd;
    
    public GeneticAlgorithm(KnapsackInstance instance, long seed){

        this.instance=instance;
        this.rnd=new Random(seed);
    }

    public double run(){

        int n=instance.n;
        double mutationRate=1.0/n;

        Solution[] pop=new Solution[POP_SIZE];

        for(int i=0; i<POP_SIZE; i++){
            pop[i]=new Solution(randomSolution(n));
            repair(pop[i]);
            pop[i].fitness=instance.evaluate(pop[i].genes);
        }

        double bestFitness=Double.NEGATIVE_INFINITY;

        for (int gen=0;gen<MAX_GEN;gen++){

            Arrays.sort(pop, (a, b) -> Double.compare(b.fitness, a.fitness));

            if(pop[0].fitness > bestFitness){
                bestFitness=pop[0].fitness;
            }

            Solution[] newPop=new Solution[POP_SIZE];

            for(int e=0; e<ELITES; e++){
                newPop[e]=pop[e].clone();
            }

            
            for(int i=ELITES; i<POP_SIZE; i++){
                int[] parent1=tournamentSelect(pop);
                int[] child;

                if(rnd.nextDouble()<CROSSOVER_RATE){
                    int[] parent2=tournamentSelect(pop);
                    child=singlePointCrossover(parent1, parent2);

                } else {
                    child=parent1.clone();
                }

                bitFlipMutate(child, mutationRate);
                Solution childSol=new Solution(child);
                repair(childSol);
                childSol.fitness=instance.evaluate(childSol.genes);
                newPop[i]=childSol;
            }

            pop=newPop;
        }

        for(int i=0; i<POP_SIZE; i++){
            double f=pop[i].fitness;
            if(f>bestFitness) bestFitness=f;
        }

        return bestFitness;
    }

    private int[] tournamentSelect(Solution[] pop){

        int best=rnd.nextInt(POP_SIZE);

        for (int k=1; k<TOURNAMENT_K; k++){

            int candidate=rnd.nextInt(POP_SIZE);

            if(pop[candidate].fitness > pop[best].fitness){
                best=candidate;
                }
        }

        return pop[best].genes.clone();
    }

    private int[] singlePointCrossover(int[] parent1, int[] parent2){

        int n=parent1.length;

        int point=1+rnd.nextInt(n - 1);
        int[] child=new int[n];

        for(int i=0; i<point; i++){
            child[i]=parent1[i];
            }

        for(int i=point; i<n; i++){ 
            child[i]=parent2[i]; 
            }

        return child;
    }

    private void bitFlipMutate(int[] sol, double mutRate){

        for(int i=0; i<sol.length; i++){

            if(rnd.nextDouble()< mutRate) sol[i]^=1;
        }
    }

    private int[] randomSolution(int n){

        int[] sol=new int[n];
        for(int i=0; i<n; i++){
            sol[i]=rnd.nextInt(2); 
            }

        return sol;
    }

    private void repair(Solution sol){
        int n=instance.n;

        while(instance.totalWeight(sol.genes)>instance.capacity){
            double worstRatio=Double.MAX_VALUE;
            int worstIdx=-1;

            for(int i=0; i<n; i++){

                if(sol.genes[i]==1){
                    double ratio=(instance.weights[i]>0)? instance.values[i] / instance.weights[i]:instance.values[i];

                    if(ratio<worstRatio){ 
                        worstRatio=ratio; worstIdx=i; 
                        }
                }
            }

            if(worstIdx==-1) break;
            sol.genes[worstIdx]=0;
        }
    }
 
}