package com.sm.utils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public class RemoveEmptyLines {

    public static void removeBlankLines(String filePath) {
        Path originalPath = Paths.get(filePath);
        Path tempPath = Paths.get(filePath + ".tmp");

        // Open the original file as a stream of lines
        try (Stream<String> lines = Files.lines(originalPath)) {
            
            // Filter out empty or whitespace-only lines and write to temp file
            Files.write(tempPath, (Iterable<String>) lines
                    .filter(line -> !line.isBlank())::iterator);
            
            // Atomically replace the original file with the cleaned temporary file
            Files.move(tempPath, originalPath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("Empty lines removed successfully.");

        } catch (IOException e) {
            System.err.println("An error occurred processing the file: " + e.getMessage());
            // Clean up the temp file if it was created and an error occurred
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ex) {
                // Ignore cleanup exceptions
            }
        }
    }

    public static void main(String[] args) {
        // Replace with your actual file path
        removeBlankLines("file.txt"); 
    }
}
