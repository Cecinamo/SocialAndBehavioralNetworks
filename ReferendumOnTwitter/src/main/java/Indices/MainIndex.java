/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Indices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import twitter4j.HashtagEntity;
import twitter4j.TwitterException;
import twitter4j.UserMentionEntity;

/**
 *
 * @author Cecilia Martinez Oliva
 */
public class MainIndex {

    private static void index(Directory dir, boolean stemming) throws IOException, TwitterException {
        // let's create an index for or the tweets collected.
        File folder = new File("stream");
        //System.out.println(folder);

        File[] days = folder.listFiles();
        for (File day : days) {
            if (day.isDirectory()) {
                System.out.println(day.getName());
                File[] listOfFiles = day.listFiles();
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        //to create the index.
                        TwitterIndex index = new TwitterIndex(dir, stemming);
                        // let's read the file
                        StatusWrapper sw;
                        FileInputStream fstream = new FileInputStream(file);
                        GZIPInputStream gzStream = new GZIPInputStream(fstream);
                        InputStreamReader isr = new InputStreamReader(gzStream, "UTF-8");
                        BufferedReader br = new BufferedReader(isr);

                        String line;

                        while ((line = br.readLine()) != null) {
                            sw = new StatusWrapper();
                            sw.load(line);
                            Long time = sw.getTime();
                            Long id = sw.getStatus().getUser().getId();
                            String screenName = sw.getStatus().getUser().getScreenName();
                            String name = sw.getStatus().getUser().getName().toLowerCase();
                            Long followers = (long) sw.getStatus().getUser().getFollowersCount();
                            String text = sw.getStatus().getText();
                            String hashtags = "";
                            HashtagEntity[] he = sw.getStatus().getHashtagEntities();
                            for (HashtagEntity hashtag : he) {
                                hashtags += hashtag.getText() + " ";
                            }
                            String mentions = "";
                            UserMentionEntity[] ue = sw.getStatus().getUserMentionEntities();
                            for (UserMentionEntity mention : ue) {
                                mentions += mention.getText() + " ";
                            }
                            //System.out.println("TEXT: "+text);
                            //System.out.println("hashtags: "+hashtags);
                            //System.out.println("mentions: "+mentions);
                            index.addTweet(time, id, screenName, name, followers, text, hashtags, mentions);
                        }

                        br.close();
                        index.close();

                    }

                }
            }
        }

    }

    public static void main(String[] args) throws IOException, TwitterException {

        String results = "indices";
        new File(results).mkdir();
        boolean stemming = true;

        String TW;

        if (stemming) {
            TW = "indices/TwitterIndex";
        } else {
            TW = "indices/TwitterIndexNoStem";
        }

        Directory dir = new SimpleFSDirectory(new File(TW));
        index(dir,stemming); // to create the index... only one time!!
    }
}
