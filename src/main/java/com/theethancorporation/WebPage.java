package com.theethancorporation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsoup.Jsoup;

import com.theethancorporation.util.EthansHashSet;
import com.theethancorporation.util.MalformedWikipediaURL;

public class WebPage {
    private final static Path resourcesPath = PageController.getResourcesFilePath();
    private Path pagePath;
    private String webPageURL;
    private String pageTopic;
    private HashMap<String, Double> termFrequencies;
    private EthansHashSet<String> tokensInWebpage;
    private HashMap<String, Double> documentFrequencies;

    public WebPage(final String webPageURL) throws MalformedWikipediaURL {
        this.webPageURL = webPageURL;

        Pattern endOfURLPattern = Pattern.compile("\\w+$");
        Matcher endOfURLMatcher = endOfURLPattern.matcher(webPageURL);
                
        if (endOfURLMatcher.find()) {
            this.pageTopic = endOfURLMatcher.group();
        }

        if ("".equals(pageTopic)) {
            throw new MalformedWikipediaURL("No end of URL, malformed.");
        }
        
        this.pagePath = Path.of(
            resourcesPath.toAbsolutePath().toString(), 
            "pageData",
            pageTopic.concat(".html")
        );
        try {
            getWebContent();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<String> contentList = List.of();
        try {
            contentList = readWebContent().collect(Collectors.toList());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        this.termFrequencies = findTermFrequencies(contentList.stream());
        this.tokensInWebpage = tokensInWebpage(contentList.stream());
    }

    /**
     * @throws IOException
     */
    private void getWebContent() throws IOException {
        final String documentText = Jsoup.connect(webPageURL).get().body().text().toLowerCase().replaceAll("[^a-z0-9]", " ");
        if (Files.notExists(pagePath)) {
            Files.createFile(pagePath);
        }
        try (BufferedWriter htmlWriter = Files.newBufferedWriter(pagePath, Charset.defaultCharset())) {
            htmlWriter.write(documentText);
        }
    }

    /**
     * Reads the web content and returns a delimited string stream.
     */
    private Stream<String> readWebContent() throws IOException {
        try (Scanner newScanner = new Scanner(pagePath)) {
            if (newScanner.hasNextLine()) {
                return Pattern.compile(" ").splitAsStream(newScanner.nextLine());
            } else {
                return Stream.empty();
            }
        }
    }


    private HashMap<String, Double> findTermFrequencies(final Stream<String>contentStream) {
        final HashMap<String, Integer> wordFrequencyTable = new HashMap<>();
        final HashMap<String, Double> relativeFrequencyTable = new HashMap<>();
        // TODO: Figure out how to add the value to totalTokens, as its being added. (Figured out the solution to this without forEach!)
        contentStream.forEach(
            (word) -> {
                if (!wordFrequencyTable.containsKey(word)) {
                    wordFrequencyTable.put(word, 1);
                } else {
                    wordFrequencyTable.replace(word, wordFrequencyTable.get(word) + 1);
                }
            }
        );
        int totalTokens = wordFrequencyTable.values().stream().reduce(0, (accumulator, current) -> accumulator + current);
        wordFrequencyTable.keySet().stream().forEach(
            (String word) -> {
                int currentWordFrequency = wordFrequencyTable.get(word);
                relativeFrequencyTable.put(word, (double) currentWordFrequency / totalTokens);
            }
        );
        return relativeFrequencyTable;
    }

    private EthansHashSet<String> tokensInWebpage(final Stream<String> contentStream) {
        EthansHashSet<String> wordExistenceSet = new EthansHashSet<>(100, 0.75);
        contentStream.forEach(
            (word) -> {
                if (!wordExistenceSet.contains(word)) {
                    wordExistenceSet.add(word);
                }
            }
        );
        return wordExistenceSet;
    }

    public HashMap<String, Double> calculateTDFIDF(HashMap<String, Double> inverseDocumentFrequencyTable) {
        HashMap<String, Double> TDFIDFTable = new HashMap<>();
        termFrequencies.keySet().stream().forEach(
            (word) -> {
                TDFIDFTable.put(word, termFrequencies.get(word) * inverseDocumentFrequencyTable.get(word));
            }
        );
        return TDFIDFTable;
    }

    /**
     * Dumps the webPage into a text file that can then be parsed.
     */
    public void dumpWebPage() {
        // Dump word frequencies
        
        // Dump the TDFIDF
        
    }

    public EthansHashSet<String> getTokenHashSet() {
        return tokensInWebpage;
    }
    public HashMap<String, Double> getTermFrequencies() {
        return termFrequencies;
    }
    public String getWebPageURL() {
        return webPageURL;
    }

    public HashMap<String, Double> getDocumentFrequencies() {
        return documentFrequencies;
    }

    public void setDocumentFrequencies(HashMap<String, Double> documentFrequencies) {
        this.documentFrequencies = documentFrequencies;
    }

    @Override
    public int hashCode() {
        return webPageURL.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        WebPage otherPage = (WebPage) obj;

        return java.util.Objects.equals(this.webPageURL, otherPage.webPageURL);
    }
}
