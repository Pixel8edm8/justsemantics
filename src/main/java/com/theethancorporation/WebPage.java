package com.theethancorporation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsoup.Jsoup;

import com.theethancorporation.util.EthansHashSet;
import com.theethancorporation.util.MalformedWikipediaURLException;
import com.theethancorporation.util.PathUtils;

public class WebPage implements Serializable {
    @SuppressWarnings("unused")
    private static final long SERIAL_VERSION_UID = 5L;
    private final static Path RESOURCES_PATH = PageController.getResourcesFilePath();
    private transient Path pageContentPath;
    private transient Path serialPath;
    private String webPageURL;
    private String pageTopic;
    private final HashMap<String, Double> termFrequencies;
    private final EthansHashSet<String> tokensInWebpage;
    private Map<String, Double> documentFrequencies;
    private Map<String, Double> TDFIDF;
    /**
     * Constructs the tables and takes arguments for WebPage
     * @param webPageURL Takes the raw String URL to point it at the specific wikipedia page.
     * @throws MalformedWikipediaURLException If there is no topic at the end of the URL, this is what WebPage throws.
     */
    public WebPage(final String webPageURL) throws MalformedWikipediaURLException {
        this.webPageURL = URLDecoder.decode(webPageURL, StandardCharsets.UTF_8);
        this.pageTopic = PathUtils.cleanseStringForWindows(URLDecoder.decode(webPageURL.split("/")[webPageURL.split("/").length - 1], StandardCharsets.UTF_8));
        
        this.pageContentPath = Path.of(RESOURCES_PATH.toAbsolutePath().toString(), 
            "pageData",
            PathUtils.cleanseStringForWindows(pageTopic.concat(".html")) 
        );

        this.serialPath = Path.of(RESOURCES_PATH.toAbsolutePath().toString(),
            "serializedPages",
            pageTopic.concat(".ser")
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
        this.termFrequencies = findRelativeTermFrequencies(contentList.stream());
        this.tokensInWebpage = tokensInWebpage(contentList.stream());
    }

    /**
     * @throws IOException
     */
    private void getWebContent() throws IOException {
        final String documentText = Jsoup.connect(webPageURL).get().body().text().toLowerCase().replaceAll("[^a-z0-9]", " ");
        if (Files.notExists(pageContentPath)) {
            Files.createFile(pageContentPath);
        }
        try (BufferedWriter htmlWriter = Files.newBufferedWriter(pageContentPath, Charset.defaultCharset())) {
            htmlWriter.write(webPageURL + "\n");
            htmlWriter.write(documentText);
        }
    }

    /**
     * Reads the web content and returns a delimited string stream.
     */
    private Stream<String> readWebContent() throws IOException {
        try (Scanner newScanner = new Scanner(pageContentPath)) {
            // To look over the URL
            newScanner.nextLine();
            if (newScanner.hasNextLine()) {
                return Pattern.compile(" ").splitAsStream(newScanner.nextLine());
            } else {
                return Stream.empty();
            }
        }
    }

    /**
     * 
     * @param contentStream
     * @return Returns a HashMap of the relative term frequenciesn of each word in the form of a HashMap<String, Double> 
     */
    private HashMap<String, Double> findRelativeTermFrequencies(final Stream<String> contentStream) {
        final HashMap<String, Integer> wordFrequencyTable = new HashMap<>();
        final HashMap<String, Double> relativeFrequencyTable = new HashMap<>();
        contentStream.forEach(
            (String word) -> {
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

    public Map<String, Double> calculateTDFIDF(Map<String, Double> inverseDocumentFrequencyTable) {
        Map<String, Double> TDFIDFTable = new HashMap<>();
        termFrequencies.keySet().stream().forEach(
            (word) -> {
                TDFIDFTable.put(word, termFrequencies.get(word) * inverseDocumentFrequencyTable.get(word));
            }
        );
        return TDFIDFTable;
    }

    // Special serializing procedures to make sure that I don't get annoying errors.
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        out.writeUTF(this.pageContentPath.toString());
        out.writeUTF(this.serialPath.toString());
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        this.pageContentPath = Path.of(in.readUTF());
        this.serialPath = Path.of(in.readUTF());
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

    public Map<String, Double> getDocumentFrequencies() {
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

    public String getPageTopic() {
        return pageTopic;
    }

    public Path getSerialPath() {
        return serialPath;
    }

    public HashMap<String, Double> getTDFIDF() {
        return (HashMap<String, Double>) TDFIDF;
    }

    public void setTDFIDF(Map<String, Double> TDFIDF) {
        this.TDFIDF = TDFIDF;
    }
}
