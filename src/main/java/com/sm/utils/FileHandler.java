package com.sm.utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileHandler {
	
    public static void sequentialFolderCopy(String sourceFolderLoc, String subFolderName) {
        Path sourceDir = Paths.get(sourceFolderLoc);

        try {
            // 1. Find the next available sequential subfolder name
            Path targetSubfolder = getNextSequentialFolder(sourceDir, subFolderName);
            
            // 2. Create the target subfolder
            Files.createDirectories(targetSubfolder);
            System.out.println("Created destination folder: " + targetSubfolder.getFileName());

            // 3. Copy files keeping their original names
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
                for (Path file : stream) {
                    // Only copy files, ignore directories to prevent infinite loops
                    if (Files.isRegularFile(file)) {
                        String originalName = file.getFileName().toString();
                        Path targetFile = targetSubfolder.resolve(originalName);
                        
                        Files.move(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Copied: " + originalName);
                    }
                }
            }
            System.out.println("All files copied successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to scan existing folders and generate the next sequence number
    private static Path getNextSequentialFolder(Path baseDir, String prefix) {
        int sequence = 1;
        Path candidatePath;
        
        // Loop increments the number until it finds a folder name that doesn't exist yet
        do {
            candidatePath = baseDir.resolve(prefix + "_" + sequence);
            sequence++;
        } while (Files.exists(candidatePath));
        
        return candidatePath;
    }
}
