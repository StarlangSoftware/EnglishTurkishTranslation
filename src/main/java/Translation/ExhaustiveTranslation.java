package Translation;

import Corpus.Sentence;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;

import java.util.ArrayList;

public class ExhaustiveTranslation {
    private TranslationType translationType;
    private FsmMorphologicalAnalyzer fsm;
    private int maxStackSize;

    /**
     * Constructor for the {@link ExhaustiveTranslation} class. Gets the translationType, Turkish morphological
     * analyzer and maximum stack size as input, and sets the corresponding attributes.
     * @param translationType Type of the translation.
     * @param fsm Turkish morphological analyzer
     * @param maxStackSize Maximum stack size allowed
     */
    public ExhaustiveTranslation(TranslationType translationType, FsmMorphologicalAnalyzer fsm, int maxStackSize){
        this.translationType = translationType;
        this.fsm = fsm;
        this.maxStackSize = maxStackSize;
    }

    /**
     * The method extracts the best translation according to the given IBMModel for the given sentence.
     * @param model IBMModel used for translation.
     * @param fromSentence Sentence to be translated.
     * @return Output translation.
     */
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

    /**
     * The method extracts the all best translations according to the given IBMModel for the given sentence.
     * @param model IBMModel used for translation.
     * @param fromSentence Sentence to be translated.
     * @return Output translation set.
     */
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
