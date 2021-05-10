package Translation.Phrase;

import AnnotatedSentence.ViewLayerType;
import AnnotatedTree.ParseNodeDrawable;
import AnnotatedTree.ParseTreeDrawable;
import AnnotatedTree.Processor.Condition.IsLeafNode;
import AnnotatedTree.Processor.NodeDrawableCollector;
import AnnotatedTree.TreeBankDrawable;
import Dictionary.Dictionary;
import Dictionary.EnglishWordComparator;
import Dictionary.Word;
import Dictionary.WordComparator;
import ParseTree.ParseTree;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
            DocumentBuilder builder = null;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            Document doc = null;
            try {
                ClassLoader classLoader = getClass().getClassLoader();
                doc = builder.parse(new InputSource(classLoader.getResourceAsStream(filename)));
            } catch (SAXException | IOException e) {
                e.printStackTrace();
            }
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

    public AutomaticTranslationDictionary(ParseTreeDrawable parseTreeDrawable, ViewLayerType fromLayer, ViewLayerType toLayer){
        this(new EnglishWordComparator());
        addTranslations(parseTreeDrawable, fromLayer, toLayer);
    }

    public AutomaticTranslationDictionary(TreeBankDrawable treeBankDrawable, ViewLayerType fromLayer, ViewLayerType toLayer, String fileName){
        boolean firstTree = true;
        AutomaticTranslationDictionary dictionary = null, tmpDictionary;
        for (ParseTree tree:treeBankDrawable.getParseTrees()){
            ParseTreeDrawable parseTree = (ParseTreeDrawable) tree;
            tmpDictionary = new AutomaticTranslationDictionary(parseTree,fromLayer, toLayer);
            if (firstTree){
                dictionary = tmpDictionary;
                firstTree = false;
            } else {
                dictionary.mergeWith(tmpDictionary);
            }
        }
        if (dictionary != null) {
            dictionary.saveAsXml(fileName);
        }
    }

    public void addTranslations(ParseTreeDrawable parseTreeDrawable, ViewLayerType fromLayer, ViewLayerType toLayer){
        NodeDrawableCollector nodeDrawableCollector = new NodeDrawableCollector((ParseNodeDrawable)parseTreeDrawable.getRoot(), new IsLeafNode());
        ArrayList<ParseNodeDrawable> leafList = nodeDrawableCollector.collect();
        for (ParseNodeDrawable leafNode: leafList){
            if (leafNode.getLayerData(fromLayer) != null && leafNode.getLayerData(toLayer) != null){
                if (leafNode.getLayerData(fromLayer).equals("*NONE*"))
                    addWord(new Word(leafNode.getLayerInfo().getRobustLayerData(fromLayer)), new Word(leafNode.getLayerInfo().getRobustLayerData(toLayer)));
                else
                    addWord(new Word(leafNode.getLayerInfo().getRobustLayerData(fromLayer).toLowerCase()), new Word(leafNode.getLayerInfo().getRobustLayerData(toLayer)));
            }
        }
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
