package org.by1337.bauction.search;

import java.util.ArrayList;
import java.util.List;

public class Trie {
    private TrieNode root;

    public Trie() {
        root = new TrieNode('\0');
    }

    public void insert(String key, String value) {
        TrieNode node = root;
        for (char ch : key.toCharArray()) {
            TrieNode child = getChildWithChar(node, ch);
            if (child == null) {
                child = new TrieNode(ch);
                node.children.add(child);
            }
            node = child;
        }
        node.isEnd = true;
        node.storedValue = value;
    }

    public List<String> getAllWithPrefix(String prefix) {
        List<String> results = new ArrayList<>();
        TrieNode node = getNodeByPrefix(prefix);
        getAllWithPrefixHelper(node, results);
        return results;
    }

    public List<String> getAllKeysWithPrefix(String prefix) {
        List<String> keys = new ArrayList<>();
        TrieNode node = getNodeByPrefix(prefix);
        getAllKeysWithPrefixHelper(node, prefix, keys);
        return keys;
    }

    private TrieNode getNodeByPrefix(String prefix) {
        TrieNode node = root;
        for (char ch : prefix.toCharArray()) {
            TrieNode child = getChildWithChar(node, ch);
            if (child == null) {
                return null;
            }
            node = child;
        }
        return node;
    }

    private TrieNode getChildWithChar(TrieNode node, char ch) {
        for (TrieNode child : node.children) {
            if (child.value == ch) {
                return child;
            }
        }
        return null;
    }

    private void getAllWithPrefixHelper(TrieNode node, List<String> results) {
        if (node == null) {
            return;
        }

        if (node.isEnd) {
            results.add(node.storedValue);
        }

        for (TrieNode child : node.children) {
            getAllWithPrefixHelper(child, results);
        }
    }

    private void getAllKeysWithPrefixHelper(TrieNode node, String currentPrefix, List<String> keys) {
        if (node == null) {
            return;
        }

        if (node.isEnd) {
            keys.add(currentPrefix);
        }

        for (TrieNode child : node.children) {
            if (child != null) {
                getAllKeysWithPrefixHelper(child, currentPrefix + child.value, keys);
            }
        }
    }
}
