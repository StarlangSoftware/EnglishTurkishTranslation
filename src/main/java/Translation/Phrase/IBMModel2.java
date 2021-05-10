package Translation.Phrase;

import Corpus.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IBMModel2 extends IBMModel1{

    protected double alignmentDistribution[][][][];//a(i|j, targetCorpus, sourceCorpus)

    /**
     * Constructor for the {@link IBMModel2} class. Gets the source corpus and the target corpus with the number of
     * iterations and trains the IBMModel2 using those corpora.
     * @param sourceCorpus Source corpus
     * @param targetCorpus Target corpus
     * @param maxIteration Maximum number of iterations to converge.
     */
    public IBMModel2(Corpus sourceCorpus, Corpus targetCorpus, int maxIteration){
        super(sourceCorpus, targetCorpus, maxIteration);
        Map<String, Map<String, Double>> count;
        Map<String, Double> total;
        Map<String, Double> s_total;
        double count_a[][][][];
        double total_a[][][], c;
        Sentence sourceSentence, targetSentence;
        int i, j, k, l, iterationCount = 0;
        int fromMaxSentenceLength = sourceCorpus.maxSentenceLength(), toMaxSentenceLength = targetCorpus.maxSentenceLength();
        String f, t;
        alignmentDistribution = new double[toMaxSentenceLength][fromMaxSentenceLength][toMaxSentenceLength][fromMaxSentenceLength];
        for (i = 0; i < toMaxSentenceLength; i++) {
            for (j = 0; j < fromMaxSentenceLength; j++) {
                for (k = 0; k < toMaxSentenceLength; k++) {
                    for (l = 0; l < fromMaxSentenceLength; l++) {
                        alignmentDistribution[i][j][k][l] = 1.0 / (fromMaxSentenceLength + 1);
                    }
                }
            }
        }
        while (iterationCount < maxIteration){
            count = new HashMap<>();
            total = new HashMap<>();
            s_total = new HashMap<>();
            count_a = new double[toMaxSentenceLength][fromMaxSentenceLength][toMaxSentenceLength][fromMaxSentenceLength];
            total_a = new double[toMaxSentenceLength][fromMaxSentenceLength][toMaxSentenceLength];
            for (i = 0; i < sourceCorpus.sentenceCount(); i++){
                sourceSentence = sourceCorpus.getSentence(i);
                targetSentence = targetCorpus.getSentence(i);
                for (j = 0; j < targetSentence.wordCount(); j++){
                    t = targetSentence.getWord(j).getName();
                    s_total.put(t, 0.0);
                    for (k = 0; k < sourceSentence.wordCount(); k++){
                        f = sourceSentence.getWord(k).getName();
                        s_total.put(t, s_total.get(t) + translationDistribution.get(f).get(t) * alignmentDistribution[targetSentence.wordCount()][sourceSentence.wordCount()][j][k]);
                    }
                }
                for (j = 0; j < targetSentence.wordCount(); j++){
                    t = targetSentence.getWord(j).getName();
                    for (k = 0; k < sourceSentence.wordCount(); k++){
                        f = sourceSentence.getWord(k).getName();
                        c = translationDistribution.get(f).get(t) * alignmentDistribution[targetSentence.wordCount()][sourceSentence.wordCount()][j][k] / s_total.get(t);
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
                        count_a[targetSentence.wordCount()][sourceSentence.wordCount()][j][k] += c;
                        total_a[targetSentence.wordCount()][sourceSentence.wordCount()][j] += c;
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
            for (i = 0; i < toMaxSentenceLength; i++) {
                for (j = 0; j < fromMaxSentenceLength; j++) {
                    for (k = 0; k < toMaxSentenceLength; k++) {
                        for (l = 0; l < fromMaxSentenceLength; l++) {
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
