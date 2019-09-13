package Translation;

import Corpus.Sentence;
import Dictionary.Word;

import java.util.ArrayList;

public class ScoredSentence extends Sentence {

    private double logProbability;

    /**
     * Constructor for the {@link ScoredSentence} class. Sets the probability for that sentence to 1.
     */
    public ScoredSentence(){
        logProbability = 0;
    }

    /**
     * Creates a copy of the sentence.
     * @return Copy of the sentence.
     */
    public ScoredSentence clone(){
        ScoredSentence s = new ScoredSentence();
        for (Word w:words)
            s.addWord(w);
        return s;
    }

    /**
     * Another constructor for the {@link ScoredSentence} class. Gets the words of the sentence as space delimited
     * tokens in a string, splits the string, and constructs the sentence.
     * @param s String containing the words of the sentence.
     */
    public ScoredSentence(String s){
        logProbability = 0;
        words = new ArrayList<Word>();
        String[] tokens = s.split(" ");
        for (String token:tokens){
            words.add(new Word(token));
        }
    }

    /**
     * Adds all words of the second sentence to the current sentence. The method also sums the log probabilities.
     * @param aSentence The second sentence to be merged.
     * @return New sentence formed by joining the current sentence with the input sentence.
     */
    public ScoredSentence join(ScoredSentence aSentence){
        ScoredSentence s = clone();
        s.words.addAll(aSentence.words);
        s.logProbability = logProbability + aSentence.logProbability;
        return s;
    }

    /**
     * Increments the log probability by the given amount.
     * @param addedLogProb The amount to be added.
     */
    public void addLogProb(double addedLogProb){
        logProbability += addedLogProb;
    }

    /**
     * Accessor for the logProbability attribute.
     * @return logProbability attribute.
     */
    public double getLogProbability(){
        return logProbability;
    }

}