package Translation;

import java.util.Comparator;

public class ScoredSentenceComparator implements Comparator<ScoredSentence> {

    /**
     * Comparator interface for two {@link ScoredSentence}s.
     * @param sentenceA First {@link ScoredSentence} to compare.
     * @param sentenceB Second {@link ScoredSentence} to compare.
     * @return a. -1 if the first probability is less than the second probability
     * b. 1 if the second probability is less than the first probability
     * c. 0 otherwise.
     */
    public int compare(ScoredSentence sentenceA, ScoredSentence sentenceB){
        return Double.compare(sentenceB.getLogProbability(), sentenceA.getLogProbability());
    }

}
