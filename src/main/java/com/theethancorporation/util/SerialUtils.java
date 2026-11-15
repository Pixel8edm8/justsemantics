package com.theethancorporation.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.theethancorporation.PageController;
import com.theethancorporation.WebPage;

public class SerialUtils {
    private final static Path SERIAL_PATH = Path.of(PageController.getResourcesFilePath().toAbsolutePath().toString(), "serializedPages"); 
    public static List<WebPage> returnArrayListOfPages() {
        final List<WebPage> webPages = new ArrayList<>();
        // For each page in the resources/serializedPages, read it in, and then add it to the array.
        for (File serializedPage : SERIAL_PATH.toFile().listFiles()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(serializedPage))) {
                WebPage readWebPage = (WebPage) ois.readObject();
                webPages.add(readWebPage);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("For some reason, there was an IOException when attempting to read an object. That's all we know.");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println("We couldn't find the class in the serialized data. That's all we know.");
            }
        }
    return webPages;
    } 
}
