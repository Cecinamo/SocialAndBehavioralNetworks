package YESNOsupporters;

import it.stilo.g.structures.WeightedDirectedGraph;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

/**
 *
 * @author Cecilia Martinez Oliva
 */
public class YESNOsupporters {

    public Set<String> allUsers() throws FileNotFoundException, IOException {

        Set<String> u = new HashSet<>();
        FileInputStream fstream = new FileInputStream("Official_SBN-ITA-2016-Net.gz");
        GZIPInputStream gzStream = new GZIPInputStream(fstream);
        InputStreamReader isr = new InputStreamReader(gzStream, "UTF-8");
        BufferedReader br = new BufferedReader(isr);

        String line;

        while ((line = br.readLine()) != null) {
            String[] edge = line.split("\t");
            u.add(edge[0]);
            u.add(edge[1]);
        }
        return u;
    }

    public List<List<String>> IDsYN(List<String> ids, boolean stemming) throws IOException, ParseException {

        List<List<String>> res = new ArrayList<>();
        List<String> yes = new ArrayList<>();
        List<String> no = new ArrayList<>();
        int min = 5;

        // l'indice che mi interessa
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
        List<List<String>> p = s.getPoliticians();
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
            // components yes
            TotalHitCountCollector collector_cn = new TotalHitCountCollector();
            searcher.search(qid, f_cn, collector_cn);
            int ncomponents_n = collector_cn.getTotalHits();

            if (nhashtags_y > min || nhashtags_n > min
                    || npoliticians_y > min || npoliticians_n > min
                    || ncomponents_y > min || ncomponents_n > min) {
                int score = (nhashtags_y * 2 + npoliticians_y + ncomponents_y) - (nhashtags_n * 2 + npoliticians_n + ncomponents_n);
                //System.out.println("SCORE: " + score);
                if (score > 0) {
                    yes.add(id);
                }
                if (score < 0) {
                    no.add(id);
                }
            }

        }
        res.add(yes);
        res.add(no);

        return res;
    }
    // dati screennames restituisce id

    public Set<Long> getIDs(List<String> screenNames) throws IOException {

        String usersIndex = "indices/UsersIndex";
        Directory dir = new SimpleFSDirectory(new File(usersIndex));
        IndexReader ir = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(ir);
        Set<Long> ids = new HashSet<>();

        Query query;

        for (String name : screenNames) {
            query = new TermQuery(new Term("screenname", name));
            TopDocs top = searcher.search(query, 1);
            ScoreDoc[] hits = top.scoreDocs;
            Document doc = searcher.doc(hits[0].doc);
            long id = Long.parseLong(doc.get("id"));
            ids.add(id);
        }
        return (ids);
    }

    // legge le componenti
    public Set<List<String>> getComponents(String file) throws FileNotFoundException, IOException {

        Set<List<String>> res = new HashSet<>();
        FileInputStream fstream = new FileInputStream(file);
        //InputStreamReader isr = new InputStreamReader(fstream, "UTF-8");
        InputStreamReader isr = new InputStreamReader(fstream);
        try (BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                // terms containing at least one alphabet character
                if (line.matches(".*[a-zA-Z]+.*") && !line.contains("cluster:")) {
                    List<String> app = new ArrayList<>();
                    //System.out.println(line);
                    for (String w : line.split(" ")) {
                        if (w.matches(".*[a-zA-Z]+.*")) {
                            app.add(w);
                            //System.out.println(w);
                        }
                    }
                    res.add(app);
                    //System.out.println(app);
                }
            }
        }
        return res;
    }

    // legge liste di parole
    public Set<String> readList(String file) throws FileNotFoundException, IOException {
        // we use a set to avoid problems if we added someone more than once.
        Set<String> res = new HashSet<>();
        FileInputStream fstream = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fstream, "UTF-8");
        try (BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                // terms containing at least one alphabet character
                if (line.matches(".*[a-zA-Z]+.*")) {
                    res.add(line);
                    //System.out.println(line);
                }
            }
        }
        return res;
    }

    // legge id
    public List<String> getIDs(String file) throws FileNotFoundException, IOException {

        List<String> ids = new ArrayList<>();
        FileInputStream fstream = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fstream, "UTF-8");
        try (BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() > 0) {
                    ids.add(line);
                }

            }
        }
        return ids;
    }

    // contiene almeno un termine
    public BooleanQuery getQuery(List<String> terms, String field) throws ParseException {

        BooleanQuery qterms = new BooleanQuery();

        for (String term : terms) {
            Query tt = new TermQuery(new Term(field, term));
            qterms.add(tt, BooleanClause.Occur.SHOULD);
        }
        BooleanQuery query = new BooleanQuery();
        query.add(qterms, BooleanClause.Occur.MUST);

        return query;
    }

    // per le componenti
    public BooleanQuery getQuery(Set<List<String>> components, String field) throws ParseException {

        BooleanQuery query = new BooleanQuery();
        for (List<String> component : components) {
            BooleanQuery qterms = new BooleanQuery();
            for (String term : component) {
                Query tt = new TermQuery(new Term(field, term));
                qterms.add(tt, BooleanClause.Occur.MUST);
            }
            query.add(qterms, BooleanClause.Occur.SHOULD);
        }
        return query;
    }

    public List<List<String>> getPoliticians() throws IOException {
        List<List<String>> politicians = new ArrayList<>();
        // files
        String yes_politicians = "yes_p.txt";
        String no_politicians = "no_p.txt";
        // politicians
        List<String> p_yes = new ArrayList<>();
        p_yes.addAll(readList(yes_politicians));
        List<String> p_no = new ArrayList<>();
        p_no.addAll(readList(no_politicians));

        Collections.addAll(politicians, p_yes, p_no);
        return politicians;
    }

    public List<List<String>> getHashtags() throws IOException {
        List<List<String>> hashtags = new ArrayList<>();

        // files
        String yes_hashtags = "TemporalAnalysis/hashtags/Yes(m).txt";
        String no_hashtags = "TemporalAnalysis/hashtags/No(m).txt";
        // hashtags
        List<String> h_yes = new ArrayList<>();
        h_yes.addAll(readList(yes_hashtags));
        List<String> h_no = new ArrayList<>();
        h_no.addAll(readList(no_hashtags));

        Collections.addAll(hashtags, h_yes, h_no);
        return hashtags;
    }

    public List<Set<List<String>>> getComponents() throws IOException {
        List<Set<List<String>>> components = new ArrayList<>();

        // files
        String yes_components = "TemporalAnalysis/results3/YesWords.txt";
        String no_components = "TemporalAnalysis/results3/NoWords.txt";
        // components
        Set<List<String>> c_yes = new HashSet<>();
        c_yes.addAll(getComponents(yes_components));
        Set<List<String>> c_no = new HashSet<>();
        c_no.addAll(getComponents(no_components));

        Collections.addAll(components, c_yes, c_no);
        return components;
    }

    public void queryIDs(List<String> ids, boolean stemming) throws IOException, ParseException {

        // l'indice che mi interessa
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
            TopDocs top = searcher.search(qid, 1);
            ScoreDoc[] hits = top.scoreDocs;
            Document doc = searcher.doc(hits[0].doc);

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

            System.out.println("name:& " + doc.get("name")+"\\\\");
            System.out.println("screen name:& " + doc.get("screenname")+"\\\\");
            System.out.println("followers:& " + doc.get("followers")+"\\\\");
            System.out.println("n hashtags-yes:& " + nhashtags_y+"\\\\");
            System.out.println("n hashtags-no:& " + nhashtags_n+"\\\\");
            System.out.println("n politicians-yes:& " + npoliticians_y+"\\\\");
            System.out.println("n politicians-no:& " + npoliticians_n+"\\\\");
            System.out.println("n components-yes:& " + ncomponents_y+"\\\\");
            System.out.println("n components-no:& " + ncomponents_n+"\\\\");
            System.out.println("& \\\\");
        }
    }

}
