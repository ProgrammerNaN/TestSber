package ru.sbt.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FinderController {

    private static final Logger logger = LoggerFactory.getLogger(FinderController.class);

    @GetMapping("/")
    public String greeting() {
        logger.info("get request!");
        return "index";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("dictionary") MultipartFile file) {
        logger.info("post file request!");
        return "redirect:/";
    }

    @PostMapping("/")
    public String handleLineRelevant(@RequestParam("line") String line) {
        logger.info("post line request!");
        return "redirect:/";
    }

}
