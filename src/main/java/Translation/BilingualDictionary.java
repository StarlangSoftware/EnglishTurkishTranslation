package Translation;

import Dictionary.*;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class BilingualDictionary extends Dictionary {

    private class ReadDictionaryTask extends SwingWorker{

        protected Object doInBackground() throws Exception {
            NamedNodeMap attributes;
            String wordName, lexicalClass, meaningClass;
            Node wordNode, rootNode, lexicalNode, translationNode;
            DocumentBuilder builder = null;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            Document doc = null;
            TargetPhrase targetPhrase;
            int parsedCount, totalCount;
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
                    SourceWord sourceWord = new SourceWord(wordName);
                    lexicalNode = wordNode.getFirstChild();
                    while (lexicalNode != null){
                        if (lexicalNode.getNodeName().equalsIgnoreCase("lexical")){
                            attributes = lexicalNode.getAttributes();
                            lexicalClass = attributes.getNamedItem("class").getNodeValue();
                            translationNode = lexicalNode.getFirstChild();
                            while (translationNode != null){
                                if (translationNode.getNodeName().equalsIgnoreCase("meaning")){
                                    if (translationNode.hasAttributes()){
                                        meaningClass = translationNode.getAttributes().getNamedItem("class").getNodeValue();
                                        targetPhrase = new TargetPhrase(lexicalClass, new WordMeaning(meaningClass, translationNode.getFirstChild().getNodeValue()));
                                        sourceWord.addTranslation(targetPhrase);
                                    } else {
                                        targetPhrase = new TargetPhrase(lexicalClass, new WordMeaning(translationNode.getFirstChild().getNodeValue()));
                                        sourceWord.addTranslation(targetPhrase);
                                    }
                                }
                                translationNode = translationNode.getNextSibling();
                            }
                        }  else {
                            if (lexicalNode.getNodeName().equalsIgnoreCase("meaning")){
                                if (lexicalNode.hasAttributes()){
                                    meaningClass = lexicalNode.getAttributes().getNamedItem("class").getNodeValue();
                                    targetPhrase = new TargetPhrase(new WordMeaning(meaningClass, lexicalNode.getFirstChild().getNodeValue()));
                                    sourceWord.addTranslation(targetPhrase);
                                } else {
                                    targetPhrase = new TargetPhrase(new WordMeaning(lexicalNode.getFirstChild().getNodeValue()));
                                    sourceWord.addTranslation(targetPhrase);
                                }
                            }
                        }
                        lexicalNode = lexicalNode.getNextSibling();
                    }
                    words.add(sourceWord);
                }
                parsedCount++;
                setProgress((100 * parsedCount) / totalCount);
                wordNode = wordNode.getNextSibling();
            }
            return 0;
        }
    }

    /**
     * Constructor for the {@link BilingualDictionary} class. Gets the translation file and word
     * comparator as input; calls its super class {@link Dictionary} with comparator and reads the bilingual
     * dictionary
     * @param fileName Name of the file containing the bilingual dictionary
     * @param comparator Comparator to compare words in the second language
     */
    public BilingualDictionary(final String fileName, WordComparator comparator){
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
     * Another default Constructor for the {@link BilingualDictionary} class. Calls the above constructor
     * with default translation file name "english-turkish.xml"
     */
    public BilingualDictionary(){
        this("english-turkish.xml", new EnglishWordComparator());
    }

    /**
     * Gets all translation of a source word, which is in the third person form. The method removes the "ing" morpheme
     * from the current word to get the root form of the word and gets the translations from the bilingual dictionary
     * for that root word.
     * @param word Lemma form containing the "ing" morpheme.
     * @return The set of translations of the lemma.
     */
    public WordTranslations inThirdPersonForm(String word){
        SourceWord sourceWord;
        if (word.endsWith("s")){
            sourceWord = (SourceWord) getWord(word.substring(0, word.length() - 1));
            if (sourceWord != null){
                return new WordTranslations(sourceWord, "fiil");
            }
        }
        return null;
    }

    /**
     * Gets all translation of a source word, which is in the plural form. The method removes the "s" or "es" morpheme
     * from the current word to get the root form of the word and gets the translations from the bilingual dictionary
     * for that root word.
     * @param word Lemma form containing the "s" or "es" morpheme.
     * @return The set of translations of the lemma.
     */
    public WordTranslations inPluralForm(String word){
        SourceWord sourceWord;
        if (word.endsWith("es")){
            sourceWord = (SourceWord) getWord(word.substring(0, word.length() - 2));
            if (sourceWord != null){
                return new WordTranslations(sourceWord, "isim");
            }
        }
        if (word.endsWith("s")){
            sourceWord = (SourceWord) getWord(word.substring(0, word.length() - 1));
            if (sourceWord != null){
                return new WordTranslations(sourceWord, "isim");
            }
        }
        return null;
    }

    /**
     * Gets all translation of a source word, which is in the past form. The method removes the "d" or "ed" morpheme
     * from the current word to get the root form of the word and gets the translations from the bilingual dictionary
     * for that root word.
     * @param word Lemma form containing the "d" or "ed" morpheme.
     * @return The set of translations of the lemma.
     */
    public WordTranslations inPastForm(String word){
        SourceWord sourceWord;
        if (word.endsWith("ed")){
            sourceWord = (SourceWord) getWord(word.substring(0, word.length() - 2));
            if (sourceWord != null){
                return new WordTranslations(sourceWord, "fiil");
            }
            sourceWord = (SourceWord) getWord(word.substring(0, word.length() - 3));
            if (sourceWord != null){
                return new WordTranslations(sourceWord, "fiil");
            }
        }
        if (word.endsWith("d")){
            sourceWord = (SourceWord) getWord(word.substring(0, word.length() - 1));
            if (sourceWord != null){
                return new WordTranslations(sourceWord, "fiil");
            }
        }
        return null;
    }

    /**
     * Gets all translation of a source word, which is in the "ing" form. The method removes the "ing" morpheme
     * from the current word to get the root form of the word and gets the translations from the bilingual dictionary
     * for that root word.
     * @param word Lemma form containing the "ing" morpheme.
     * @return The set of translations of the lemma.
     */
    public WordTranslations inIngForm(String word){
        SourceWord sourceWord;
        if (word.endsWith("ing")){
            sourceWord = (SourceWord) getWord(word.substring(0, word.length() - 3));
            if (sourceWord != null){
                return new WordTranslations(sourceWord, "fiil");
            }
            sourceWord = (SourceWord) getWord(word.substring(0, word.length() - 3) + "e");
            if (sourceWord != null){
                return new WordTranslations(sourceWord, "fiil");
            }
        }
        return null;
    }

}
