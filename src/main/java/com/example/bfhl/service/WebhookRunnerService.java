package com.example.bfhl.service;

import com.example.bfhl.model.GenerateWebhookRequest;
import com.example.bfhl.model.GenerateWebhookResponse;
import com.example.bfhl.util.PdfDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WebhookRunnerService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(WebhookRunnerService.class);

    private final RestTemplate restTemplate;

    private static final String GENERATE_URL =
            "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    private static final String TEST_URL =
            "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

    public WebhookRunnerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        GenerateWebhookRequest request =
                new GenerateWebhookRequest("John Doe", "REG12347", "john@example.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<GenerateWebhookResponse> response =
                restTemplate.postForEntity(
                        GENERATE_URL,
                        new HttpEntity<>(request, headers),
                        GenerateWebhookResponse.class
                );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            log.error("Webhook generation failed");
            return;
        }

        GenerateWebhookResponse body = response.getBody();
        String webhook = body.getWebhook();
        String token = body.getAccessToken();

        int lastTwoDigits = extractDigits(request.regNo());
        boolean isOdd = (lastTwoDigits % 2 == 1);

        String localQueryPath = isOdd ?
                "final-queries/q1.sql" :
                "final-queries/q2.sql";

        String finalQuery = PdfDownloader.readLocalQuery(localQueryPath);

        if (finalQuery == null || finalQuery.isBlank()) {
            finalQuery = "-- PLACE YOUR FINAL SQL QUERY HERE --";
        }

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_JSON);
        postHeaders.set("Authorization", token);

        Map<String, String> payload = Map.of("finalQuery", finalQuery);

        HttpEntity<Map<String, String>> finalReq =
                new HttpEntity<>(payload, postHeaders);

        String urlToSubmit = (webhook != null && !webhook.isBlank()) ? webhook : TEST_URL;

        try {
            ResponseEntity<String> submitResp =
                    restTemplate.postForEntity(urlToSubmit, finalReq, String.class);

            log.info("Submission result: {}", submitResp.getBody());

        } catch (Exception e) {
            log.error("Submission failed: {}", e.getMessage());
        }
    }

    private int extractDigits(String regNo) {
        String digits = regNo.replaceAll("\\D", "");

        if (digits.length() < 2) return 0;
        return Integer.parseInt(digits.substring(digits.length() - 2));
    }
}
