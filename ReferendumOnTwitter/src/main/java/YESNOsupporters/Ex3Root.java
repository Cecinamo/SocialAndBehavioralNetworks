package YESNOsupporters;

import Indices.Writer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

/**
 * 1.
 * Partitioning the users of M​ according to the candidates they mention (each
 * user can mention more that one candidate more than one time). Identify the
 * users mentioning more frequently each candidate or support YES/NO and measure
 * their centrality (Hubness Authority). Find the 500 (for each option YES/NO)
 * who both support the candidate frequently and are highly central (define some
 * combined measure to select such candidates and propose a method to give
 * sentiment to those mentions). Let Influencer M' ​in M​ be these users.
 *
 * @author Cecilia Martinez Oliva
 */
public class Ex3Root {

    private static List<NavigableMap<Integer, List<String>>> findRoot(List<String> ids, boolean stemming) throws IOException, ParseException {

        List<NavigableMap<Integer, List<String>>> res = new ArrayList<>();
        NavigableMap<Integer, List<String>> yes = new TreeMap<>(Collections.reverseOrder());
        NavigableMap<Integer, List<String>> no = new TreeMap<>(Collections.reverseOrder());

        String index;
        if (stemming) {
            index = "indices/TwitterIndex";
        } else {
            index = "indices/TwitterIndexNoStem";
        }
        Directory dir = new SimpleFSDirectory(new File(index));
        IndexReader ir = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(ir);

        YESNOsupporters s = new YESNOsupporters();
        List<List<String>> p = s.getPoliticians();;
        List<String> politicians_y = p.get(0);
        List<String> politicians_n = p.get(1);
        List<List<String>> h = s.getHashtags();
        List<String> hashtags_y = h.get(0);
        List<String> hashtags_n = h.get(1);
        List<Set<List<String>>> c = s.getComponents();
        Set<List<String>> components_y = c.get(0);
        Set<List<String>> components_n = c.get(1);
        
        // YES
        Filter f_hy = new CachingWrapperFilter(new QueryWrapperFilter(s.getQuery(hashtags_y, "hashtags")));
        Filter f_py = new CachingWrapperFilter(new QueryWrapperFilter(s.getQuery(politicians_y, "mentions")));
        Filter f_cy = new CachingWrapperFilter(new QueryWrapperFilter(s.getQuery(components_y, "text")));

        // NO
        Filter f_hn = new CachingWrapperFilter(new QueryWrapperFilter(s.getQuery(hashtags_n, "hashtags")));
        Filter f_pn = new CachingWrapperFilter(new QueryWrapperFilter(s.getQuery(politicians_n, "mentions")));
        Filter f_cn = new CachingWrapperFilter(new QueryWrapperFilter(s.getQuery(components_n, "text")));

        BooleanQuery q;
        Query qid;

        for (String id : ids) {
            BytesRef ref = new BytesRef();
            NumericUtils.longToPrefixCoded(Long.parseLong(id), 0, ref);
            qid = new TermQuery(new Term("id", ref));

            // hashtags yes
            TotalHitCountCollector collector_hy = new TotalHitCountCollector();
            searcher.search(qid, f_hy, collector_hy);
            int nhashtags_y = collector_hy.getTotalHits();
            // politicians yes
            TotalHitCountCollector collector_py = new TotalHitCountCollector();
            searcher.search(qid, f_py, collector_py);
            int npoliticians_y = collector_py.getTotalHits();
                        // components yes
            TotalHitCountCollector collector_cy = new TotalHitCountCollector();
            searcher.search(qid, f_cy, collector_cy);
            int ncomponents_y = collector_cy.getTotalHits();
            
            // hashtags no
            TotalHitCountCollector collector_hn = new TotalHitCountCollector();
            searcher.search(qid, f_hn, collector_hn);
            int nhashtags_n = collector_hn.getTotalHits();
            // politicians no
            TotalHitCountCollector collector_pn = new TotalHitCountCollector();
            searcher.search(qid, f_pn, collector_pn);
            int npoliticians_n = collector_pn.getTotalHits();
            // components no
            TotalHitCountCollector collector_cn = new TotalHitCountCollector();
            searcher.search(qid, f_cn, collector_cn);
            int ncomponents_n = collector_cn.getTotalHits();

            if (nhashtags_y > 10 || nhashtags_n > 10 || npoliticians_y > 10 || npoliticians_n > 10) {
                System.out.println("n hashtags YES: " + nhashtags_y);
                System.out.println("n hashtags NO: " + nhashtags_n);
                System.out.println("n politicians YES: " + npoliticians_y);
                System.out.println("n politicians NO: " + npoliticians_n);
                System.out.println("n components-yes: " + ncomponents_y);
                System.out.println("n components-no: " + ncomponents_n);
                
                int score = (nhashtags_y*2 + npoliticians_y + ncomponents_y) - (nhashtags_n*2 + npoliticians_n + ncomponents_n);
                System.out.println("SCORE: " + score);
                if (score > 0) {
                    List<String> app = new ArrayList<>();
                    app.add(id);
                    if (yes.containsKey(score) == true) {// if already exist words with that score
                        app.addAll(yes.get(score));
                    }
                    yes.put(score, app);
                }
                if (score < 0) {
                    score = score * (-1);
                    List<String> app = new ArrayList<>();
                    app.add(id);
                    if (no.containsKey(score) == true) {// if already exist words with that score
                        app.addAll(no.get(score));
                    }
                    no.put(score, app);
                }
            }
        }
        res.add(yes);
        res.add(no);

        return res;
    }

    private static List<String> topRoot(NavigableMap<Integer, List<String>> list, int max) {
        int n = 0;
        List<String> res = new ArrayList<>();
        for (int score : list.navigableKeySet()) {
            //System.out.println(score);
            for (String id : list.get(score)) {
                res.add(id);
                if (res.size() == max) {
                    break;
                }
            }
            if (res.size() == max) {
                break;
            }
        }

        return res;
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public static void main(String[] args) throws IOException, ParseException {

        boolean stemming = true;
        String results = "YNsupporters/results3";
        new File(results).mkdirs();

        YESNOsupporters s = new YESNOsupporters();
        // let's start from the prevoius ids
        List<String> ids = new ArrayList<>(); //
        ids.addAll(s.getIDs("YNsupporters/results1/Myes.txt"));
        ids.addAll(s.getIDs("YNsupporters/results1/Mno.txt"));
        
        
        //ids = new ArrayList<>();
        //ids.add("128311427");
        //ids.add("2278958635");

        List<NavigableMap<Integer, List<String>>> root = findRoot(ids, stemming);
        List<String> root_yes = topRoot(root.get(0), 100);
        List<String> root_no = topRoot(root.get(1), 100);

        //System.out.println(root_yes.size());
        //System.out.println(root_no.size());

        Writer yes = new Writer(results + "/yes.txt");
        Writer no = new Writer(results + "/no.txt");

        for (String y : root_yes) {

            yes.add(y + "\n");
        }
        yes.close();

        for (String n : root_no) {

            no.add(n + "\n");
        }
        no.close();

    }

}
