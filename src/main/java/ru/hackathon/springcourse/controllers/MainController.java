package ru.hackathon.springcourse.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hackathon.springcourse.services.NodeApiClient;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/spring-api")
public class MainController {

    private final NodeApiClient nodeApiClient = new NodeApiClient();

    @PostMapping("/upload")
    public String uploadResume(@RequestParam("file") MultipartFile file,
                               @RequestParam("job") String job) throws IOException {

        // Сохраняем временно файл
        File tempFile = File.createTempFile("resume-", "-" + file.getOriginalFilename());
        file.transferTo(tempFile);

        // Вызываем Node.js API
        String response = nodeApiClient.analyzeResume(tempFile, job);

        // Удаляем временный файл
        tempFile.delete();

        return response;
    }
}
