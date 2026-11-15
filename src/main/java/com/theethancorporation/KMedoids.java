package com.theethancorporation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.theethancorporation.util.SerialUtils;

public class KMedoids {

    public List<SimilarityTracker> getK_MEDOIDS_TRACKER() {
        return K_MEDOIDS_TRACKER;
    }
    public record SimilarityObject (WebPage closeObject, Double distance) {}
    public record SimilarityTracker(WebPage medoid, ArrayList<SimilarityObject> mostSimilarPages) {}
    private List<WebPage> K_CLUSTERS = null;
    private List<SimilarityTracker> K_MEDOIDS_TRACKER = new ArrayList<>(); 
    final private List<WebPage> ALL_WEBPAGES = SerialUtils.returnArrayListOfPages();
    private int MAX_ITERATIONS = 0;
    /**
     * A KMedoids class that reads out serialized objects from ./resources/serializedPages, and then uses KMedoids to determine similarity.
     * @param K Dictates how many clusters are expected.
     */
    public KMedoids(final int K, final int MAX_ITERATIONS) {
        this.K_CLUSTERS = determineInitialKClusters(K);
        this.MAX_ITERATIONS = MAX_ITERATIONS;
        this.K_MEDOIDS_TRACKER = initializeClusters();

    }


    private double calculateCosineSimilarity(final WebPage webPageX, final WebPage webPageY) {
        HashMap<String, Double> vectorA = webPageX.getTDFIDF();
        HashMap<String, Double> vectorB = webPageY.getTDFIDF();
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
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dotProduct / ((Math.sqrt(normA) * (Math.sqrt(normB))));
    }

    private double distance(WebPage first, WebPage second) {
        return 1.0 - calculateCosineSimilarity(first, second);
    }

    private List<WebPage> determineInitialKClusters(final int K) {
        int[] pickedIndices = new int[K];
        for (int j = 0; j < K; j++) {
            pickedIndices[j] = -1;
        }
        final List<WebPage> pickedInitialClusters = new ArrayList<>(); 
        for (int i = 0; i < K; i++) {
            boolean isDuplicate = false;
            int randomIndex = ThreadLocalRandom.current().nextInt(ALL_WEBPAGES.size());
            for (int number : pickedIndices) {
                if (number == randomIndex) {
                    isDuplicate = true;
                    break;
                }
            }

            if (isDuplicate) {
                i--;
            } else {
                pickedIndices[i] = randomIndex;
                pickedInitialClusters.add(ALL_WEBPAGES.get(randomIndex));
            }
        }
        return pickedInitialClusters;
    }
    // PSUEDOCODE:
    // 1. Randomly select K_CLUSTERS objects out of a list with random. (DONE)
    // 2. Each page goes to its closest medoid by cosineDistance.
    // 3. In each cluster, make the member with the smallest distance to other members new medoid.
    // 4. Complete until MAX_ITERATIONS or convergence. FOr simplicity, i'll probably just have it do until max_convergence
    // 5. Make some sort of visual for it in the app.

    /**
     * Assigns each initial cluster to a list, and then find the cosine similarity between the medoid and all pages. 
     */
    private List<SimilarityTracker> initializeClusters() {
        List<SimilarityTracker> clusters = new ArrayList<>();
        // Initialize a SimilarityTracker for each medoid.
        for (WebPage medoid : K_CLUSTERS) {
            clusters.add(new SimilarityTracker(medoid, new ArrayList<>()));
        }

        // For each page, assign it a medoid based on what its closest to.

        for (WebPage webPage : ALL_WEBPAGES) {
            // For each medoid WebPage in each Similarity Tracker,
            SimilarityObject closestMedoid = new SimilarityObject(clusters.get(0).medoid, distance(webPage, clusters.get(0).medoid));
            for (SimilarityTracker similarityTracker : clusters) {
                final double distanceToComparedCluster = distance(webPage, similarityTracker.medoid);
                if (distanceToComparedCluster < closestMedoid.distance) {
                    closestMedoid = new SimilarityObject(similarityTracker.medoid, distanceToComparedCluster);
                }
            }
            // Then, add the SimilarityObject to the proper medoid.
            for (SimilarityTracker similarityTracker : clusters) {
                if (closestMedoid.closeObject.equals(similarityTracker.medoid)) {
                    similarityTracker.mostSimilarPages.add(new SimilarityObject(webPage, closestMedoid.distance));
                } 
            }
        }
        return clusters;
    }

    // For each cluster,
    // and for each page
    // Check the sum of the distance from that page to everyone else.
    // Whoever has the smallest total is the new medoid!
    // And if, for some reason, the 
    private List<SimilarityTracker> refreshClusters() {
    // Disgusting and hard to read code. But it works. Yay!
    final List<SimilarityTracker> newClusters = new ArrayList<>();
    // Pick next best medoid.
    for (SimilarityTracker medoidTracker : K_MEDOIDS_TRACKER) {
        double initialSummationDistance = 0.0;
        for (SimilarityObject member : medoidTracker.mostSimilarPages) {
            initialSummationDistance += distance(medoidTracker.medoid, member.closeObject);
        }
        SimilarityObject bestMedoidYet = new SimilarityObject(medoidTracker.medoid, initialSummationDistance);
        for (SimilarityObject currentPage : medoidTracker.mostSimilarPages) {
            double distanceSummation = 0.0;
            for (SimilarityObject comparedPage : medoidTracker.mostSimilarPages) {
                // Ask DL: is it better stylistically to have a continue; or to just leave the brackets there if its just an if-else for a loop?
                distanceSummation += distance(currentPage.closeObject, comparedPage.closeObject);
            }
            if (distanceSummation < bestMedoidYet.distance) {
                bestMedoidYet = new SimilarityObject(currentPage.closeObject, distanceSummation);
            }
        }
        // And then refresh the entire thing to reflect the new medoids.
        newClusters.add(new SimilarityTracker(bestMedoidYet.closeObject, new ArrayList<>()));
    }
    for (WebPage webPage : ALL_WEBPAGES) {
        SimilarityObject bestMedoidYet = new SimilarityObject(newClusters.get(0).medoid, distance(webPage, newClusters.get(0).medoid));
        for (SimilarityTracker currentSimilarityTracker : newClusters) {
            final double distanceToComparedCluster = distance(webPage, currentSimilarityTracker.medoid);
            if (distanceToComparedCluster < bestMedoidYet.distance) {
                bestMedoidYet = new SimilarityObject(currentSimilarityTracker.medoid, distanceToComparedCluster);
            }
        }
        for (SimilarityTracker medoidToCheck : newClusters) {
            if (bestMedoidYet.closeObject.equals(medoidToCheck.medoid)) {
                medoidToCheck.mostSimilarPages.add(new SimilarityObject(webPage, distance(webPage, medoidToCheck.medoid)));
            }
        }
    }
    this.K_MEDOIDS_TRACKER = newClusters;
    return newClusters;
}


    public void printClusters() {
        for (SimilarityTracker cluster : K_MEDOIDS_TRACKER) {
            System.out.println("CLUSTER " + cluster.medoid.getPageTopic() + "==========================");
            // Some functional stuff, yay! Basically, turn it into a stream, filter it to not be outliers, sort it from greatest to least, and then limit it to
            cluster.mostSimilarPages.stream()
            .filter(similarObject -> similarObject.distance < .95 && !similarObject.distance.equals(0.0))
            .sorted((firstPage, secondPage) -> Double.compare(firstPage.distance, secondPage.distance))
            .limit(10)
            .forEach((funObject) -> System.out.println(String.format("Page: %s, Distance: %.4f", funObject.closeObject.getPageTopic(), funObject.distance)));
        }
    }

    public void runKMedoids() {
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            List<SimilarityTracker> previousClusters = new ArrayList<>(K_MEDOIDS_TRACKER);
            List<SimilarityTracker> newClusters = refreshClusters();
            int similarityCount = 0;
            for (SimilarityTracker previousTracker : previousClusters) {
                for (SimilarityTracker currentTracker : newClusters) {
                    if (previousTracker.medoid.equals(currentTracker.medoid)) {
                        similarityCount++;
                    }
                }
            }
            if (similarityCount == K_MEDOIDS_TRACKER.size()) {
                // If it converges, then you break it.
                break;
            }
        }
        printClusters();
    }
}
