package Translation;

import Corpus.Corpus;

public class TestTranslation {

    public static void main(String[] args){
        IBMModel ibmModel;
        Corpus english = new Corpus("english.txt");
        Corpus turkish = new Corpus("turkish.txt");
        ibmModel = new IBMModel2(english, turkish, 10);
        ibmModel.saveModel("map2.txt");
    }
}
