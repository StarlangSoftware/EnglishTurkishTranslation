package Translation;

import Dictionary.*;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class BilingualDictionary extends Dictionary {

    private class ReadDictionaryTask extends SwingWorker{

        protected Object doInBackground() throws Exception {
            NamedNodeMap attributes;
            String wordName, lexicalClass, meaningClass;
            Node wordNode, rootNode, lexicalNode, translationNode;
            DOMParser parser = new DOMParser();
            TargetPhrase targetPhrase;
            Document doc;
            int parsedCount, totalCount;
            try {
                ClassLoader classLoader = getClass().getClassLoader();
                parser.parse(new InputSource(classLoader.getResourceAsStream(filename)));
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
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

    public BilingualDictionary(final String fileName, WordComparator comparator){
        super(comparator);
        this.filename = fileName;
        ReadDictionaryTask task = new ReadDictionaryTask();
        task.execute();
        try {
            task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Collections.sort(words, comparator);
    }

    public BilingualDictionary(){
        this("english-turkish.xml", new EnglishWordComparator());
    }

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
