package Translation;

public class TargetPhrase {

    protected String lexicalClass = null;
    protected WordMeaning meaning;

    public TargetPhrase(String lexicalClass, WordMeaning meaning){
        this.lexicalClass = lexicalClass;
        this.meaning = meaning;
    }

    public TargetPhrase(WordMeaning meaning){
        this.meaning = meaning;
    }

    public TargetPhrase(String meaningText){
        this.meaning = new WordMeaning(meaningText);
    }

    public String getTranslation(){
        return meaning.getMeaningText();
    }

    public String getLexicalClass(){
        return lexicalClass;
    }

    public String toString(){
        return getTranslation();
    }
}
