package Translation.Phrase;

import Dictionary.*;
import Xml.XmlDocument;
import Xml.XmlElement;

public class BilingualDictionary extends Dictionary {

    private void readDictionary() {
        String wordName, lexicalClass, meaningClass;
        XmlElement wordNode, rootNode, lexicalNode, translationNode;
        ClassLoader classLoader = getClass().getClassLoader();
        XmlDocument doc = new XmlDocument(classLoader.getResourceAsStream(fileName));
        doc.parse();
        TargetPhrase targetPhrase;
        rootNode = doc.getFirstChild();
        wordNode = rootNode.getFirstChild();
        while (wordNode != null){
            if (wordNode.hasAttributes()){
                wordName = wordNode.getAttributeValue("name");
                SourceWord sourceWord = new SourceWord(wordName);
                lexicalNode = wordNode.getFirstChild();
                while (lexicalNode != null){
                    if (lexicalNode.getName().equalsIgnoreCase("lexical")){
                        lexicalClass = lexicalNode.getAttributeValue("class");
                        translationNode = lexicalNode.getFirstChild();
                        while (translationNode != null){
                            if (translationNode.getName().equalsIgnoreCase("meaning")){
                                if (translationNode.hasAttributes()){
                                    meaningClass = translationNode.getAttributeValue("class");
                                    targetPhrase = new TargetPhrase(lexicalClass, new WordMeaning(meaningClass, translationNode.getPcData()));
                                    sourceWord.addTranslation(targetPhrase);
                                } else {
                                    targetPhrase = new TargetPhrase(lexicalClass, new WordMeaning(translationNode.getPcData()));
                                    sourceWord.addTranslation(targetPhrase);
                                }
                            }
                            translationNode = translationNode.getNextSibling();
                        }
                    }  else {
                        if (lexicalNode.getName().equalsIgnoreCase("meaning")){
                            if (lexicalNode.hasAttributes()){
                                meaningClass = lexicalNode.getAttributeValue("class");
                                targetPhrase = new TargetPhrase(new WordMeaning(meaningClass, lexicalNode.getPcData()));
                                sourceWord.addTranslation(targetPhrase);
                            } else {
                                targetPhrase = new TargetPhrase(new WordMeaning(lexicalNode.getPcData()));
                                sourceWord.addTranslation(targetPhrase);
                            }
                        }
                    }
                    lexicalNode = lexicalNode.getNextSibling();
                }
                words.add(sourceWord);
            }
            wordNode = wordNode.getNextSibling();
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
        this.fileName = fileName;
        readDictionary();
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
