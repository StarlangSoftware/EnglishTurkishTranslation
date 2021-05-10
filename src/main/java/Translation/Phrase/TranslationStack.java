package Translation.Phrase;

import java.util.ArrayList;

public class TranslationStack {

    private ArrayList<PartialTranslation> partialTranslations;
    private TranslationType translationType;
    private int maxStackSize;

    /**
     * Constructor for {@link TranslationStack} class. Gets translationType and maximum stack size as inputs and sets
     * the corresponding attributes.
     * @param translationType Translation type applied
     * @param maxStackSize Maximum stack size used in the translation stack before giving stack overflow error.
     */
    public TranslationStack(TranslationType translationType, int maxStackSize){
        this.translationType = translationType;
        this.maxStackSize = maxStackSize;
        partialTranslations = new ArrayList<PartialTranslation>();
    }

    /**
     * Adds a given translation to the translation stack if it does not exists in the stack.
     * @param translation New translation candidate
     */
    public void add(PartialTranslation translation){
        for (PartialTranslation p: partialTranslations){
            if (p.equals(translation))
                return;
        }
        partialTranslations.add(translation);
    }

    /**
     * Checks for the emptiness of the stack.
     * @return True if the translation stack is empty; false otherwise.
     */
    public boolean isEmpty(){
        return partialTranslations.isEmpty();
    }

    /**
     * Given the IBMModel for translation, the method chooses the best option to translate.
     * @param model IBMModel trained before.
     * @return An array of partial translations.
     */
    public ArrayList<PartialTranslation> translateBestOption(IBMModel model){
        double maxLogProbability = -Double.MAX_VALUE;
        PartialTranslation candidate = null;
        ArrayList<Integer> indexList;
        ArrayList<PartialTranslation> result = new ArrayList<PartialTranslation>();
        if (translationType == TranslationType.WORD_BASED && partialTranslations.size() > 0){
            candidate = partialTranslations.get(0);
        } else {
            for (PartialTranslation p: partialTranslations){
                if (p.getLogProbability() > maxLogProbability){
                    maxLogProbability = p.getLogProbability();
                    candidate = p;
                }
            }
        }
        if (candidate != null){
            indexList = candidate.getCandidateList(translationType);
            for (Integer index : indexList) {
                ArrayList<PartialTranslation> currentResult = model.translate(candidate, index);
                if (currentResult != null) {
                    result.addAll(currentResult);
                }
            }
            partialTranslations.remove(candidate);
        }
        return result;
    }

    /**
     * If the number of possible translations exceed the stack size, this method can enforce the stack size, that is
     * deletes the translations after the maximum stack size.
     */
    public void enforceStackSize(){
        partialTranslations.sort(new PartialTranslationComparator());
        if (partialTranslations.size() > maxStackSize){
            partialTranslations.subList(maxStackSize, partialTranslations.size()).clear();
        }
    }

}
