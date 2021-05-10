package Translation.Phrase;

public class WordMeaning {

    private String meaningClass = null;
    private String meaningText;

    /**
     * Constructor for the {@link WordMeaning} class. Gets meaningClass and meaningText as input and sets the
     * corresponding attributes.
     * @param meaningClass Meaning class of the word.
     * @param meaningText Meaning text of the word.
     */
    public WordMeaning(String meaningClass, String meaningText){
        this.meaningClass = meaningClass;
        this.meaningText = meaningText;
    }

    /**
     * Another constructor for the {@link WordMeaning} class. Gets meaningText as input and sets the corresponding
     * attribute.
     * @param meaningText Meaning text of the word.
     */
    public WordMeaning(String meaningText){
        this.meaningText = meaningText;
    }

    /**
     * Accessor for the meaningClass attribute.
     * @return MeaningClass attribute.
     */
    public String getMeaningClass(){
        return meaningClass;
    }

    /**
     * Accessor for the meaningText attribute.
     * @return MeaningText attribute.
     */
    public String getMeaningText(){
        return meaningText;
    }

    /**
     * Mutator for the meaningText attribute.
     * @param meaningText New meaning text.
     */
    public void setMeaningText(String meaningText){
        this.meaningText = meaningText;
    }
}
