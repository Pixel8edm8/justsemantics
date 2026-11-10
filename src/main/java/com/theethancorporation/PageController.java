package com.theethancorporation;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Stream;

import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import com.theethancorporation.util.EthansHashSet;
import com.theethancorporation.util.MalformedWikipediaURLException;

/**
 * 
 * PageController
 */
public final class PageController {
    static private Path resourcesFilePath;
    static private boolean CONTROL_PATH_EXISTS = false;

    public static boolean isCONTROL_PATH_EXISTS() {
        return CONTROL_PATH_EXISTS;
    }

    private ArrayList<WebPage> listOfPages = null;
    private final HashMap<String, Double> INVERSE_DOCUMENT_FREQUENCY_HASHMAP;
    private final HashMap<WebPage, HashMap<String, Double>> WEBPAGE_TO_TFIDF;
    private int amountOfURLsToAnalyze = 0;
    /**
     * The constructor for the PageController.
     * @param resourcesFilePath A control.txt file that you can fill with wikipedia URLs, which the PageController will subsequently search.
     * @param amountOfURLsToAnalyze A fallback, if there is not control.txt, the PageController will randomly sample this amount of URLs from the Wikipedia random page.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public PageController(final Path resourcesFilePath, final int amountOfURLsToAnalyze) throws FileNotFoundException, IOException {
        PageController.resourcesFilePath = resourcesFilePath;
        this.amountOfURLsToAnalyze = amountOfURLsToAnalyze;
        try {
            this.listOfPages = populateWebpagesList();
        } catch (InterruptedException ex) {
            System.getLogger(PageController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        this.INVERSE_DOCUMENT_FREQUENCY_HASHMAP = calculateInverseDocumentFrequency();
        this.WEBPAGE_TO_TFIDF = calculateAllTFIDF();
        if (Path.of(resourcesFilePath.toString() + "control.txt").toFile().exists()) {
            PageController.CONTROL_PATH_EXISTS = true;
        }
        serializeWebPages();
    }

    /**
     * A populator that takes either the control.txt or a random sample of size amountOfURLsToAnalyze, and populates the listOfPages with WebPage objects.
     * @return An ArrayList of WebPages that represent a specific topic and its associated data.
     * @throws InterruptedException 
     */
    private ArrayList<WebPage> populateWebpagesList() throws IOException, InterruptedException {
        final ArrayList<WebPage> localList = new ArrayList<>();
        EthansHashSet<WebPage> checkSet = new EthansHashSet<>(10, 0.75);
        final Path controlFile = Path.of(resourcesFilePath.toString() + "control.txt");

        // If we have a control file, use that.
        if (resourcesFilePath != null && CONTROL_PATH_EXISTS) {
            try (Stream<String> controlFileStream = Files.lines(controlFile)) {
            controlFileStream.forEach((line) -> {
                try {
                    if (checkSet.contains(new WebPage(line))) {
                        // continue;
                    } else {
                        checkSet.add(new WebPage(line));
                        localList.add(new WebPage(line));
                    }
                } catch (MalformedWikipediaURLException ex) {
                    System.getLogger(PageController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            });
            } catch (IOException e) {
                System.out.println("Some I/O problem happened, when attempting to read the control file.");
                e.printStackTrace();
            }
            // Else, if we need more files to shove into the cache, we add until we get 200.
            // TODO: We still need to have this dynamically retrieve all 200 files.
        } 

        final Path PATH_OF_PAGE_DATA = Path.of(resourcesFilePath.toString(), "pageData");
        
        for (String pathString : PATH_OF_PAGE_DATA.toFile().list()) {
            System.out.println(Path.of(PATH_OF_PAGE_DATA.toString(), pathString));
        
            try (Scanner newScanner = new Scanner(Path.of(PATH_OF_PAGE_DATA.toString(), pathString))) {
                localList.add(new WebPage(newScanner.nextLine()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }

        if (PATH_OF_PAGE_DATA.toFile().list().length < amountOfURLsToAnalyze) {
            for (int webPageIndex = resourcesFilePath.toFile().list().length; webPageIndex < amountOfURLsToAnalyze; webPageIndex++) {
                try {
                    final Response currentResponse = Jsoup.connect("https://en.wikipedia.org/wiki/Special:Random").execute();
                    final String resolvedURL = currentResponse.url().toExternalForm();
                    localList.add(new WebPage(resolvedURL));
                    System.out.println("Added " + resolvedURL);
                    // TODO Auto-generated catch block
                } catch (MalformedWikipediaURLException ex) {
                    System.getLogger(PageController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                } catch (HttpStatusException e) {
                    if (e.getMessage().equals("200")) {
                        System.out.println("Its a 200 error.");
                    }
                }
            }
        } 
        return localList;
    }

    public void serializeWebPages() {
        for (WebPage webPage : listOfPages) { 
            if (!webPage.getSerialPath().toFile().exists()) {
                try {
                    webPage.getSerialPath().toFile().createNewFile();
                } catch (IOException e) {
                    System.out.println("Could not make the file dictated by the serialPath.");
                    e.printStackTrace();
                }
            }
            // IF the serialPath doesnt exist, then make it exist as 
            try (FileOutputStream serialFile = new FileOutputStream(webPage.getSerialPath().toFile()); ObjectOutputStream outputStream = new ObjectOutputStream(serialFile)) {
                outputStream.writeObject(webPage);
            } catch (FileNotFoundException e) {
                System.out.println("When serializing the webPage, we could not make or find the file.");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("I could not write to the file, even though I found it.");
                e.printStackTrace();
            }
            System.out.println(webPage.getSerialPath().toString());
        }
    }

    /**
     * 
     * @return
     */
    private HashMap<String, Double> calculateInverseDocumentFrequency() {
        HashMap<String, Integer> corpusHashMap = new HashMap<>();
        HashMap<String, Double> corpusDocumentFrequencyHashMap = new HashMap<>();
        // For each webpage object, iterate over the Keys.
        // How many times does the corpus mention the word?
        for (WebPage currentWebPage : listOfPages) {
            for (ArrayList<String> bucket : currentWebPage.getTokenHashSet().getBuckets()) {
                for (String possiblyNewWord : bucket) {
                    if (!corpusHashMap.containsKey(possiblyNewWord)) {
                        corpusHashMap.put(possiblyNewWord, 0);
                    } else {
                        corpusHashMap.put(possiblyNewWord, corpusHashMap.get(possiblyNewWord) + 1);
                    }
                }
            }
        }
        // For each word, check if the current webpage has said word.
        // If it does have that word, then 
        corpusHashMap.keySet().stream().forEach((word) -> {
                corpusDocumentFrequencyHashMap.put(word, Math.log(((listOfPages.size() + 1) / (corpusHashMap.get(word) + 1))));
            }
        );
        return corpusDocumentFrequencyHashMap;
    }

    /**
     * Calculates the cosine similarity (or lack thereof) between two target WebPage objects.
     * @param webPageX
     * @param webPageY
     * @return The Cosine Similarity between the two.
     */
    public double calculateCosineSimilarity(final WebPage webPageX, final WebPage webPageY) {
        HashMap<String, Double> vectorA = WEBPAGE_TO_TFIDF.get(webPageX);
        HashMap<String, Double> vectorB = WEBPAGE_TO_TFIDF.get(webPageY);
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (String currentWord : vectorA.keySet()) {
            normA += Math.pow(vectorA.get(currentWord), 2);
            if (vectorB.containsKey(currentWord)) {
                dotProduct += (vectorA.get(currentWord) * vectorB.get(currentWord));
            }
        }

        for (String currentWord : vectorB.keySet()) {
            normB += Math.pow(vectorB.get(currentWord), 2);
        }
        return dotProduct / ((Math.sqrt(normA) * (Math.sqrt(normB))));
    }

    /**
     * 
     */
    public HashMap<WebPage, HashMap<String, Double>> calculateAllTFIDF() {
        HashMap<WebPage, HashMap<String, Double>> newHashMap = new HashMap<>();
        for (WebPage currentWebPage : listOfPages) {
            newHashMap.put(currentWebPage, currentWebPage.calculateTDFIDF(INVERSE_DOCUMENT_FREQUENCY_HASHMAP));
        }
        return newHashMap;
    }

    /**
     * 
     * @return
     */
    public static Path getResourcesFilePath() {
        return resourcesFilePath;
    }

    /**
     * 
     * @return
     */
    public HashMap<String, Double> getInverseDocumentFrequencyHashMap() {
        return INVERSE_DOCUMENT_FREQUENCY_HASHMAP;
    }

    public ArrayList<WebPage> getListOfPages() {
        return listOfPages;
    }

}