package ru.sbt.test.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.sbt.test.exceptions.EmptyUploadFileException;
import ru.sbt.test.exceptions.NullInputSentenceException;
import ru.sbt.test.exceptions.NullUploadFileException;
import ru.sbt.test.exceptions.UnloadDictionaryFileException;
import ru.sbt.test.logic.SentenceDictionaryFinder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class FinderController {

    private static final Logger logger = LoggerFactory.getLogger(FinderController.class);

    private static final String UPLOAD_DIR = "/upload/";

    private static final int SENTENCES_BATCH = 512;

    private final SentenceDictionaryFinder finder;

    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("statuses");

    public FinderController(SentenceDictionaryFinder finder) {
        this.finder = finder;
    }

    @GetMapping("/")
    public String handlerIndex() {
        logger.info("get request!");
        return "index";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("dictionary") MultipartFile file, RedirectAttributes ra, HttpServletRequest request) throws IOException {
        logger.info("post file request!");
        HttpSession session = request.getSession();
        //Происходит загрузка файла на жесткий диск сервера для экономии опреативной памяти
        if (file != null) {
            if (!file.isEmpty()) {
                String uploadDirRealPath = request.getServletContext().getRealPath(UPLOAD_DIR);
                if (!new File(uploadDirRealPath).exists()) {
                    new File(uploadDirRealPath).mkdir();
                }
                File inputFile = new File(uploadDirRealPath + file.getOriginalFilename());
                file.transferTo(inputFile);
                session.setAttribute(resourceBundle.getString("session.file"), uploadDirRealPath + file.getOriginalFilename());
                ra.addFlashAttribute(resourceBundle.getString("dictionary.success"), "Словарь успешно загружен!");
            } else {
                throw new EmptyUploadFileException();
            }
        } else {
            throw new NullUploadFileException();
        }
        return "redirect:/";
    }

    @PostMapping("/")
    public String handleLineRelevant(@RequestParam("line") String line, RedirectAttributes ra, HttpServletRequest request) throws IOException {
        logger.info("post line request!");

        HttpSession session = request.getSession();

        File inputFile = new File((String)session.getAttribute(resourceBundle.getString("session.file")));

        if (inputFile != null && inputFile.exists()) {
            //Для временного хранения строк был выбран ArrayList,
            //т.к. в основном происходит вставка в конец списка и перебор его элементов
            List<String> sentences = new ArrayList<>();

            //Для хранения строки с ее релеваантностью была выбрана структура данных HashMap
            //Она позволяет хранить данные ключ-значение и обеспечивает уникальность ключей
            Map<String, Integer> relevantSentences = new HashMap<>();

            //Загрузка строк в оперативную память из файла осуществляется по частям
            //Это сделано для того, чтобы экономить оперативную память
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));) {
                String sentence = bufferedReader.readLine();
                int counter = 0;
                while (sentence != null) {
                    sentences.add(sentence);
                    sentence = bufferedReader.readLine();
                    if (counter > SENTENCES_BATCH) {
                        relevantSentences.putAll(this.finder.findRelevantSentences(line, sentences));
                        counter = 0;
                        sentences.clear();
                    } else {
                        counter++;
                    }
                }
            }
            if (relevantSentences.isEmpty()) {
                relevantSentences.putAll(this.finder.findRelevantSentences(line, sentences));
            }
            //Сортировка элементов Map по значению
            List<Map.Entry<String, Integer>> entries = relevantSentences.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(Collectors.toList());
            ra.addFlashAttribute(resourceBundle.getString("sentences.relevant"), entries);
            ra.addFlashAttribute(resourceBundle.getString("sentences.user"), line);

            if (inputFile.delete()) {
                logger.info("Словарь удален!");
            }
        } else {
            throw new UnloadDictionaryFileException();
        }

        return "redirect:/";
    }

    @ExceptionHandler(EmptyUploadFileException.class)
    public String handleEmptyUploadFileException(EmptyUploadFileException e, RedirectAttributes ra) {
        ra.addFlashAttribute(resourceBundle.getString("dictionary.status"), "Загружаемый файл пустой! Загрузите файл, содержащий записи!");
        return "redirect:/";
    }

    @ExceptionHandler(NullUploadFileException.class)
    public String handlerNullUploadFileException(NullUploadFileException e, RedirectAttributes ra) {
        ra.addFlashAttribute(resourceBundle.getString("dictionary.status"), "Ошибка при загрузке файла!");
        return "redirect:/";
    }

    @ExceptionHandler(UnloadDictionaryFileException.class)
    public String handlerUnloadDictionaryFileException(UnloadDictionaryFileException e, RedirectAttributes ra) {
        ra.addFlashAttribute(resourceBundle.getString("dictionary.error"), "Загрузите словарь для выполнения операции!");
        return "redirect:/";
    }

    @ExceptionHandler(NullInputSentenceException.class)
    public String handlerNullInputSentenceException(NullInputSentenceException e, RedirectAttributes ra) {
        ra.addFlashAttribute(resourceBundle.getString("sentences.user.null"), "Введите корректную строку для обработки!");
        return "redirect:/";
    }

}
