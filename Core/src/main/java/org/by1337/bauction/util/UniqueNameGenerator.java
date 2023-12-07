package org.by1337.bauction.util;

import org.jetbrains.annotations.TestOnly;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UniqueNameGenerator {
    private char[] symbols;
    private long currentPosition = 0;
    private long maxPos;
    private final Lock lock;
    private final int seed;

    public UniqueNameGenerator(int seed) {
        this.seed = seed;
        lock = new ReentrantLock();
        symbols = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
        shuffleArray(symbols, seed);
        maxPos = (long) Math.pow(symbols.length, 36);
    }

    public UniqueName getNextCombination() {
        lock.lock();
        try {
            if (currentPosition >= maxPos) { // max limit 0x7fffffffffffffffL
                throw new IllegalStateException("all combinations are exhausted");
            }
            StringBuilder combination = new StringBuilder();
            long position = currentPosition;
            for (int i = 0; i < 36; i++) {
                int charIndex = (int) ((position + i) % symbols.length);
                combination.append(symbols[charIndex]);
                position /= symbols.length;
            }
            CUniqueName un = new CUniqueName(combination.toString(), seed, currentPosition);
            currentPosition++;
            return un;
        } finally {
            lock.unlock();
        }
    }

    public static UniqueName fromSeedAndPos(int seed, long pos) {
        char[] symbols = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
        shuffleArray(symbols, seed);
        StringBuilder combination = new StringBuilder();
        long pos1 = pos;
        for (int i = 0; i < 36; i++) {
            int charIndex = (int) ((pos1 + i) % symbols.length);
            combination.append(symbols[charIndex]);
            pos1 /= symbols.length;
        }
        return new CUniqueName(combination.toString(), seed, pos);
    }

    private static void shuffleArray(char[] array, int seed) {
        Random rand = new Random(seed);
        for (int i = array.length - 1; i > 0; i--) {
            int randomIndexToSwap = rand.nextInt(i + 1);
            char temp = array[randomIndexToSwap];
            array[randomIndexToSwap] = array[i];
            array[i] = temp;
        }
    }

    @TestOnly
    public long getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getSeed() {
        return seed;
    }
}
