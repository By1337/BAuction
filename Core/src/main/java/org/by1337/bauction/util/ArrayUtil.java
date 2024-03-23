package org.by1337.bauction.util;

import java.util.Iterator;
import java.util.List;

public class ArrayUtil {
    public static int[] listToIntArray(List<Integer> list) {
        int[] intArray = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            intArray[i] = list.get(i);
        }
        return intArray;
    }

    public static Iterator<Integer> intArrayIterator(int[] arr) {
        return new IntArrIterator(arr);
    }

    static class IntArrIterator implements Iterator<Integer> {
        final int[] source;
        int pos;

        IntArrIterator(int[] source) {
            this.source = source;
        }

        @Override
        public boolean hasNext() {
            return source.length > pos;
        }

        @Override
        public Integer next() {
            return source[pos++];
        }
    }
}
