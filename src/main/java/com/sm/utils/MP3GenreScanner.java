package com.sm.utils;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MP3GenreScanner {

    public static void main(String[] args) {
        // Change this to your actual music directory path
        String targetDirectory = "C:\\\\Users\\\\Sarath\\\\Downloads\\\\Music\\\\CAR_USB\\\\3_Malayalam"; 

        Path startPath = Paths.get(targetDirectory);

        if (!Files.exists(startPath) || !Files.isDirectory(startPath)) {
            System.err.println("The provided path does not exist or is not a directory.");
            return;
        }

        System.out.println("Scanning directory and counting files per genre: " + targetDirectory);
        Map<String, Long> genreCounts = countGenresRecursively(startPath);

        System.out.println("\n=== Genre Distribution ===");
        if (genreCounts.isEmpty()) {
            System.out.println("No MP3 files with genre tags found.");
        } else {
            // Print the results nicely padded
            genreCounts.forEach((genre, count) -> 
                System.out.printf("%-25s : %d file(s)%n", genre, count)
            );
        }
    }

    /**
     * Traverses folders and returns a Map containing genre names and their file counts.
     */
    public static Map<String, Long> countGenresRecursively(Path rootPath) {
        try (Stream<Path> stream = Files.walk(rootPath)) {
            
            return stream.filter(Files::isRegularFile)
                  .filter(path -> path.toString().toLowerCase().endsWith(".mp3"))
                  .map(MP3GenreScanner::getMp3Genre)
                  .filter(genre -> genre != null && !genre.trim().isEmpty())
                  // Standardizes formatting by trimming spaces
                  .map(String::trim) 
                  .collect(Collectors.groupingBy(
                      genre -> genre,
                      // TreeMap keeps the final map keys sorted alphabetically
                      () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER), 
                      Collectors.counting()
                  ));

        } catch (IOException e) {
            System.err.println("Error reading the directory structure: " + e.getMessage());
            return new TreeMap<>();
        }
    }

    /**
     * Reads the ID3 tags of an individual MP3 file and returns its genre string.
     */
    private static String getMp3Genre(Path file) {
        try {
            AudioFile audioFile = AudioFileIO.read(file.toFile());
            Tag tag = audioFile.getTag();

            if (tag != null) {
                String genre = tag.getFirst(FieldKey.GENRE);
                return (genre == null) ? "Unknown" : genre;
            }
        } catch (Exception e) {
            // Silently fall back to Unknown for unreadable files to prevent stream crashes
            return "Unreadable/Error";
        }
        return "Unknown";
    }
}
