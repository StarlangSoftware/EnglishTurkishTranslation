package Translation;

import DataStructure.CounterHashMap;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;

public class BleuMeasure {
    private static final int NGRAM_COUNT = 4;
    private int[] nGrams = new int[NGRAM_COUNT];
    private int[] candidateLength = new int[NGRAM_COUNT];
    private int referenceLength;
    private int[] currentNGrams = new int[NGRAM_COUNT];
    private int[] currentCandidateLength = new int[NGRAM_COUNT];
    private int currentReferenceLength;

    /**
     * Empty constructor for {@link BleuMeasure} class.
     */
    public BleuMeasure() {
    }

    /**
     * Adds a new sentence to the counts and update the statistics. ReferenceCounts represents the gold standard
     * translated sentence, whereas candidateTokens represents current translation. The method checks for each NGram
     * in the candidateTokens if they are in the referenceTokens are not.
     * @param referenceTokens the gold standard translated sentence
     * @param candidateTokens current translation.
     */
    private void addSentence(String[] referenceTokens, String[] candidateTokens) {
        for (int i = 1; i <= NGRAM_COUNT; ++i) {
            currentNGrams[i - 1] = 0;
            saveNGrams(i, referenceTokens, candidateTokens);
            currentCandidateLength[i - 1] = candidateTokens.length - i + 1;
            candidateLength[i - 1] += currentCandidateLength[i - 1];
        }
        currentReferenceLength = referenceTokens.length;
        referenceLength += currentReferenceLength;
    }

    /**
     * The method saves nGrams in the currentNGrams and nGrams attributes.
     * @param nGramIndex Current nGram index
     * @param referenceTokens the gold standard translated sentence
     * @param candidateTokens current translation.
     */
    private void saveNGrams(int nGramIndex, String[] referenceTokens, String[] candidateTokens) {
        CounterHashMap<String> candidateStats = getStats(candidateTokens, nGramIndex);
        CounterHashMap<String> referenceStats = getStats(referenceTokens, nGramIndex);
        for (String ngramKey : candidateStats.keySet()) {
            int referenceCount = 0;
            int candidateCount = candidateStats.get(ngramKey);
            if (referenceStats.containsKey(ngramKey))
                referenceCount = referenceStats.get(ngramKey);
            currentNGrams[nGramIndex - 1] += Math.min(candidateCount, referenceCount);
            nGrams[nGramIndex - 1] += Math.min(candidateCount, referenceCount);
        }
    }

    /**
     * The method constructs a {@link CounterHashMap} from the given sentence.
     * @param inputTokens current translation.
     * @param N N in NGram
     * @return A {@link CounterHashMap} containing the NGrams and counts.
     */
    private static CounterHashMap<String> getStats(String[] inputTokens, int N) {
        CounterHashMap<String> stats = new CounterHashMap<>();
        for (int i = 0; i < inputTokens.length; ++i) {
            String nGramString = makeNGram(inputTokens, i, N);
            if (nGramString == null)
                break;
            stats.put(nGramString);
        }
        return stats;
    }

    /**
     * Generates NGram from the given text from the given index according to the given N.
     * @param candTokens current translation.
     * @param off offset i.e. starting index for creating the NGram.
     * @param N N in NGram
     * @return NGram string constructed.
     */
    private static String makeNGram(String[] candTokens, int off, int N) {
        StringBuilder sb;
        if (off + N > candTokens.length) {
            return null;
        }
        sb = new StringBuilder(candTokens[off]);
        for (int j = off + 1; j < off + N; ++j) {
            sb.append(' ');
            sb.append(candTokens[j]);
        }
        return sb.toString();
    }

    /**
     * The method calculates the Bleu measure for the overall translation.
     * @param candidateLength The number of all NGrams for all N's. candidateLength[i] is the number of all i grams.
     * @param referenceLength The number of all unigrams.
     * @param nGrams The number of matching NGrams for all N's.
     * @return The Bleu score calculated.
     */
    private double calculateBleu(int[] candidateLength, int referenceLength, int[] nGrams) {
        double brevityPenalty = 1.0, precisionAverage = 0.0, bleuScore;
        if (candidateLength[0] <= referenceLength)
            brevityPenalty = Math.exp(1.0 - referenceLength / candidateLength[0]);
        for (int i = 0; i < NGRAM_COUNT; i++)
            if (candidateLength[i] > 0 && nGrams[i] > 0){
                precisionAverage += (1.0 / NGRAM_COUNT) * Math.log(nGrams[i] / (double) candidateLength[i]);
            }
        bleuScore = 100 * brevityPenalty * Math.exp(precisionAverage);
        return bleuScore;
    }

    /**
     * The method calculates the Bleu measure by comparing the given input translation file and the gold standard
     * translation file.
     * @param referenceFile Gold standard translations, one line per sentence.
     * @param candidateFile Current translation, one line per sentence.
     * @param debugMode If yes, the Bleu measure is also calculated per sentence and displayed.
     * @return Calculated Bleu score.
     */
    public double execute(String referenceFile, String candidateFile, boolean debugMode) throws IOException {
        boolean EOF = false;
        BufferedReader refBr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(referenceFile)), StandardCharsets.UTF_8));
        BufferedReader candBr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(candidateFile)), StandardCharsets.UTF_8));
        while (!EOF) {
            String candLine = null, refLine = null;
            String[] candTokens, refTokens;
            try {
                refLine = refBr.readLine();
                candLine = candBr.readLine();
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
                System.exit(1);
            }
            if (candLine == null && refLine == null)
                break;
            if (candLine == null || refLine == null)
                System.exit(1);
            candTokens = candLine.trim().split("\\s+");
            refTokens = refLine.trim().split("\\s+");
            addSentence(refTokens, candTokens);
            if (debugMode){
                System.out.println(calculateBleu(currentCandidateLength, currentReferenceLength, currentNGrams) + "-->" + candLine + "-->" + refLine);
            }
        }
        return calculateBleu(candidateLength, referenceLength, nGrams);
    }
}
