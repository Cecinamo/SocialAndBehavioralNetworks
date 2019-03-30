package SpreadOfInfluence;

import YESNOsupporters.Graph;
import YESNOsupporters.YESNOsupporters;
import it.stilo.g.structures.WeightedDirectedGraph;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Using a modified version of LPA (Label Propagation Algorithm start from the
 * provided one in the G library) that start assigning a label only for those
 * users that are classified with YES or NO estimates over the whole network
 * which party spread more over the network. How is the spread over the network
 * if: – only the identified k-Players K​ are used as seeds of the modified LPA?
 * (SKIP IT FOR GROUPS OF 1) –Using M ​–Using only the M’
 *
 * @author Cecilia Martinez Oliva
 */
public class Ex1 {

    private static WeightedDirectedGraph g;
    private static Graph ga;

    private static int[] initLabels(String yFile, String nFile) throws IOException {
        int[] labels = new int[g.size];

        YESNOsupporters s = new YESNOsupporters();
        List<String> y = new ArrayList<>();
        y.addAll(s.getIDs(yFile));
        int[] ynodes = ga.getNodes(y);
        List<String> n = new ArrayList<>();
        n.addAll(s.getIDs(nFile));
        int[] nnodes = ga.getNodes(n);

        for (int node : ynodes) {
            labels[node] = 1;
        }

        for (int node : nnodes) {
            labels[node] = 2;
        }

        return labels;
    }
    
    private static void results(int[] lpa) throws IOException {
        int ycount = 0;
        int ncount = 0;
        for(int r : lpa) {
            if(r==1)
                ycount++;
            if(r==2)
                ncount++;
        }
        
        System.out.println("YES: "+ycount);
        System.out.println("NO: "+ncount);
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {

        ga = new Graph();
        // let's load the graph
        g = ga.getGraph();

        // M YES-NO SUPPORTERS
        String yf = "YNsupporters/results1/Myes.txt";
        String nf = "YNsupporters/results1/Mno.txt";

        int[] lpaM = NewLPA.compute(g, initLabels(yf,nf), 0.001, 1);
        System.out.println("---------------------------------M--------------------------------------");
        results(lpaM);

        // M' YES-NO SUPPORTERS
        yf = "YNsupporters/results3/hubsyes.txt";
        nf = "YNsupporters/results3/hubsno.txt";

        int[] lpaM1 = NewLPA.compute(g, initLabels(yf,nf), 0.001, 1);
        System.out.println("---------------------------------M'--------------------------------------");
        results(lpaM1);
    }

}
