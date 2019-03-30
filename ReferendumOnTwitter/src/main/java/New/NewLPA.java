
package New;

import it.stilo.g.structures.WeightedGraph;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Cecilia Martinez Oliva
 */

public class NewLPA implements Runnable {

    private static final Logger logger = LogManager.getLogger(NewLPA.class);

    private static Random rnd;
    private WeightedGraph g;

    private int chunk;
    private int runner;
    private CountDownLatch barrier;

    private int[] labels;
    private int[] list = null;

    private NewLPA(WeightedGraph g, CountDownLatch cb, int[] labels, int chunk, int runner) {
        this.g = g;
        this.runner = runner;
        this.barrier = cb;
        this.labels = labels;
        this.chunk = chunk;
    }

    private boolean initList() {
        if (list == null) {
            //labels = startingLabels;

            // Partitioning over worker
            list = new int[(g.in.length / runner) + runner];

            int j = 0;

            for (int i = chunk; i < g.in.length; i += runner) {

                // possono cambiare tutte
                if (g.in[i] != null) {
                    //System.out.println(labels[i]);
                    list[j] = i;
                    j++;
                }
            }
            list = Arrays.copyOf(list, j);

            //System.out.println(list.length);
            //Shuffle
            for (int i = 0; i < list.length; i++) {
                for (int z = 0; z < 10; z++) {
                    int randomPosition = rnd.nextInt(list.length);
                    int temp = list[i];
                    list[i] = list[randomPosition];
                    list[randomPosition] = temp;
                }
            }

            return true;
        }
        return false;
    }

    public void run() {

        if (!initList()) {
            for (int i = 0; i < list.length; i++) {
                int[] near = g.in[list[i]];
                int[] nearLabs = new int[near.length];
                for (int x = 0; x < near.length; x++) {
                    nearLabs[x] = labels[near[x]];
                }
                int bl = bestLabel(nearLabs);
                if (bl != 0) {
                    labels[list[i]] = bl;
                }
                //if (chunk == 0) {
                //    System.out.println("i: " + list[i]);
                //    System.out.println("nearLabs: " + Arrays.toString(nearLabs));
                //    System.out.println("labels: " + Arrays.toString(labels));
                //}
            }
        }
        
        barrier.countDown();
    }

    public static int bestLabel(int[] neighborhood) {
        Arrays.sort(neighborhood);
        
        // se è una sola, quella vince
        if (neighborhood.length == 1) {
            return neighborhood[0];
        }
        int best = -1;
        int maxCount = 0;
        int counter = 0;
        int last = -1; // perché -1 esiste veramente
        for (int i = 0; i < neighborhood.length; i++) {
            //System.out.println("best: " + best);
            //System.out.println(nneighborhood[i]);
            // se maxcount è maggiore di quelle che mancano
            //System.out.println("missing: " + (nneighborhood.length - i));
            if ((maxCount - counter) > (neighborhood.length - i)) {
                break;
            }

            // se è una nuova etichetta
            if (neighborhood[i] != last) {
                counter = 1;
                last = neighborhood[i];
            } else {
                counter++;
            }
            //System.out.println("counter: " + counter);
            // se sono uguali, estraggo
            if (counter == maxCount) {
                int coin = rnd.nextInt(2);
                //System.out.println("coin: " + coin);
                if (coin == 1) {
                    best = last;
                }
            }
            // se l'ultima eitchetta ha superato la ex migliore
            if (counter > maxCount) {
                maxCount = counter;
                best = last;
                //System.out.println("new best: " + best);
            }

            //System.out.println("maxcount: " + maxCount);
        }
        return best;
    }

    public static int[] compute(final WeightedGraph g, int[] startingLabels, double threshold, int runner) {

        //NewLPA.startingLabels = startingLabels;
        NewLPA.rnd = new Random(123);

        //int[] labels = new int[g.size];
        int[] labels = startingLabels;
        int[] newLabels = labels;
        int iter = 0;

        long time = System.nanoTime();
        CountDownLatch latch = null;

        NewLPA[] runners = new NewLPA[runner];

        for (int i = 0; i < runner; i++) {
            runners[i] = new NewLPA(g, latch, labels, i, runner);
        }

        ExecutorService ex = Executors.newFixedThreadPool(runner);

        do {
            iter++;
            labels = newLabels;
            newLabels = Arrays.copyOf(labels, labels.length);
            latch = new CountDownLatch(runner);

            //Label Propagation
            for (int i = 0; i < runner; i++) {
                runners[i].barrier = latch;
                runners[i].labels = newLabels;
                ex.submit(runners[i]);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                logger.debug(e);
            }

        } while (smoothEnd(labels, newLabels, iter, threshold));

        ex.shutdown();

        //System.out.println("n. iterations: "+iter);
        logger.info(((System.nanoTime() - time) / 1000000000d) + "\ts");
        return labels;
    }

    private static boolean smoothEnd(int[] labels, int[] newLabels, int iter, double threshold) {
        if (iter < 2) {
            return true;
        }

        int k = 3;

        if (iter > k) {
            int equality = 0;

            for (int i = 0; i < labels.length; i++) {
                if (labels[i] == newLabels[i]) {
                    equality++;
                }
            }
            double currentT = (equality / ((double) labels.length));

            return !(currentT >= threshold);
        }
        return !Arrays.equals(labels, newLabels);
    }
}

