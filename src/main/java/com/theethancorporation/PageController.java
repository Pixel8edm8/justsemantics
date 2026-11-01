package com.theethancorporation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.theethancorporation.util.EthansHashSet;
import com.theethancorporation.util.MalformedWikipediaURL;

/**
 * 
 * PageController
 */
public final class PageController {
    static private Path resourcesFilePath;
    private final ArrayList<WebPage> listOfPages;
    private final HashMap<String, Double> inverseDocumentFrequencyHashMap;
    private final HashMap<WebPage, HashMap<String, Double>> webPageToTFIDF;
    private final int amountOfURLsToAnalyze;

    /**
     * The constructor for the PageController.
     * @param resourcesFilePath A control.txt file that you can fill with wikipedia URLs, which the PageController will subsequently search.
     * @param amountOfURLsToAnalyze A fallback, if there is not control.txt, the PageController will randomly sample this amount of URLs from the Wikipedia random page.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public PageController(final Path resourcesFilePath, final int amountOfURLsToAnalyze) throws FileNotFoundException, IOException {
        PageController.resourcesFilePath = resourcesFilePath;
        this.listOfPages = populateWebpagesList();
        this.inverseDocumentFrequencyHashMap = calculateInverseDocumentFrequency();
        this.webPageToTFIDF = calculateAllTFIDF();
        this.amountOfURLsToAnalyze = amountOfURLsToAnalyze;
    }

    /**
     * A populator that takes either the control.txt or a random sample of size amountOfURLsToAnalyze, and populates the listOfPages with WebPage objects.
     * @return An ArrayList of WebPages that represent a specific topic and its associated data.
     */
    private ArrayList<WebPage> populateWebpagesList() {
        final ArrayList<WebPage> localList = new ArrayList<>();
        EthansHashSet<WebPage> checkSet = new EthansHashSet<>(10, 0.75);
        if (resourcesFilePath != null) {
            try (Stream<String> controlFileStream = Files.lines(Path.of(resourcesFilePath.toString(), "control.txt"))) {
            controlFileStream.forEach((line) -> {
                try {
                    if (checkSet.contains(new WebPage(line))) {
                        // continue;
                    } else {
                        checkSet.add(new WebPage(line));
                        localList.add(new WebPage(line));
                    }
                } catch (MalformedWikipediaURL ex) {
                    System.getLogger(PageController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            });
            } catch (IOException e) {
                System.out.println("Some I/O problem happened, when attempting to read the control file.");
                e.printStackTrace();
            }    
        } else {
            for (int webPageIndex = 0; webPageIndex <= amountOfURLsToAnalyze; webPageIndex++) {
                try {
                    final Response currentResponse = Jsoup.connect("https://en.wikipedia.org/wiki/Special:Random").execute();
                    final String resolvedURL = currentResponse.url().toExternalForm();
                    localList.add(new WebPage(resolvedURL));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (MalformedWikipediaURL ex) {
                    System.getLogger(PageController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
        }
        return localList;
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
        corpusHashMap.keySet().stream().forEach(
            (word) -> {
                corpusDocumentFrequencyHashMap.put(word, Math.log(((listOfPages.size() + 1) / (corpusHashMap.get(word) + 1))));
            }
        );
        return corpusDocumentFrequencyHashMap;
    }

    /**
     * 
     * @param webPageX
     * @param webPageY
     * @return
     */
    public double calculateCosineSimilarity(final WebPage webPageX, final WebPage webPageY) {
        HashMap<String, Double> vectorA = webPageToTFIDF.get(webPageX);
        HashMap<String, Double> vectorB = webPageToTFIDF.get(webPageY);
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
            newHashMap.put(currentWebPage, currentWebPage.calculateTDFIDF(inverseDocumentFrequencyHashMap));
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
        return inverseDocumentFrequencyHashMap;
    }

    public ArrayList<WebPage> getListOfPages() {
        return listOfPages;
    }

}
