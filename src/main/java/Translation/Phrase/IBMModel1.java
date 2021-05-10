package Translation.Phrase;

import Corpus.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map.*;

public class IBMModel1 extends IBMModel{
    protected Corpus sourceCorpus;
    protected Corpus targetCorpus;
    protected Map<String, Map<String, Double>> translationDistribution;//t(targetCorpus|sourceCorpus)

    /**
     * Constructor for the {@link IBMModel1} class. Gets the source corpus and the target corpus with the number of
     * iterations and trains the IBMModel1 using those corpora.
     * @param sourceCorpus Source corpus
     * @param targetCorpus Target corpus
     * @param maxIteration Maximum number of iterations to converge.
     */
    public IBMModel1(Corpus sourceCorpus, Corpus targetCorpus, int maxIteration){
        this.sourceCorpus = sourceCorpus;
        this.targetCorpus = targetCorpus;
        Map<String, Map<String, Double>> count;
        Map<String, Double> total;
        Map<String, Double> s_total;
        String f, t;
        int i, j, k, iterationCount = 0;
        Sentence sourceSentence, targetSentence;
        translationDistribution = new HashMap<>();
        while (iterationCount < maxIteration){
            count = new HashMap<>();
            total = new HashMap<>();
            s_total = new HashMap<>();
            for (i = 0; i < sourceCorpus.sentenceCount(); i++){
                sourceSentence = sourceCorpus.getSentence(i);
                targetSentence = targetCorpus.getSentence(i);
                for (j = 0; j < targetSentence.wordCount(); j++){
                    t = targetSentence.getWord(j).getName();
                    s_total.put(t, 0.0);
                    for (k = 0; k < sourceSentence.wordCount(); k++){
                        f = sourceSentence.getWord(k).getName();
                        if (!translationDistribution.containsKey(f)){
                            translationDistribution.put(f, new HashMap<>());
                        }
                        if (!translationDistribution.get(f).containsKey(t)){
                            translationDistribution.get(f).put(t, 1.0 / targetCorpus.wordCount());
                        }
                        s_total.put(t, s_total.get(t) + translationDistribution.get(f).get(t));
                    }
                }
                for (j = 0; j < targetSentence.wordCount(); j++){
                    t = targetSentence.getWord(j).getName();
                    for (k = 0; k < sourceSentence.wordCount(); k++){
                        f = sourceSentence.getWord(k).getName();
                        if (!count.containsKey(f)){
                            count.put(f, new HashMap<>());
                        }
                        if (!count.get(f).containsKey(t)) {
                            count.get(f).put(t, translationDistribution.get(f).get(t) / s_total.get(t));
                        } else {
                            count.get(f).put(t, count.get(f).get(t) + translationDistribution.get(f).get(t) / s_total.get(t));
                        }
                        if (!total.containsKey(f)) {
                            total.put(f, translationDistribution.get(f).get(t) / s_total.get(t));
                        } else {
                            total.put(f, total.get(f) + translationDistribution.get(f).get(t) / s_total.get(t));
                        }
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

    public void saveModel(String fileName){
        try {
            PrintWriter output = new PrintWriter(new File(fileName));
            for (String fromWord : translationDistribution.keySet()){
                Set<Entry<String, Double>> set = translationDistribution.get(fromWord).entrySet();
                List<Entry<String, Double>> list = new ArrayList<>(set);
                list.sort((o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));
                if (list.size() >= 10){
                    output.println(fromWord + "\t10");
                    for (int i = 0; i < 10; i++){
                        output.println(list.get(i).getKey() + "\t" + list.get(i).getValue());
                    }
                } else {
                    output.println(fromWord + "\t" + list.size());
                    for (Entry<String, Double> entry : list) {
                        output.println(entry.getKey() + "\t" + entry.getValue());
                    }
                }
            }
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadModel(String modelFile) {
    }

}
