package Translation;

import Corpus.Sentence;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;

import java.util.ArrayList;

public class ExhaustiveTranslation {
    private TranslationType translationType;
    private FsmMorphologicalAnalyzer fsm;
    private int maxStackSize;

    public ExhaustiveTranslation(TranslationType translationType, FsmMorphologicalAnalyzer fsm, int maxStackSize){
        this.translationType = translationType;
        this.fsm = fsm;
        this.maxStackSize = maxStackSize;
    }

    public PartialTranslation bestTranslation(IBMModel model, Sentence fromSentence){
        PartialTranslation best = null, current = new PartialTranslation(fromSentence);
        ArrayList<PartialTranslation> candidates;
        TranslationStack currentStack, nextStack;
        currentStack = new TranslationStack(translationType, maxStackSize);
        currentStack.add(current);
        for (int i = 0; i < fromSentence.wordCount(); i++){
            nextStack = new TranslationStack(translationType, maxStackSize);
            while (!currentStack.isEmpty()){
                candidates = currentStack.translateBestOption(model);
                for (PartialTranslation candidate: candidates){
                    if (candidate.done()){
                        if (translationType == TranslationType.WORD_BASED && !candidate.simplifyTranslation(fsm)){
                            continue;
                        }
                        if (best == null || candidate.getLogProbability() > best.getLogProbability()){
                            best = candidate;
                        }
                    } else {
                        if (candidate.validTranslation(fsm)){
                            nextStack.add(candidate);
                        }
                    }
                }
            }
            currentStack = nextStack;
            currentStack.enforceStackSize();
        }
        if (best != null)
            return best;
        else
            return null;
    }

    public ArrayList<PartialTranslation> bestTranslationList(IBMModel model, Sentence fromSentence){
        ArrayList<PartialTranslation> result = new ArrayList<PartialTranslation>();
        PartialTranslation current = new PartialTranslation(fromSentence);
        ArrayList<PartialTranslation> candidates;
        TranslationStack currentStack, nextStack;
        currentStack = new TranslationStack(translationType, maxStackSize);
        currentStack.add(current);
        for (int i = 0; i < fromSentence.wordCount(); i++){
            nextStack = new TranslationStack(translationType, maxStackSize);
            while (!currentStack.isEmpty()){
                candidates = currentStack.translateBestOption(model);
                for (PartialTranslation candidate: candidates){
                    if (candidate.done()){
                        if (translationType == TranslationType.WORD_BASED && !candidate.simplifyTranslation(fsm)){
                            continue;
                        }
                        result.add(candidate);
                    } else {
                        if (candidate.validTranslation(fsm)){
                            nextStack.add(candidate);
                        }
                    }
                }
            }
            currentStack = nextStack;
            currentStack.enforceStackSize();
        }
        return result;
    }
}
