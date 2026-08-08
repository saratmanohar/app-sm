package com.sm.pages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sm.core.WebDriverFactory;
import com.sm.utils.FileHandler;
import com.sm.utils.FuzzySentenceMatcher;


public class DownloadMP3 {

	private WebDriver driver = WebDriverFactory.getDriver();
	WebDriverWait wait;

	static int failedDownloadFiles;
	
	String currentPlayListURL;
    static boolean lastDownloadStatus;
    

    public void downloadMP3() throws Exception {
    	
    	Map<String,String> playLists = new LinkedHashMap<String,String>();
    	playLists.put("MAL Top 145", "file.txt");
    	
//    	playLists.put("Name", "MusicPlayList");
//    	playLists.put("Name", "MusicPlayList");
    	
    	

    	wait = new WebDriverWait(driver, Duration.ofSeconds(60));
    	for (Map.Entry<String, String> entry : playLists.entrySet()) {
    		failedDownloadFiles = 0;
    	    downloadMP3List(entry.getKey(),entry.getValue());
    	}
    	System.out.println("ALL DONE");
    	WebDriverFactory.quitDriver();
    }
    

    Map<String,String> captureListYTMusicURLs(String listUrl){
    	
    	Map<String,String> allItems = new LinkedHashMap<String,String>();
    	driver.get(listUrl);
    	hardWait(10000);
    	
    	String xpath = "//div[@class='style-scope ytmusic-playlist-shelf-renderer']//div[@class='title-column style-scope ytmusic-responsive-list-item-renderer']//a[@class='yt-simple-endpoint style-scope yt-formatted-string']";
    	List<WebElement> list = driver.findElements(By.xpath(xpath));
    	
    	for(int i=0;i<list.size();i++) {
    		String item = list.get(i).getAttribute("href").split("&list=")[0];
    		System.out.println(list.get(i).getText() + ": "+ item);
    		allItems.put(list.get(i).getText(),item);
    		if(i==list.size()-2) {
    			JavascriptExecutor js = (JavascriptExecutor) driver;
            	js.executeScript("arguments[0].scrollIntoView(true);", list.get(i));
            	hardWait(10000);
    			list = driver.findElements(By.xpath(xpath));
    		}
    	}
    	return allItems;
    }
    
    Map<String,String> captureListYTURLs(){
    	
    	Map<String,String> allItems = new LinkedHashMap<String,String>();
    	driver.get(currentPlayListURL);
    	hardWait(10000);
    	List<WebElement> list = driver.findElements(By.xpath("//*[@class='playlist-items style-scope ytd-playlist-panel-renderer']//*[@id='video-title']/ancestor::a[@id='wc-endpoint']"));
    	List<WebElement> listNames = driver.findElements(By.xpath("//*[@class='playlist-items style-scope ytd-playlist-panel-renderer']//*[@id='video-title']"));
    	if(list.size() != listNames.size())
    		return null;
    	
    	for(int i=0;i<list.size();i++) {
    		list = driver.findElements(By.xpath("//*[@class='playlist-items style-scope ytd-playlist-panel-renderer']//*[@id='video-title']/ancestor::a[@id='wc-endpoint']"));
        	listNames = driver.findElements(By.xpath("//*[@class='playlist-items style-scope ytd-playlist-panel-renderer']//*[@id='video-title']"));
        	
    		String item = list.get(i).getAttribute("href").split("&list=")[0];
    		System.out.println(listNames.get(i).getText() + ": "+ item);
    		allItems.put(listNames.get(i).getText(),item);
    	}
    	return allItems;
    }
    
    public void downloadMP3List(String listName,String listUrl) throws Exception {
    	
    	currentPlayListURL = listUrl;
    	
    	Map<String,String> allItems;
    	if(currentPlayListURL.startsWith("https://music.youtube"))
    		allItems = captureListYTMusicURLs(currentPlayListURL);
    	else 
    		//allItems = captureListYTURLs();
    		allItems = captureListFromFile("file.txt");
    	
    	for (Map.Entry<String, String> entry : allItems.entrySet()) {
    	    String name = entry.getKey();
    	    String url = entry.getValue();
    	    System.out.println(name + ": " + y2Mate3(url, name));
    	}
    	System.out.println("DONE for List: "+listName);
    	FileHandler.sequentialFolderCopy(WebDriverFactory.getDownloadPath(), listName);
    }
    
    public boolean y2Mate3(String videoUrl,String expName) throws Exception {
    	boolean success = false;
    	try {
	    	driver.get("https://y2mate.vet/convert");
	    	wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@aria-label='YouTube video URL or search term']")));
	    	driver.findElement(By.xpath("//input[@aria-label='YouTube video URL or search term']")).clear();
	    	driver.findElement(By.xpath("//input[@aria-label='YouTube video URL or search term']")).sendKeys(videoUrl);
	    	driver.findElement(By.xpath("//button[@class='search-bar-btn search-button']")).click();
	    	hardWait(4000);
	    	for(int i=0; i<30 && driver.findElements(By.xpath("//button[@class='result-card__btn download-form__btn']")).size()==0;i++) {
	    		if(isDisplayed("//*[text()='No results found. Try a different search.']")) {
	    			driver.findElement(By.xpath("//button[@class='search-bar-btn search-button']")).click();
	    			hardWait(2000);
	    		}
	    		hardWait(2000);
	    	}
	    	
	    	String title = clickY2Mate3MatchingResult(expName);
	    	if(title==null)
	    		return false;
	    	
	    	isDisplayed("//*[@class='result-card__btn download-form__btn']");
	    	driver.switchTo().frame(driver.findElement(By.xpath("//iframe[@id='widget-iframe']")));
	    	driver.findElement(By.xpath("//*[@class='convert_btn']")).click();
	    	for(int i=0; i<10 && driver.findElements(By.xpath("//div[@per='100%']")).size()==0;i++) {
	    		hardWait(3000);
	    	}
	    	
	    	driver.findElement(By.xpath("//div[@class='download_btn']/a")).click();
	    	hardWait(2000);
	    	driver.switchTo().defaultContent();
	    	closeAdditionalWindows();
	    	
	    	//title = title + " [" + videoUrl.split("watch?v=")[1] + "]";
	    	System.out.println("Title: "+title);
	    	success = waitForDownloadComplete(WebDriverFactory.getDownloadPath(), title, 60);
	    	addSequenceToMp3(WebDriverFactory.getDownloadPath());
    	}catch(Exception e) {
    		success = false;
    	} finally {
    		driver.switchTo().defaultContent();
    		closeAdditionalWindows();
    	}
    	return success;
    }
    
    private String clickY2Mate3MatchingResult(String expName) {
    	
    	String title;
		List<WebElement> results = driver.findElements(By.xpath("//*[@class='result-card__info']/*[@class='result-card__title']"));
		for(int i=0;i<results.size() && i<10; i++) {
			title = results.get(i).getText();
			if(title.toLowerCase().contains(expName.toLowerCase())) {
				driver.findElements(By.xpath("//*[@class='result-card__info']//button[@class='result-card__btn download-form__btn']")).get(i).click();
				return title;
			}
			int matchResult = FuzzySentenceMatcher.getFuzzyMatchPercentage(title, expName);
			if(matchResult>=40) {
				driver.findElements(By.xpath("//*[@class='result-card__info']//button[@class='result-card__btn download-form__btn']")).get(i).click();
				return title;
			}
		}
    	return null;
    }
    
    public boolean y2MateDownload(String videoUrl) throws Exception {
    	boolean success = false;
    	try {
	    	driver.get("https://v3.y2mate.nu/");
	    	wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='video']")));
	    	driver.findElement(By.xpath("//input[@id='video']")).clear();
	    	driver.findElement(By.xpath("//input[@id='video']")).sendKeys(videoUrl);
	    	driver.findElement(By.xpath("//button[text()='Convert']")).click();
	    	hardWait(4000);
	    	for(int i=0; i<30 && !driver.findElement(By.xpath("//*[@class='form__input--progress']")).getText().equals("completed");i++) {
	    		hardWait(2000);
	    	}
	    	
	    	wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Download']")));
	    	String title = driver.findElement(By.xpath("//*[@class='form__title']")).getText();
	    	driver.findElement(By.xpath("//button[text()='Download']")).click();
	    	hardWait(3000);
	    	success = waitForDownloadComplete(WebDriverFactory.getDownloadPath(), title, 60);
    	}catch(Exception e) {
    		success = false;
    	} finally {
    		closeAdditionalWindows();
    	}
    	return success;
    }
   
    public boolean y2MateNetDownload(String videoUrl) throws Exception {
    	boolean success = false;
    	try {
	    	driver.get("https://y2mate.net.co/3");
	    	wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='search']")));
	    	
	    	int retry = 5;
	    	while(--retry>0) {
	    		if(y2MateNetSearch(videoUrl))
	    			break;
	    		else
	    			hardWait(3000);
	    	}
	    	driver.findElement(By.xpath("//button[text()='Get Link']")).click();
	    	
	    	int count = 30;
	    	while(--count>0 && driver.findElements(By.xpath("//button[text()='Converting, please wait...']")).size()>0) {
	    		hardWait(2000);
	    	}
    	
	    	wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[div[span[text()='Download']]]")));
	    	String title = driver.findElement(By.xpath("//a[div[span[text()='Download']]]")).getAttribute("href");
	    	title = title.substring(title.lastIndexOf('/') + 1);
	    	driver.findElement(By.xpath("//a[div[span[text()='Download']]]")).click();
	    	hardWait(3000);
	    	success = waitForDownloadComplete(WebDriverFactory.getDownloadPath(), title, 60);
    	}catch(Exception e) {
    		success = false;
    	} finally {
    		closeAdditionalWindows();
    	}
    	return success;
    }
    
    private boolean waitUntil(String xpath) {
    	try {
    		wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
    		return driver.findElement(By.xpath(xpath)).isDisplayed();
    	} catch(Exception e) {
    		return false;
    	}
    }
    
    private boolean isDisplayed(String xpath) {
    	try {
    		return driver.findElement(By.xpath(xpath)).isDisplayed();
    	} catch(Exception e) {
    		return false;
    	}
    }
    
    private boolean y2MateNetSearch(String videoUrl) {
    	boolean success = false;
    	try {
    		if(!driver.getCurrentUrl().startsWith("https://y2mate.net.co")) {
    			driver.get("https://y2mate.net.co/3");
    			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='search']")));
    		}
    		
    		driver.findElement(By.xpath("//input[@type='search']")).clear();
	    	driver.findElement(By.xpath("//input[@type='search']")).sendKeys(videoUrl);
	    	driver.findElement(By.xpath("//input[@type='search']")).sendKeys(Keys.TAB);
	    	hardWait(10000);
	    	if(isDisplayed("//button[text()='Get Link']")) {
	    		success = true;
	    	} else {
		    	driver.findElement(By.xpath("//button[@type='submit']")).click();
	    		hardWait(1000);
		    	closeAdditionalWindows();
		    	if(!driver.getCurrentUrl().startsWith("https://y2mate.net.co")) {
	    			driver.get("https://y2mate.net.co/3");
	    			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='search']")));
	    		} else {
			    	success = waitUntil("//button[text()='Get Link']");
	    		}
	    	}
    	}catch(Exception e) {
    		success = false;
    	}finally {
    		closeAdditionalWindows();
    	}
    	return success;
    }
        
    public static boolean waitForDownloadComplete(String folderPath, String fileName, int timeoutInSeconds) {
    	
    	if(!fileName.endsWith(".mp3"))
    		fileName += ".mp3";
    	
    	long endTime = System.currentTimeMillis() + (timeoutInSeconds * 1000);
    	
    	File folder = new File(folderPath);
    	boolean inProgress = true;
    	int downloadCount = failedDownloadFiles;
    	while (inProgress && System.currentTimeMillis() < endTime) {
    		downloadCount = failedDownloadFiles;
    		for(File file: folder.listFiles()) {
	    		if(file.getName().endsWith(".crdownload")||file.getName().endsWith(".part")) {
	    			hardWait(5000); // Poll every 5 second
	    			downloadCount++;
	    		}
	    	}
    		if(downloadCount>failedDownloadFiles)
    			inProgress = true;
    		else
    			inProgress = false;
		}
    	
    	if(downloadCount > (failedDownloadFiles+1))
    		failedDownloadFiles++;
    	
        File file = new File(folderPath + "\\" + fileName);
        if(file.exists())
        	return true;
        else {
        	folder = new File(folderPath);
        	fileName = fileName.replace(".mp3","");
        	for(File file1: folder.listFiles()) {
        		if(file1.getName().contains(fileName))
    	    		return true;
        		int match = FuzzySentenceMatcher.getFuzzyMatchPercentage(file1.getName(), fileName);
        		if(match>=80)
        			return true;
        	}
    	    	
        }
        return false;
    }
        
    private static void hardWait(int millis) {
    	try {
            Thread.sleep(millis); // Poll every millis second
        } catch (InterruptedException e) {
        }
    }
    
    private void closeAdditionalWindows() {
    	String parentWindow = driver.getWindowHandle();
		try {
    		Set<String> allWindows = driver.getWindowHandles();
    		for (String handle : allWindows) {
    		    if (!handle.equals(parentWindow)) {
    		        driver.switchTo().window(handle);
    		        driver.close();
    		    }
    		}
    	} catch(Exception e) {
    		
    	} finally {
    		driver.switchTo().window(parentWindow);
    	}
    }
    
        public static void addSequenceToMp3(String dirPath) {
        	Path folder = Paths.get(dirPath);

            List<Path> allMp3Files = new ArrayList<>();

            // 1. Collect all MP3 files
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.mp3")) {
                for (Path file : stream) {
                    allMp3Files.add(file);
                }
            } catch (IOException e) {
                System.err.println("Error reading directory: " + e.getMessage());
                return;
            }

            // 2. Sort files alphabetically
            Collections.sort(allMp3Files);

            // 3. Find highest existing sequence number
            int maxSequence = 0;
            List<Path> filesToRename = new ArrayList<>();

            for (Path file : allMp3Files) {
                String name = file.getFileName().toString();
                
                // Matches any digits at the start followed by an underscore
                if (name.matches("^\\d+_.+")) {
                    String numStr = name.split("_")[0];
                    try {
                        int currentNum = Integer.parseInt(numStr);
                        if (currentNum > maxSequence) {
                            maxSequence = currentNum;
                        }
                    } catch (NumberFormatException e) {
                        filesToRename.add(file);
                    }
                } else {
                    filesToRename.add(file);
                }
            }

            // 4. Rename unsequenced files with 01_, 02_ format
            int nextSequence = maxSequence + 1;
            for (Path file : filesToRename) {
                String originalName = file.getFileName().toString();
                
                // %02d ensures at least 2 digits with a leading zero (e.g., 01, 02... 10)
                String newName = String.format("%02d_%s", nextSequence, originalName); 
                
                Path newPath = file.resolveSibling(newName);
                
                try {
                    Files.move(file, newPath);
                    System.out.println("Renamed: " + originalName + " -> " + newName);
                    nextSequence++;
                } catch (IOException e) {
                    System.err.println("Failed to rename " + originalName + ": " + e.getMessage());
                }
            }
            
            System.out.println("Renaming process complete.");
        }

        public Map<String, String> captureListFromFile(String filePath) {
            Map<String, String> allItems = new LinkedHashMap<>();
            
            // Use try-with-resources to automatically close the reader
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                
                // Read file line by line
                while ((line = br.readLine()) != null) {
                    // Split the line by comma. Limit to 2 in case the value itself contains commas.
                    String[] parts = line.split(",", 2);
                    
                    // Ensure there are at least two parts before adding to the map
                    if (parts.length >= 2) {
                        // Using trim() to remove any accidental whitespace around the words
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        
                        allItems.put(key, value);
                    }
                }
            } catch (IOException e) {
                // Handle file reading errors (e.g., file not found)
                System.err.println("Error reading the file: " + e.getMessage());
            }
            
            return allItems;
        }

}
