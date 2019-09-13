package Translation;

public class TargetPhrase {

    protected String lexicalClass = null;
    protected WordMeaning meaning;

    /**
     * Constructor for the {@link TargetPhrase} class. Gets the attributes lexicalClass and meaning as input and sets
     * the corresponding attributes.
     * @param lexicalClass Lexical class of the word.
     * @param meaning Meaning text of the word in {@link WordMeaning} form.
     */
    public TargetPhrase(String lexicalClass, WordMeaning meaning){
        this.lexicalClass = lexicalClass;
        this.meaning = meaning;
    }

    /**
     * Another constructor for the {@link TargetPhrase} class. Gets the attribute meaning as input and sets the
     * corresponding attribute.
     * @param meaning Meaning text of the word in {@link WordMeaning} form.
     */
    public TargetPhrase(WordMeaning meaning){
        this.meaning = meaning;
    }

    /**
     * Another constructor for the {@link TargetPhrase} class. Gets the attribute meaning as input and sets the
     * corresponding attribute.
     * @param meaningText Meaning text of the word in string form.
     */
    public TargetPhrase(String meaningText){
        this.meaning = new WordMeaning(meaningText);
    }

    /**
     * Accessor for the meaning attribute.
     * @return The meaning attribute
     */
    public String getTranslation(){
        return meaning.getMeaningText();
    }

    /**
     * Accessor for the lexicalClass attribute.
     * @return The lexicalClass attribute.
     */
    public String getLexicalClass(){
        return lexicalClass;
    }

    public String toString(){
        return getTranslation();
    }
}
