package Translation;

import java.util.Comparator;

public class PartialTranslationComparator implements Comparator<PartialTranslation>{

        public int compare(PartialTranslation partialA, PartialTranslation partialB){
            if (partialB.getLogProbability() < partialA.getLogProbability()){
                return -1;
            } else {
                if (partialB.getLogProbability() > partialA.getLogProbability()){
                    return 1;
                } else {
                    return 0;
                }
            }
        }

}
