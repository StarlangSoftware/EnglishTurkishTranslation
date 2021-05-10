package Translation.Phrase;

import Dictionary.Word;

import java.util.ArrayList;

public class SourceWord extends Word {

    protected ArrayList<TargetPhrase> translations;

    /**
     * Constructor for {@link SourceWord}. The method gets lemma of the word as input and calls its super. It also
     * initializes the possible translations of the word.
     * @param name Lemma of the word.
     */
    public SourceWord(String name){
        super(name);
        translations = new ArrayList<TargetPhrase>();
    }

    /**
     * Returns all possible translations for the word
     * @return An ArrayList of all possible translations of the word
     */
    public ArrayList<TargetPhrase> getTranslations(){
        return translations;
    }

    /**
     * Adds another possible translation to the all possible translations array
     * @param translation New translation of the current word
     */
    public void addTranslation(TargetPhrase translation){
        translations.add(translation);
    }

    /**
     * Returns number of possible translations.
     * @return Number of translations.
     */
    public int translationCount(){
        return translations.size();
    }

    /**
     * Returns the i'th possible translation for the current word.
     * @param index Index of the translation
     * @return i'th possible translation for the current word.
     */
    public TargetPhrase getTranslation(int index){
        return translations.get(index);
    }

}
