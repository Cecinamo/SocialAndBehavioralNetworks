package Indices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class YESNOUsers {

    /**
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static Set<String> listOfUsers(String file) throws FileNotFoundException, IOException {
        // we use a set to avoid problems if we added someone more than once.
        Set<String> users = new HashSet<>();
        FileInputStream fstream = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fstream, "UTF-8");
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
    private static ArrayList<String> getScreenNames(Directory dir, Set<String> users) throws IOException, ParseException {
        IndexReader ir;
        IndexSearcher searcher;
        ir = DirectoryReader.open(dir);
        searcher = new IndexSearcher(ir);

        Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_41);
        QueryParser parser = new QueryParser(Version.LUCENE_41, "name", analyzer);
        parser.setDefaultOperator(QueryParser.Operator.AND);
        Query query;

        ArrayList<String> screenNames = new ArrayList<>();
        // per ogni nome nella lista
        for (String name : users) {
            query = parser.parse(name);
            TopDocs top = searcher.search(query, 1000000000);
            ScoreDoc[] hits = top.scoreDocs;
            Document doc;
            long maxFollowers = 0L;
            String screenName = "";
            // prima cerchiamo lo screenname associato al name con più followers (supponiamo che sia quello vero)
            for (ScoreDoc entry : hits) {
                doc = searcher.doc(entry.doc);
                long followers = Long.parseLong(doc.get("followers"));
                if (followers > maxFollowers) {
                    maxFollowers = followers;
                    screenName = doc.get("screenname");
                }
            }
            // se esiste, lo aggiunge
            if (screenName.length() > 0) {
                screenNames.add(screenName);
            }
        }

        return (screenNames);
    }

    private static Map<String, List<String>> classifyUsers(Directory dir, ArrayList<String> users, boolean stemming) throws IOException, ParseException {
        IndexReader ir;
        IndexSearcher searcher;
        ir = DirectoryReader.open(dir);
        searcher = new IndexSearcher(ir);
        Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_41);
        QueryParser parser = new QueryParser(Version.LUCENE_41, "hashtags", analyzer);
        String[] yes_tags = {"bastaunsì", "bastaunsi", "iovotosì", "iovotosi"};
        String[] no_tags = {"iodicono", "iovotono"};
        BooleanQuery terms_yes = new BooleanQuery();
        for (String tag : yes_tags) {
            Query w;
            if (stemming) {
                w = parser.parse(tag);
            } else {
                w = new TermQuery(new Term("hashtags", tag));
            }
            terms_yes.add(w, BooleanClause.Occur.SHOULD);
        }

        BooleanQuery terms_no = new BooleanQuery();
        for (String tag : no_tags) {
            Query w;
            if (stemming) {
                w = parser.parse(tag);
            } else {
                w = new TermQuery(new Term("hashtags", tag));
            }
            terms_no.add(w, BooleanClause.Occur.SHOULD);
        }

        TotalHitCountCollector collector;
        Query u;
        List<String> yes_users = new ArrayList<>();
        List<String> no_users = new ArrayList<>();
        Map<String, List<String>> ans = new HashMap<>();
        for (String user : users) {
            //System.out.println(user);
            u = new TermQuery(new Term("screenname", user));
            BooleanQuery query_yes = new BooleanQuery();
            query_yes.add(terms_yes, BooleanClause.Occur.MUST);
            query_yes.add(u, BooleanClause.Occur.MUST);
            BooleanQuery query_no = new BooleanQuery();
            query_no.add(terms_no, BooleanClause.Occur.MUST);
            query_no.add(u, BooleanClause.Occur.MUST);

            collector = new TotalHitCountCollector();
            searcher.search(query_yes, collector);
            int n_yes = collector.getTotalHits();

            collector = new TotalHitCountCollector();
            searcher.search(query_no, collector);
            int n_no = collector.getTotalHits();

            if (n_yes != 0 || n_no != 0) {
                if (n_yes > n_no) {
                    yes_users.add(user);
                    System.out.println(user);
                    System.out.println("YES: " + n_yes + " - " + n_no);
                } else {
                    no_users.add(user);
                    System.out.println(user);
                    System.out.println("NO: " + n_yes + " - " + n_no);
                }
            }
        }
        ans.put("yes", yes_users);
        ans.put("no", no_users);
        return (ans);
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

    public static void main(String[] args) throws IOException, ParseException {

        boolean stemming = true;

        String TWUsers = "indices/UsersIndex";
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
        String file_users = "list.txt";

        // let's read our list of users
        Set<String> users = listOfUsers(file_users);
        System.out.println("Starting n. of users: " + users.size());
        // we than need to extract their twitter screen names
        Directory usersIndex = new SimpleFSDirectory(new File(TWUsers));

        ArrayList<String> screenNames = getScreenNames(usersIndex, users);
        System.out.println("n. of users: " + screenNames.size());
        
        // and to classify them
        Directory twitterIndex = new SimpleFSDirectory(new File(TW));

        String file_yes = "yes_p.txt";
        String file_no = "no_p.txt";

        Map<String, List<String>> YN = classifyUsers(twitterIndex, screenNames, stemming);

        System.out.println("n. YES: "+YN.get("yes").size());
        System.out.println("n. NO: "+YN.get("no").size());
        
        Writer w;
        w = new Writer(file_yes);
        for(String u:YN.get("yes")) {
            w.add(u+"\r\n");
        }
        w.close();
        
        w = new Writer(file_no);
        for(String u:YN.get("no")) {
            w.add(u+"\r\n");
        }
        w.close();

        ArrayList<String> users_yes = new ArrayList<>();
        users_yes.addAll(listOfUsers(file_yes));
        ArrayList<String> users_no = new ArrayList<>();
        users_no.addAll(listOfUsers(file_no));

         index(twitterIndex, users_yes, TWYES, stemming); 
         index(twitterIndex, users_no, TWNO, stemming);
    }

}
