package com.theethancorporation;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.theethancorporation.util.MalformedWikipediaURL;

public class AppUI {
    private JFrame frame = null;
    final private JComboBox<String> page1Dropdown;

    /**
     * Exactly what you think it is.
     * @param controller The pageController that has 
     * @param pages
     */
    public AppUI(final PageController controller) {
        final ArrayList<WebPage> pages = controller.getListOfPages();
        frame = new JFrame("Semantics Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setResizable(false);
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

        resultsJLabel.setFont(new Font("Coutier New", Font.PLAIN, 12));
        
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
                    throw new MalformedWikipediaURL("No end of URL, malformed.");
                } catch (MalformedWikipediaURL ex) {
                    System.getLogger(AppUI.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    return;
                }
            }
                resultsJLabel.setText(String.format("Similar topics: %s and %s.", topicOne, topicTwo));
                resultsJLabel.setToolTipText(String.format("Similar topics: %s and %s.", topicOne, topicTwo));
    }   
        );
        frame.setVisible(true);
    }
}