
package com.ark.bcp.domain.util;

import java.util.Random;

/**
 */
public class Randoms {
    public static int getRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max) % (max - min + 1) + min;
    }
}
