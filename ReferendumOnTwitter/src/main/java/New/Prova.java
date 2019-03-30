/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package New;

import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author cecin
 */
public class Prova {
    
    private static Random rnd;

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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        rnd = new Random(System.currentTimeMillis());
        int[] p = {1,1,1,2,2,2,1,0,0,0};
        System.out.println(bestLabel(p));
    }

}
