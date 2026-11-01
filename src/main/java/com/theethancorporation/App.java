package com.theethancorporation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.SwingUtilities;

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
            SwingUtilities.invokeLater(
                () -> {
                    AppUI ui = new AppUI(currentPageController);
                }
            );
        } catch (FileNotFoundException e) {
            System.out.println("Page controller could not find control file.");
        }
        
    }
}