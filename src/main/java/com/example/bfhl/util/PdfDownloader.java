package com.example.bfhl.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class PdfDownloader {

    private static final Logger log = LoggerFactory.getLogger(PdfDownloader.class);

    public static String downloadAndExtractText(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            if (conn.getResponseCode() != 200) {
                log.warn("Failed PDF download: {}", fileUrl);
                return null;
            }

            try (InputStream in = conn.getInputStream(); PDDocument doc = PDDocument.load(in)) {
                return new PDFTextStripper().getText(doc);
            }

        } catch (Exception e) {
            log.error("PDF download error: {}", e.getMessage());
            return null;
        }
    }

    public static String readLocalQuery(String resourcePath) {
        try (InputStream is = new ClassPathResource(resourcePath).getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null)
                sb.append(line).append("\n");

            return sb.toString().trim();

        } catch (IOException e) {
            log.error("Cannot read local query: {}", resourcePath);
            return null;
        }
    }
}
