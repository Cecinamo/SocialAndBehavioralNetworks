package YESNOsupporters;

import it.stilo.g.algo.ConnectedComponents;
import it.stilo.g.algo.HubnessAuthority;
import it.stilo.g.algo.SubGraph;
import it.stilo.g.structures.DoubleValues;
import it.stilo.g.structures.WeightedDirectedGraph;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 1.
 * Using the provided Graph and the library G (see slides to obtain it) first
 * select the subgraph induced by users S(M)​ then find the largest connected
 * component CC​ and compute HITS on this subgraph. Then, find the 1000 highest
 * ranked (Authority) users. Who are they? Can be divided in YES and NO
 * supporters? Propose some metrics.
 *
 * @author Cecilia Martinez Oliva
 */
public class Ex2 {

    private static Set<Integer> largestComponent(WeightedDirectedGraph graph) throws InterruptedException {

        int s = 0;
        Set<Integer> lc = new HashSet<>();

        int[] all = new int[graph.size];
        for (int i = 0; i < graph.size; i++) {
            all[i] = i;
        }
        // per non farlo andare out of memory
        int t = 10000;
        int iter = graph.size / t + 1; //non posso fare più di mille query
        for (int i = 0; i < iter; i++) {
            int maxidx = t * (i + 1);
            if (maxidx > graph.size) {
                maxidx = graph.size;
            }
            int[] subset = Arrays.copyOfRange(all, t * i, maxidx);
            Set<Set<Integer>> comps = ConnectedComponents.rootedConnectedComponents(graph, subset, 2);
            for (Set<Integer> cc : comps) {
                if (cc.size() > s) {
                    s = cc.size();
                    lc = cc;
                }
            }

        }

        return lc;
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        String results = "YNsupporters/results2";
        new File(results).mkdirs();
        YESNOsupporters s = new YESNOsupporters();
        Graph ga = new Graph();
        WeightedDirectedGraph g = ga.getGraph();
        List<String> ids = new ArrayList<>();
        ids.addAll(s.getIDs("YNsupporters/results1/Myes.txt"));
        ids.addAll(s.getIDs("YNsupporters/results1/Mno.txt"));
        int[] nodes = ga.getNodes(ids);
        System.out.println("nodes:" + nodes.length);
        WeightedDirectedGraph gnew = SubGraph.extract(g, nodes, 2);

        // PROVA
        Set<Integer> n = new HashSet<>();
        for (int i = 0; i < gnew.in.length; i++) {
            if (gnew.in[i] != null) {
                for (int j = 0; j < gnew.in[i].length; j++) {
                    n.add(gnew.in[i][j]);
                }
            }

        }
        System.out.println(n.size());
        //

        Set<Integer> lc = largestComponent(gnew);
        System.out.println("lcc size: " + lc.size());
        int[] lca = lc.stream().mapToInt(i -> i).toArray();
        WeightedDirectedGraph lcGraph = SubGraph.extract(g, lca, 2);
        ga.saveGraph(lcGraph, results);
        ArrayList<ArrayList<DoubleValues>> hits = HubnessAuthority.compute(lcGraph, 0.00001, 2);
        ga.getAuthorities(hits, 1000, results + "/authorities.txt");
        ga.getHubs(hits, 1000, results + "/hubs.txt");
    }

}
