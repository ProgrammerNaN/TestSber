package ru.sbt.test.logic;

import org.springframework.stereotype.Component;
import ru.sbt.test.exceptions.NullInputSentenceException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SimpleSentenceDictionaryFinder implements SentenceDictionaryFinder {
    private static final int SAME_WORD_SCORES = 2;
    private static final int SAME_WORDS_ORDER = 1;

    /**
     * Рассчет релеванстности для всех строк словаря
     * @param line исходная строка
     * @param sentences словарь строк
     * @return карта, в которой ключ - строка из словаря, значение - ее релеванстность
     */
    @Override
    public Map<String, Integer> findRelevantSentences(String line, List<String> sentences) throws NullInputSentenceException {

        if (line == null) {
            throw new NullInputSentenceException();
        }

        Map<String, Integer> relevantSentences = new HashMap<>();

        for (String sentence: sentences) {
            Integer relevance = calculateSentenceRelevance(line, sentence);
            if (relevance > 0) {
                relevantSentences.put(sentence, relevance);
            }
        }

        return relevantSentences;
    }

    /**
     * Вычисление релевантности одной строки
     * @param line входная строка
     * @param sentence строка из словаря
     * @return релевантность строки из словаря
     */
    private Integer calculateSentenceRelevance(String line, String sentence) {
        Integer relevance = 0;

        List<String> lineWords = Arrays.asList(line.toLowerCase().split(" "));
        List<String> sentenceWords = Arrays.asList(sentence.toLowerCase().split(" "));

        //Проверяется каждое слово входной строки - есть ли оно в строке из словаря
        //Если есть то к релеванстности прибавляется 2 очка
        //И дальше проверяется следующее слово исходной строки и строки из словаря
        //Если они совпадают, то к релевантности прибавляется еще 1 балл
        for (int i = 0; i < lineWords.size(); i++) {
            if (sentenceWords.contains(lineWords.get(i))) {
                relevance += SAME_WORD_SCORES;
                int wordIndexInSentence = sentenceWords.indexOf(lineWords.get(i));
                if (lineWords.size() - 1 > i) {
                    String lineNextWord = lineWords.get(i + 1);
                    String sentenceNextWord = sentenceWords.get(wordIndexInSentence + 1);
                    if (lineNextWord.equals(sentenceNextWord)) {
                        relevance += SAME_WORDS_ORDER;
                    }
                }
            }
        }

        return relevance;
    }
}
