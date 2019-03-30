package TemporalAnalysis;

import Indices.Writer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
public class Ex4latex {

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

        String results = "TemporalAnalysis/results4";

        boolean stemming = true;

        String TW;
        if (stemming) {
            TW = "indices/TwitterIndex";
        } else {
            TW = "indices/TwitterIndexNoStem";
        }

        TemporalAnalysis ta = new TemporalAnalysis(TW);
        // YES
        Writer w = new Writer(results + "/Ylatex.txt");
        String s = "\\begin{footnotesize} \r\n\\texttt{ \r\n";
        w.add(s);
        System.out.println("YES");

        FileInputStream fstream = new FileInputStream("TemporalAnalysis/results3/YesWords.txt");
        InputStreamReader isr = new InputStreamReader(fstream);
        BufferedReader bufferedReader = new BufferedReader(isr);

        String line;
        int cluster = -1;
        int component = 0;
        s = "";

        while ((line = bufferedReader.readLine()) != null) {
            String[] t = line.split(" ");
            if (t[0].equals("cluster:")) {
                // aggiungo quello precendente
                if (s.length() > 0) {
                    s += "} \r\n\\end{footnotesize}\r\n\\begin{center}\r\n\\includegraphics[width=17cm, height= 8cm]{Yescluster" + cluster;
                    s += ".png}\r\n\\captionof{figure}{YES cluster" + cluster + "}\r\n\\label{fig:yc" + cluster + "}\r\n\\end{center}\r\n";
                    s += "\\begin{footnotesize}\r\n\\noindent\\\\ \r\n\\texttt{\r\n";
                    w.add("cluster" + cluster + " \\\\ \r\n" + s);
                    s = "";
                }
                cluster++;
                component = 0;
            } else {
                if (line.length() > 0) {
                    ArrayList<String> cwords = new ArrayList<>();
                    s += "comp" + component + ": ";
                    for (String word : t) {
                        int wf = ta.termFrequency(word, "text", false);
                        cwords.add(word);
                        s += word + " freq: " + wf + " ";
                    }
                    int cf = ta.termFrequencies(cwords, false);
                    s += "\\\\\r\ntfreq: " + cf + " \\\\\r\n";
                    component++;
                }
            }

        }
        isr.close();
        // l'ultimo 
        if (s.length() > 0) {
            s += "} \r\n\\end{footnotesize}\r\n\\begin{center}\r\n\\includegraphics[width=17cm, height= 8cm]{Yescluster" + cluster;
            s += ".png}\r\n\\captionof{figure}{YES cluster" + cluster + "}\r\n\\label{fig:yc" + cluster + "}\r\n\\end{center}\r\n";
            w.add("cluster" + cluster + " \\\\ \r\n" + s);
        }
        w.close();
        
        
        
        
        
        
        
        // NO
        w = new Writer(results + "/Nlatex.txt");
        s = "\\begin{footnotesize} \r\n\\texttt{ \r\n";
        w.add(s);
        System.out.println("NO");

        fstream = new FileInputStream("TemporalAnalysis/results3/NoWords.txt");
        isr = new InputStreamReader(fstream);
        bufferedReader = new BufferedReader(isr);

        cluster = -1;
        component = 0;
        s = "";

        while ((line = bufferedReader.readLine()) != null) {
            String[] t = line.split(" ");
            if (t[0].equals("cluster:")) {
                // aggiungo quello precendente
                if (s.length() > 0) {
                    s += "} \r\n\\end{footnotesize}\r\n\\begin{center}\r\n\\includegraphics[width=17cm, height= 8cm]{Nocluster" + cluster;
                    s += ".png}\r\n\\captionof{figure}{NO cluster" + cluster + "}\r\n\\label{fig:nc" + cluster + "}\r\n\\end{center}\r\n";
                    s += "\\begin{footnotesize}\r\n\\noindent\\\\ \r\n\\texttt{\r\n";
                    w.add("cluster" + cluster + " \\\\ \r\n" + s);
                    s = "";
                }
                cluster++;
                component = 0;
            } else {
                if (line.length() > 0) {
                    ArrayList<String> cwords = new ArrayList<>();
                    s += "comp" + component + ": ";
                    for (String word : t) {
                        int wf = ta.termFrequency(word, "text", false);
                        cwords.add(word);
                        s += word + " freq: " + wf + " ";
                    }
                    int cf = ta.termFrequencies(cwords, false);
                    s += "\\\\\r\ntfreq: " + cf + " \\\\\r\n";
                    component++;
                }
            }

        }
        isr.close();
        // l'ultimo 
        if (s.length() > 0) {
            s += "} \r\n\\end{footnotesize}\r\n\\begin{center}\r\n\\includegraphics[width=17cm, height= 8cm]{Nocluster" + cluster;
            s += ".png}\r\n\\captionof{figure}{NO cluster" + cluster + "}\r\n\\label{fig:nc" + cluster + "}\r\n\\end{center}\r\n";
            w.add("cluster" + cluster + " \\\\ \r\n" + s);
        }
        w.close();
    }

}
