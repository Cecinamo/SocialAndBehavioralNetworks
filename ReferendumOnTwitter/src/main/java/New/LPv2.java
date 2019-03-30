package New;

import Indices.Writer;
import SpreadOfInfluence.NewLPA;
import YESNOsupporters.YESNOsupporters;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.util.NodesMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Label Propagation 3 opzioni: sì, no, non schierato (possono cambiare).
 *
 * @author Cecilia Martinez Oliva
 */
public class LPv2 {

    private static WeightedDirectedGraph g;
    private static NodesMapper<String> mapper;

    private static void getGraph() throws FileNotFoundException, IOException {
        mapper = new NodesMapper<>();
        g = new WeightedDirectedGraph(1000000);
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
    }

    private static int[] getNodes(List<String> ids) {
        int[] nodes = new int[ids.size()];

        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = mapper.getId(ids.get(i));
        }
        return nodes;
    }

    private static int[] initLabels(String yFile, String nFile) throws IOException {
        int[] labels = new int[g.size];

        YESNOsupporters s = new YESNOsupporters();
        List<String> y = new ArrayList<>();
        y.addAll(s.getIDs(yFile));
        int[] ynodes = getNodes(y);
        List<String> n = new ArrayList<>();
        n.addAll(s.getIDs(nFile));
        int[] nnodes = getNodes(n);

        for (int node : ynodes) {
            labels[node] = 1;
        }

        for (int node : nnodes) {
            labels[node] = 2;
        }

        return labels;
    }

    private static String results(int[] lpa) throws IOException {
        String res="";
        int ycount = 0;
        int ncount = 0;
        int rcount = 0;
        
        for (int r : lpa) {
            if (r == 1) {
                ycount++;
            }
            if (r == 2) {
                ncount++;
            }
            if (r == 0) {
                rcount++;
            }
        }

        res+= "YES:       " + ycount + "\r\n";
        res+= "NO:        " + ncount+ "\r\n";
        res+= "remaining: " + rcount+ "\r\n";
        
        return res;
    }

    public static void main(String[] args) throws IOException {

        String results = "SpreadOfInfluence/New";
        new File(results).mkdir();
        
        // let's load the graph
        getGraph();

        // M YES-NO SUPPORTERS
        String yf = "YNsupporters/results1/Myes.txt";
        String nf = "YNsupporters/results1/Mno.txt";

        String res = "";
        int[] lpaM = NewLPA.compute(g, initLabels(yf, nf), 0.001, 1);
        String head = "---------------------------------M--------------------------------------\r\n";
        res += head + results(lpaM) +"\r\n";

        // M' YES-NO SUPPORTERS
        yf = "YNsupporters/results3/hubsyes.txt";
        nf = "YNsupporters/results3/hubsno.txt";

        int[] lpaM1 = NewLPA.compute(g, initLabels(yf, nf), 0.001, 1);
        head = "---------------------------------M'-------------------------------------\r\n";
        res += head + results(lpaM1);
        
        System.out.println(res);
        
        Writer w = new Writer(results + "/LPv2.txt");
        
        w.add(res);
        w.close();
        
    }

}
