package Translation.Phrase;

import java.util.Comparator;

public class PartialTranslationComparator implements Comparator<PartialTranslation>{

    /**
     * Comparator interface for two {@link PartialTranslation}s.
     * @param partialA First {@link PartialTranslation} to compare.
     * @param partialB Second {@link PartialTranslation} to compare.
     * @return a. -1 if the first probability is less than the second probability
     * b. 1 if the second probability is less than the first probability
     * c. 0 otherwise.
     */
    public int compare(PartialTranslation partialA, PartialTranslation partialB){
        return Double.compare(partialB.getLogProbability(), partialA.getLogProbability());
    }

}
