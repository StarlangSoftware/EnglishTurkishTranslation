package Translation.Tree.Rule;

import AnnotatedSentence.ViewLayerType;
import AnnotatedTree.ParseNodeDrawable;
import Translation.Phrase.AutomaticTranslationDictionary;
import Translation.Phrase.BilingualDictionary;
import Translation.Phrase.WordTranslations;

import java.util.ArrayList;

public class BaseLineTurkishAutoTranslator extends AutoTranslator {

    public BaseLineTurkishAutoTranslator(AutomaticTranslationDictionary dictionary, BilingualDictionary bilingualDictionary){
        super(ViewLayerType.TURKISH_WORD, dictionary, bilingualDictionary);
        autoPreprocessor = new TurkishAutoPreprocessor();
    }

    protected String autoTranslateWithRules(ParseNodeDrawable parseNode, boolean noneCase, ArrayList<String> parentList, ArrayList<String> englishWordList, int index, WordTranslations translations) {
        if (translations.translationCount() > 0){
            return translations.getTranslation(0).getTranslation();
        } else {
            return "";
        }
    }

}
