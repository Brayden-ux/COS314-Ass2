import java.io.*;
import java.util.*;

public class KnapsackInstance{

    public final int n;//num items        
    public final double capacity;
    public final double[] weights;
    public final double[] values;
    public final String name;

    public KnapsackInstance(String name, int n, double capacity, double[] weights, double[] values) {
        this.name=name;
        this.n=n;
        this.capacity=capacity;
        this.weights=weights;
        this.values=values;
    }

    public double evaluate(int[] solution){
        double totalWeight=0, totalValue=0;

        for(int i=0;i<n;i++){

            if(solution[i] == 1){
                totalWeight +=weights[i];
                totalValue +=values[i];
            }
        }

        return (totalWeight<=capacity)? totalValue:0.0;
    }

    public double totalWeight(int[] solution){
        double weight=0;

        for(int i=0; i<n; i++){ 

            if(solution[i]==1) {
                weight+=weights[i];
              }
            }

        return weight;
    }

    public static KnapsackInstance load(File file) throws IOException{
        BufferedReader br=new BufferedReader(new FileReader(file));

        StringTokenizer st=new StringTokenizer(br.readLine().trim());

        int n=Integer.parseInt(st.nextToken());


        double capacity=Double.parseDouble(st.nextToken());

        double[] weights=new double[n];
        double[] values=new double[n];

        for(int i=0; i<n;i++){

            String line=br.readLine();

            if(line == null) break;

            st=new StringTokenizer(line.trim());

            values[i]=Double.parseDouble(st.nextToken());
            weights[i]=Double.parseDouble(st.nextToken());

        }

        br.close();
        return new KnapsackInstance(file.getName(), n, capacity, weights, values);
    }

}