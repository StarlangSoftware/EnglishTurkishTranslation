package Translation;

import Dictionary.Word;

import java.util.Collections;

public class WordTranslations extends SourceWord {

    private int totalCount = 0;

    public WordTranslations(String name){
        super(name);
        totalCount = 0;
    }

    private String removeParentheses(String word){
        while (word.contains("(") && word.contains(")")){
            int start = word.indexOf("(");
            int end = word.indexOf(")");
            word = word.substring(0, start) + word.substring(end + 1);
        }
        return word;
    }

    public WordTranslations(SourceWord word){
        super(word.getName());
        for (TargetPhrase targetPhrase : word.getTranslations()){
            if (!targetPhrase.getTranslation().contains("(") && !targetPhrase.getTranslation().contains(")")){
                addTranslation(new Word(targetPhrase.getTranslation().trim()), 1);
            } else {
                addTranslation(new Word(removeParentheses(targetPhrase.getTranslation()).trim()), 1);
            }
        }
    }

    public WordTranslations(SourceWord word, String pos){
        super(word.getName());
        for (TargetPhrase targetPhrase : word.getTranslations()){
            if (targetPhrase.getLexicalClass() != null && targetPhrase.getLexicalClass().equals(pos)){
                if (!targetPhrase.getTranslation().contains("(") && !targetPhrase.getTranslation().contains(")")){
                    addTranslation(new Word(targetPhrase.getTranslation().trim()), 1);
                } else {
                    addTranslation(new Word(removeParentheses(targetPhrase.getTranslation()).trim()), 1);
                }
            }
        }
    }

    public void addTranslation(Word translation, int count){
        translations.add(new WordTranslationCandidate(translation.getName(), count));
        totalCount += count;
    }

    public void addTranslation(Word translation){
        boolean found = false;
        for (TargetPhrase targetPhrase :translations){
            WordTranslationCandidate wordTranslationCandidate = (WordTranslationCandidate) targetPhrase;
            if (wordTranslationCandidate.getTranslation().equals(translation.getName())){
                wordTranslationCandidate.incrementCount();
                totalCount++;
                found = true;
                break;
            }
        }
        if (!found){
            translations.add(new WordTranslationCandidate(translation.getName()));
        }
    }

    public double getProbability(WordTranslationCandidate translation){
        return translation.getCount() / (totalCount + 0.0);
    }

    public void sortTranslations(){
        for (int i = 0; i < translations.size(); i++)
            for (int j = 0; j < translations.size(); j++){
                if (((WordTranslationCandidate) translations.get(i)).getCount() > ((WordTranslationCandidate) translations.get(j)).getCount()){
                    Collections.swap(translations, i, j);
                }
            }
    }

    public void mergeTranslations(WordTranslations word){
        boolean found;
        for (TargetPhrase targetPhrase:word.getTranslations()){
            WordTranslationCandidate translation = (WordTranslationCandidate) targetPhrase;
            found = false;
            for (TargetPhrase currentTargetPhrase:translations){
                WordTranslationCandidate currentTranslation = (WordTranslationCandidate) currentTargetPhrase;
                if (currentTranslation.getTranslation().equals(translation.getTranslation())){
                    currentTranslation.addCount(translation.getCount());
                    totalCount += translation.getCount();
                    found = true;
                    break;
                }
            }
            if (!found){
                translations.add(translation);
            }
        }
    }

    public String toXml(){
        sortTranslations();
        String result = "\t<word name=\"" + name.replaceAll("&", "&amp;") + "\">\n";
        for (TargetPhrase targetPhrase:translations){
            WordTranslationCandidate translation = (WordTranslationCandidate) targetPhrase;
            result = result + translation.toXml();
        }
        result = result + "\t</word>\n";
        return result;
    }

}
