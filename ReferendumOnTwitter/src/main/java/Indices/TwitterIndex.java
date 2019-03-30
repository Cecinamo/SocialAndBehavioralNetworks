package Indices;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import static org.apache.lucene.util.Version.LUCENE_41;

/**
 * To write the lucene index.
 *
 * @author Cecilia Martinez Oliva
 */
public class TwitterIndex {

    private final IndexWriter writer;

    private final LongField date;
    private final LongField id;
    private final StringField screenname;
    private final TextField name;
    private final LongField followers;
    private final TextField text;
    private final TextField hashtags;
    private final TextField mentions;

    private final Document doc;

    public TwitterIndex(Directory dir, boolean stemming) throws IOException {
        Analyzer textAnalyzer;
        Map<String, Analyzer> analyzerPerField = new HashMap<>();
        if (stemming) {
            textAnalyzer = new ItalianAnalyzer(Version.LUCENE_41);
        } else {
            //Path path = Paths.get("stopwords-it.txt");
            Reader reader = new FileReader("stopwords-it.txt");
            textAnalyzer = new StopAnalyzer(Version.LUCENE_41, reader);
        }
        analyzerPerField.put("text", textAnalyzer);
        Analyzer nameAnalyzer = new SimpleAnalyzer(Version.LUCENE_41);
        analyzerPerField.put("name", nameAnalyzer);
        // for other fields we apply only tokenization
        Analyzer oAnalyzer = new WhitespaceAnalyzer(Version.LUCENE_41);
        PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(oAnalyzer, analyzerPerField);
        IndexWriterConfig cfg = new IndexWriterConfig(LUCENE_41, aWrapper);
        
        this.writer = new IndexWriter(dir, cfg);

        this.date = new LongField("date", 0L, Field.Store.YES);
        this.id = new LongField("id", 0L, Field.Store.YES);
        this.screenname = new StringField("screenname", "", Field.Store.YES);
        this.name = new TextField("name", "", Field.Store.YES);
        this.followers = new LongField("followers", 0L, Field.Store.YES);
        this.text = new TextField("text", "", Field.Store.YES);
        this.hashtags = new TextField("hashtags", "", Field.Store.YES);
        this.mentions = new TextField("mentions", "", Field.Store.YES);
        
        doc = new Document();
        doc.add(this.date); //
        doc.add(this.id); //
        doc.add(this.screenname); //
        doc.add(this.name); //
        doc.add(this.followers); //
        doc.add(this.text); //
        doc.add(this.hashtags); //
        doc.add(this.mentions); //
    }

    public void addTweet(Long date_value, Long id_value, String screenname_value, String name_value, 
            Long followers_value, String text_value, String hashtags_value, String mentions_value) throws IOException {
        this.date.setLongValue(date_value);
        this.id.setLongValue(id_value);
        this.screenname.setStringValue(screenname_value);
        this.name.setStringValue(name_value);
        this.followers.setLongValue(followers_value);
        this.text.setStringValue(text_value);
        this.hashtags.setStringValue(hashtags_value.toLowerCase());
        this.mentions.setStringValue(mentions_value);

        this.writer.addDocument(doc);
    }

    public void close() throws IOException {
        this.writer.commit();
        this.writer.close();
    }

}
