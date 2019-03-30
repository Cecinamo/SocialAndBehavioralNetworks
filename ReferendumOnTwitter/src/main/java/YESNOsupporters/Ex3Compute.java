package YESNOsupporters;

import it.stilo.g.algo.HubnessAuthority;
import it.stilo.g.algo.SubGraph;
import it.stilo.g.structures.DoubleValues;
import it.stilo.g.structures.WeightedDirectedGraph;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 2.
 * Partitioning the users of M​ according to the candidates they mention (each
 * user can mention more that one candidate more than one time). Identify the
 * users mentioning more frequently each candidate or support YES/NO and measure
 * their centrality (Hubness Authority). Find the 500 (for each option YES/NO)
 * who both support the candidate frequently and are highly central (define some
 * combined measure to select such candidates and propose a method to give
 * sentiment to those mentions). Let Influencer M' ​in M​ be these users.
 *
 * @author Cecilia Martinez Oliva
 */
public class Ex3Compute {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        //String c = "yes";
        String c = "no";
        String results = "YNsupporters/results3";
        new File(results).mkdirs();
        YESNOsupporters s = new YESNOsupporters();
        Graph ga = new Graph();
        // let's load the graph, but only for the chosen supporters
        WeightedDirectedGraph g = ga.getGraph();
        List<String> ids = new ArrayList<>();
        ids.addAll(s.getIDs("YNsupporters/results1/M"+c+".txt"));
        int[] startingNodes = ga.getNodes(ids);
        WeightedDirectedGraph gs = SubGraph.extract(g, startingNodes, 2);
        // load the root
        List<String> root = s.getIDs("YNsupporters/results3/"+c+".txt");
        int[] nodes = ga.getNodes(root);
        // find S=B U R
        Set<Integer> ss = new HashSet<>();
        Collections.addAll(ss,Arrays.stream(nodes).boxed().toArray(Integer[]::new));
        ss.addAll(ga.getIns(nodes, gs));
        ss.addAll(ga.getOuts(nodes, gs));
        
        System.out.println("size S: "+ss.size());
        int[] s_nodes = ss.stream().mapToInt(Number::intValue).toArray();

        WeightedDirectedGraph gnew = SubGraph.extract(gs, s_nodes, 2);
        
        // compute  hits
        
        ArrayList<ArrayList<DoubleValues>> hits = HubnessAuthority.compute(gnew, 0.00001, 2);
        ga.getAuthorities(hits, 500, results + "/authorities"+c+".txt");
        ga.getHubs(hits, 500, results + "/hubs"+c+".txt");

    }

}
