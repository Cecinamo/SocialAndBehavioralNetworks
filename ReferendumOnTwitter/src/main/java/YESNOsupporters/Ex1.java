package YESNOsupporters;

import Indices.Writer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

/**
 * From the entire tweets dataset, identify tweets of users that mention one of
 * the politicians (include also the previously founded P​ account) that support
 * YES or NO or directly express their opinion about the referendum (use also
 * t_i’(Y)​, t_j’(N)​ groups of words). How many users you get? How many tweets?
 * Let M​ be the set of such users and let T(M)​ be the set of related tweets.
 *
 * @author Cecilia Martinez Oliva
 */
public class Ex1 {

    private static int ntweets(List<String> ids, boolean stemming) throws IOException, ParseException {
        String index;
        if (stemming) {
            index = "indices/TwitterIndex";
        } else {
            index = "indices/TwitterIndexNoStem";
        }
        Directory dir = new SimpleFSDirectory(new File(index));
        IndexReader ir = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(ir);

        BooleanQuery qids = new BooleanQuery();
        for (String id : ids) {
            BytesRef ref = new BytesRef();
            NumericUtils.longToPrefixCoded(Long.parseLong(id), 0, ref);
            Query ii = new TermQuery(new Term("id", ref));
            qids.add(ii, BooleanClause.Occur.SHOULD);
        }
        BooleanQuery query = new BooleanQuery();
        query.add(qids, BooleanClause.Occur.MUST);

        TotalHitCountCollector collector = new TotalHitCountCollector();
        searcher.search(query, collector);
        return collector.getTotalHits();
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public static void main(String[] args) throws IOException, ParseException {

        boolean stemming = true;
        String results = "YNsupporters/results1";
        new File(results).mkdirs(); //

        // we need all users in the graph
        YESNOsupporters s = new YESNOsupporters();
        List<String> users = new ArrayList<>();
        users.addAll(s.allUsers());
        System.out.println("n. of users in the graph: " + users.size());
        // let's find yes/no supporters
        List<List<String>> supporters = s.IDsYN(users, stemming);

        System.out.println("M yes: " + supporters.get(0).size());
        System.out.println("M no: " + supporters.get(1).size());

        Writer w = new Writer(results + "/Myes.txt");

        for (String id : supporters.get(0)) {
            w.add(id + "\r\n");
        }
        w.close();

        w = new Writer(results + "/Mno.txt");
        for (String id : supporters.get(1)) {
            w.add(id + "\r\n");
        }
        w.close();

        // n tweets
        int ntweets = 0;
        int iter = supporters.get(0).size() / 1000 + 1; //non posso fare più di mille query

        for (int i = 0; i < iter; i++) {
            int maxidx = 1000 * (i + 1);
            if (maxidx > supporters.get(0).size()) {
                maxidx = supporters.get(0).size();
            }
            ntweets += ntweets(supporters.get(0).subList(1000 * i, maxidx), stemming);
        }

        System.out.println("N. of yes tweets: " + ntweets);

        
        ntweets = 0;
        iter = supporters.get(1).size() / 1000 + 1; //non posso fare più di mille query

        for (int i = 0; i < iter; i++) {
            int maxidx = 1000 * (i + 1);
            if (maxidx > supporters.get(1).size()) {
                maxidx = supporters.get(1).size();
            }
            ntweets += ntweets(supporters.get(1).subList(1000 * i, maxidx), stemming);
        }

        System.out.println("N. of yes tweets: " + ntweets);
        
    }

}
