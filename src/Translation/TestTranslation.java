package Translation;

import Corpus.Corpus;

public class TestTranslation {

    public static void main(String[] args){
        IBMModel ibmModel;
        Corpus english = new Corpus("./Data/Translation/penntreebank-english.txt");
        Corpus turkish = new Corpus("./Data/Translation/penntreebank-turkish.txt");
        ibmModel = new IBMModel1(english, turkish, 1);
    }
}
