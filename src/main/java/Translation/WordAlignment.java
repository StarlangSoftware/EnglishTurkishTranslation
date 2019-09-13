package Translation;

import Corpus.Sentence;

public class WordAlignment {

    private Sentence sourceSentence;
    private Sentence targetSentence;
    private int[] alignment;

    /**
     * Constructor for {@link WordAlignment} class. Gets input the target sentence and the source sentence and sets
     * the corresponding attributes. The method also initializes the alignment to the unary alignment.
     * @param targetSentence Target sentence
     * @param sourceSentence Source sentence
     */
    public WordAlignment(Sentence targetSentence, Sentence sourceSentence){
        int i;
        this.targetSentence = targetSentence;
        this.sourceSentence = sourceSentence;
        alignment = new int[targetSentence.wordCount()];
        for (i = 0; i < targetSentence.wordCount(); i++)
            if (i < sourceSentence.wordCount()){
                alignment[i] = i;
            } else {
                alignment[i] = sourceSentence.wordCount();
            }
    }

    /**
     * Another constructor for {@link WordAlignment} class. Gets input the target sentence, the source sentence and
     * the alignment sets the corresponding attributes.
     * @param targetSentence Target sentence
     * @param sourceSentence Source sentence
     * @param alignment Current alignment
     */
    public WordAlignment(Sentence targetSentence, Sentence sourceSentence, int[] alignment){
        this.targetSentence = targetSentence;
        this.sourceSentence = sourceSentence;
        this.alignment = alignment.clone();
    }

    /**
     * Mutator for the alignment. Sets the given alignment to the given value.
     * @param pos Index in the alignment array
     * @param value New value for that indexed alignment
     */
    public void set(int pos, int value){
        if (value >= 0 && value <= sourceSentence.wordCount()){
            alignment[pos] = value;
        } else {
            System.out.println("Wrong set");
        }
    }

    /**
     * Accessor for the alignment element
     * @param pos Index of the alignment element
     * @return Alignment element at index pos
     */
    public int get(int pos){
        return alignment[pos];
    }

    /**
     * Accessor for the source sentence.
     * @return Source sentence
     */
    public Sentence from(){
        return sourceSentence;
    }

    /**
     * Accessor for the target sentence.
     * @return Target sentence.
     */
    public Sentence to(){
        return targetSentence;
    }

    /**
     * Creates a copy of the current word alignment.
     * @return A new copy of this current alignment object.
     */
    public WordAlignment copy(){
        return new WordAlignment(this.targetSentence, this.sourceSentence, this.alignment);
    }

}
