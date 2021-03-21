package ru.sbt.test.logic;

import java.util.List;
import java.util.Map;

public interface SentenceDictionaryFinder {
    Map<String, Integer> findRelevantSentences(String line, List<String> sentences);
}
