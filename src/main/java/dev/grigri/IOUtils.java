package dev.grigri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IOUtils {
    public static String[] readLines(InputStream inputStream, String charset) {
        try (var reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            return reader.lines().toList().toArray(String[]::new);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
