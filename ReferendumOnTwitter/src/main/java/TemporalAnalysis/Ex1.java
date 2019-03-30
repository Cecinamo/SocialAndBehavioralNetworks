package TemporalAnalysis;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * Manually/Semi-Automatically collect on the web (political party website,
 * institutional website, wikipedia, public available list of politicians) and
 * collect all possible italian politics (or journalists) name/twitter account
 * P; divide them in two group according to their support to Yes Y or No N (skip
 * otherwise); How many users you get? How many tweets? Which is their
 * distribution over time?
 *
 * @author Cecilia Martinez Oliva
 */
public class Ex1 {

    /**
     *
     * @param args
     * @throws IOException ...
     * @throws ParseException ...
     * @throws org.apache.lucene.queryparser.classic.ParseException ...
     * @see TemporalAnalysis#setGrain(long)
     * @see TemporalAnalysis#trange()
     * @see TemporalAnalysis#ntweets()
     * @see TemporalAnalysis#nusers()
     * @see TemporalAnalysis#getTaxis()
     * @see TemporalAnalysis#temp()
     * @see Plot
     */
    public static void main(String[] args) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {

        String results = "TemporalAnalysis/results1";
        new File(results).mkdirs();
        boolean stemming = true;

        String TW;
        String TWYES;
        String TWNO;
        if (stemming) {
            TW = "indices/TwitterIndex";
            TWYES = "indices/TwitterIndexYES";
            TWNO = "indices/TwitterIndexNO";
        } else {
            TW = "indices/TwitterIndexNoStem";
            TWYES = "indices/TwitterIndexYESNoStem";
            TWNO = "indices/TwitterIndexNONoStem";

        }

        long h12 = 43200000; // grain 12h

        TemporalAnalysis ta = new TemporalAnalysis(TW);
        ta.setGrain(h12);

        //Date[] range = ta.trange();
        //System.out.println("First date: " + range[0]);
        //System.out.println("Last date: " + range[1]);
        int ndocs = ta.ntweets();
        System.out.println("Total n. of tweets: " + ndocs);

        int users = ta.nusers();
        System.out.println("Total n. of users: " + users);
        
        // YES-NO---------------------------------------------

        TemporalAnalysis taYES = new TemporalAnalysis(TWYES);
        TemporalAnalysis taNO = new TemporalAnalysis(TWNO);
        taYES.setGrain(h12);
        taNO.setGrain(h12);

        int uYES = taYES.nusers();
        int uNO = taNO.nusers();

        System.out.println("N. of 'yes' users: " + uYES);
        System.out.println("N. of 'no' users: " + uNO);

        int nYES = taYES.ntweets();
        int nNO = taNO.ntweets();

        System.out.println("N. of 'yes' tweets: " + nYES);
        System.out.println("N. of 'no' tweets: " + nNO);

        //PLOT
        //List<String> dates = taYES.getTaxis();
        //DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        //dataset = ta.createDataset(dataset, dates, taYES.temp(), "YES tweets", 2);
        //dataset = ta.createDataset(dataset, dates, taNO.temp(), "NO tweets", 2);

        //Plot p_yesno = new Plot("YES/NO", "Number of tweets over time", dataset, results + "/Tweets-YN.png", 200);
        //p_yesno.pack();
        //p_yesno.setLocation(800, 20);
        //p_yesno.setVisible(true);
        
        
        
        
        
        
        
        TimeSeriesCollection dataset2 = new TimeSeriesCollection();
        dataset2 = ta.createDataset(dataset2, taYES.temp(), "YES tweets");
        dataset2 = ta.createDataset(dataset2, taNO.temp(), "NO tweets");

        TSPlot p_yesno2 = new TSPlot("YES/NO", "Number of tweets over time", dataset2, results + "/Tweets-YN.png", 200);
        p_yesno2.pack();
        p_yesno2.setLocation(800, 20);
        p_yesno2.setVisible(true);
    }

}
