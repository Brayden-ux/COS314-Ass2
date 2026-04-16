

public class Solution{

        public int[] genes;
        public double fitness;

        public Solution(int[] genes){
            this.genes=genes;
            this.fitness=Double.NEGATIVE_INFINITY;
        }

        public Solution clone(){
            Solution copy= new Solution(this.genes.clone());
            copy.fitness=this.fitness;
            return copy;
        }
    }