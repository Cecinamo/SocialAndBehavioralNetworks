package YESNOsupporters;

import Indices.Writer;
import it.stilo.g.structures.DoubleValues;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.util.NodesMapper;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author Cecilia Martinez Oliva
 */
public class Graph {

    private final NodesMapper<String> mapper;

    public Graph() {
        mapper = new NodesMapper<>();
    }

    public WeightedDirectedGraph getGraph() throws FileNotFoundException, IOException {
        WeightedDirectedGraph g = new WeightedDirectedGraph(1000000);
        FileInputStream fstream = new FileInputStream("Official_SBN-ITA-2016-Net.gz");
        GZIPInputStream gzStream = new GZIPInputStream(fstream);
        InputStreamReader isr = new InputStreamReader(gzStream, "UTF-8");
        BufferedReader br = new BufferedReader(isr);

        String line;

        while ((line = br.readLine()) != null) {
            String[] edge = line.split("\t");
            double w = Double.parseDouble(edge[2]);
            g.add(mapper.getId(edge[0]), mapper.getId(edge[1]), w);
        }
        return g;
    }

    public int[] getNodes(List<String> ids) {
        int[] nodes = new int[ids.size()];

        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = mapper.getId(ids.get(i));
        }
        return nodes;
    }

    public void saveGraph(WeightedDirectedGraph g, String results) throws IOException {
        Writer w = new Writer(results + "/lc.txt");
        for (int i = 0; i < g.size; i++) {
            if (g.out[i] != null) {
                for (int j = 0; j < g.out[i].length; j++) {
                    w.add(mapper.getNode(i) + "\t" + mapper.getNode(g.out[i][j]) + "\t" + g.weights[i][j] + "\n");
                }
            }
        }
        w.close();
    }

    public void getAuthorities(ArrayList<ArrayList<DoubleValues>> hits, int n, String file) throws IOException {

        Writer w = new Writer(file);
        // i=0 sono le authority, i=1 sono gli hubness, sono gia' ordinati in ordine descrescente
        ArrayList<DoubleValues> authorities = hits.get(0);
        for (int i = 0; i < n; i++) {
            String id = mapper.getNode(authorities.get(i).index);
            w.add(id + "\n");
        }
        w.close();
    }

    public void getHubs(ArrayList<ArrayList<DoubleValues>> hits, int n, String file) throws IOException {

        Writer w = new Writer(file);
        // i=0 sono le authority, i=1 sono gli hubness, sono gia' ordinati in ordine descrescente
        ArrayList<DoubleValues> hubs = hits.get(1);
        for (int i = 0; i < n; i++) {
            String id = mapper.getNode(hubs.get(i).index);
            w.add(id + "\n");
        }
        w.close();
    }

    public Set<Integer> getIns(int[] nodes, WeightedDirectedGraph g) throws IOException {

        Set<Integer> ins = new HashSet<>();

        for (int n : nodes) {
            if (g.in[n] != null) {
                for (int j = 0; j < g.in[n].length; j++) {
                    ins.add(g.in[n][j]);
                }
            }
        }
        return ins;
    }

    public Set<Integer> getOuts(int[] nodes, WeightedDirectedGraph g) throws IOException {

        Set<Integer> outs = new HashSet<>();

        for (int n : nodes) {
            if (g.out[n] != null) {
                for (int j = 0; j < g.out[n].length; j++) {
                    outs.add(g.out[n][j]);
                }
            }
        }
        return outs;
    }

}
