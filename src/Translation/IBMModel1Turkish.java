package Translation;

import Dictionary.*;

import java.util.ArrayList;

public class IBMModel1Turkish extends IBMModel {
    private AutomaticTranslationDictionary dictionary;

    @Override
    public ArrayList<PartialTranslation> translate(PartialTranslation current, int index) {
        ArrayList<PartialTranslation> translations;
        WordTranslations wordTranslations;
        wordTranslations = (WordTranslations) dictionary.getWord(current.getFromSentence().getWord(index).getName().toLowerCase());
        translations = new ArrayList<PartialTranslation>();
        if (wordTranslations == null){
            translations.add(current.translateWord(index, new Word(current.getFromSentence().getWord(index).getName().toLowerCase()), 0));
        } else {
            for (TargetPhrase targetPhrase : wordTranslations.getTranslations()){
                WordTranslationCandidate wordTranslationCandidate = (WordTranslationCandidate) targetPhrase;
                translations.add(current.translateWord(index, new Word(wordTranslationCandidate.getTranslation()), Math.log(wordTranslations.getProbability(wordTranslationCandidate))));
            }
        }
        return translations;
    }

    @Override
    public void loadModel(String modelFile) {
        dictionary = new AutomaticTranslationDictionary(modelFile, new EnglishWordComparator());
    }
}
