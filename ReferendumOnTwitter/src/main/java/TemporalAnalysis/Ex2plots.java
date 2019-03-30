/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TemporalAnalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import net.seninp.jmotif.sax.SAXException;
import org.jfree.data.time.TimeSeriesCollection;

/**
 *
 * @author Cecilia Martinez Oliva
 */
public class Ex2plots {

    public static void main(String[] args) throws FileNotFoundException, IOException, ParseException, SAXException, org.apache.lucene.queryparser.classic.ParseException {
        String resYES = "TemporalAnalysis/results2/YESclusters";
        String resNO = "TemporalAnalysis/results2/NOclusters";
        new File(resYES).mkdirs();
        new File(resNO).mkdirs();
        boolean stemming = true;
        int alphabetSize = 2;
        int nterms = 100;

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
        ArrayList<Long> grain = taYES.getT();

        FileInputStream fstream = new FileInputStream("TemporalAnalysis/results2/clustersYES.txt");
        InputStreamReader isr = new InputStreamReader(fstream); // questa volta senza utf8
        BufferedReader bufferedReader = new BufferedReader(isr);

        String line;
        int cluster = 0;
        while ((line = bufferedReader.readLine()) != null) {
            String[] t = line.split(" ");
            double num = Double.parseDouble(t[0]) / (t.length - 1);
            String[] tnew = Arrays.copyOfRange(t, 1, t.length);
            System.out.println("CLUSTER:");
            TimeSeriesCollection dYES = new TimeSeriesCollection();
            int nt = 0;
            for (String term : tnew) {
                System.out.println(term);
                if (nt < nterms) {
                    ArrayList<Integer> values = new ArrayList<>();
                    String saxString;
                    ArrayList<Double> saxValues;
                    int res;
                    for (int d = 1; d < grain.size(); d++) {
                        res = taYES.queries(term, "text", grain.get(d - 1), grain.get(d), false);
                        values.add(res);
                    }
                    saxString = taYES.sax(values, alphabetSize);
                    saxValues = taYES.toNum(saxString);
                    dYES = taYES.createDataset(dYES, saxValues, term);
                    //System.out.println(saxValues);
                }
                nt++;
            }
            TSPlot pYES = new TSPlot("YES cluster" + cluster, "SAX cluster" + cluster + " score: " + num, dYES, resYES + "/cluster" + cluster + ".png", 1000);
            cluster++;

        }
        isr.close();

        fstream = new FileInputStream("TemporalAnalysis/results2/clustersNO.txt");
        isr = new InputStreamReader(fstream);
        bufferedReader = new BufferedReader(isr);

        cluster = 0;
        while ((line = bufferedReader.readLine()) != null) {
            String[] t = line.split(" ");
            double num = Double.parseDouble(t[0]) / (t.length - 1);
            String[] tnew = Arrays.copyOfRange(t, 1, t.length);
            System.out.println("CLUSTER:");
            TimeSeriesCollection dNO = new TimeSeriesCollection();
            int nt = 0;
            for (String term : tnew) {
                System.out.println(term);
                if (nt < nterms) {
                    ArrayList<Integer> values = new ArrayList<>();
                    String saxString;
                    ArrayList<Double> saxValues;
                    int res;
                    for (int d = 1; d < grain.size(); d++) {
                        res = taNO.queries(term, "text", grain.get(d - 1), grain.get(d), false);
                        values.add(res);
                    }
                    saxString = taNO.sax(values, alphabetSize);
                    saxValues = taNO.toNum(saxString);
                    dNO = taNO.createDataset(dNO, saxValues, term);
                }
                nt++;
            }
            TSPlot pNO = new TSPlot("NO cluster" + cluster, "SAX cluster" + cluster + " score: " + num, dNO, resNO + "/cluster" + cluster + ".png", 1000);
            cluster++;
        }

        isr.close();
    }
}
