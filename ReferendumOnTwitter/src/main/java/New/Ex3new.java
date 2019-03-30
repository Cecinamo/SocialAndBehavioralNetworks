package New;

import Indices.Writer;
import TemporalAnalysis.TemporalAnalysis;
import it.stilo.g.algo.ConnectedComponents;
import it.stilo.g.algo.CoreDecomposition;
import it.stilo.g.algo.SubGraph;
import it.stilo.g.structures.Core;
import it.stilo.g.structures.WeightedUndirectedGraph;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 * salvare anche componenti connesse.
 *
 * @author Cecilia Martinez Oliva
 */
public class Ex3new {

    private static String results;
    private static long grain;
    private static double p;

    private static Map<Integer, List<String>> clusters(String f) throws FileNotFoundException, IOException {

        Map<Integer, List<String>> clusters = new HashMap<>();
        File folder = new File(f);

        File[] clust = folder.listFiles();
        for (File c : clust) {
            if (c.isFile()) {
                String name = c.getName();
                int cluster = Integer.parseInt(name.split("-")[1].split(".txt")[0]);
                //System.out.println(name);
                //System.out.println(cluster);

                FileInputStream fstream = new FileInputStream(f + "/" + name);
                InputStreamReader isr = new InputStreamReader(fstream); // senza utf8
                try (BufferedReader br = new BufferedReader(isr)) {
                    String line;
                    br.readLine();
                    br.readLine();
                    List<String> terms = new ArrayList<>();
                    while ((line = br.readLine()) != null) {
                        terms.add(line.split(":")[0]);
                    }
                    clusters.put(cluster, terms);
                }
            }
        }

        return clusters;
    }

    private static void components(Map<Integer, List<String>> clusters, TemporalAnalysis ta, String f) throws IOException, ParseException, InterruptedException, java.text.ParseException {
        
        ta.setGrain(grain);
        Writer w;
        // GRAPH
        w = new Writer(results + "/"+f+"Words.txt");
        Writer old = new Writer("TemporalAnalysis/results3/"+f+"Words.txt");
        for (int clust : clusters.keySet()) {
            old.add("cluster:\r\n");
            System.out.println("cluster-"+f+": " + clust); //
            System.out.println(clusters.get(clust));
            w.add("cluster-"+clust+":\r\n"); //
            w.add(clusters.get(clust)+"\r\n"); //
            WeightedUndirectedGraph graph = new WeightedUndirectedGraph(clusters.get(clust).size());
            for (int t1 = 0; t1 < clusters.get(clust).size(); t1++) {
                for (int t2 = t1 + 1; t2 < clusters.get(clust).size(); t2++) {
                    String term1 = clusters.get(clust).get(t1);
                    String term2 = clusters.get(clust).get(t2);
                    int weight = ta.queries(term1, term2, "text", false);
                    // threshold
                    // prendiamo la frequenza minore tra le due parole e usiamo come soglia una percentuale
                    int term1Freq = ta.termFrequency(term1, "text", false);
                    int term2Freq = ta.termFrequency(term2, "text", false);
                    int minFreq;
                    //System.out.println(term1+": "+term1Freq+" - "+term2+": "+term2Freq);
                    if (term1Freq < term2Freq) {
                        minFreq = term1Freq;
                    } else {
                        minFreq = term2Freq;
                    }
                    double t = minFreq * p;
                    //System.out.println(minFreq+" - t: "+t);
                    if (weight > t) {
                        //graph.add(t1, t2, weight);
                        graph.add(t1, t2, 1);
                    }
                    //System.out.println(term1+" "+term2+": "+taYES.queries(term1,term2));
                }

            }
            // connected components
            int[] all = new int[graph.size];
            for (int i = 0; i < graph.size; i++) {
                all[i] = i;
            }

            Set<Set<Integer>> comps = ConnectedComponents.rootedConnectedComponents(graph, all, 2);
            System.out.println("CC");
            for (Set<Integer> cc : comps) {
                System.out.println("component:"); //
                w.add("connected component:\r\n");
                for (int node : cc) {
                    System.out.print(clusters.get(clust).get(node) + " ");
                    w.add(clusters.get(clust).get(node) + " ");

                }
                w.add("\r\n"); //
                System.out.println(); //
                // k core
                WeightedUndirectedGraph graphComp = SubGraph.extract(graph, cc.stream().mapToInt(Number::intValue).toArray(), 2);

                Core c = CoreDecomposition.getInnerMostCore(graphComp, 2);
                System.out.println("INNERMOST CORE");
                System.out.println("k: " + c.minDegree);
                w.add("innermost core:\r\n");
                w.add("k: "+c.minDegree+"\r\n");
                for (int node : c.seq) {
                    System.out.print(clusters.get(clust).get(node) + " ");
                    w.add(clusters.get(clust).get(node) + " ");
                    old.add(clusters.get(clust).get(node) + " ");
                }
                w.add("\r\n");
                old.add("\r\n");
                System.out.println();

            }
            w.add("\r\n");
            old.add("\r\n");
            System.out.println();
        }
        w.close();
        old.close();

    }

    public static void main(String[] args) throws IOException, ParseException, InterruptedException, java.text.ParseException {
        results = "TemporalAnalysis/results3/New";
        new File(results).mkdir();
        
        grain = 43200000; // grain 12h
        p = 0.5;

        boolean stemming = true;

        String TWYES;
        String TWNO;
        if (stemming) {
            TWYES = "indices/TwitterIndexYES";
            TWNO = "indices/TwitterIndexNO";
        } else {
            TWYES = "indices/TwitterIndexYESNoStem";
            TWNO = "indices/TwitterIndexNONoStem";
        }

        TemporalAnalysis taYES = new TemporalAnalysis(TWYES);
        TemporalAnalysis taNO = new TemporalAnalysis(TWNO);

        Map<Integer, List<String>> kmYES = clusters("TemporalAnalysis/results2/New/ClustersYES");
        Map<Integer, List<String>> kmNO = clusters("TemporalAnalysis/results2/New/ClustersNO");
        System.out.println(kmYES);
        
        components(kmYES, taYES, "Yes");
        components(kmNO, taNO, "No");
    }

}
