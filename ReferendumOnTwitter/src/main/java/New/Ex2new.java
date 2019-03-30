package New;

import Indices.Writer;
import TemporalAnalysis.Kmeans;
import TemporalAnalysis.TSPlot;
import TemporalAnalysis.TemporalAnalysis;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.seninp.jmotif.sax.SAXException;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * Per ogni cluster OR parole e stringa centroide
 *
 * @author Cecilia Martinez Oliva
 */
public class Ex2new {

    private static String results;
    private static long grain;
    private static int alphabetSize;
    private static String match;
    private static int nclusters;
    private static int nrand;

    private static String toAlphabet(ArrayList<Double> vec) {
        String res = "";
        for (Double d : vec) {
            if (d > 1.5) {
                res += "b ";
            } else {
                res += "a ";
            }
        }
        //System.out.println(vec);
        //System.out.println(res);
        return res;
    }

    private static String spaces(int n) {
        String res = "";

        for (int i = 0; i < n; i++) {
            res += " ";
        }

        return res;
    }

    private static ArrayList<Double> timeSeries(String term, TemporalAnalysis ta) throws ParseException, IOException, org.apache.lucene.queryparser.classic.ParseException {
        
        int tf = ta.termFrequency(term, "text", false);
        ArrayList<Double> ts = new ArrayList<>();
        double res;
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        long start = sdf.parse("2016/11/26 12:00:00").getTime();
        long end = sdf.parse("2016/12/07 00:00:00").getTime();

        for (long d = start; d < end; d += grain) {
            res = ta.queries(term, "text", d, d+grain, false);
            // normalizzazione, ogni termine deve avere lo stesso peso
            ts.add(res/tf);
        }
        
        return ts;
    }
    
    private static ArrayList<Double> clusterTS(ArrayList<String> terms, TemporalAnalysis ta) throws ParseException, IOException, org.apache.lucene.queryparser.classic.ParseException {
        Double[] ts = new Double[21];
        for(int i=0; i<21; i++)
            ts[i] = 0d;
        
        for(String term:terms) {
            int n=0;
            for(double v:timeSeries(term, ta)) {
                // normalizzazione per numero di parole
                ts[n] = ts[n]+(v/terms.size());
                n++;
            }
        }
        
        return(new ArrayList<>(Arrays.asList(ts)));
        
    }

    private static void kmeansResults(TemporalAnalysis ta, String f) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException, SAXException {

        ta.setGrain(grain);
        Map<String, ArrayList<Double>> sax = ta.findSax("text", alphabetSize, match);

        TimeSeriesCollection ds = new TimeSeriesCollection();
        Kmeans km = new Kmeans(sax);
        Map<Integer, ArrayList<String>> clusters = km.compute(nclusters, nrand);
        List<Double> scores = km.getScores();
        ArrayList<ArrayList<Double>> centroids = km.getCentroids();
        System.out.println(clusters.size());

        Writer w;
        Writer old = new Writer("TemporalAnalysis/results2/clusters"+f+".txt");
        String tests = "TemporalAnalysis/results2/tests"+f;
        new File(tests).mkdirs();
        Writer wtests;

        int n = 0;
        for (int clust : clusters.keySet()) {
            old.add(scores.get(n)+" ");
            // per ogni cluster salviamo centroide, OR normalizzato e SAX dei singoli termini.
            ds = ta.createDataset(ds, clusterTS(clusters.get(clust), ta), "cluster"+clust);
            w = new Writer(results + "/Clusters" + f + "/cluster-" + n + ".txt");
            int s = 30 - 10;
            w.add("centroid: " + spaces(s) + toAlphabet(centroids.get(n)) + "\r\n");
            s = 30 - 28;
            w.add("average euclidean distance: " + spaces(s) + scores.get(n) / clusters.get(clust).size() + "\r\n");
            for (String term : clusters.get(clust)) {
                wtests = new Writer(tests + "/" + term + ".txt");
                wtests.add(ta.queriesTweets(term, "text", false));
                wtests.close();
                old.add(term+" ");
                s = 30 - term.length() - 2;
                w.add(term + ": " + spaces(s) + toAlphabet(sax.get(term)) + "\r\n");
            }
            old.add("\r\n");
            w.close();
            n++;
        }
        old.close();
        TSPlot plot = new TSPlot("clusters"+f, "clusters"+f, ds, results + "/clusters"+f+".png", 1);
    }

    public static void main(String[] args) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException, SAXException {

        results = "TemporalAnalysis/results2/New";
        new File(results + "/ClustersYES").mkdirs();
        new File(results + "/ClustersNO").mkdirs();
        boolean stemming = true;
        alphabetSize = 2;
        match = "a*b+a*b*a*";
        nclusters = 20;
        nrand = 10000; // n. kmeans randomizations

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

        grain = 43200000; // grain 12h

        kmeansResults(taYES, "YES");
        kmeansResults(taNO, "NO");
    }

}
