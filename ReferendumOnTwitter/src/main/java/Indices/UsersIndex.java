/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Indices;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import static org.apache.lucene.util.Version.LUCENE_41;

/**
 *
 * @author Cecilia Martinez Oliva
 */
public class UsersIndex {

    private static IndexReader ir;
    private static IndexSearcher searcher;
    private static IndexWriter writer;
    private static LongField id;
    private static StringField screenname;
    private static TextField name;
    private static LongField followers;

    private static Document doc;

    private static void addTweet(Long id_value, String screenname_value, String name_value,
            Long followers_value) throws IOException {
        id.setLongValue(id_value);
        screenname.setStringValue(screenname_value);
        name.setStringValue(name_value);
        followers.setLongValue(followers_value);
        //System.out.println(screenname_value);
        writer.addDocument(doc);
    }

    private static void close() throws IOException {
        writer.commit();
        writer.close();
    }

    private static ArrayList<String> getScreenNames() throws IOException {
        ArrayList<String> screenNames = new ArrayList<>();
        Fields fields = MultiFields.getFields(ir);
        Terms terms = fields.terms("screenname");
        TermsEnum iterator = terms.iterator(null);
        BytesRef byteRef;

        while ((byteRef = iterator.next()) != null) {
            // String sn = new String(byteRef.bytes, byteRef.offset, byteRef.length);
            String sn = byteRef.utf8ToString();
            screenNames.add(sn);
        }
        return (screenNames);
    }

    private static String[] getUserInfo(String user) throws IOException {

        String[] info = new String[3]; // id, name, followers
        Query query = new TermQuery(new Term("screenname", user));
        TopDocs top = searcher.search(query, 1);
        ScoreDoc[] hits = top.scoreDocs;
        ScoreDoc entry = hits[0];
        Document tw = searcher.doc(entry.doc);
        info[0] = tw.get("id");
        info[1] = tw.get("name");
        info[2] = tw.get("followers");

        return info;
    }

    public static void main(String[] args) throws IOException {

        // let's initialize index
        String TW;
        TW = "indices/UsersIndex";

        Directory dir = new SimpleFSDirectory(new File(TW));

        // splits on whitespace and special characters; applies lowercase. 
        Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_41);
        //Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_41);

        IndexWriterConfig cfg = new IndexWriterConfig(LUCENE_41, analyzer);
        writer = new IndexWriter(dir, cfg);

        id = new LongField("id", 0L, Field.Store.YES);
        screenname = new StringField("screenname", "", Field.Store.YES);
        name = new TextField("name", "", Field.Store.YES);
        followers = new LongField("followers", 0L, Field.Store.YES);

        doc = new Document();
        doc.add(id);
        doc.add(screenname);
        doc.add(name);
        doc.add(followers);

        // first we need to read the main index and to find all the screen names
        Directory twitterIndex = new SimpleFSDirectory(new File("indices/TwitterIndex"));
        ir = DirectoryReader.open(twitterIndex);
        searcher = new IndexSearcher(ir);
        ArrayList<String> screenNames = getScreenNames();

        for (String sn : screenNames) {
            String[] info = getUserInfo(sn);
            // id, screenname, name, followers
            addTweet(Long.parseLong(info[0]), sn, info[1], Long.parseLong(info[2]));
        }

        close();
    }
}
