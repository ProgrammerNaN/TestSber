package ru.sbt.test.logic;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SimpleSentenceDictionaryFinder implements SentenceDictionaryFinder {
    private static final int SAME_WORD_SCORES = 2;
    private static final int SAME_WORDS_ORDER = 1;

    @Override
    public Map<String, Integer> findRelevantSentences(String line, List<String> sentences) {
        Map<String, Integer> relevantSentences = new HashMap<>();

        for (String sentence: sentences) {
            Integer relevance = calculateSentenceRelevance(line, sentence);
            if (relevance > 0) {
                relevantSentences.put(sentence, relevance);
            }
        }

        return relevantSentences;
    }

    private Integer calculateSentenceRelevance(String line, String sentence) {
        Integer relevance = 0;

        List<String> lineWords = Arrays.asList(line.split(" "));
        List<String> sentenceWords = Arrays.asList(sentence.split(" "));

        for (int i = 0; i < lineWords.size(); i++) {
            if (sentenceWords.contains(lineWords.get(i))) {
                relevance += SAME_WORD_SCORES;
                int wordIndexInSentence = sentenceWords.indexOf(lineWords.get(i));
                if (lineWords.size() - 1 > i) {
                    if (lineWords.get(i + 1).equals(sentenceWords.get(wordIndexInSentence + 1))) {
                        relevance += SAME_WORDS_ORDER;
                    }
                }
            }
        }

        return relevance;
    }
}
