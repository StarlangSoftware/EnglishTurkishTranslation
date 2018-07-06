package Translation;

import Corpus.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IBMModel2 extends IBMModel1{

    protected double alignmentDistribution[][][][];//a(i|j, toLanguage, fromLanguage)

    public IBMModel2(Corpus fromLanguage, Corpus toLanguage, int maxIteration){
        super(fromLanguage, toLanguage, maxIteration);
        Map<String, Map<String, Double>> count;
        Map<String, Double> total;
        Map<String, Double> s_total;
        double count_a[][][][];
        double total_a[][][], c;
        Sentence fromSentence, toSentence;
        int i, j, k, l, iterationCount = 0;
        String f, t;
        alignmentDistribution = new double[toLanguage.maxSentenceLength()][fromLanguage.maxSentenceLength()][toLanguage.maxSentenceLength()][fromLanguage.maxSentenceLength()];
        for (i = 0; i < toLanguage.maxSentenceLength(); i++) {
            for (j = 0; j < fromLanguage.maxSentenceLength(); j++) {
                for (k = 0; k < toLanguage.maxSentenceLength(); k++) {
                    for (l = 0; l < fromLanguage.maxSentenceLength(); l++) {
                        alignmentDistribution[i][j][k][l] = 1.0 / (fromLanguage.maxSentenceLength() + 1);
                    }
                }
            }
        }
        while (iterationCount < maxIteration){
            count = new HashMap<>();
            total = new HashMap<>();
            s_total = new HashMap<>();
            count_a = new double[toLanguage.maxSentenceLength()][fromLanguage.maxSentenceLength()][toLanguage.maxSentenceLength()][fromLanguage.maxSentenceLength()];
            total_a = new double[toLanguage.maxSentenceLength()][fromLanguage.maxSentenceLength()][toLanguage.maxSentenceLength()];
            for (i = 0; i < fromLanguage.sentenceCount(); i++){
                fromSentence = fromLanguage.getSentence(i);
                toSentence = toLanguage.getSentence(i);
                for (j = 0; j < toSentence.wordCount(); j++){
                    t = toSentence.getWord(j).getName();
                    s_total.put(t, 0.0);
                    for (k = 0; k < fromSentence.wordCount(); k++){
                        f = fromSentence.getWord(k).getName();
                        s_total.put(t, s_total.get(t) + translationDistribution.get(f).get(t) * alignmentDistribution[toSentence.wordCount()][fromSentence.wordCount()][j][k]);
                    }
                }
                for (j = 0; j < toSentence.wordCount(); j++){
                    t = toSentence.getWord(j).getName();
                    for (k = 0; k < fromSentence.wordCount(); k++){
                        f = fromSentence.getWord(k).getName();
                        c = translationDistribution.get(f).get(t) * alignmentDistribution[toSentence.wordCount()][fromSentence.wordCount()][j][k] / s_total.get(t);
                        if (!count.containsKey(f)) {
                            count.put(f, new HashMap<>());
                        }
                        if (!count.get(f).containsKey(t)) {
                            count.get(f).put(t, c);
                        } else {
                            count.get(f).put(t, count.get(f).get(t) + c);
                        }
                        if (!total.containsKey(f)) {
                            total.put(f, c);
                        } else {
                            total.put(f, total.get(f) + c);
                        }
                        count_a[toSentence.wordCount()][fromSentence.wordCount()][j][k] += c;
                        total_a[toSentence.wordCount()][fromSentence.wordCount()][j] += c;
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
            for (i = 0; i < toLanguage.maxSentenceLength(); i++) {
                for (j = 0; j < fromLanguage.maxSentenceLength(); j++) {
                    for (k = 0; k < toLanguage.maxSentenceLength(); k++) {
                        for (l = 0; l < fromLanguage.maxSentenceLength(); l++) {
                            alignmentDistribution[i][j][k][l] = count_a[i][j][k][l] / total_a[i][j][k];
                        }
                    }
                }
            }
            iterationCount++;
        }
    }

    public ArrayList<PartialTranslation> translate(PartialTranslation current, int index){
        return super.translate(current, index);
    }

    public void loadModel(String modelFile) {
    }

}
