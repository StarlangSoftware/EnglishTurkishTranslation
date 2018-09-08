package Translation;

import java.util.Comparator;

public class ScoredSentenceComparator implements Comparator<ScoredSentence> {

    public int compare(ScoredSentence sentenceA, ScoredSentence sentenceB){
        if (sentenceB.getLogProbability() < sentenceA.getLogProbability()){
            return -1;
        } else {
            if (sentenceB.getLogProbability() > sentenceA.getLogProbability()){
                return 1;
            } else {
                return 0;
            }
        }
    }

}
