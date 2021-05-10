package Translation.Phrase;

import Corpus.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IBMModel3 extends IBMModel2{

    protected Map<String, int[]> fertility;//n(phi|sourceCorpus)
    protected double distortionDistribution[][][][];//d(j|i, targetCorpus, sourceCorpus)
    protected double p0, p1;
    static final int MAX_FERTILITY = 10;

    /**
     * Constructor for the {@link IBMModel3} class. Gets the source corpus and the target corpus with the number of
     * iterations and trains the IBMModel3 using those corpora.
     * @param sourceCorpus Source corpus
     * @param targetCorpus Target corpus
     * @param maxIteration Maximum number of iterations to converge.
     */
    public IBMModel3(Corpus sourceCorpus, Corpus targetCorpus, int maxIteration){
        super(sourceCorpus, targetCorpus, maxIteration);
        Map<String, Map<String, Double>> count_t;
        Map<String, Double> total_t;
        Map<String, double[]> count_f;
        Map<String, Double> total_f;
        double count_d[][][][];
        double total_d[][][], countP1, countP0, cTotal, c;
        int fromMaxSentenceLength = sourceCorpus.maxSentenceLength(), toMaxSentenceLength = targetCorpus.maxSentenceLength();
        ArrayList<WordAlignment> A;
        Sentence sourceSentence, targetSentence;
        String f, t;
        int i, j, k, l, iterationCount = 0, nullCount, fertilityCount;
        fertility = new HashMap<>();
        distortionDistribution = new double[toMaxSentenceLength][fromMaxSentenceLength][fromMaxSentenceLength][toMaxSentenceLength];
        for (i = 0; i < toMaxSentenceLength; i++) {
            for (j = 0; j < fromMaxSentenceLength; j++) {
                for (k = 0; k < fromMaxSentenceLength; k++) {
                    for (l = 0; l < toMaxSentenceLength; l++) {
                        distortionDistribution[i][j][k][l] = 1.0 / (toMaxSentenceLength + 1);
                    }
                }
            }
        }
        p0 = 0.5;
        p1 = 0.5;
        while (iterationCount < maxIteration){
            countP0 = 0.0;
            countP1 = 0.0;
            count_f = new HashMap<>();
            total_f = new HashMap<>();
            count_t = new HashMap<>();
            total_t = new HashMap<>();
            count_d = new double[toMaxSentenceLength][fromMaxSentenceLength][fromMaxSentenceLength][toMaxSentenceLength];
            total_d = new double[toMaxSentenceLength][fromMaxSentenceLength][fromMaxSentenceLength];
            for (k = 0; k < sourceCorpus.sentenceCount(); k++){
                sourceSentence = sourceCorpus.getSentence(k);
                targetSentence = targetCorpus.getSentence(k);
                A = sample(sourceSentence, targetSentence);
                cTotal = 0.0;
                for (WordAlignment a:A){
                    cTotal += translationProbability(a);
                }
                for (WordAlignment a:A){
                    c = translationProbability(a) / cTotal;
                    nullCount = 0;
                    for (j = 0; j < targetSentence.wordCount(); j++){
                        if (a.get(j) == sourceSentence.wordCount())
                            nullCount++;
                        else{
                            count_d[targetSentence.wordCount()][sourceSentence.wordCount()][a.get(j)][j] += c;
                            total_d[targetSentence.wordCount()][sourceSentence.wordCount()][a.get(j)] += c;
                            t = targetSentence.getWord(j).getName();
                            f = sourceSentence.getWord(a.get(j)).getName();
                            if (!count_t.containsKey(f))
                                count_t.put(f, new HashMap<>());
                            if (!count_t.get(f).containsKey(t))
                                count_t.get(f).put(t, c);
                            else
                                count_t.get(f).put(t, count_t.get(f).get(t) + c);
                            if (!total_t.containsKey(f))
                                total_t.put(f, c);
                            else
                                total_t.put(f, total_t.get(f) + c);
                        }
                    }
                    countP1 += nullCount * c;
                    countP0 += (targetSentence.wordCount() - 2 * nullCount) * c;
                    for (j = 0; j < sourceSentence.wordCount(); j++){
                        fertilityCount = 0;
                        for (i = 0; i < targetSentence.wordCount(); i++) {
                            if (j == a.get(i)) {
                                fertilityCount++;
                            }
                        }
                        f = sourceSentence.getWord(j).getName();
                        if (!count_f.containsKey(f)) {
                            count_f.put(f, new double[MAX_FERTILITY]);
                        }
                        double[] counts = count_f.get(f);
                        counts[fertilityCount] += c;
                        count_f.put(f, counts);
                        if (!total_f.containsKey(f)) {
                            total_f.put(f, c);
                        } else {
                            total_f.put(f, total_f.get(f) + c);
                        }
                    }
                }
            }
            for (String word : translationDistribution.keySet()) {
                f = word;
                int [] counts = new int[MAX_FERTILITY];
                for (j = 0; j < MAX_FERTILITY; j++) {
                    counts[j] = (int) (count_f.get(f)[j] / total_f.get(f));
                }
                fertility.put(f, counts);
            }
            for (String word : translationDistribution.keySet()) {
                f = word;
                for (String word1 : translationDistribution.get(f).keySet()) {
                    t = word1;
                    translationDistribution.get(f).put(t, count_t.get(f).get(t) / total_t.get(f));
                }
            }
            for (i = 0; i < toMaxSentenceLength; i++) {
                for (j = 0; j < fromMaxSentenceLength; j++) {
                    for (k = 0; k < fromMaxSentenceLength; k++) {
                        for (l = 0; l < toMaxSentenceLength; l++) {
                            distortionDistribution[i][j][k][l] = count_d[i][j][k][l] / total_d[i][j][k];
                        }
                    }
                }
            }
            p1 = countP1 / (countP0 + countP1);
            p0 = 1 - p1;
            iterationCount++;
        }
    }

    private ArrayList<WordAlignment> neighboring(WordAlignment a, int jPegged){
        int i, j, j1, j2, tmp;
        Sentence sourceSentence = a.from(), targetSentence = a.to();
        ArrayList<WordAlignment> N;
        N = new ArrayList<>();
        for (j = 0; j < targetSentence.wordCount(); j++) {
            if (j != jPegged) {
                for (i = 0; i < sourceSentence.wordCount() + 1; i++) {
                    WordAlignment newAlignment = a.copy();
                    newAlignment.set(j, i);
                    N.add(newAlignment);
                }
            }
        }
        for (j1 = 0; j1 < targetSentence.wordCount(); j1++) {
            if (j1 != jPegged) {
                for (j2 = 0; j2 < targetSentence.wordCount(); j2++) {
                    if (j2 != jPegged) {
                        WordAlignment newAlignment = a.copy();
                        tmp = a.get(j1);
                        newAlignment.set(j1, a.get(j2));
                        newAlignment.set(j2, tmp);
                        N.add(newAlignment);
                    }
                }
            }
        }
        return N;
    }

    private double factorial(int n){
        int i;
        double result = 1.0;
        for (i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    private double binomial(int m, int n){
        return factorial(m) / (factorial(m - n) * factorial(n));
    }

    private double translationProbability(WordAlignment a){
        int i, j, phi[], sum, phi_0;
        double maxP;
        String f, t;
        Sentence sourceSentence = a.from(), targetSentence = a.to();
        double result = 1.0;
        phi = new int[sourceSentence.wordCount()];
        sum = 0;
        for (i = 0; i < sourceSentence.wordCount(); i++){
            f = sourceSentence.getWord(i).getName();
            maxP = 0.0;
            phi[i] = 1;
            if (fertility.containsKey(f)){
                for (j = 0; j < MAX_FERTILITY; j++) {
                    if (fertility.get(f)[j] > maxP) {
                        maxP = fertility.get(f)[j];
                        phi[i] = j;
                    }
                }
            }
            sum += phi[i];
        }
        phi_0 = targetSentence.wordCount() - sum;
        result *= binomial(targetSentence.wordCount() - phi_0, phi_0);
        result *= Math.pow(p1, phi_0);
        result *= Math.pow(p0, targetSentence.wordCount() - phi_0);
        result /= factorial(phi_0);
        for (i = 0; i < sourceSentence.wordCount(); i++){
            f = sourceSentence.getWord(i).getName();
            if (fertility.containsKey(f)) {
                result *= phi[i] * fertility.get(f)[phi[i]];
            } else {
                if (phi[i] != 1) {
                    result = 0.0;
                }
            }
        }
        for (j = 0; j < targetSentence.wordCount(); j++){
            if (a.get(j) < sourceSentence.wordCount()){
                f = sourceSentence.getWord(a.get(j)).getName();
                t = targetSentence.getWord(j).getName();
                result *= translationDistribution.get(f).get(t);
                result *= distortionDistribution[targetSentence.wordCount()][sourceSentence.wordCount()][a.get(j)][j];
            }
        }
        return result;
    }

    private WordAlignment hillClimbing(WordAlignment a, int jPegged){
        boolean changed = true;
        ArrayList<WordAlignment> neighbors;
        WordAlignment best = a.copy();
        while (changed){
            changed = false;
            neighbors = neighboring(best, jPegged);
            for (WordAlignment w:neighbors){
                if (translationProbability(w) > translationProbability(best)){
                    changed = true;
                    best = w.copy();
                }
            }
        }
        return best;
    }

    private ArrayList<WordAlignment> sample(Sentence sourceSentence, Sentence targetSentence){
        int i, j, jPrime, k, bestAlignment;
        double alignmentScore, bestAlignmentScore = -1;
        ArrayList<WordAlignment> a;
        String f, t;
        a = new ArrayList<>();
        for (j = 0; j < targetSentence.wordCount(); j++){
            for (i = 0; i < sourceSentence.wordCount(); i++){
                WordAlignment wa = new WordAlignment(targetSentence, sourceSentence);
                wa.set(j, i);
                for (jPrime = 0; jPrime < targetSentence.wordCount(); jPrime++) {
                    if (jPrime != j) {
                        t = targetSentence.getWord(jPrime).getName();
                        bestAlignment = -1;
                        for (k = 0; k < sourceSentence.wordCount(); k++) {
                            f = sourceSentence.getWord(k).getName();
                            alignmentScore = translationDistribution.get(f).get(t) * alignmentDistribution[targetSentence.wordCount()][sourceSentence.wordCount()][jPrime][k];
                            if (alignmentScore > bestAlignmentScore) {
                                bestAlignmentScore = alignmentScore;
                                bestAlignment = k;
                            }
                        }
                        if (bestAlignment != -1) {
                            wa.set(jPrime, bestAlignment);
                        }
                    }
                }
                WordAlignment bestLocal = hillClimbing(wa, j);
                a.addAll(neighboring(bestLocal, j));
            }
        }
        return a;
    }

    public ArrayList<PartialTranslation> translate(PartialTranslation current, int index){
        return super.translate(current, index);
    }

    public void loadModel(String modelFile) {
    }

}
