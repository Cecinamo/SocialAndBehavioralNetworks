/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TemporalAnalysis;

import Indices.Writer;
import java.io.File;
import java.io.IOException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author cecin
 */
public class Hashtags {

    private static void getHashtags(String index, String[] ws, String folder, String support) throws IOException, ParseException {

        String res = folder+"/tests"+support;
        new File(res).mkdirs();
        
        TemporalAnalysis ta = new TemporalAnalysis(index);
        Writer wh = new Writer(folder+"/"+support+".txt");
        Writer whtest;
        
        Directory dir = new SimpleFSDirectory(new File(index));
        IndexReader ir = DirectoryReader.open(dir);
        Fields fields = MultiFields.getFields(ir);
        Terms terms = fields.terms("hashtags");
        TermsEnum iterator = terms.iterator(null);
        BytesRef byteRef;

        while ((byteRef = iterator.next()) != null) {

            String tt = byteRef.utf8ToString();
            int freq = iterator.docFreq();

            for (String w : ws) {
                if (tt.matches(".*" + w + ".*") && freq > 5) {
                    //System.out.println(tt);
                    wh.add(tt+"\n");
                    whtest = new Writer(res+"/"+tt+".txt");
                    String tweets = ta.queriesTweets(tt, "hashtags", false);
                    whtest.add(tweets);
                    whtest.close();
                }
            }
        }
        wh.close();
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException, ParseException {

        String results = "TemporalAnalysis/hashtags";
        new File(results).mkdirs();

        String indexY = "indices/TwitterIndexYES";
        String indexN = "indices/TwitterIndexNO";

        String[] w = {"si", "sì","no"};
        
        getHashtags(indexY, w, results, "Yes");
        getHashtags(indexN, w, results, "No");
    }

}
