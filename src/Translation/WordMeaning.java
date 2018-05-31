package Translation;

public class WordMeaning {

    private String meaningClass = null;
    private String meaningText;

    public WordMeaning(String meaningClass, String meaningText){
        this.meaningClass = meaningClass;
        this.meaningText = meaningText;
    }

    public WordMeaning(String meaningText){
        this.meaningText = meaningText;
    }

    public String getMeaningClass(){
        return meaningClass;
    }

    public String getMeaningText(){
        return meaningText;
    }

    public void setMeaningText(String meaningText){
        this.meaningText = meaningText;
    }
}
