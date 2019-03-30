package TemporalAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * K-means algorithm.
 *
 * @author Camila Maria Garcìa
 * @author Cecilia Martinez Oliva
 *
 */
public class Kmeans {

    private final Map<String, ArrayList<Double>> dic;
    private final String[] keys;
    private final int nterms;
    private final int ncomponents;
    private double clusterDist;
    private List<Double> scores;
    private ArrayList<ArrayList<Double>> clusterCentroids;
    private final Random rand = new Random(123);

    /**
     * @param dic dictonary containing the terms as keys, the time series as
     * values.
     */
    public Kmeans(Map<String, ArrayList<Double>> dic) {
        this.dic = dic;
        this.keys = dic.keySet().toArray(new String[dic.keySet().size()]);
        this.nterms = keys.length;
        this.ncomponents = dic.get(keys[0]).size();
    }

    /**
     * @param n number of clusters to be returned.
     * @return a dictonary containing for each cluster, the terms assigned to
     * it.
     */
    public Map<Integer, ArrayList<String>> compute(int n) {
        Map<Integer, ArrayList<String>> fclusters = new HashMap<>();
        // c are the terms chosen as centroids
        ArrayList<String> c = getCentroids(n);
        // we then need to save their associated vectors
        ArrayList<ArrayList<Double>> centroids = new ArrayList<>();
        for (String k : c) {
            centroids.add(dic.get(k));
        }
        //System.out.println(c);
        double eps = 0.000000001;
        int maxiter = 1000000000;

        double tOldDist;
        double tNewDist = 9999; // if it is 0, it will stop at the 2 step.
        int iter = 0;
        int[] clusters;
        boolean cont = true;
        while (cont && iter < maxiter) {
            clusters = new int[nterms];
            iter++;
            // for each term
            for (int term = 0; term < nterms; term++) {
                double mindist = 99999;
                // we have to find the best cluster (depending on the nearest centroid)
                for (int clust = 0; clust < n; clust++) {
                    double dist = eucDistance(centroids.get(clust), dic.get(keys[term]));
                    //System.out.println(clust + " " + keys[term] + " " + dist);
                    if (dist < mindist) {
                        mindist = dist;
                        clusters[term] = clust;
                    }
                    // se ci sono due cluster esattamente uguali, andrà sicuramente nel primo
                    if (dist == mindist) {
                        int r = rand.nextInt(2); // testa o croce
                        if (r == 1) {
                            mindist = dist;
                            clusters[term] = clust;

                        }
                    }
                }
                //System.out.println(clusters[term]);
            }
            // to have a dictionary of terms for each cluster instead of an array of integers.
            fclusters = getClusters(clusters);
            n = fclusters.size();
            tOldDist = tNewDist;
            tNewDist = getScore(centroids, fclusters);
            clusterCentroids = centroids;
            centroids = newCentroids(fclusters);
            //System.out.println(fclusters);
            //System.out.println(centrs);
            //System.out.println(tNewDist);
            cont = (tOldDist - tNewDist) > eps;

        }
        //System.out.println(centroids);
        clusterDist = tNewDist;
        //System.out.println("iterations: " + iter);
        return fclusters;
    }
    
public Map<Integer, ArrayList<String>> compute(int n, int nrand) {
    Map<Integer, ArrayList<String>> fclusters = new HashMap<>();
    List<Double> cScores = new ArrayList<>();
    ArrayList<ArrayList<Double>> centroids = new ArrayList<>();
    
    double distOld = 9999999;
    double distNew;
    for(int iter=0; iter<nrand; iter++) {
        Map<Integer, ArrayList<String>> clusters = compute(n);
        distNew = clusterDist;
        if(distOld>distNew) {
            System.out.println("best score:"+distNew);
            fclusters = clusters;
            distOld = distNew;
            cScores = scores;
            centroids = clusterCentroids;
        }
    }
    scores = cScores;
    clusterCentroids = centroids;
    return(fclusters);
}

    // chooses at random n starting terms, as centroids.
    private ArrayList<String> getCentroids(int n) {
        Set<String> c = new HashSet();
        do {
            c.add(this.keys[rand.nextInt(this.keys.length)]);
        } while (c.size() < n);
        ArrayList<String> centroids = new ArrayList<>();
        centroids.addAll(c);
        return (centroids);
    }
    
    public ArrayList<ArrayList<Double>> getCentroids() {
        return clusterCentroids;
    }

    // computes the eucledean distance of two given vectors
    private double eucDistance(ArrayList<Double> vec1, ArrayList<Double> vec2) {
        double dist = 0;
        for (int comp = 0; comp < ncomponents; comp++) {
            dist += Math.pow(vec1.get(comp) - vec2.get(comp), 2);
        }
        return Math.sqrt(dist);
    }

    // each element of clusters is the cluster assigned a term (the position is 
    // given by the array keys).
    private Map<Integer, ArrayList<String>> getClusters(int[] clusters) {
        Map<Integer, ArrayList<String>> fclusters = new HashMap<>();
        for (int term = 0; term < nterms; term++) {
            int cluster = clusters[term];
            ArrayList<String> terms = new ArrayList<>();
            terms.add(keys[term]);
            if (fclusters.containsKey(cluster)) {
                terms.addAll(fclusters.get(cluster));
            }
            fclusters.put(cluster, terms);
        }
        return fclusters;
    }

    // we have to compute the mean of all terms in the clusters.
    private ArrayList<ArrayList<Double>> newCentroids(Map<Integer, ArrayList<String>> clusters) {

        ArrayList<ArrayList<Double>> centroids = new ArrayList<>();
        //System.out.println(clusters.keySet());
        //System.out.println(clusters.keySet().size());

        // for each cluster
        for (int clust : clusters.keySet()) {
            // all zeros
            double[] app = new double[ncomponents];
            for (int i = 0; i < app.length; i++) {
                app[i] = 0;
            }
            // for each term in the cluster
            for (String term : clusters.get(clust)) {
                // for each component
                for (int comp = 0; comp < ncomponents; comp++) {
                    app[comp] += dic.get(term).get(comp);
                }
            }
            // compute the mean and add to the final list
            ArrayList<Double> app2 = new ArrayList<>();
            for (int i = 0; i < app.length; i++) {
                app[i] = app[i] / clusters.get(clust).size();
                app2.add(app[i]);
            }
            centroids.add(app2);
        }
        return (centroids);
    }

    // the score is given by the mean of the intra cluster distances.
    private double getScore(ArrayList<ArrayList<Double>> centroids, Map<Integer, ArrayList<String>> clusters) {

        List<Double> cScores = new ArrayList<>();
        double score = 0;

        int n = 0; // centroids se il cluster è vuoto salta
        // for each cluster
        for (int clust : clusters.keySet()) {
            //System.out.println(clusters.size());
            //System.out.println(centroids.size());
            // for each term in the cluster
            double dist = 0;

            for (String term : clusters.get(clust)) {
                dist += eucDistance(centroids.get(n), dic.get(term));
            }
            // dist of a cluster
            cScores.add(dist);
            score += dist; // clusters.get(clust).size();
            n++;
        }
        scores = cScores;
        // mean dist of all clusters
        //score = score / clusters.size();
        return score;
    }
    
    public List<Double> getScores() {
        return scores;
    }
}
