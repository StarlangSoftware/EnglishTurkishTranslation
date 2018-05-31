package Translation;

import java.io.*;
import java.util.Hashtable;

public class BleuMeasure {
    private static final int NGRAM_COUNT = 4;
    private int nGrams[] = new int[NGRAM_COUNT];
    private int candidateLength[] = new int[NGRAM_COUNT];
    private int referenceLength;
    private int currentNGrams[] = new int[NGRAM_COUNT];
    private int currentCandidateLength[] = new int[NGRAM_COUNT];
    private int currentReferenceLength;

    public BleuMeasure() {
    }

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

    private void saveNGrams(int nGram, String[] referenceTokens, String[] candidateTokens) {
        Hashtable<String, Integer> candidateStats = getStats(candidateTokens, nGram);
        Hashtable<String, Integer> referenceStats = getStats(referenceTokens, nGram);
        for (String ngramKey : candidateStats.keySet()) {
            int referenceCount = 0;
            int candidateCount = candidateStats.get(ngramKey);
            if (referenceStats.containsKey(ngramKey))
                referenceCount = referenceStats.get(ngramKey);
            currentNGrams[nGram - 1] += Math.min(candidateCount, referenceCount);
            nGrams[nGram - 1] += Math.min(candidateCount, referenceCount);
        }
    }

    private static Hashtable<String, Integer> getStats(String[] inputTokens, int nGram) {
        Hashtable<String, Integer> stats = new Hashtable<String, Integer>();
        for (int i = 0; i < inputTokens.length; ++i) {
            String nGramString = makeNGram(inputTokens, i, nGram);
            if (nGramString == null)
                break;
            if (stats.containsKey(nGramString)) {
                stats.put(nGramString, stats.get(nGramString) + 1);
            } else {
                stats.put(nGramString, 1);
            }
        }
        return stats;
    }

    private static String makeNGram(String[] candTokens, int off, int nGram) {
        StringBuilder sb;
        if (off + nGram > candTokens.length) {
            return null;
        }
        sb = new StringBuilder(candTokens[off]);
        for (int j = off + 1; j < off + nGram; ++j) {
            sb.append(' ');
            sb.append(candTokens[j]);
        }
        return sb.toString();
    }

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

    public double execute(String referenceFile, String candidateFile, boolean debugMode) throws IOException {
        boolean EOF = false;
        BufferedReader refBr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(referenceFile)), "UTF-8"));
        BufferedReader candBr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(candidateFile)), "UTF-8"));
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
