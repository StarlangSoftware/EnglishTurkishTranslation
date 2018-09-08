package Translation;

public class WordTranslationCandidate extends TargetPhrase{

    private int count;

    public WordTranslationCandidate(String translation){
        super(translation);
        count = 1;
    }

    public WordTranslationCandidate(String translation, int count){
        super(translation);
        this.count = count;
    }

    public int getCount(){
        return count;
    }

    public void incrementCount(){
        count++;
    }

    public void addCount(int addedCount){
        count += addedCount;
    }

    public String toString(){
        return getTranslation() + " (" + count + ")";
    }

    public String toXml(){
        return "\t\t<translation name=\"" + getTranslation().replaceAll("&", "&amp;") + "\" count=\"" + count + "\"/>\n";
    }
}
