package Indices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

/**
 *
 * @author cecin
 */
public class YESNOUsers2 {

    private static Set<String> listOfUsers(String file) throws FileNotFoundException, IOException {
        // we use a set to avoid problems if we added someone more than once.
        Set<String> users = new HashSet<>();
        FileInputStream fstream = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fstream);
        try (BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                // terms containing at least one alphabet character
                if (line.matches(".*[a-zA-Z]+.*")) {
                    users.add(line);
                    //System.out.println(line);
                }
            }
        }
        return users;
    }

    /**
     *
     * @param users
     * @param fileName
     * @param stemming
     * @throws IOException
     */
    private static void index(Directory dir, ArrayList<String> screenNames, String fileName, boolean stemming) throws IOException {
        IndexReader ir;
        IndexSearcher searcher;
        ir = DirectoryReader.open(dir);
        searcher = new IndexSearcher(ir);
        BooleanQuery names = new BooleanQuery();
        //String q = "";
        for (String name : screenNames) {
            Query n = new TermQuery(new Term("screenname", name));
            names.add(n, BooleanClause.Occur.SHOULD);
        }
        BooleanQuery query = new BooleanQuery();
        query.add(names, BooleanClause.Occur.MUST);

        TopDocs top = searcher.search(query, 100000000);
        ScoreDoc[] hits = top.scoreDocs;

        Document doc;
        Directory d = new SimpleFSDirectory(new File(fileName));
        TwitterIndex index = new TwitterIndex(d, stemming);

        for (ScoreDoc entry : hits) {
            doc = searcher.doc(entry.doc);
            Long date = Long.parseLong(doc.get("date"));
            Long id = Long.parseLong(doc.get("id"));
            String screenName = doc.get("screenname");
            String name = doc.get("name");
            Long followers = Long.parseLong(doc.get("followers"));
            String text = doc.get("text");
            String hashtags = doc.get("hashtags");
            String mentions = doc.get("mentions");

            index.addTweet(date, id, screenName, name, followers, text, hashtags, mentions);
        }
        index.close();

    }

    public static void main(String[] args) throws IOException {
        
        boolean stemming = false;

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
        Directory twitterIndex = new SimpleFSDirectory(new File(TW));
        String file_yes = "yes_p.txt";
        String file_no = "no_p.txt";
        
        ArrayList<String> users_yes = new ArrayList<>();
        users_yes.addAll(listOfUsers(file_yes));
        ArrayList<String> users_no = new ArrayList<>();
        users_no.addAll(listOfUsers(file_no));

        index(twitterIndex, users_yes, TWYES, stemming);
        index(twitterIndex, users_no, TWNO, stemming);
    }

}
