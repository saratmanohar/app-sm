package com.sm.utils;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class MP3GenreUpdater {

    public static void main(String[] args) {
        // Change these to your actual directory path and desired genre
        String targetDirectory = "C:\\Users\\Sarath\\Downloads\\Music\\CAR_USB\\6_Others"; 
        //F:\Music\5_Global
        //C:\Users\Sarath\Downloads\Music\CAR_USB\5_Global
        String newGenre = "Tamil & Others"; 

        Path startPath = Paths.get(targetDirectory);

        if (!Files.exists(startPath) || !Files.isDirectory(startPath)) {
            System.err.println("The provided path does not exist or is not a directory.");
            return;
        }

        System.out.println("Starting recursive search in: " + targetDirectory);
        updateGenresRecursively(startPath, newGenre);
        System.out.println("Processing complete!");
    }

    /**
     * Recursively traverses folders to find MP3 files and updates their Genre.
     */
    public static void updateGenresRecursively(Path rootPath, String genre) {
        // Files.walk automatically traverses subdirectories recursively
        try (Stream<Path> stream = Files.walk(rootPath)) {
            stream.filter(Files::isRegularFile)
                  .filter(path -> path.toString().toLowerCase().endsWith(".mp3"))
                  .forEach(mp3Path -> updateMp3Genre(mp3Path, genre));
        } catch (IOException e) {
            System.err.println("Error reading the directory structure: " + e.getMessage());
        }
    }

    /**
     * Reads the ID3 tags of an individual MP3 file and commits the new genre.
     */
    private static void updateMp3Genre(Path file, String genre) {
        try {
            // Read audio file metadata
            AudioFile audioFile = AudioFileIO.read(file.toFile());
            Tag tag = audioFile.getTag();

            // If the MP3 file lacks a tag structure entirely, create a default one
            if (tag == null) {
                tag = audioFile.createDefaultTag();
                audioFile.setTag(tag);
            }

            // Set the genre property
            tag.setField(FieldKey.GENRE, genre);

            // Write and commit changes to the physical file
            audioFile.commit();
            System.out.println("Updated: " + file.getFileName() + " -> " + genre);

        } catch (Exception e) {
            System.err.println("Failed to update tags for " + file.getFileName() + ": " + e.getMessage());
        }
    }
}
