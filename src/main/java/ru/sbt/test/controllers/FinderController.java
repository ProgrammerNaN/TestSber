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
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class FinderController {

    private static final Logger logger = LoggerFactory.getLogger(FinderController.class);

    private final SentenceDictionaryFinder finder;

    private List<String> sentences;

    private File inputFile;

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
        //Происходит загрузка файла на жесткий диск сервера для экономии опреативной памяти
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
    public String handleLineRelevant(@RequestParam("line") String line, RedirectAttributes ra) throws IOException {
        logger.info("post line request!");

        if (this.inputFile != null && this.inputFile.exists()) {
            //Для временного хранения строк был выбран ArrayList,
            //т.к. в основном происходит вставка в конец списка и перебор его элементов
            this.sentences = new ArrayList<>();

            //Для хранения строки с ее релеваантностью была выбрана структура данных HashMap
            //Она позволяет хранить данные ключ-значение и обеспечивает уникальность ключей
            Map<String, Integer> relevantSentences = new HashMap<>();

            //Загрузка строк в оперативную память из файла осуществляется по частям
            //Это сделано для того, чтобы экономить оперативную память
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(this.inputFile));) {
                String sentence = bufferedReader.readLine();
                int counter = 0;
                while (sentence != null) {
                    this.sentences.add(sentence);
                    sentence = bufferedReader.readLine();
                    if (counter > 512) {
                        relevantSentences.putAll(this.finder.findRelevantSentences(line, this.sentences));
                        counter = 0;
                        this.sentences.clear();
                    } else {
                        counter++;
                    }
                }
            }
            //Сортировка элементов Map по значению
            List<Map.Entry<String, Integer>> entries = relevantSentences.entrySet().stream()
                                                            .sorted(Map.Entry.<String, Integer> comparingByValue().reversed())
                                                            .collect(Collectors.toList());
            ra.addFlashAttribute("relevantSentences", entries);
            ra.addFlashAttribute("userLine", line);

            if (this.inputFile.delete()) {
                logger.info("Словарь удален!");
            }
        } else {
            ra.addFlashAttribute("dictionaryError", "Словарь не загружен! Загрузите словарь для начала поиска!");
        }

        return "redirect:/";
    }

}
