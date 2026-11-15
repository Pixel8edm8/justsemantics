package com.theethancorporation;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.theethancorporation.KMedoids.SimilarityTracker;
import com.theethancorporation.util.MalformedWikipediaURLException;

public class AppUI {
    private JFrame frame = null;
    final private JComboBox<String> page1Dropdown;

    /**
     * Exactly what you think it is.
     * @param controller The pageController that has 
     * @param pages
     */
    public AppUI(final PageController controller, final KMedoids kMedoids) {

        final ArrayList<WebPage> pages = controller.getListOfPages();
        
        frame = new JFrame("Semantics Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setResizable(true);
        frame.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Semantics Calculator", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Courier New", Font.BOLD, 18));

        frame.add(titleLabel, BorderLayout.NORTH);
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new GridLayout(4, 1, 5, 5));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(5, 40, 5, 40));
        
        String[] pageNames = new String[pages.size()];

        for (int i = 0; i < pages.size(); i++) {
            pageNames[i] = pages.get(i).getWebPageURL();
        }

        page1Dropdown = new JComboBox<>(pageNames);

        JLabel firstJLabel = new JLabel("Select Page: ");
        // I'm sure there was a far more efficient way to do this. Oh well!
        firstJLabel.setFont(new Font("Courier New", Font.PLAIN, 12));
        controlsPanel.add(firstJLabel);
        page1Dropdown.setFont(new Font("Courier New", Font.PLAIN, 12));
        controlsPanel.add(page1Dropdown);

        frame.add(controlsPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(2, 1, 10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 30, 40));

        JButton calculateButton = new JButton("Find Similar Sites?");
        JLabel resultsJLabel = new JLabel("No similar sites...yet.");
        bottomPanel.add(calculateButton);
        bottomPanel.add(resultsJLabel);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        resultsJLabel.setFont(new Font("Courier New", Font.PLAIN, 12));
        
        JButton clustersModeButton = new JButton("Switch to clusters mode?");
        bottomPanel.add(clustersModeButton);
        
        JPanel clustersPanel = new JPanel(new BorderLayout());
        clustersPanel.setBorder(BorderFactory.createEmptyBorder(5, 40, 5, 40));
        
        JList<String> clusterList = new JList<>();
        JList<String> pageList = new JList<>();
        
        clustersPanel.add(new JScrollPane(clusterList), BorderLayout.WEST);
        clustersPanel.add(new JScrollPane(pageList), BorderLayout.CENTER);
        
        clusterList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int index = clusterList.getSelectedIndex();
            if (index < 0) {
                return;
            }
            
            KMedoids.SimilarityTracker tracker = kMedoids.getK_MEDOIDS_TRACKER().get(index);
            List<String> names = new ArrayList<>();

            // An almost schizophrenic disaster where I tried to make it look pretty. I don't care enough now.
            // TODO: Finish this shit.
            // List<SimilarityTracker> nicelyFormattedList
            // for (SimilarityTracker cluster : kMedoids.getK_MEDOIDS_TRACKER()) {
                // SimilarityTracker newSimilarityTracker = new SimilarityTracker(cluster.medoid(), cluster.mostSimilarPages().stream()
                // .filter(similarObject -> similarObject.distance() < .95 && !similarObject.distance()
                // .equals(0.0))
                // .limit(10)
                // Probably for each one, add it to a new list and then merge that into the SimilarityTracker. I just don't care enough rn.
            // }
            // I can kind of deal with this with just like if statements and the List sort function, though. Good enough.
            List<KMedoids.SimilarityObject> similarPages = new ArrayList<>(tracker.mostSimilarPages());
            similarPages.sort((firstPage, secondPage) -> Double.compare(firstPage.distance(), secondPage.distance()));
            
            for (KMedoids.SimilarityObject member : tracker.mostSimilarPages()) {
                if (!member.closeObject().equals(tracker.medoid())) {
                    if (member.distance() < .95 && !member.distance().equals(0.0)) {
                        // TODO: Strip the underscores to make it a little prettier.
                        names.add(String.format("%s, Distance: %.4f", member.closeObject().getPageTopic(), member.distance()));
                    }
                }
            }
            pageList.setListData(names.toArray(new String[0]));
        });
        clustersModeButton.addActionListener(e -> {
            if (clustersPanel.getParent() == null) {
                List<SimilarityTracker> trackers = kMedoids.getK_MEDOIDS_TRACKER();
                String[] clusterNames = new String[trackers.size()];
                for (int i = 0; i < trackers.size(); i++) {
                    clusterNames[i] = "Cluster " + (i + 1) + ": " + trackers.get(i).medoid().getPageTopic();
                }
                clusterList.setListData(clusterNames);
                frame.remove(controlsPanel);
                frame.add(clustersPanel, BorderLayout.CENTER);
            }
            frame.repaint();
        });


        calculateButton.addActionListener(
        (e) -> {
            WebPage selectedWebPage = pages.get(page1Dropdown.getSelectedIndex());
            // This is quite disgusting. Oh well!
            // TODO: Refactor, so that its notably easier to actually READ.
            double firstMostSimilarScore = -1.0;
            WebPage firstMostSimilarSite = null;
            double secondMostSimilarScore = -1.0;
            WebPage secondMostSimilarSite = null;
            for (WebPage currentWebPage : pages) {
                if (!currentWebPage.equals(selectedWebPage)) {
                    final double similarityMeasure = controller.calculateCosineSimilarity(currentWebPage, selectedWebPage);
                    if (similarityMeasure > firstMostSimilarScore)  {
                        firstMostSimilarScore = similarityMeasure;
                        firstMostSimilarSite = currentWebPage;
                    } else if (similarityMeasure > secondMostSimilarScore) {
                        secondMostSimilarScore = similarityMeasure;
                        secondMostSimilarSite = currentWebPage;
                    }
                }
            }

            if (firstMostSimilarSite == null || secondMostSimilarSite == null) {
                System.out.println("For some reason, one of the websites was null!");
                return;
            }

            // TODO: Fix this so that it doesn't look so disgusting!
            Pattern endOfURLPattern = Pattern.compile("[^/]+$");
            Matcher topicOneMatcher = endOfURLPattern.matcher(firstMostSimilarSite.getWebPageURL());
            Matcher topicTwoMatcher = endOfURLPattern.matcher(secondMostSimilarSite.getWebPageURL());     
            String topicOne = "";
            String topicTwo = "";
            if (topicOneMatcher.find()) {
                topicOne = topicOneMatcher.group();
            }

            if (topicTwoMatcher.find()) {
                topicTwo = topicTwoMatcher.group();
            }
            if ("".equals(topicOne) || "".equals(topicTwo)) {
                try {
                    throw new MalformedWikipediaURLException("No end of URL, malformed.");
                } catch (MalformedWikipediaURLException ex) {
                    System.getLogger(AppUI.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    return;
                }
            }
                resultsJLabel.setText(String.format("Similar topics: %s and %s.", topicOne.replaceAll("_", " "), topicTwo.replaceAll("_", " ")));
                resultsJLabel.setToolTipText(String.format("Similar topics: %s and %s.", topicOne.replaceAll("_", " "), topicTwo.replaceAll("_", " ")));
    }   
        );
        frame.setVisible(true);
    }
}