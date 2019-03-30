package New;

import Indices.Writer;
import YESNOsupporters.Graph;
import YESNOsupporters.YESNOsupporters;
import it.stilo.g.algo.HubnessAuthority;
import it.stilo.g.algo.SubGraph;
import it.stilo.g.structures.DoubleValues;
import it.stilo.g.structures.WeightedDirectedGraph;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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
 * quanti nel sottografo HITS
 *
 * @author Cecilia Martinez Oliva
 */
public class NHITS {

    private static String results;
    private static WeightedDirectedGraph g;
    private static Graph ga;

    private static String spaces(int n) {
        String res = "";

        for (int i = 0; i < n; i++) {
            res += " ";
        }

        return res;
    }

    private static String queryIDs(List<String> ids) throws IOException, ParseException {

        String res = "";
        String index = "indices/TwitterIndex";
        
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

            int score = (nhashtags_y*2 + npoliticians_y + ncomponents_y) - (nhashtags_n*2 + npoliticians_n + ncomponents_n);
            if (score<0)
                score = score*(-1);
            int n = 20;
            res+="name: "+spaces(n-6)+doc.get("name")+"\r\n";
            res+="screen name: "+spaces(n-13)+doc.get("screenname")+"\r\n";
            res+="followers: "+spaces(n-11)+doc.get("followers")+"\r\n";
            res+="n hashtags-yes: "+spaces(n-16)+ nhashtags_y+"\r\n";
            res+="n hashtags-no: "+spaces(n-15)+nhashtags_n+"\r\n";
            res+="n politicians-yes: "+spaces(n-19)+npoliticians_y+"\r\n";
            res+="n politicians-no: "+spaces(n-18)+npoliticians_n+"\r\n";
            res+="n components-yes: "+spaces(n-18)+ncomponents_y+"\r\n";
            res+="n components-no: "+spaces(n-17)+ncomponents_n+"\r\n";
            res+="SCORE: "+spaces(n-7)+score+"\r\n";
            res+="\r\n";
        }
        
        return res;
    }

    private static void nHITS(String f) throws IOException, ParseException {

        Writer w = new Writer(results + "/" + f + ".txt");
        YESNOsupporters s = new YESNOsupporters();
        List<String> ids = new ArrayList<>();
        ids.addAll(s.getIDs("YNsupporters/results1/M" + f + ".txt"));
        int[] startingNodes = ga.getNodes(ids);
        w.add("starting size: " + startingNodes.length + "\r\n");
        WeightedDirectedGraph gs = SubGraph.extract(g, startingNodes, 2);
        // load the root
        List<String> root = s.getIDs("YNsupporters/results3/" + f + ".txt");
        Writer wr = new Writer(results + "/" + f + "-root.txt");
        wr.add(queryIDs(root));
        wr.close();
        int[] nodes = ga.getNodes(root);
        int n = 15 - 5;
        w.add("|R|: " + spaces(n) + nodes.length + "\r\n");
        // find S=B U R
        Set<Integer> ss = new HashSet<>();
        ss.addAll(ga.getIns(nodes, gs));
        ss.addAll(ga.getOuts(nodes, gs));
        w.add("|B|: " + spaces(n) + ss.size() + "\r\n");
        Collections.addAll(ss, Arrays.stream(nodes).boxed().toArray(Integer[]::new));

        n = 15 - 13;
        w.add("|S|=|B U R|: " + spaces(n) + ss.size() + "\r\n");
        System.out.println("size S: " + ss.size());
        w.close();

        //int[] s_nodes = ss.stream().mapToInt(Number::intValue).toArray();
        //WeightedDirectedGraph gnew = SubGraph.extract(gs, s_nodes, 2);
        // compute  hits
        //ArrayList<ArrayList<DoubleValues>> hits = HubnessAuthority.compute(gnew, 0.00001, 2);
        //ga.getAuthorities(hits, 500, results + "/authorities"+f+".txt");
        //ga.getHubs(hits, 500, results + "/hubs"+f+".txt");
    }

    public static void main(String[] args) throws IOException, ParseException {

        results = "YNsupporters/results3/New";
        new File(results).mkdirs();

        ga = new Graph();
        // let's load the graph
        g = ga.getGraph();

        nHITS("yes");
        nHITS("no");
    }

}
