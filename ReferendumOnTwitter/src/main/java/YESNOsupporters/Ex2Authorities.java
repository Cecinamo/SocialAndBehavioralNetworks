package YESNOsupporters;

import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.util.NodesMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import org.apache.lucene.document.Document;
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
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

/**
 * 2.
 *
 * @author Cecilia Martinez Oliva
 */
public class Ex2Authorities {

    private static NodesMapper<String> mapper;
    private static WeightedDirectedGraph g;

    private static void getGraph() throws FileNotFoundException, IOException {
        g = new WeightedDirectedGraph(2287781);
        FileInputStream fstream = new FileInputStream("YNsupporters/results2/lc.txt");
        InputStreamReader isr = new InputStreamReader(fstream, "UTF-8");
        BufferedReader br = new BufferedReader(isr);

        String line;

        while ((line = br.readLine()) != null) {
            String[] edge = line.split("\t");
            double w = Double.parseDouble(edge[2]);
            g.add(mapper.getId(edge[0]), mapper.getId(edge[1]), w);
        }
    }

    private static int[] supporters(List<String> ids) throws IOException {

        int[] res = new int[2];
        YESNOsupporters s = new YESNOsupporters();
        List<String> yes = s.getIDs("YNsupporters/results1/Myes.txt");
        //List<String> no = s.getIDs("YNsupporters/results1/Mno.txt");
        int countYes = 0;
        int countNo = 0;

        for (String id : ids) {
            if (yes.contains(id)) {
                countYes++;
            } else {
                countNo++;
            }
        }
        res[0] = countYes;
        res[1] = countNo;
        return res;
    }

    // per ogni id voglio nome, followers, in-degree, e se è per il sì o per il no
    private static void queryIDs(List<String> ids, boolean stemming) throws IOException, ParseException {

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

            System.out.println("name: " + doc.get("name"));
            System.out.println("screen name: " + doc.get("screenname"));
            System.out.println("followers: " + doc.get("followers"));
            int n = mapper.getId(id);
            System.out.println("in-degree: " + g.in[n].length);
            System.out.println("n mentions: " + mentions(doc.get("screenname"), true));
            System.out.println("n hashtags-yes: " + nhashtags_y);
            System.out.println("n hashtags-no: " + nhashtags_n);
            System.out.println("n politicians-yes: " + npoliticians_y);
            System.out.println("n politicians-no: " + npoliticians_n);
            System.out.println("n components-yes: " + ncomponents_y);
            System.out.println("n components-no: " + ncomponents_n);
            System.out.println();
        }
    }

    private static int mentions(String screenname, boolean stemming) throws IOException, ParseException {

        String index;
        if (stemming) {
            index = "indices/TwitterIndex";
        } else {
            index = "indices/TwitterIndexNoStem";
        }
        Directory dir = new SimpleFSDirectory(new File(index));
        IndexReader ir = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(ir);

        Query query = new TermQuery(new Term("mentions", screenname));

        TopDocs top = searcher.search(query, 1000000000);

        return top.totalHits;
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public static void main(String[] args) throws IOException, ParseException {

        boolean stemming = true;
        mapper = new NodesMapper<>();
        getGraph();

        YESNOsupporters s = new YESNOsupporters();
        List<String> authorities = s.getIDs("YNsupporters/results2/authorities.txt");
        List<String> hubs = s.getIDs("YNsupporters/results2/hubs.txt");

        int[] yn = supporters(authorities);
        System.out.println("N yes authorities: " + yn[0]);
        System.out.println("N no authorities: " + yn[1]);

        yn = supporters(hubs);
        System.out.println("N yes hubs: " + yn[0]);
        System.out.println("N no hubs: " + yn[1]);

        System.out.println();

        //queryIDs(authorities, stemming);
        queryIDs(hubs, stemming);
    }

}
