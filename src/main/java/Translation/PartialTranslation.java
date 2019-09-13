package Translation;

import Corpus.Sentence;
import Dictionary.TxtDictionary;
import Dictionary.TxtWord;
import Dictionary.Word;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import MorphologicalAnalysis.State;
import MorphologicalAnalysis.Transition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class PartialTranslation {
    private Sentence sourceSentence;
    private boolean[] processed;
    private Sentence targetSentence;
    private double logProbability;
    private int processedCount = 0;

    /**
     * Empty constructor for {@link PartialTranslation} class.
     */
    public PartialTranslation(){
    }

    /**
     * Constructor for {@link PartialTranslation} class. Gets the source sentence as input, sets the corresponding
     * attribute, initializes the target sentence and processed array.
     * @param sourceSentence Source sentence input.
     */
    public PartialTranslation(Sentence sourceSentence){
        this.sourceSentence = sourceSentence;
        targetSentence = new Sentence();
        processed = new boolean[sourceSentence.wordCount()];
        logProbability = 0;
        processedCount = 0;
    }

    /**
     * Return current translation of the source sentence.
     * @return Current translation of the course sentence.
     */
    public Sentence getTranslation(){
        return targetSentence;
    }

    /**
     * Equality check method for {@link PartialTranslation} class. Checks if the current translation is equal to the
     * given partial translation.
     * @param p Second partial translation to compare.
     * @return True if both translations are equal, false otherwise.
     */
    public boolean equals(PartialTranslation p){
        if (!targetSentence.equals(p.targetSentence))
            return false;
        for (int i = 0; i < processed.length; i++)
            if (processed[i] != p.processed[i])
                return false;
        return true;
    }

    /**
     * Extracts all morphosyntactic tokens as a list of strings. For each word in the target sentence, the method splits
     * the word into tokens by the separator + and space.
     * @return All morphosyntactic tokens as a list of strings.
     */
    private ArrayList<String> getMorphosyntacticTokenList(){
        ArrayList<String> tokenList = new ArrayList<String>();
        for (Word word: targetSentence.getWords()){
            if (!word.getName().equalsIgnoreCase("*NONE*")){
                String[] tokens = word.getName().split("[\\s\\+]");
                Collections.addAll(tokenList, tokens);
            }
        }
        return tokenList;
    }

    /**
     * Adds the given token to the dictionary. Since dictionary does not contain numbers, time, fraction expressions,
     * this method will add them to the dictionary while translation.
     * @param dictionary Current dictionary as a {@link TxtDictionary} class.
     * @param token Token to be added to the dictionary.
     */
    private void updateDictionary(TxtDictionary dictionary, String token){
        if (token.contains("\\/")){
            dictionary.addFraction(token);
        } else {
            if (token.contains(":")){
                dictionary.addTime(token);
            } else {
                try{
                    Double.parseDouble(token);
                    dictionary.addNumber(token);
                } catch (NumberFormatException nfe2){
                    dictionary.addProperNoun(token);
                }
            }
        }
    }

    /**
     * Checks if the current generated translation sentence is a valid translation as a metamorphic expression. The
     * translation is usually obtained by concatenating metamorphemes, and not all metamorphemes can come one after
     * another. This method checks the possibility for an ordered list of metamorphemes.
     * @param fsm Turkish morphological analyzer
     * @return True, if the current translation is a valid translation; false otherwise.
     */
    public boolean validTranslation(FsmMorphologicalAnalyzer fsm){
        TxtDictionary dictionary = fsm.getDictionary();
        String currentWord = "";
        TxtWord currentRoot = null;
        ArrayList<String> tokenList = getMorphosyntacticTokenList();
        for (String token:tokenList){
            if (!fsm.getFiniteStateMachine().isValidTransition(token)){
                if (!currentWord.isEmpty()){
                    if (!fsm.morphologicalAnalysisExists(currentRoot, currentWord)){
                        return false;
                    }
                }
                currentWord = token;
                currentRoot = (TxtWord) dictionary.getWord(token.toLowerCase(new Locale("tr")));
                if (currentRoot == null){
                    updateDictionary(dictionary, token);
                    currentRoot = (TxtWord) dictionary.getWord(token.toLowerCase(new Locale("tr")));
                }
            } else {
                if (currentRoot == null){
                    return false;
                }
                Transition transition = new Transition(token);
                currentWord = transition.makeTransition(currentRoot, currentWord);
            }
        }
        return true;
    }

    public boolean simplifyTranslation(FsmMorphologicalAnalyzer fsm){
        TxtDictionary dictionary = fsm.getDictionary();
        String currentWord = "";
        TxtWord currentRoot = null;
        ArrayList<String> tokenList = getMorphosyntacticTokenList();
        targetSentence = new Sentence();
        for (String token:tokenList){
            if (!fsm.getFiniteStateMachine().isValidTransition(token)){
                if (!currentWord.isEmpty()){
                    if (fsm.morphologicalAnalysisExists(currentRoot, currentWord)){
                        targetSentence.addWord(new Word(currentWord));
                    } else {
                        return false;
                    }
                }
                currentWord = token;
                currentRoot = (TxtWord) dictionary.getWord(token.toLowerCase(new Locale("tr")));
                if (currentRoot == null){
                    updateDictionary(dictionary, token);
                    currentRoot = (TxtWord) dictionary.getWord(token.toLowerCase(new Locale("tr")));
                }
            } else {
                if (currentRoot == null){
                    return false;
                }
                Transition transition = new Transition(token);
                if (currentRoot.isVerb()){
                    currentWord = transition.makeTransition(currentRoot, currentWord, new State("VerbalRoot", true, false));
                } else {
                    currentWord = transition.makeTransition(currentRoot, currentWord, new State("NominalRoot", true, false));
                }
            }
        }
        if (!currentWord.isEmpty()){
            if (fsm.morphologicalAnalysisExists(currentRoot, currentWord)){
                targetSentence.addWord(new Word(currentWord));
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Accessor for the sourceSentence attribute.
     * @return Source sentence.
     */
    public Sentence getSourceSentence(){
        return sourceSentence;
    }

    /**
     * Accessor for the targetSentence attribute.
     * @return Target sentence.
     */
    public Sentence getTargetSentence(){
        return targetSentence;
    }

    /**
     * Accessor for the logProbability attribute.
     * @return Logarithm of the probability of the sentence.
     */
    public double getLogProbability(){
        return logProbability;
    }

    /**
     * Checks if the translation is completed or not. If all tokens in the source sentence are processed, the method
     * returns true.
     * @return True if all tokens in the source sentence are translated, false otherwise.
     */
    public boolean done(){
        for (boolean token:processed){
            if (!token){
                return false;
            }
        }
        return true;
    }

    /**
     * Constructs a clone of the current object.
     * @return Clone of the current object.
     */
    public PartialTranslation clone(){
        PartialTranslation p = new PartialTranslation();
        p.sourceSentence = sourceSentence;
        p.processed = processed.clone();
        p.targetSentence = targetSentence.clone();
        p.logProbability = logProbability;
        p.processedCount = processedCount;
        return p;
    }

    /**
     * Translates a word at the given index to the given target word with the given probability.
     * @param index Index of the word to be translated.
     * @param toWord Target word, i.e. translation of the source word.
     * @param logProbability Logarithm of the probability of the translation.
     * @return Clone of the current object with the word translated.
     */
    public PartialTranslation translateWord(int index, Word toWord, double logProbability){
        PartialTranslation p = this.clone();
        p.processed[index] = true;
        p.targetSentence.addWord(toWord);
        p.logProbability += logProbability;
        p.processedCount = processedCount + 1;
        return p;
    }

    /**
     * Constructs an array of untranslated word indexes.
     * @param translationType Translation type.
     * @return An array of untranslated word indexes.
     */
    public ArrayList<Integer> getCandidateList(TranslationType translationType){
        ArrayList candidateList = new ArrayList();
        switch (translationType){
            case WORD_BASED:
                candidateList.add(processedCount);
                break;
            case PHRASE_BASED:
                for (int i = 0; i < sourceSentence.wordCount(); i++)
                    if (!processed[i])
                        candidateList.add(i);
                break;
        }
        return candidateList;
    }

}
