package Translation;

import Corpus.Sentence;
import Dictionary.Word;

import java.util.ArrayList;

public class ScoredSentence extends Sentence {

    private double logProbability;

    public ScoredSentence(){
        logProbability = 0;
    }

    public ScoredSentence clone(){
        ScoredSentence s = new ScoredSentence();
        for (Word w:words)
            s.addWord(w);
        return s;
    }

    public ScoredSentence(String s){
        logProbability = 0;
        words = new ArrayList<Word>();
        String[] tokens = s.split(" ");
        for (String token:tokens){
            words.add(new Word(token));
        }
    }

    public ScoredSentence join(ScoredSentence aSentence){
        ScoredSentence s = clone();
        s.words.addAll(aSentence.words);
        s.logProbability = logProbability + aSentence.logProbability;
        return s;
    }

    public void addLogProb(double addedLogProb){
        logProbability += addedLogProb;
    }

    public double getLogProbability(){
        return logProbability;
    }

}