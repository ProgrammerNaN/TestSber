package ru.sbt.test.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.sbt.test.logic.SentenceDictionaryFinder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class FinderController {

    private static final Logger logger = LoggerFactory.getLogger(FinderController.class);

    private final SentenceDictionaryFinder finder;

    private List<String> sentences;

    public FinderController(SentenceDictionaryFinder finder) {
        this.finder = finder;
    }

    @GetMapping("/")
    public String handlerIndex() {
        logger.info("get request!");
        return "index";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("dictionary") MultipartFile file, RedirectAttributes ra) throws IOException {
        logger.info("post file request!");
        if (file != null) {
            if (!file.isEmpty()) {
                String fileString = new String(file.getBytes(), "UTF-8");
                this.sentences = Arrays.asList(fileString.split("\r\n"));
                ra.addFlashAttribute("dictSuccess", "Словарь успешно загружен!");
            } else {
                logger.info("dictStatus - empty");
                ra.addFlashAttribute("dictStatus", "Загружаемый файл пустой! Загрузите файл, содержащий записи!");
            }
        } else {
            logger.info("dictStatus - null");
            ra.addFlashAttribute("dictStatus", "Ошибка при загрузке файла!");
        }
        return "redirect:/";
    }

    @PostMapping("/")
    public String handleLineRelevant(@RequestParam("line") String line, RedirectAttributes ra) {
        logger.info("post line request!");

        if (this.sentences != null && !this.sentences.isEmpty()) {
            Map<String, Integer> relevantSentences = finder.findRelevantSentences(line, this.sentences);
            List<Map.Entry<String, Integer>> entries = relevantSentences.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(Collectors.toList());
            ra.addFlashAttribute("relevantSentences", entries);
            ra.addFlashAttribute("userLine", line);
        } else {
            ra.addFlashAttribute("dictionaryError", "Словарь не загружен! Загрузите словарь для начала поиска!");
        }

        return "redirect:/";
    }

}
