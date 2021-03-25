package ru.sbt.test.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.sbt.test.logic.SentenceDictionaryFinder;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class FinderController {

    private static final Logger logger = LoggerFactory.getLogger(FinderController.class);

    private static final String UPLOAD_DIR = "/upload/";

    private final SentenceDictionaryFinder finder;

    private List<String> sentences;

    private HttpServletRequest request;

    private File inputFile;

    public FinderController(SentenceDictionaryFinder finder, HttpServletRequest request) {
        this.finder = finder;
        this.request = request;
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
                String uploadDirRealPath = request.getServletContext().getRealPath(UPLOAD_DIR);
                if (! new File(uploadDirRealPath).exists()) {
                    new File(uploadDirRealPath).mkdir();
                }
                this.inputFile = new File(uploadDirRealPath + file.getOriginalFilename());
                logger.info(uploadDirRealPath + file.getOriginalFilename());
                file.transferTo(this.inputFile);
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
            this.sentences = new ArrayList<>();

            BufferedReader bufferedReader = new BufferedReader(new FileReader(this.inputFile));
            String sentence = bufferedReader.readLine();
            while (sentence != null) {
                this.sentences.add(sentence);
                sentence = bufferedReader.readLine();
            }

            Map<String, Integer> relevantSentences = this.finder.findRelevantSentences(line, this.sentences);
            ra.addFlashAttribute("relevantSentences", relevantSentences);
            ra.addFlashAttribute("userLine", line);

            if (this.inputFile.delete()) {
                logger.info("Словарь удален!");
            } else {
                logger.info("Словарь не удален!");
            }
        } else {
            ra.addFlashAttribute("dictionaryError", "Загрузите словарь для выполнения операции!");
        }

        return "redirect:/";
    }

}
