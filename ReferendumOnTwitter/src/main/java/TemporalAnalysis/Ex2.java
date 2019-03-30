package TemporalAnalysis;

import Indices.Writer;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.seninp.jmotif.sax.SAXException;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * For each p​ in Y | N​ analyze all the tweets/retweet T(Y) ​and build SAX
 * string (grain = 12h) for the Top 1000 words by frequencies that expose the
 * typical pattern that capture the collective attention (see slides SAX*);
 * group together (implementing a trivial ​K-Means​) all the strings that expose
 * an equal temporal behaviour (same or very similar SAX string) t_1(Y), t_2(Y),
 * t_1(N) , t_2(N)...​; (Clusters of terms)
 *
 * @author Cecilia Martinez Oliva
 */
public class Ex2 {

    /**
     *
     * @param args
     * @throws IOException ...
     * @throws ParseException ...
     * @throws org.apache.lucene.queryparser.classic.ParseException ...
     * @throws SAXException ...
     * @see TemporalAnalysis#setGrain(long)
     * @see TemporalAnalysis#getTaxis()
     * @see TemporalAnalysis#findSax(boolean)
     * @see TemporalAnalysis#createDataset(DefaultCategoryDataset, List,
     * ArrayList, String)
     * @see TemporalAnalysis#queriesTweets(String, boolean)
     * @see Plot
     * @see Kmeans
     */
    public static void main(String[] args) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException, SAXException {

        String results = "TemporalAnalysis/results2";
        String testsYES = results + "/testsYES";
        String testsNO = results + "/testsNO";
        new File(testsYES).mkdirs();
        new File(testsNO).mkdirs();
        boolean stemming = true;
        int alphabetSize = 2;
        //String match = "[a-z]+[ab]{6}";
        String match = "a*b+a*b*a*";
        int nYES = 20; // n. of yes clusters
        int nNO = 20; // n. of no clusters
        int nrand = 10000; // n. kmeans randomizations

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
        long h12 = 43200000; // grain 12h
        taYES.setGrain(h12);
        taNO.setGrain(h12);
        //List<String> dates = taYES.getTaxis();
        
        String [] t_yes = {"bastaunsi","iovotosi"};
        String [] t_no = {"iovotono","stavoltano","comitatodelno"};
        taYES.testSax(t_yes, "text", stemming, alphabetSize);
        taNO.testSax(t_no, "text", stemming, alphabetSize);
        Map<String, ArrayList<Double>> saxYES = taYES.findSax("text", alphabetSize, match);
        Map<String, ArrayList<Double>> saxNO = taNO.findSax("text", alphabetSize, match);
        System.out.println("SIZE: " + saxYES.size());
        System.out.println(saxYES.keySet());
        System.out.println("SIZE: " + saxNO.size());
        System.out.println(saxNO.keySet());

        //DefaultCategoryDataset datasetYES = new DefaultCategoryDataset();
        //DefaultCategoryDataset datasetNO = new DefaultCategoryDataset();
        Writer w;

        // YES
        for (String term : saxYES.keySet()) {
            //datasetYES = taYES.createDataset(datasetYES, dates, saxYES.get(term), term);
            w = new Writer(testsYES + "/" + term + ".txt");
            w.add(taYES.queriesTweets(term, "text", false));
            w.close();
        }

        //Plot p_yes = new Plot("YESterms", "Number of tweets over time", datasetYES, results + "/YES-terms.png", 20);
        //p_yes.pack();
        //p_yes.setLocation(800, 20);
        //p_yes.setVisible(true);

        // NO
        for (String term : saxNO.keySet()) {
            //datasetNO = taNO.createDataset(datasetNO, dates, saxNO.get(term), term);
            w = new Writer(testsNO + "/" + term + ".txt");
            w.add(taNO.queriesTweets(term, "text", false));
            w.close();
        }

        //Plot p_no = new Plot("NOterms", "Number of tweets over time", datasetNO, results + "/NO-terms.png", 20);
        //p_no.pack();
        //p_no.setLocation(800, 20);
        //p_no.setVisible(true);

        //System.out.println("YES CLUSTERS------------------------------------------------------");
        Kmeans km = new Kmeans(saxYES);
        Map<Integer, ArrayList<String>> kmYES = km.compute(nYES, nrand);
        List<Double> scores = km.getScores();
        ArrayList<ArrayList<Double>> centroids = km.getCentroids();
        System.out.println(kmYES.size());

        w = new Writer(results + "/clustersYES.txt");

        int n = 0;
        for (int clust : kmYES.keySet()) {
            //System.out.println("CLUSTER" + clust + ":");
            w.add(scores.get(n)+" ");
            for (String term : kmYES.get(clust)) {
                w.add(term + " ");
                //System.out.println(term + ": " + saxYES.get(term));
            }
            w.add("\r\n");
            //w.add(centroids.get(n)+"\r\n");
            n++;
        }
        w.close();

        //System.out.println("NO CLUSTERS------------------------------------------------------");
        km = new Kmeans(saxNO);
        Map<Integer, ArrayList<String>> kmNO = km.compute(nNO, nrand);
        scores = km.getScores();
        centroids = km.getCentroids();
        System.out.println(kmNO.size());

        w = new Writer(results + "/clustersNO.txt");

        n = 0;
        for (int clust : kmNO.keySet()) {
            //System.out.println("CLUSTER" + clust + ":");
            w.add(scores.get(n)+" ");
            for (String term : kmNO.get(clust)) {
                w.add(term + " ");
                //System.out.println(term + ": " + saxNO.get(term));
            }
            w.add("\r\n");
            //w.add(centroids.get(n)+"\r\n");
            n++;
        }
        w.close();
    }

}
