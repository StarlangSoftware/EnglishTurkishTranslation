package Translation;

import Corpus.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IBMModel3 extends IBMModel2{

    protected Map<String, int[]> fertility;//n(phi|fromLanguage)
    protected double distortionDistribution[][][][];//d(j|i, toLanguage, fromLanguage)
    protected double p0, p1;
    static final int MAX_FERTILITY = 5;

    public IBMModel3(Corpus fromLanguage, Corpus toLanguage, int maxIteration){
        super(fromLanguage, toLanguage, maxIteration);
        Map<String, Map<String, Double>> count_t;
        Map<String, Double> total_t;
        Map<String, double[]> count_f;
        Map<String, Double> total_f;
        double count_d[][][][];
        double total_d[][][], countP1, countP0, cTotal, c;
        ArrayList<WordAlignment> A;
        Sentence fromSentence, toSentence;
        String f, t;
        int i, j, k, l, iterationCount = 0, nullCount, fertilityCount;
        fertility = new HashMap<String, int[]>();
        distortionDistribution = new double[toLanguage.maxSentenceLength()][fromLanguage.maxSentenceLength()][fromLanguage.maxSentenceLength()][toLanguage.maxSentenceLength()];
        for (i = 0; i < toLanguage.maxSentenceLength(); i++)
            for (j = 0; j < fromLanguage.maxSentenceLength(); j++)
                for (k = 0; k < fromLanguage.maxSentenceLength(); k++)
                    for (l = 0; l < toLanguage.maxSentenceLength(); l++)
                        distortionDistribution[i][j][k][l] = 1.0 / (toLanguage.maxSentenceLength() + 1);

        p0 = 0.5;
        p1 = 0.5;
        while (iterationCount < maxIteration){
            countP0 = 0.0;
            countP1 = 0.0;
            count_f = new HashMap<String, double[]>();
            total_f = new HashMap<String, Double>();
            count_t = new HashMap<String, Map<String, Double>>();
            total_t = new HashMap<String, Double>();
            count_d = new double[toLanguage.maxSentenceLength()][fromLanguage.maxSentenceLength()][fromLanguage.maxSentenceLength()][toLanguage.maxSentenceLength()];
            total_d = new double[toLanguage.maxSentenceLength()][fromLanguage.maxSentenceLength()][fromLanguage.maxSentenceLength()];
            for (k = 0; k < fromLanguage.sentenceCount(); k++){
                fromSentence = fromLanguage.getSentence(k);
                toSentence = toLanguage.getSentence(k);
                A = sample(fromSentence, toSentence);
                cTotal = 0.0;
                for (WordAlignment a:A){
                    cTotal += translationProbability(a);
                }
                for (WordAlignment a:A){
                    c = translationProbability(a) / cTotal;
                    nullCount = 0;
                    for (j = 0; j < toSentence.wordCount(); j++){
                        if (a.get(j) == fromSentence.wordCount())
                            nullCount++;
                        else{
                            count_d[toSentence.wordCount()][fromSentence.wordCount()][a.get(j)][j] += c;
                            total_d[toSentence.wordCount()][fromSentence.wordCount()][a.get(j)] += c;
                            t = toSentence.getWord(j).getName();
                            f = fromSentence.getWord(a.get(j)).getName();
                            if (!count_t.containsKey(f))
                                count_t.put(f, new HashMap<String, Double>());
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
                    countP0 += (toSentence.wordCount() - 2 * nullCount) * c;
                    for (j = 0; j < fromSentence.wordCount(); j++){
                        fertilityCount = 0;
                        for (i = 0; i < toSentence.wordCount(); i++)
                            if (j == a.get(i))
                                fertilityCount++;
                        f = fromSentence.getWord(j).getName();
                        if (!count_f.containsKey(f))
                            count_f.put(f, new double[MAX_FERTILITY]);
                        double[] counts = count_f.get(f);
                        counts[fertilityCount] += c;
                        count_f.put(f, counts);
                        if (!total_f.containsKey(f))
                            total_f.put(f, c);
                        else
                            total_f.put(f, total_f.get(f) + c);
                    }
                }
            }
            for (String word : translationDistribution.keySet()) {
                f = word;
                int [] counts = new int[MAX_FERTILITY];
                for (j = 0; j < MAX_FERTILITY; j++)
                    counts[j] = (int) (count_f.get(f)[j] / total_f.get(f));
                fertility.put(f, counts);
            }
            for (String word : translationDistribution.keySet()) {
                f = word;
                for (String word1 : translationDistribution.get(f).keySet()) {
                    t = word1;
                    translationDistribution.get(f).put(t, count_t.get(f).get(t) / total_t.get(f));
                }
            }
            for (i = 0; i < toLanguage.maxSentenceLength(); i++)
                for (j = 0; j < fromLanguage.maxSentenceLength(); j++)
                    for (k = 0; k < fromLanguage.maxSentenceLength(); k++)
                        for (l = 0; l < toLanguage.maxSentenceLength(); l++)
                            distortionDistribution[i][j][k][l] = count_d[i][j][k][l] / total_d[i][j][k];
            p1 = countP1 / (countP0 + countP1);
            p0 = 1 - p1;
            iterationCount++;
        }
    }

    private ArrayList<WordAlignment> neighboring(WordAlignment a, int jPegged){
        int i, j, j1, j2, tmp;
        Sentence fromSentence = a.from(), toSentence = a.to();
        ArrayList<WordAlignment> N;
        N = new ArrayList<WordAlignment>();
        for (j = 0; j < toSentence.wordCount(); j++)
            if (j != jPegged)
                for (i = 0; i < fromSentence.wordCount() + 1; i++){
                    WordAlignment newAlignment = a.copy();
                    newAlignment.set(j, i);
                    N.add(newAlignment);
                }
        for (j1 = 0; j1 < toSentence.wordCount(); j1++)
            if (j1 != jPegged)
                for (j2 = 0; j2 < toSentence.wordCount(); j2++)
                    if (j2 != jPegged){
                        WordAlignment newAlignment = a.copy();
                        tmp = a.get(j1);
                        newAlignment.set(j1, a.get(j2));
                        newAlignment.set(j2, tmp);
                        N.add(newAlignment);
                    }
        return N;
    }

    private double factorial(int n){
        int i;
        double result = 1.0;
        for (i = 2; i <= n; i++)
            result *= i;
        return result;
    }

    private double binomial(int m, int n){
        return factorial(m) / (factorial(m - n) * factorial(n));
    }

    private double translationProbability(WordAlignment a){
        int i, j, phi[], sum, phi_0;
        double maxP;
        String f, t;
        Sentence fromSentence = a.from(), toSentence = a.to();
        double result = 1.0;
        phi = new int[fromSentence.wordCount()];
        sum = 0;
        for (i = 0; i < fromSentence.wordCount(); i++){
            f = fromSentence.getWord(i).getName();
            maxP = 0.0;
            phi[i] = 1;
            if (fertility.containsKey(f)){
                for (j = 0; j < MAX_FERTILITY; j++)
                    if (fertility.get(f)[j] > maxP){
                        maxP = fertility.get(f)[j];
                        phi[i] = j;
                    }
            }
            sum += phi[i];
        }
        phi_0 = toSentence.wordCount() - sum;
        result *= binomial(toSentence.wordCount() - phi_0, phi_0);
        result *= Math.pow(p1, phi_0);
        result *= Math.pow(p0, toSentence.wordCount() - phi_0);
        result /= factorial(phi_0);
        for (i = 0; i < fromSentence.wordCount(); i++){
            f = fromSentence.getWord(i).getName();
            if (fertility.containsKey(f))
                result *= phi[i] * fertility.get(f)[phi[i]];
            else
                if (phi[i] != 1)
                    result = 0.0;
        }
        for (j = 0; j < toSentence.wordCount(); j++){
            if (a.get(j) < fromSentence.wordCount()){
                f = fromSentence.getWord(a.get(j)).getName();
                t = toSentence.getWord(j).getName();
                result *= translationDistribution.get(f).get(t);
                result *= distortionDistribution[toSentence.wordCount()][fromSentence.wordCount()][a.get(j)][j];
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

    private ArrayList<WordAlignment> sample(Sentence fromSentence, Sentence toSentence){
        int i, j, jPrime, k, bestAlignment;
        double alignmentScore, bestAlignmentScore = -1;
        ArrayList<WordAlignment> a;
        String f, t;
        a = new ArrayList<WordAlignment>();
        for (j = 0; j < toSentence.wordCount(); j++){
            for (i = 0; i < fromSentence.wordCount(); i++){
                WordAlignment wa = new WordAlignment(toSentence, fromSentence);
                wa.set(j, i);
                for (jPrime = 0; jPrime < toSentence.wordCount(); jPrime++)
                    if (jPrime != j){
                        t = toSentence.getWord(jPrime).getName();
                        bestAlignment = -1;
                        for (k = 0; k < fromSentence.wordCount(); k++){
                            f = fromSentence.getWord(k).getName();
                            alignmentScore = translationDistribution.get(f).get(t) * alignmentDistribution[toSentence.wordCount()][fromSentence.wordCount()][jPrime][k];
                            if (alignmentScore > bestAlignmentScore){
                                bestAlignmentScore = alignmentScore;
                                bestAlignment = k;
                            }
                        }
                        if (bestAlignment != -1){
                            wa.set(jPrime, bestAlignment);
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
