package TemporalAnalysis;

import Indices.Writer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * Using the original statistics (collection), trace the time series (grain 3h)
 * for each obtained group of token t_i’(Y)​, t_j’(N)​; compare (manually) the
 * time series of each group Y​ and N​ and comments about some possible kind of
 * action-reaction that should be clearly identified. (look also at the content
 * of the tweets)
 *
 * @author Cecilia Martinez Oliva
 */
public class Ex4 {

    private static String results;
    private static TemporalAnalysis ta;
    
    private static void ts(String f) throws FileNotFoundException, IOException, org.apache.lucene.queryparser.classic.ParseException, ParseException {
        System.out.println(f);

        FileInputStream fstream = new FileInputStream("TemporalAnalysis/results3/"+f+"Words.txt");
        //InputStreamReader isr = new InputStreamReader(fstream, "UTF-8");
        InputStreamReader isr = new InputStreamReader(fstream);
        BufferedReader bufferedReader = new BufferedReader(isr);

        TimeSeriesCollection d = new TimeSeriesCollection();

        String line;
        int cluster = -1;
        int component = 0;
        String title = "";

        while ((line = bufferedReader.readLine()) != null) {
            String[] t = line.split(" ");
            if (t[0].equals("cluster:")) {
                if (d.getSeriesCount() > 0) {
                    TSPlot p = new TSPlot(f+" cluster" + cluster, "", d, results + "/clusters"+f+"plots/"+f+"cluster" + cluster + ".png", 1000);
                    p.pack();
                    p.setLocation(800, 20);
                    p.setVisible(true);
                }
                cluster++;
                title = "cluster" + cluster;
                System.out.println("\n"+title);
                component = 0;
                d = new TimeSeriesCollection();
            } else {
                if (line.length() > 0) {
                    ArrayList<String> cwords = new ArrayList<>();
                    System.out.print("comp" + component + ": ");
                    for (String word : t) {
                        int wf = ta.termFrequency(word, "text", false);
                        cwords.add(word);
                        System.out.print(word + " freq: " + wf + " ");
                    }
                    int cf = ta.termFrequencies(cwords, false);
                    if (cf > 0) {
                        System.out.println("\ntfreq: " + cf + "\n");
                        String s = results + "/clusters"+f+ "/cluster" + cluster + "/comp" + component;
                        new File(s).mkdirs();
                        d = ta.createDataset(d, ta.temp(t, s, false), "comp" + component);
                        component++;
                    }
                }
            }

        }
        isr.close();

        // l'ultimo
        if (d.getSeriesCount() > 0) {
            //Plot pYES = new Plot("YES clusters", "Number of tweets over time", dYES, results + "/YESclusters.png", 1000);
            TSPlot p = new TSPlot(f+" cluster" + cluster, "", d, results + "/clusters"+f+"plots/"+f+"cluster" + cluster + ".png", 1000);
            p.pack();
            p.setLocation(800, 20);
            p.setVisible(true);
        }
    }
    /**
     *
     * @param args
     * @throws IOException
     * @throws ParseException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     * @see TemporalAnalysis#setGrain(long)
     * @see TemporalAnalysis#getTaxis()
     * @see TemporalAnalysis#createDataset(DefaultCategoryDataset, List,
     * ArrayList, String)
     * @see TemporalAnalysis#temp(String[], String, boolean)
     * @see Plot
     */
    public static void main(String[] args) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {

        results = "TemporalAnalysis/results4";
        String clustersYES = results + "/clustersYes";
        String clustersNO = results + "/clustersNo";
        String clustersYESplots = results + "/clustersYesplots";
        String clustersNOplots = results + "/clustersNoplots";
        new File(clustersYES).mkdirs();
        new File(clustersNO).mkdirs();
        new File(clustersYESplots).mkdirs();
        new File(clustersNOplots).mkdirs();
        boolean stemming = true;

        String TW;
        if (stemming) {
            TW = "indices/TwitterIndex";
        } else {
            TW = "indices/TwitterIndexNoStem";
        }

        long h3 = 10800000l; // grain 3h
        ta = new TemporalAnalysis(TW);
        ta.setGrain(h3);
        
        ts("Yes");
        ts("No");

    }

}
