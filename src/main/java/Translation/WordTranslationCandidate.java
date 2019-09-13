package Translation;

public class WordTranslationCandidate extends TargetPhrase{

    private int count;

    /**
     * Constructor for {@link WordTranslationCandidate}. Gets the translation string as input and calls its super class
     * with it. Since this is the first occurrence for this translation, it sets count to 1.
     * @param translation Translation of the word.
     */
    public WordTranslationCandidate(String translation){
        super(translation);
        count = 1;
    }

    /**
     * Another constructor for {@link WordTranslationCandidate}. Gets the translation string and count for that
     * translation as input. Calls its superclass with translation and sets the translation count.
     * @param translation Translation of the word.
     * @param count Number of occurrences of this translation.
     */
    public WordTranslationCandidate(String translation, int count){
        super(translation);
        this.count = count;
    }

    /**
     * Accessor for the count attribute.
     * @return Count attribute.
     */
    public int getCount(){
        return count;
    }

    /**
     * Increments the count attribute.
     */
    public void incrementCount(){
        count++;
    }

    /**
     * Increments the count attribute by the amount of input addedCount.
     * @param addedCount Increment value.
     */
    public void addCount(int addedCount){
        count += addedCount;
    }

    public String toString(){
        return getTranslation() + " (" + count + ")";
    }

    /**
     * Converts the translation to Xml format.
     * @return Xml string form of translation.
     */
    public String toXml(){
        return "\t\t<translation name=\"" + getTranslation().replaceAll("&", "&amp;") + "\" count=\"" + count + "\"/>\n";
    }
}
