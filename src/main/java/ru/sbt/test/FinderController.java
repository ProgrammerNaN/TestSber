package ru.sbt.test;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Controller
public class FinderController {

    private static final Logger logger = LoggerFactory.getLogger(FinderController.class);

    private List<String> sentences;

    @GetMapping("/")
    public String greeting() {
        logger.info("get request!");
        return "index";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("dictionary") MultipartFile file, RedirectAttributes ra) throws IOException {
        logger.info("post file request!");
        if (file != null) {
            if (!file.isEmpty()) {
                String fileString = new String(file.getBytes(), "UTF-8");
                this.sentences = Arrays.asList(fileString.split("\n"));
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
    public String handleLineRelevant(@RequestParam("line") String line, Model model) {
        logger.info("post line request!");
        return "redirect:/";
    }

}
