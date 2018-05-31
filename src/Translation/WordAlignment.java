package Translation;

import Corpus.Sentence;

public class WordAlignment {

    private Sentence fromSentence;
    private Sentence toSentence;
    private int alignment[];

    public WordAlignment(Sentence toSentence, Sentence fromSentence){
        int i;
        this.toSentence = toSentence;
        this.fromSentence = fromSentence;
        alignment = new int[toSentence.wordCount()];
        for (i = 0; i < toSentence.wordCount(); i++)
            if (i < fromSentence.wordCount()){
                alignment[i] = i;
            } else {
                alignment[i] = fromSentence.wordCount();
            }
    }

    public WordAlignment(Sentence toSentence, Sentence fromSentence, int[] alignment){
        this.toSentence = toSentence;
        this.fromSentence = fromSentence;
        this.alignment = alignment.clone();
    }

    public void set(int pos, int value){
        if (value >= 0 && value <= fromSentence.wordCount()){
            alignment[pos] = value;
        } else {
            System.out.println("Wrong set");
        }
    }

    public int get(int pos){
        return alignment[pos];
    }

    public Sentence from(){
        return fromSentence;
    }

    public Sentence to(){
        return toSentence;
    }

    public WordAlignment copy(){
        return new WordAlignment(this.toSentence, this.fromSentence, this.alignment);
    }

}
