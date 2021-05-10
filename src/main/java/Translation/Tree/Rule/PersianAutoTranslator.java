package Translation.Tree.Rule;

import AnnotatedSentence.ViewLayerType;
import AnnotatedTree.ParseNodeDrawable;
import AnnotatedTree.ParseTreeDrawable;
import Translation.Phrase.AutomaticTranslationDictionary;
import Translation.Phrase.BilingualDictionary;
import Translation.Phrase.WordTranslations;

import java.util.ArrayList;

public class PersianAutoTranslator extends AutoTranslator {

    public PersianAutoTranslator(AutomaticTranslationDictionary dictionary, BilingualDictionary bilingualDictionary) {
        super(ViewLayerType.PERSIAN_WORD, dictionary, bilingualDictionary);
    }

    protected void autoFillWithNoneTags(ParseTreeDrawable parseTree) {
    }

    protected void autoSwap(ParseTreeDrawable parseTree) {
    }

    protected String autoTranslateWithRules(ParseNodeDrawable parseNode, boolean noneCase, ArrayList<String> parents, ArrayList<String> englishWords, int index, WordTranslations translations) {
        return null;
    }

}
