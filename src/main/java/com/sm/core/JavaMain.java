package com.sm.core;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.io.IOException;
import java.nio.file.Files;


public class JavaMain {
	public static int count = 0;
	public static void main(String[] args) {
		
		String folderPath[] = {
				"F:\\Music"
				
		};
		//findTotalMp3("C:\\Users\\Sarath\\Downloads\\Music\\CAR_USB");
		
		for(String folder:folderPath)
			process(folder);
		System.out.println("\nProcess finished. Successfully renamed " + count + " files.");
	}
	
	public static void findTotalMp3(String folderPathLoc) {
        Path folderPath = Paths.get(folderPathLoc); // Replace with your path

        try (Stream<Path> stream = Files.walk(folderPath)) {
            long totalMp3Files = stream
                .filter(Files::isRegularFile) // Ensure it's a file, not a directory
                .filter(p -> p.toString().toLowerCase().endsWith(".mp3")) // Filter by extension
                .count(); // Terminal operation to get total count

            System.out.println("Total MP3 files: " + totalMp3Files);
        } catch (IOException e) {
            System.err.println("Error reading the folder: " + e.getMessage());
        }
    }
	
    public static void process(String folderPath) {
        
        File folder = new File(folderPath);
        
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Error: The specified path is not a valid directory.");
            return;
        }

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("No files found in the folder.");
            return;
        }

        int successCount = 0;

        for (File file : files) {
        	if(file.isDirectory())
        		process(file.getAbsolutePath());
            if (file.isFile() && file.getName().toLowerCase().endsWith(".mp3")) {
                String originalName = file.getName();
                
                // Regex matches optional space, followed by '[', any characters inside, ']', right before '.mp3'
                String newName = originalName.replaceAll("\\s*\\[[^\\]]+\\](?=\\.mp3$)", "");

                // Only rename if the pattern matched and changed the filename
                if (!originalName.equals(newName)) {
                    Path targetPath = Paths.get(folderPath, newName);
                    
                    if (file.renameTo(targetPath.toFile())) {
                        System.out.println("Renamed: \"" + originalName + "\" -> \"" + newName + "\"");
                        successCount++;
                        count++;
                    } else {
                        System.out.println("Failed to rename: " + originalName);
                    }
                }
            }
        }
        System.out.println("\nProcess finished. Successfully renamed " + successCount + " files.");
    }
}
