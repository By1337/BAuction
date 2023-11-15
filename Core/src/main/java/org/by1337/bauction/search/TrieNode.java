package org.by1337.bauction.search;

import java.util.ArrayList;
import java.util.List;

public class TrieNode {
    char value;
    boolean isEnd;
    List<TrieNode> children;
    String storedValue;

    public TrieNode(char value) {
        this.value = value;
        this.isEnd = false;
        this.children = new ArrayList<>();
        this.storedValue = null;
    }
}
