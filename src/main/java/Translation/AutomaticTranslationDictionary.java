package Translation;

import Dictionary.*;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

public class AutomaticTranslationDictionary extends Dictionary {

    /**
     * Constructor for the {@link AutomaticTranslationDictionary} class. Gets word comparator as input and calls its
     * super class {@link Dictionary} with it.
     * @param comparator Comparator to compare words in the second language
     */
    public AutomaticTranslationDictionary(WordComparator comparator){
        super(comparator);
    }

    private class ReadDictionaryTask extends SwingWorker{

        public ReadDictionaryTask(){
        }

        protected Object doInBackground() throws Exception {
            NamedNodeMap attributes;
            String wordName, translation;
            int count, parsedCount, totalCount;
            Node wordNode, rootNode, translationNode;
            DOMParser parser = new DOMParser();
            Document doc;
            try {
                ClassLoader classLoader = getClass().getClassLoader();
                parser.parse(new InputSource(classLoader.getResourceAsStream(filename)));
            } catch (SAXException e) {
                e.printStackTrace();
            }
            doc = parser.getDocument();
            rootNode = doc.getFirstChild();
            wordNode = rootNode.getFirstChild();
            parsedCount = 0;
            totalCount = rootNode.getChildNodes().getLength();
            while (wordNode != null){
                if (wordNode.hasAttributes()){
                    attributes = wordNode.getAttributes();
                    wordName = attributes.getNamedItem("name").getNodeValue();
                    WordTranslations WordTranslations = new WordTranslations(wordName);
                    translationNode = wordNode.getFirstChild();
                    while (translationNode != null){
                        if (translationNode.hasAttributes()){
                            attributes = translationNode.getAttributes();
                            translation = attributes.getNamedItem("name").getNodeValue();
                            count = Integer.parseInt(attributes.getNamedItem("count").getNodeValue());
                            WordTranslations.addTranslation(new Word(translation), count);
                        }
                        translationNode = translationNode.getNextSibling();
                    }
                    words.add(WordTranslations);
                }
                parsedCount++;
                setProgress((100 * parsedCount) / totalCount);
                wordNode = wordNode.getNextSibling();
            }
            return 0;
        }
    }

    /**
     * Another Constructor for the {@link AutomaticTranslationDictionary} class. Gets the translation file and word
     * comparator as input; calls its super class {@link Dictionary} with comparator and reads the translation
     * dictionary
     * @param fileName Name of the file containing the translation dictionary
     * @param comparator Comparator to compare words in the second language
     */
    public AutomaticTranslationDictionary(final String fileName, WordComparator comparator){
        super(comparator);
        this.filename = fileName;
        ReadDictionaryTask task = new ReadDictionaryTask();
        task.execute();
        try {
            task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        words.sort(comparator);
    }

    /**
     * Another default Constructor for the {@link AutomaticTranslationDictionary} class. Calls the above constructor
     * with default translation file name "translation.xml"
     */
    public AutomaticTranslationDictionary(){
        this("translation.xml", new EnglishWordComparator());
    }

    /**
     * The method merges the current translation dictionary with the given second translation dictionary. Basically
     * 1. If a word in the second dictionary does not exist in the current dictionary, the translations of that word
     * are all imported.
     * 2. If a word in the second dictionary exists in the current dictionary, the translations are checked; if the
     * translations are same; the counts are added, otherwise the counts are directly imported.
     * @param secondDictionary Second translation dictionary.
     */
    public void mergeWith(Dictionary secondDictionary){
        int i, secondIndex;
        WordTranslations word, word2;
        for (i = 0; i < words.size(); i++){
            word = (WordTranslations) words.get(i);
            secondIndex = secondDictionary.getWordIndex(word.getName());
            if (secondIndex != -1){
                word2 = (WordTranslations) secondDictionary.getWord(secondIndex);
                word.mergeTranslations(word2);
            }
        }
        for (i = 0; i < secondDictionary.size(); i++){
            word = (WordTranslations) secondDictionary.getWord(i);
            if (getWord(word.getName()) == null){
                words.add(word);
                words.sort(comparator);
            }
        }
    }

    /**
     * Adds a new word with the given translation to the translation dictionary.
     * @param word The word to be translated.
     * @param translation The translation of the word.
     */
    public void addWord(Word word, Word translation){
        WordTranslations WordTranslations;
        if (getWord(word.getName()) != null){
            WordTranslations = (WordTranslations) getWord(word.getName());
        } else {
            WordTranslations = new WordTranslations(word.getName());
            words.add(WordTranslations);
            words.sort(comparator);
        }
        WordTranslations.addTranslation(translation);
    }

    /**
     * Saves the translation dictionary as an xml file.
     * @param outputFileName Name of the xml output file.
     */
    public void saveAsXml(String outputFileName){
        int i;
        BufferedWriter outfile;
        try {
            outfile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName), StandardCharsets.UTF_8));
            outfile.write("<lexicon>\n");
            for (i = 0; i < words.size(); i++){
                outfile.write(((WordTranslations) words.get(i)).toXml());
            }
            outfile.write("</lexicon>\n");
            outfile.close();
        } catch (IOException e) {
            System.out.println("Output file can not be opened");
        }
    }

}
