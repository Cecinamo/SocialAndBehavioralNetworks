package YESNOsupporters;

import java.io.IOException;
import java.util.List;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 * 3.
 * @author Cecilia Martinez Oliva
 */
public class Ex3Results {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public static void main(String[] args) throws IOException, ParseException {

        String c = "no";
        boolean stemming = true;
        YESNOsupporters s = new YESNOsupporters();
        List<String> ids = s.getIDs("YNsupporters/results3/hubs" + c + ".txt");

        s.queryIDs(ids, stemming);
    }

}
