package Translation;

import Corpus.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IBMModel1 extends IBMModel{
    protected Corpus fromLanguage;
    protected Corpus toLanguage;
    protected Map<String, Map<String, Double>> translationDistribution;//t(toLanguage|fromLanguage)

    public IBMModel1(Corpus fromLanguage, Corpus toLanguage, int maxIteration){
        this.fromLanguage = fromLanguage;
        this.toLanguage = toLanguage;
        Map<String, Map<String, Double>> count;
        Map<String, Double> total;
        Map<String, Double> s_total;
        String f, t;
        int i, j, k, iterationCount = 0;
        Sentence fromSentence, toSentence;
        translationDistribution = new HashMap<String, Map<String, Double>>();
        while (iterationCount < maxIteration){
            count = new HashMap<String, Map<String, Double>>();
            total = new HashMap<String, Double>();
            s_total = new HashMap<String, Double>();
            for (i = 0; i < fromLanguage.sentenceCount(); i++){
                fromSentence = fromLanguage.getSentence(i);
                toSentence = toLanguage.getSentence(i);
                for (j = 0; j < toSentence.wordCount(); j++){
                    t = toSentence.getWord(j).getName();
                    s_total.put(t, 0.0);
                    for (k = 0; k < fromSentence.wordCount(); k++){
                        f = fromSentence.getWord(k).getName();
                        if (!translationDistribution.containsKey(f))
                            translationDistribution.put(f, new HashMap<String, Double>());
                        if (!translationDistribution.get(f).containsKey(t))
                            translationDistribution.get(f).put(t, 1.0 / toLanguage.wordCount());
                        s_total.put(t, s_total.get(t) + translationDistribution.get(f).get(t));
                    }
                }
                for (j = 0; j < toSentence.wordCount(); j++){
                    t = toSentence.getWord(j).getName();
                    for (k = 0; k < fromSentence.wordCount(); k++){
                        f = fromSentence.getWord(k).getName();
                        if (!count.containsKey(f))
                            count.put(f, new HashMap<String, Double>());
                        if (!count.get(f).containsKey(t))
                            count.get(f).put(t, translationDistribution.get(f).get(t) / s_total.get(t));
                        else
                            count.get(f).put(t, count.get(f).get(t) + translationDistribution.get(f).get(t) / s_total.get(t));
                        if (!total.containsKey(f))
                            total.put(f, translationDistribution.get(f).get(t) / s_total.get(t));
                        else
                            total.put(f, total.get(f) + translationDistribution.get(f).get(t) / s_total.get(t));
                    }
                }
            }
            for (String word : translationDistribution.keySet()) {
                f = word;
                for (String word1 : translationDistribution.get(f).keySet()) {
                    t = word1;
                    translationDistribution.get(f).put(t, count.get(f).get(t) / total.get(f));
                }
            }
            iterationCount++;
        }
    }

    public ArrayList<PartialTranslation> translate(PartialTranslation current, int index){
        return null;
    }

    @Override
    public void loadModel(String modelFile) {
    }

}
