package com.google.appinventor.client.utils;

import java.util.*;
import java.util.Map.Entry;
import java.util.List;

public class Trie {
  protected final Map<Character, Trie> children;
  protected String word;
  protected boolean isEndofWord = false;

  public Trie() {
    this(null);
  }

  private Trie(String word) {
    this.word = word;
    children = new HashMap<Character, Trie>();
  }

  protected void add(char c) {
    String val;
    if (this.word == null) {
      val = Character.toString(c);
    } else {
      val = this.word + c;
    }
    children.put(c, new Trie(val));
  }

  public void insert(String newWord) {
    Trie node = this;
    for (char c : newWord.toCharArray()) {
      if(!node.children.containsKey(c)) {
        node.add(c);
      }
      node = node.children.get(c);
    }
    node.isEndofWord = true;
  }

  public Collection<String> getAllWords(String prefix) {
    Trie node = this;
    for (char c: prefix.toCharArray()) {
      if(!node.children.containsKey(c)) {
        return Collections.emptyList();
      }
      node = node.children.get(c);
    }
    return node.allPrefixes();
  }

  protected Collection<String> allPrefixes() {
    List<String> results = new ArrayList<String>();
    if (this.isEndofWord) {
      results.add(this.word);
    }
    for (Entry<Character, Trie> entry: children.entrySet()) {
      Trie child = entry.getValue();
      Collection<String> childPrefixes = child.allPrefixes();
      results.addAll(childPrefixes);
    }
    return results;
  }
}

