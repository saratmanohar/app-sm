package com.sm.utils;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.ID3v23Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MP3TitleUpdater {
    public static void main(String[] args) {
        String targetDirectory = args.length > 0 ? args[0] : "C:\\Users\\Sarath\\Downloads\\Music\\CAR_USB\\4_Dhruv Selections";
        Path startPath = Paths.get(targetDirectory);

        if (!Files.exists(startPath) || !Files.isDirectory(startPath)) {
            System.err.println("Error: The provided path does not exist or is not a directory: " + targetDirectory);
            return;
        }

        System.out.println("Starting recursive search in: " + targetDirectory);
        int updatedCount = updateTitlesRecursively(startPath);
        System.out.println("Processing complete! Successfully updated properties for " + updatedCount + " files.");
    }

    /**
     * Safely collects files first to avoid stream termination on permission errors.
     */
    public static int updateTitlesRecursively(Path rootPath) {
        AtomicInteger successCount = new AtomicInteger(0);
        List<Path> mp3Files;

        try (Stream<Path> stream = Files.find(rootPath, Integer.MAX_VALUE, (path, attr) -> {
            if (!attr.isRegularFile()) return false;
            String name = path.getFileName().toString().toLowerCase();
            return name.endsWith(".mp3");
        })) {
            mp3Files = stream.collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Critical error mapping directory structure: " + e.getMessage());
            return 0;
        }

        for (Path mp3Path : mp3Files) {
            try {
                if (updateMp3Metadata(mp3Path)) {
                    successCount.incrementAndGet();
                }
            } catch (Exception e) {
                System.err.println("Skipped file due to unexpected error: " + mp3Path.getFileName() + " | " + e.getMessage());
            }
        }

        return successCount.get();
    }

    /**
     * Extracts Album/Genre, deep-erases all corrupt/legacy tag layers, 
     * and writes a fresh ID3v2.3 tag with the updated Title.
     */
    private static boolean updateMp3Metadata(Path file) {
        try {
            java.io.File ioFile = file.toFile();
            if (!ioFile.canWrite()) {
                ioFile.setWritable(true);
            }

            // 1. Initial read to grab existing data
            MP3File mp3File = (MP3File) AudioFileIO.read(ioFile);
            Tag oldTag = mp3File.getTag();

            String existingAlbum = "";
            String existingGenre = "";

            // Safely hold onto Album and Genre if they exist
            if (oldTag != null) {
                existingAlbum = oldTag.getFirst(FieldKey.ALBUM);
                existingGenre = oldTag.getFirst(FieldKey.GENRE);
            }

            // 2. ABSOLUTE ERASE: Completely strip every old tag block out of the file
            mp3File.setID3v1Tag(null);
            mp3File.setID3v2Tag(null);
            AudioFileIO.delete(mp3File); 

            // 3. RE-READ THE RAW FILE: Fetch the completely blank canvas
            mp3File = (MP3File) AudioFileIO.read(ioFile);

            // 4. CREATE CLEAN ID3v2.3 TAG: Highly compatible with Windows & Car players
            ID3v23Tag freshTag = new ID3v23Tag();
            
            // 5. Apply the filename as the Title
            String fullFileName = file.getFileName().toString();
            String newTitle = fullFileName.replaceAll("(?i)\\.mp3$", "");
            freshTag.setField(FieldKey.TITLE, newTitle);

            // 6. Restore the preserved fields if they weren't blank
            if (existingAlbum != null && !existingAlbum.trim().isEmpty()) {
                freshTag.setField(FieldKey.ALBUM, existingAlbum.trim());
            }
            if (existingGenre != null && !existingGenre.trim().isEmpty()) {
                freshTag.setField(FieldKey.GENRE, existingGenre.trim());
            }

            // 7. Attach and lock everything down to disk
            mp3File.setTag(freshTag);
            AudioFileIO.write(mp3File);

            System.out.println("Cleaned & Updated: " + newTitle + " [Preserved Album: " + existingAlbum + " | Genre: " + existingGenre + "]");
            return true;

        } catch (org.jaudiotagger.audio.exceptions.CannotWriteException e) {
            System.err.println("File write locked by an active process: " + file.getFileName());
        } catch (Exception e) {
            System.err.println("Failed to hard update: " + file.getFileName() + " | Error: " + e.getMessage());
        }
        return false;
    }
}
