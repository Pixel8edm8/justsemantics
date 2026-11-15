package com.theethancorporation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.swing.SwingUtilities;

import com.theethancorporation.util.SerialUtils;
/**
 * Hello world!
 * The starter for the whole app.
 */
public class App 
{
    private static AppUI ui;
    public static void main( String[] args ) throws IOException
    {
        // Caveat: This only works on MY machine, because of the absolute pathing. Fix this!
        // FIXME: Fix this!
        Path resourcesPath = Path.of("C:\\", "Users", "fiver", "OneDrive", "Desktop", "justsemantics", "src", "main", "java", "resources");
        System.out.println(resourcesPath.toString());
        try {
            final PageController currentPageController = new PageController(resourcesPath, 200);
            final List<WebPage> testArrayList = SerialUtils.returnArrayListOfPages();
            for (WebPage readWebPage : testArrayList) {
                System.out.println(readWebPage.getPageTopic());
            }
            final var newKMedoids = new KMedoids(10, 100);
            // Tried putting it in the actual UI. yikes!!
            newKMedoids.runKMedoids();
            SwingUtilities.invokeLater(
                () -> {
                    AppUI ui = new AppUI(currentPageController, newKMedoids);
                }
            );
        } catch (FileNotFoundException e) {
            System.out.println("Page controller could not find control file.");
        }
        
    }
}