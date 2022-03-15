package edu.missouriwestern.agrant4;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is a program that looks for text files on a given webpage then compiles a list of words in
 * each file and their frequency of use.
 *
 * @since March 2022
 * @author Aaron Grant
 */
public class App {

    //Create logger instance
    public final static Logger LOG = LogManager.getLogger(App.class);

    public static void main( String[] args ) {
        LOG.info("Program started");
        LOG.trace("Inside main function");
        String url = "";
        //look for webpage in command line args
        try {
            url = args[0];
        } catch (Exception e) {
            LOG.error("Error reading url from command line: " + e.getMessage());
            System.err.println("Error reading url from command line: " + e.getMessage());
            System.exit(1);
        }
        //Gets a list of URLs from website in command line argument
        ArrayList<String> urls = getUrls(url);
        LOG.info("There are " + urls.size() + " urls in the list.");
        TreeMap<String, Integer> wordFrequency = getWordFrequency(urls);
        LOG.info("There are " + wordFrequency.size() + " words in the TreeMap.");
        printMap(wordFrequency);
        LOG.info("Map has been printed.");
        LOG.info("Exiting Program");
    }


    /**
     * This is a method for printing the contents of a generic map.
     *
     * @param map Map: a generic map
     */
    public static void printMap(Map map) {
        LOG.trace("Inside function printMap");
        System.out.printf("\n========There are %d items in the map========\n", map.size());
        for (var key : map.keySet()) {
            var value = map.get(key);
            System.out.printf("[%s]\t%s\n", key, value);
        }
    }

    /**
     * A method for scraping words from a set of online text files given an
     * ArrayList of urls of those files. Returns a TreeMap with all words and their
     * frequencies
     *
     * @param urls ArrayList<String>: a list of text-file urls
     */
    public static TreeMap<String, Integer> getWordFrequency(ArrayList<String> urls) {
        LOG.trace("Inside function getWordsFrequency");
        TreeMap<String, Integer> wordFrequency = new TreeMap<>();
        for(String url : urls) {
            scrapeWords(url, wordFrequency);
        }
        return wordFrequency;

    }

    /**
     * A method for scraping words from a single online text file
     *
     * @param address String: a text-file url
     * @param wordFrequency TreeMap<String, Integer>: a TreeMap of words and their frequencies
     * @return void: this modifies the TreeMap in place
     */
    public static void scrapeWords(String address, TreeMap<String, Integer> wordFrequency) {
        LOG.trace("Inside function scrapeWords");
        try {
            URL url = new URL(address);
            InputStreamReader inStream = new InputStreamReader(url.openStream());
            BufferedReader input = new BufferedReader(inStream);
            LOG.trace("Connected to " + address);

            /*
             * This loop reads through the page looking for words. If the word isn't
             * blank, it increases the frequency of that word in the map by 1. The
             * filter is case insensitive.
             */
            String line;
            while ((line = input.readLine()) != null) {
                //Splits line into array of Strings. Matches based on word character
                String[] words = line.split("\\W");
                LOG.debug("Adding line " + Arrays.toString(words) + " to TreeMap");

                for(String word : words) {
                    //Converts word to lowercase, as we want to be case insensitive
                    String lowerCaseWord = word.toLowerCase();
                    if (lowerCaseWord.length() > 0) {
                        if(wordFrequency.containsKey(word)) {
                            wordFrequency.put(lowerCaseWord, wordFrequency.get(lowerCaseWord) + 1);
                        } else {
                            wordFrequency.put(lowerCaseWord, 1);
                        }
                    }
                }
                LOG.debug("Added line. There are now " + wordFrequency.size() + " words in the map.");
            }

            input.close();
        } catch (Exception e) {
            LOG.error(e.getMessage());
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }


    /**
     * A method for finding all the text-file urls on a given webpage.
     *
     * @param address String: a url to connect to
     * @return ArrayList<String>: returns an ArrayList of urls
     */
    public static ArrayList<String> getUrls(String address) {
        LOG.trace("Inside function getUrls");
        ArrayList<String> urls = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(address).get();
            LOG.info("Connected to " + address);
            //selects all elements with an "a" tag
            Elements elements = doc.select("a");
            for (Element element: elements) {
                //Separate out the href attribute from the "a" tag
                String elementHref = element.attr("href");
                if (elementHref.endsWith(".txt")) {
                    urls.add(address + elementHref);
                    LOG.debug("Added url: " + address + elementHref);
                } else{
                    LOG.debug("Did not create url from href: " + elementHref);
                }
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        return urls;
    }
}
