package ru.hackathon.springcourse.services;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

public class NodeApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public String analyzeResume(File resumeFile, String job) {
        String url = "http://localhost:3001/api/analyze";

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("resume", new FileSystemResource(resumeFile));
        body.add("job", job);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Ошибка запроса: " + response.getStatusCode());
        }
    }
}
