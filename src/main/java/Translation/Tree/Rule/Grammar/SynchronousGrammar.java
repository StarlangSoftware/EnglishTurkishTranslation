package Translation.Tree.Rule.Grammar;/* Created by oguzkeremyildiz on 12.05.2021 */

import ParseTree.ParseNode;

import java.util.*;

public class SynchronousGrammar<Symbol> {

    private final Symbol turkish;
    private final Symbol english;
    private final ArrayList<ParseNode> turkishList;
    private final ArrayList<ParseNode> englishList;
    private final HashMap<ParseNode, ParseNode> parseNodeDrawableMap;

    public SynchronousGrammar(Symbol turkish, Symbol english) {
        this.turkish = turkish;
        this.english = english;
        turkishList = new ArrayList<>();
        englishList = new ArrayList<>();
        parseNodeDrawableMap = new HashMap<>();
    }

    public void addTurkishList(ParseNode p) {
        turkishList.add(p);
    }

    public void addTurkishList(int index, ParseNode p) {
        turkishList.add(index, p);
    }

    public ParseNode getTurkishList(int index) {
        return turkishList.get(index);
    }

    public void addEnglishList(ParseNode p) {
        englishList.add(p);
    }

    public void addEnglishList(int index, ParseNode p) {
        englishList.add(index, p);
    }

    public ParseNode getEnglishList(int index) {
        return englishList.get(index);
    }

    public int englishSize() {
        return englishList.size();
    }

    public int turkishSize() {
        return turkishList.size();
    }

    public void put(ParseNode first, ParseNode second) {
        parseNodeDrawableMap.put(first, second);
    }

    @Override
    public String toString() {
        return turkish.toString() + " / " + english.toString();
    }
}
