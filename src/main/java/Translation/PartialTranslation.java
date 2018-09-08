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
    private Sentence fromSentence;
    private boolean[] processed;
    private Sentence toSentence;
    private double logProbability;
    private int processedCount = 0;

    private PartialTranslation(){

    }

    public PartialTranslation(Sentence fromSentence){
        this.fromSentence = fromSentence;
        toSentence = new Sentence();
        processed = new boolean[fromSentence.wordCount()];
        logProbability = 0;
        processedCount = 0;
    }

    public Sentence getTranslation(){
        return toSentence;
    }

    public boolean equals(PartialTranslation p){
        if (!toSentence.equals(p.toSentence))
            return false;
        for (int i = 0; i < processed.length; i++)
            if (processed[i] != p.processed[i])
                return false;
        return true;
    }

    private ArrayList<String> getMorphosyntacticTokenList(){
        ArrayList<String> tokenList = new ArrayList<String>();
        for (Word word:toSentence.getWords()){
            if (!word.getName().equalsIgnoreCase("*NONE*")){
                String[] tokens = word.getName().split("[\\s\\+]");
                Collections.addAll(tokenList, tokens);
            }
        }
        return tokenList;
    }

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
        toSentence = new Sentence();
        for (String token:tokenList){
            if (!fsm.getFiniteStateMachine().isValidTransition(token)){
                if (!currentWord.isEmpty()){
                    if (fsm.morphologicalAnalysisExists(currentRoot, currentWord)){
                        toSentence.addWord(new Word(currentWord));
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
                toSentence.addWord(new Word(currentWord));
            } else {
                return false;
            }
        }
        return true;
    }

    public Sentence getFromSentence(){
        return fromSentence;
    }

    public Sentence getToSentence(){
        return toSentence;
    }

    public double getLogProbability(){
        return logProbability;
    }

    public boolean done(){
        for (boolean token:processed){
            if (!token){
                return false;
            }
        }
        return true;
    }

    public PartialTranslation clone(){
        PartialTranslation p = new PartialTranslation();
        p.fromSentence = fromSentence;
        p.processed = processed.clone();
        p.toSentence = toSentence.clone();
        p.logProbability = logProbability;
        p.processedCount = processedCount;
        return p;
    }

    public PartialTranslation translateWord(int index, Word toWord, double logProbability){
        PartialTranslation p = this.clone();
        p.processed[index] = true;
        p.toSentence.addWord(toWord);
        p.logProbability += logProbability;
        p.processedCount = processedCount + 1;
        return p;
    }

    public ArrayList<Integer> getCandidateList(TranslationType translationType){
        ArrayList candidateList = new ArrayList();
        switch (translationType){
            case WORD_BASED:
                candidateList.add(processedCount);
                break;
            case PHRASE_BASED:
                for (int i = 0; i < fromSentence.wordCount(); i++)
                    if (!processed[i])
                        candidateList.add(i);
                break;
        }
        return candidateList;
    }

}
