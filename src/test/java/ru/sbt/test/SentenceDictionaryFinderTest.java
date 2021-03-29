package ru.sbt.test;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.sbt.test.logic.SentenceDictionaryFinder;
import ru.sbt.test.logic.SimpleSentenceDictionaryFinder;

import java.util.*;

public class SentenceDictionaryFinderTest {

    private SentenceDictionaryFinder finder;

    public SentenceDictionaryFinderTest() {

    }

    @Before
    public void init() {
        this.finder = new SimpleSentenceDictionaryFinder();
    }

    @Test
    public void testOneStringOneDictionary() {
        List<String> dictionary = Arrays.asList("аппетит приходит во время еды");
        String line = "не приходит";
        Map<String, Integer> expectation = new HashMap<>();
        expectation.put("аппетит приходит во время еды", 2);
        Map<String, Integer> result = this.finder.findRelevantSentences(line, dictionary);
        Assert.assertEquals(expectation, result);
    }

    @Test
    public void testOneStringManyDictionary() {
        List<String> dictionary = Arrays.asList("аппетит приходит во время еды", "беда не приходит одна");
        String line = "не приходит";
        Map<String, Integer> expectation = new HashMap<>();
        expectation.put("аппетит приходит во время еды", 2);
        expectation.put("беда не приходит одна", 5);
        Map<String, Integer> result = this.finder.findRelevantSentences(line, dictionary);
        Assert.assertEquals(expectation, result);
    }

    @Test
    public void testOneStringEmptyDictionary() {
        List<String> dictionary = new ArrayList<>();
        String line = "не приходит";
        Map<String, Integer> expectation = new HashMap<>();
        Map<String, Integer> result = this.finder.findRelevantSentences(line, dictionary);
        Assert.assertEquals(expectation, result);
    }

}
