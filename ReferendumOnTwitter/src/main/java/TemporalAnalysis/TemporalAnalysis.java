package TemporalAnalysis;

import Indices.Writer;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FieldCacheRangeFilter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.Version;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 *
 * @author Camila Maria Garcìa
 * @author Cecilia Martinez Oliva
 */
public class TemporalAnalysis {

    private final IndexReader ir;
    //private final ArrayList<String> time = new ArrayList<>();
    private ArrayList<Long> t;

    /**
     * @param index the lucene index folder.
     * @throws java.io.IOException ...
     */
    public TemporalAnalysis(String index) throws IOException {
        Directory dir = new SimpleFSDirectory(new File(index));
        this.ir = DirectoryReader.open(dir);
        //setGrain(grain);

    }

    public void setGrain(long grain) throws ParseException {
        String s = "2016/11/26 12:00:00";
        String e = "2016/12/07 00:00:00";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date sd = sdf.parse(s);
        Date ed = sdf.parse(e);
        long sm = sd.getTime();
        long em = ed.getTime();
        this.t = new ArrayList<>();

        for (long d = sm; d < em + grain; d += grain) {
            this.t.add(d);
        }
    }

    private List<Long> getGrain(long grain) throws ParseException {
        String s = "2016/11/26 12:00:00";
        String e = "2016/12/07 00:00:00";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date sd = sdf.parse(s);
        Date ed = sdf.parse(e);
        long sm = sd.getTime();
        long em = ed.getTime();
        List<Long> t_o = new ArrayList<>();

        for (long d = sm; d < em + grain; d += grain) {
            t_o.add(d);
        }
        return t_o;
    }

    /**
     * Extracts the number of tweets from the index.
     *
     * @return the number of tweets.
     * @throws java.io.IOException ...
     */
    public int ntweets() throws IOException {
        return (this.ir.numDocs());
    }

    /**
     * Extracts the number of users from the index.
     *
     * @return the number of users.
     * @throws java.io.IOException ...
     */
    public int nusers() throws IOException {
        Fields fields = MultiFields.getFields(this.ir);
        Terms terms = fields.terms("screenname");
        TermsEnum iterator = terms.iterator(null);
        BytesRef byteRef;

        int count = 0;
        while ((byteRef = iterator.next()) != null) {
            //System.out.println(new String(byteRef.bytes, byteRef.offset, byteRef.length));
            count++;
        }
        return (count);

    }

    public int nusers_test() throws IOException {
        Fields fields = MultiFields.getFields(this.ir);
        String field = "id";
        Terms terms = fields.terms(field);
        TermsEnum iterator = terms.iterator(null);
        BytesRef byteRef;

        IndexSearcher searcher = new IndexSearcher(this.ir);

        int count = 0;
        while ((byteRef = iterator.next()) != null) {
            Query n;
            if (field.equals("id")) {
                // QUERY PER ID: alcuni id non esistono!!
                long id = NumericUtils.prefixCodedToLong(byteRef);
                System.out.println(id + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

                BytesRef ref = new BytesRef();
                NumericUtils.longToPrefixCoded(id, 0, ref);
                n = new TermQuery(new Term(field, ref));
            } else {
                // QUERY PER NAME (O SCREENNAME)
                String name = new String(byteRef.bytes, byteRef.offset, byteRef.length);
                System.out.println(name + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                n = new TermQuery(new Term(field, name));

            }

            //
            TopDocs top = searcher.search(n, 100000);
            ScoreDoc[] hits = top.scoreDocs;

            Document doc;
            Set<ArrayList<String>> s = new HashSet<>();

            for (ScoreDoc entry : hits) {
                ArrayList<String> tris = new ArrayList<>();
                doc = searcher.doc(entry.doc);
                tris.add(doc.get("name"));
                tris.add(doc.get("screenname"));
                tris.add(doc.get("id"));
                if (s.contains(tris) == false) {
                    System.out.println(entry.doc + "-" + tris.get(0) + "-" + tris.get(1) + "-" + tris.get(2));
                    count++;
                } else {
                    //System.out.println("lalalalal");
                }
                s.add(tris);
            }
        }
        return (count);

    }

    /**
     * Extracts the first and last date from the index.
     *
     * @return the first date, the last date.
     * @throws java.io.IOException ...
     */
    public Date[] trange() throws IOException {
        ArrayList<Long> tt = new ArrayList<>();
        Date[] range = new Date[2];
        Fields fields = MultiFields.getFields(this.ir);
        Terms terms = fields.terms("date");
        TermsEnum iterator = terms.iterator(null);
        BytesRef byteRef;

        while ((byteRef = iterator.next()) != null) {
            Long time = NumericUtils.prefixCodedToLong(byteRef);
            tt.add(time);
            //System.out.println(new Date(time));
        }
        range[0] = new Date(tt.get(0));
        range[1] = new Date(tt.get(tt.size() - 1));

        return (range);

    }


    /**
     * Number of tweets in a given range of time.
     *
     * @return number of tweets.
     */
    private int queries(long millis_start, long millis_end) throws IOException, org.apache.lucene.queryparser.classic.ParseException {

        IndexSearcher searcher = new IndexSearcher(this.ir);
        Query query = NumericRangeQuery.newLongRange("date", millis_start, millis_end, true, false);
        TopDocs top = searcher.search(query, 1000000000);
        return (top.totalHits);
    }

    /**
     * Frequency of a term in a given range of time.
     *
     * @param term
     * @param field
     * @param millis_start
     * @param millis_end
     * @param stemming
     * @return number of tweets.
     * @throws java.io.IOException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public int queries(String term, String field, long millis_start, long millis_end, boolean stemming) throws IOException, org.apache.lucene.queryparser.classic.ParseException {

        IndexSearcher searcher = new IndexSearcher(ir);
        Analyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41);
        QueryParser parser = new QueryParser(Version.LUCENE_41, field, analyzer);
        Query query;
        if (stemming) {
            query = parser.parse(term);
        } else {
            query = new TermQuery(new Term(field, term));
        }
        TotalHitCountCollector collector = new TotalHitCountCollector();
        FieldCacheRangeFilter<Long> dateFilter = FieldCacheRangeFilter.newLongRange("date", millis_start, millis_end, true, true);
        searcher.search(query, dateFilter, collector);
        return (collector.getTotalHits());
    }

    public int queries(String[] terms, long millis_start, long millis_end, boolean stemming) throws IOException, org.apache.lucene.queryparser.classic.ParseException {

        IndexSearcher searcher = new IndexSearcher(ir);
        Analyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41);
        QueryParser parser = new QueryParser(Version.LUCENE_41, "text", analyzer);
        BooleanQuery qterms = new BooleanQuery();
        for (String tt : terms) {
            Query term;
            if (stemming) {
                term = parser.parse(tt);
            } else {
                term = new TermQuery(new Term("text", tt));
            }
            qterms.add(term, BooleanClause.Occur.MUST);
        }
        //BooleanQuery query = new BooleanQuery();
        //query.add(qterms, BooleanClause.Occur.MUST);
        TotalHitCountCollector collector = new TotalHitCountCollector();
        FieldCacheRangeFilter<Long> dateFilter = FieldCacheRangeFilter.newLongRange("date", millis_start, millis_end, true, true);
        searcher.search(qterms, dateFilter, collector);
        return (collector.getTotalHits());
    }

    public String queriesTweets(String[] terms, long millis_start, long millis_end, boolean stemming) throws IOException, org.apache.lucene.queryparser.classic.ParseException {

        String tweets = "";
        IndexSearcher searcher = new IndexSearcher(ir);
        Analyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41);
        QueryParser parser = new QueryParser(Version.LUCENE_41, "text", analyzer);
        BooleanQuery qterms = new BooleanQuery();
        for (String tt : terms) {
            Query term;
            if (stemming) {
                term = parser.parse(tt);
            } else {
                term = new TermQuery(new Term("text", tt));
            }
            qterms.add(term, BooleanClause.Occur.MUST);
        }
        //BooleanQuery query = new BooleanQuery();
        //query.add(qterms, BooleanClause.Occur.MUST);
        FieldCacheRangeFilter<Long> dateFilter = FieldCacheRangeFilter.newLongRange("date", millis_start, millis_end, true, true);
        TopDocs top = searcher.search(qterms, dateFilter, 100);
        ScoreDoc[] hits = top.scoreDocs;

        Document doc;

        for (ScoreDoc entry : hits) {
            doc = searcher.doc(entry.doc);
            long date = Long.parseLong(doc.get("date"));
            String datec = new Date(date).toString();
            tweets += "DATE: " + datec + "\r\n";
            tweets += "NAME: " + doc.get("name") + "\r\n";
            //System.out.println("ID: " + doc.get("id"));
            tweets += "TWEET: " + doc.get("text") + "\r\n";
            tweets += "\r\n";
        }
        //System.out.println(tweets);
        return (tweets);
    }

    public String queriesTweets(String term, String field, boolean stemming) throws IOException, org.apache.lucene.queryparser.classic.ParseException {

        String tweets = "";
        IndexSearcher searcher = new IndexSearcher(ir);
        Analyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41);
        QueryParser parser = new QueryParser(Version.LUCENE_41, field, analyzer);

        Query query;

        if (stemming) {
            query = parser.parse(term);
        } else {
            query = new TermQuery(new Term(field, term));
        }

        TopDocs top = searcher.search(query, 100);
        ScoreDoc[] hits = top.scoreDocs;

        Document doc;

        for (ScoreDoc entry : hits) {
            doc = searcher.doc(entry.doc);
            long date = Long.parseLong(doc.get("date"));
            String datec = new Date(date).toString();
            tweets += "DATE: " + datec + "\r\n";
            tweets += "NAME: " + doc.get("name") + "\r\n";
            //System.out.println("ID: " + doc.get("id"));
            tweets += "TWEET: " + doc.get("text") + "\r\n";
            tweets += "\r\n";
        }
        //System.out.println(tweets);
        return (tweets);
    }

    /**
     * Number of tweets in common for two given terms.
     *
     * @param term1 first term
     * @param term2 second term
     * @param stemming
     * @return number of tweets.
     * @throws java.io.IOException ...
     * @throws org.apache.lucene.queryparser.classic.ParseException ...
     */
    public int queries(String term1, String term2, String field, boolean stemming) throws IOException, org.apache.lucene.queryparser.classic.ParseException {

        IndexSearcher searcher = new IndexSearcher(ir);
        Analyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41);
        QueryParser parser = new QueryParser(Version.LUCENE_41, field, analyzer);
        Query t1;
        Query t2;
        if (stemming) {
            t1 = parser.parse(term1);
            t2 = parser.parse(term2);
        } else {
            t1 = new TermQuery(new Term(field, term1));
            t2 = new TermQuery(new Term(field, term2));
        }
        BooleanQuery query = new BooleanQuery();
        query.add(t1, BooleanClause.Occur.MUST);
        query.add(t2, BooleanClause.Occur.MUST);

        TotalHitCountCollector collector = new TotalHitCountCollector();
        searcher.search(query, collector);

        return (collector.getTotalHits());
    }

    /**
     * Distribution of tweets in time (the discretization is given by variable
     * time).
     *
     * @return time series of tweets.
     * @throws java.text.ParseException ...
     * @throws java.io.IOException ...
     * @throws org.apache.lucene.queryparser.classic.ParseException ...
     * @see #queries(long, long)
     */
    public ArrayList<Double> temp() throws ParseException, IOException, org.apache.lucene.queryparser.classic.ParseException {

        ArrayList<Double> values = new ArrayList<>();
        double res;

        for (int n = 1; n < t.size(); n++) {
            res = queries(t.get(n - 1), t.get(n));
            values.add(res);
        }

        return (values);
    }

    /**
     * Distribution of a group of terms in time (the discretization is given by
     * variable time).
     *
     * @param terms to be analyzed
     * @param path
     * @param stemming
     * @return time series of tweets.
     * @throws java.text.ParseException ...
     * @throws java.io.IOException ...
     * @throws org.apache.lucene.queryparser.classic.ParseException ...
     * @see #queriesTweets(java.lang.String[], long, long, boolean)
     * @see #queries(java.lang.String[], long, long, boolean)
     * @see Writer
     */
    public ArrayList<Double> temp(String[] terms, String path, boolean stemming) throws ParseException, IOException, org.apache.lucene.queryparser.classic.ParseException {

        ArrayList<Double> values = new ArrayList<>();
        double res;
        //List<String> h = getTaxis();

        for (int n = 0; n < t.size() - 1; n++) {
            String[] datec = new Date(t.get(n)).toString().split(" ");
            String name = path + "/" + datec[1] + datec[2] + "-" + datec[3].substring(0, 2) + ".txt";
            //System.out.println(name);
            Writer w = new Writer(name);
            w.add(queriesTweets(terms, t.get(n), t.get(n + 1), stemming));
            w.close();

            res = queries(terms, t.get(n), t.get(n + 1), stemming);
            values.add(res);
        }

        return (values);
    }

    /**
     * Discretization of time.
     *
     * @return time.
     */
    public List<String> getTaxis() {
        List<String> time = new ArrayList<>();
        for (long d : t) {
            Date date = new Date(d);
            time.add(date.toString().substring(4, 16));

        }
        return (time.subList(0, time.size() - 1));
    }

    public ArrayList<Long> getT() {
        return (t);
    }

    // we want to order all terms for frequencies. We create a tree map in which keys 
    // are the term frequencies, inside we have the terms associated to tha frequency. 
    private NavigableMap<Integer, ArrayList<String>> getDic(String field) throws IOException {
        NavigableMap<Integer, ArrayList<String>> allTerms = new TreeMap<>(Collections.reverseOrder());
        Fields fields = MultiFields.getFields(ir);
        Terms terms = fields.terms(field);
        TermsEnum iterator = terms.iterator(null);
        BytesRef byteRef;

        while ((byteRef = iterator.next()) != null) {

            //String tt = new String(byteRef.bytes, byteRef.offset, byteRef.length);
            String tt = byteRef.utf8ToString();

            int freq = iterator.docFreq();
            ArrayList<String> ts = new ArrayList<>();
            ts.add(tt);
            if (allTerms.containsKey(freq) == true) {// if already exist words with that frequency
                ts.addAll(allTerms.get(freq));
            }
            allTerms.put(freq, ts);
        }
        return (allTerms);
    }

    /**
     * Terms ordered by frequency. Terms with no alphabet characters are
     * discarded.
     *
     * @param field
     * @return ordered list of terms.
     * @throws java.io.IOException ...
     * @see #getDic()
     */
    public ArrayList<String> orderedTerms(String field) throws IOException {
        NavigableMap<Integer, ArrayList<String>> terms = getDic(field);

        ArrayList<String> oterms = new ArrayList<>();
        for (Integer freq : terms.keySet()) {
            if (freq > 10) {
                //System.out.println(terms.get(freq) + ": " + freq);
                for (String term : terms.get(freq)) {
                    // terms containing at least one alphabet character
                    if (term.matches(".*[a-z]+.*") && term.length() > 2) {
                        oterms.add(term);
                    }
                }
            }
        }
        return (oterms);
    }

    // to create the plot's dataset.
    public DefaultCategoryDataset createDataset(DefaultCategoryDataset dataset, List<String> dates, ArrayList<Double> values, String term, int n) {
        for (int date = 0; date < dates.size(); date++) {
            dataset.addValue(values.get(date), term, dates.get(date));
        }
        return dataset;
    }

    // to create the plot's dataset.
    public TimeSeriesCollection createDataset(TimeSeriesCollection dataset, ArrayList<Double> values, String term) {

        TimeSeries series = new TimeSeries(term);
        for (int date = 0; date < t.size() - 1; date++) {
            Hour h = new Hour(new Date(t.get(date)));
            series.add(h, values.get(date));
        }
        dataset.addSeries(series);
        return dataset;
    }

    // to build the sax string, given the time series. two symbols.
    public String sax(ArrayList<Integer> timeSeries, int alphabetSize) throws SAXException {
        double[] ts = timeSeries.stream().mapToDouble(Integer::doubleValue).toArray();

        double nThreshold = 0.01;
        // instantiate classes
        NormalAlphabet na = new NormalAlphabet();
        SAXProcessor sp = new SAXProcessor();

        // perform the discretization
        SAXRecords res = sp.ts2saxByChunking(ts, ts.length, na.getCuts(alphabetSize), nThreshold);
        String sax = res.getSAXString("");

        return (sax);
    }

    /**
     * Conversion from alphabet characters to numbers.
     *
     * @param saxString sax in alphabet characters.
     * @return sax in numbers.
     */
    public ArrayList<Double> toNum(String saxString) {
        ArrayList<Double> saxn = new ArrayList<>();
        char[] ch = saxString.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            double temp = (double) ch[i];
            double temp_integer = 96d; //for lower case
            saxn.add(temp - temp_integer);
        }
        return (saxn);
    }

    /**
     * SAX for the top terms
     *
     * @param field
     * @param stemming
     * @param alphabetSize
     * @param match
     * @return final terms.
     * @throws java.text.ParseException ...
     * @throws java.io.IOException ...
     * @throws org.apache.lucene.queryparser.classic.ParseException ...
     * @throws net.seninp.jmotif.sax.SAXException ...
     * @see #orderedTerms()
     * @see #queries(java.lang.String, long, long, boolean)
     * @see #sax(java.util.ArrayList)
     * @see #toNum(java.lang.String)
     */
    public Map<String, ArrayList<Double>> findSax(String field, int alphabetSize, String match) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException, SAXException {

        int res;
        boolean b;
        ArrayList<String> terms = orderedTerms(field);
        Map<String, ArrayList<Double>> sax = new HashMap<>();

        for (String term : terms) {
            // we first need to test the collective attention
            ArrayList<Integer> values = new ArrayList<>();
            String saxString;
            //ArrayList<Double> saxValues;
            List<Long> g = getGrain(43200000 * 2L);
            for (int d = 1; d < g.size(); d++) {
                res = queries(term, field, g.get(d - 1), g.get(d), false);
                values.add(res);
            }
            saxString = sax(values, 2);
            //saxValues = toNum(saxString);
            //System.out.println(saxString);
            //System.out.println(saxValues);
            //b = saxString.matches("[a-z]+[ab]{6}"); //6
            b = saxString.matches(match); //6
            //b = true; // ATTENZIONE!!!!
            if (b) {
                //System.out.println(term);
                //System.out.println(saxString);

                // lo ricalcoliamo con il grain giusto
                values = new ArrayList<>();
                for (int d = 1; d < t.size(); d++) {
                    res = queries(term, field, t.get(d - 1), t.get(d), false);
                    values.add(res);
                }
                saxString = sax(values, alphabetSize);
                ArrayList<Double> saxValues = toNum(saxString);
                sax.put(term, saxValues);
                //System.out.println(values);
            }
            if (sax.size() == 1000) {
                break;
            }
        }

        return (sax);
    }

    public void testSax(String[] terms, String field, boolean stemming, int alphabetSize) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException, SAXException {

        int res;

        for (String term : terms) {
            ArrayList<Integer> values = new ArrayList<>();
            String saxString;
            //ArrayList<Double> saxValues;
            List<Long> g = getGrain(43200000 * 2L);
            for (int d = 1; d < g.size(); d++) {
                res = queries(term, field, g.get(d - 1), g.get(d), stemming);
                values.add(res);
            }
            saxString = sax(values, alphabetSize);
            //saxValues = toNum(saxString);
            System.out.println(term + ": " + saxString);
            //System.out.println(saxValues);
        }
    }

    public int termFrequency(String term, String field, boolean stemming) throws org.apache.lucene.queryparser.classic.ParseException, IOException {
        IndexSearcher searcher = new IndexSearcher(ir);
        Analyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41);
        QueryParser parser = new QueryParser(Version.LUCENE_41, field, analyzer);
        Query qt;
        if (stemming) {
            qt = parser.parse(term);
        } else {
            qt = new TermQuery(new Term(field, term));
        }

        TotalHitCountCollector collector = new TotalHitCountCollector();
        searcher.search(qt, collector);

        return (collector.getTotalHits());
    }

    public int termFrequencies(ArrayList<String> terms, boolean stemming) throws org.apache.lucene.queryparser.classic.ParseException, IOException {
        IndexSearcher searcher = new IndexSearcher(ir);
        Analyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41);
        QueryParser parser = new QueryParser(Version.LUCENE_41, "text", analyzer);
        BooleanQuery qt = new BooleanQuery();
        if (stemming) {
            for (String term : terms) {
                qt.add(parser.parse(term), BooleanClause.Occur.MUST);
            }
        } else {
            for (String term : terms) {
                qt.add(new TermQuery(new Term("text", term)), BooleanClause.Occur.MUST);
            }
        }

        TotalHitCountCollector collector = new TotalHitCountCollector();
        searcher.search(qt, collector);

        return (collector.getTotalHits());
    }

}
