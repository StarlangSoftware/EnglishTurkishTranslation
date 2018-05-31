package Translation;

import Dictionary.Word;

import java.util.ArrayList;

public class SourceWord extends Word {

    protected ArrayList<TargetPhrase> translations;

    public SourceWord(String name){
        super(name);
        translations = new ArrayList<TargetPhrase>();
    }

    public ArrayList<TargetPhrase> getTranslations(){
        return translations;
    }

    public void addTranslation(TargetPhrase translation){
        translations.add(translation);
    }

    public int translationCount(){
        return translations.size();
    }

    public TargetPhrase getTranslation(int index){
        return translations.get(index);
    }


}
