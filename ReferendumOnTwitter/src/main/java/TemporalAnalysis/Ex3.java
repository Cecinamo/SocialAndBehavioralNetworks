package TemporalAnalysis;

import Indices.Writer;
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
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;

/**
 * For each group of token in t_i(Y)​ and in t_j(N)​ build the co-occurrence
 * graph of them (two word t_1 , t_2 ​have an edge e​, if they appear in the
 * same document; the weight w​ of e is equal to the number of documents where
 * both token appear); using a threshold over edge weights (decide which one
 * produce best results) , identifying the Connected Components CC​ and extract
 * the innermost core (K-Core) from each of them, producing subgroup of tokens,
 * t_1’(Y),t_2’’(Y)​.. Comment about the differences and decide which is the
 * best strategy K-Core vs Simple Connected Component.
 *
 * @author Cecilia Martinez Oliva
 */
public class Ex3 {

    /**
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static Map<Integer, List<String>> clusters(String file) throws FileNotFoundException, IOException {
        // we use a set to avoid problems if we added someone more than once.
        Map<Integer, List<String>> c = new HashMap<>();
        FileInputStream fstream = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fstream); // senza utf8
        try (BufferedReader br = new BufferedReader(isr)) {
            String line;
            int n = 0;
            while ((line = br.readLine()) != null) {
                String[] r = line.split(" ");
                // il primo è un numero
                String[] terms = Arrays.copyOfRange(r, 1, r.length);
                c.put(n, Arrays.asList(terms));
                n++;
            }
        }
        return c;
    }

    /**
     *
     * @param args
     * @throws IOException ...
     * @throws InterruptedException ...
     * @throws ParseException ...
     * @throws org.apache.lucene.queryparser.classic.ParseException ...
     * @see TemporalAnalysis#setGrain(long)
     * @see TemporalAnalysis#queries(String, String, boolean)
     * @see Writer
     */
    public static void main(String[] args) throws IOException, InterruptedException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
        String results = "TemporalAnalysis/results3";
        new File(results).mkdirs();
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

        double p = 0.5;
        TemporalAnalysis taYES = new TemporalAnalysis(TWYES);
        TemporalAnalysis taNO = new TemporalAnalysis(TWNO);
        long h12 = 43200000; // grain 12h
        taYES.setGrain(h12);
        taNO.setGrain(h12);
        //int thresholdYES = 5;
        //int thresholdNO = 5;

        Map<Integer, List<String>> kmYES = clusters("TemporalAnalysis/results2/clustersYES.txt");
        Map<Integer, List<String>> kmNO = clusters("TemporalAnalysis/results2/clustersNO.txt");

        Writer w;
        // GRAPH YES
        w = new Writer(results + "/YesWords.txt");
        for (int clust : kmYES.keySet()) {
            System.out.println("cluster-yes: " + clust); //
            System.out.println(kmYES.get(clust));
            w.add("cluster:\r\n"); //
            WeightedUndirectedGraph graph = new WeightedUndirectedGraph(kmYES.get(clust).size());
            for (int t1 = 0; t1 < kmYES.get(clust).size(); t1++) {
                for (int t2 = t1 + 1; t2 < kmYES.get(clust).size(); t2++) {
                    String term1 = kmYES.get(clust).get(t1);
                    String term2 = kmYES.get(clust).get(t2);
                    int weight = taYES.queries(term1, term2, "text", false);
                    // threshold
                    // prendiamo la frequenza minore tra le due parole e usiamo come soglia una percentuale
                    int term1Freq = taYES.termFrequency(term1, "text", false);
                    int term2Freq = taYES.termFrequency(term2, "text", false);
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
                for (int node : cc) {
                    System.out.print(kmYES.get(clust).get(node) + " ");
                    //w.add(kmYES.get(clust).get(node) + " ");

                }
                //w.add("\r\n"); //
                System.out.println(); //
                // k core
                WeightedUndirectedGraph graphComp = SubGraph.extract(graph, cc.stream().mapToInt(Number::intValue).toArray(), 2);

                Core c = CoreDecomposition.getInnerMostCore(graphComp, 2);
                System.out.println("K CORE");
                System.out.println("k: " + c.minDegree);
                for (int node : c.seq) {
                    System.out.print(kmYES.get(clust).get(node) + " ");
                    w.add(kmYES.get(clust).get(node) + " ");
                }
                w.add("\r\n");
                System.out.println();

            }
            w.add("\r\n");
            System.out.println();
        }

        w.close();

        // GRAPH NO
        w = new Writer(results + "/NoWords.txt");
        for (int clust : kmNO.keySet()) {
            System.out.println("cluster-no: " + clust); //
            System.out.println(kmNO.get(clust));
            w.add("cluster:\r\n"); //
            WeightedUndirectedGraph graph = new WeightedUndirectedGraph(kmNO.get(clust).size());
            for (int t1 = 0; t1 < kmNO.get(clust).size(); t1++) {
                for (int t2 = t1 + 1; t2 < kmNO.get(clust).size(); t2++) {
                    String term1 = kmNO.get(clust).get(t1);
                    String term2 = kmNO.get(clust).get(t2);
                    int weight = taNO.queries(term1, term2, "text", false);
                    // threshold
                    // prendiamo la frequenza minore tra le due parole e usiamo come soglia una percentuale
                    int term1Freq = taNO.termFrequency(term1, "text", false);
                    int term2Freq = taNO.termFrequency(term2, "text", false);
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
                for (int node : cc) {
                    System.out.print(kmNO.get(clust).get(node) + " ");
                    //w.add(kmNO.get(clust).get(node) + " ");

                }
                //w.add("\r\n"); //
                System.out.println(); //

                // k core
                WeightedUndirectedGraph graphComp = SubGraph.extract(graph, cc.stream().mapToInt(Number::intValue).toArray(), 2);

                // k core
                Core c = CoreDecomposition.getInnerMostCore(graphComp, 2);
                System.out.println("K CORE");
                System.out.println("k: " + c.minDegree);
                for (int node : c.seq) {
                    System.out.print(kmNO.get(clust).get(node) + " ");
                    w.add(kmNO.get(clust).get(node) + " ");
                }
                System.out.println();
                w.add("\r\n");
            }
            w.add("\r\n");
            System.out.println();

        }

        w.close();
    }

}
