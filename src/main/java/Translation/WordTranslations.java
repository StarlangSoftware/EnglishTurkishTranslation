package Translation;

import Dictionary.Word;

import java.util.Collections;

public class WordTranslations extends SourceWord {

    private int totalCount = 0;

    /**
     * Constructor for {@link WordTranslations} class. Gets the translation string as input and calls its super class.
     * @param name Translation string.
     */
    public WordTranslations(String name){
        super(name);
        totalCount = 0;
    }

    /**
     * Removes all matching parantheses from the given input word.
     * @param word Input text.
     * @return The word with all matching paramtheses removed.
     */
    private String removeParentheses(String word){
        while (word.contains("(") && word.contains(")")){
            int start = word.indexOf("(");
            int end = word.indexOf(")");
            word = word.substring(0, start) + word.substring(end + 1);
        }
        return word;
    }

    /**
     * Another constructor for {@link WordTranslations} class. Gets the sourceWord as input, calls its superclass with
     * it. Afterwards, for each translation in that word, it adds that translation to the current words translations
     * list.
     * @param word Copied source word.
     */
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

    /**
     * Another construfor {@link WordTranslations} class. Gets the sourceWord as input, calls its superclass with
     * it. Afterwards, for each translation in that word, it adds that translation to the current words translations
     * list, if it has the same pos with the input.
     * @param word Copied source word.
     * @param pos Part of speech tag input.
     */
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

    /**
     * Adds a totally new translation to the translation list with the given amount.
     * @param translation Translation string.
     * @param count Number of occurrences of that translation.
     */
    public void addTranslation(Word translation, int count){
        translations.add(new WordTranslationCandidate(translation.getName(), count));
        totalCount += count;
    }

    /**
     * Adds a new translation to the translation list. If that translation already occurs in the translation list, it
     * only increments the count. Otherwise, it adds as a new translation with count 1.
     * @param translation Translation string.
     */
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

    /**
     * Returns the probability of a given translation.
     * @param translation Translation.
     * @return The probability of the given translation.
     */
    public double getProbability(WordTranslationCandidate translation){
        return translation.getCount() / (totalCount + 0.0);
    }

    /**
     * Sorts all translations of the current word in the increasing order of number of occurrences of the translations.
     */
    public void sortTranslations(){
        for (int i = 0; i < translations.size(); i++)
            for (int j = 0; j < translations.size(); j++){
                if (((WordTranslationCandidate) translations.get(i)).getCount() > ((WordTranslationCandidate) translations.get(j)).getCount()){
                    Collections.swap(translations, i, j);
                }
            }
    }

    /**
     * Merges the translation list of a given word with the current word's translation list.
     * The translations are checked; if the translations are same; the counts are added, otherwise the counts and
     * translation are directly merged.
     * @param word Word to be merged.
     */
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

    /**
     * Converts the current object to Xml form.
     * @return Xml form of the current object.
     */
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
