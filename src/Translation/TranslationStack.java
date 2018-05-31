package Translation;

import Dictionary.Word;
import java.util.ArrayList;
import java.util.Collections;

public class TranslationStack {

    private ArrayList<PartialTranslation> partialTranslations;
    private TranslationType translationType;
    private int maxStackSize;

    public TranslationStack(TranslationType translationType, int maxStackSize){
        this.translationType = translationType;
        this.maxStackSize = maxStackSize;
        partialTranslations = new ArrayList<PartialTranslation>();
    }

    public void add(PartialTranslation translation){
        for (PartialTranslation p: partialTranslations){
            if (p.equals(translation))
                return;
        }
        partialTranslations.add(translation);
    }

    public boolean isEmpty(){
        return partialTranslations.isEmpty();
    }

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

    public void enforceStackSize(){
        Collections.sort(partialTranslations, new PartialTranslationComparator());
        if (partialTranslations.size() > maxStackSize){
            partialTranslations.subList(maxStackSize, partialTranslations.size()).clear();
        }
    }

}
