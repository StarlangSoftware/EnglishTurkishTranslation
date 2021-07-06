package Translation.Tree.Rule.Grammar;

import AnnotatedSentence.ViewLayerType;
import AnnotatedTree.ParseNodeDrawable;
import AnnotatedTree.Processor.Condition.*;
import AnnotatedTree.Processor.NodeDrawableCollector;
import ParseTree.ParseNode;
import ParseTree.Symbol;

import java.util.*;

public class GrammarGenerator {

    private static int contains(ParseNode turkish, ParseNode english, HashMap<String, String> turkishEnglishMap) {
        ArrayList<ParseNodeDrawable> turkishLeaves = new NodeDrawableCollector((ParseNodeDrawable) turkish, new IsTurkishLeafNode()).collect();
        ArrayList<ParseNodeDrawable> englishLeaves = new NodeDrawableCollector((ParseNodeDrawable) english, new IsEnglishLeafNode()).collect();
        HashMap<ParseNodeDrawable, ParseNodeDrawable> removedMap = new HashMap<>();
        for (ParseNodeDrawable turkishLeaf : turkishLeaves) {
            for (ParseNodeDrawable englishLeaf : englishLeaves) {
                if (englishLeaf.getData().getName().equals(turkishEnglishMap.get(turkishLeaf.getLayerData(ViewLayerType.TURKISH_WORD)))) {
                    removedMap.put(englishLeaf, turkishLeaf);
                }
            }
        }
        for (ParseNodeDrawable key : removedMap.keySet()) {
            englishLeaves.remove(key);
            turkishLeaves.remove(removedMap.get(key));
        }
        if (turkishLeaves.isEmpty() && englishLeaves.isEmpty()) {
            return 0;
        } else if (!turkishLeaves.isEmpty() && englishLeaves.isEmpty()) {
            return 1;
        } else if (turkishLeaves.isEmpty()) {
            return 2;
        }
        return 3;
    }

    private static int findIndex(ArrayList<ParseNode> language, Set<ParseNode> set, int currentIndex) {
        int index = 0;
        for (int i = 0; i < currentIndex; i++) {
            if (set.contains(language.get(i))) {
                index++;
            }
        }
        return index;
    }

    private static ArrayList<ParseNode> createList(ParseNode parent, Set<ParseNode> used) {
        ArrayList<ParseNode> list = new ArrayList<>();
        for (int i = 0; i < parent.numberOfChildren(); i++) {
            ParseNode child = parent.getChild(i);
            if (!used.contains(child)) {
                list.add(child);
            }
        }
        return list;
    }

    private static ArrayList<ParseNode> createList(ArrayList<ParseNode> language, Set<ParseNode> used) {
        ArrayList<ParseNode> list = new ArrayList<>();
        for (ParseNode child : language) {
            if (!used.contains(child)) {
                list.add(child);
            }
        }
        return list;
    }

    private static boolean specialState(ParseNode current, HashMap<String, String> turkishEnglishMap) {
        if (current.numberOfChildren() == 1) {
            ParseNode child = current;
            while (!child.isLeaf()) {
                child = child.getChild(0);
            }
            return !turkishEnglishMap.containsValue(child.getData().getName());
        }
        return false;
    }

    private static ParseNode addForSpecialState(ArrayList<ParseNode> english, int i) {
        ParseNode current = english.get(i);
        ParseNode child = new ParseNode(new Symbol(current.getData().getName()));
        ParseNode currentChild = current;
        do {
            currentChild = currentChild.getChild(0);
            child.addChild(new ParseNode(new Symbol(currentChild.getData().getName())));
        } while (!currentChild.isLeaf());
        return child;
    }

    private static ParseNode addChildToParseNode(ArrayList<ParseNode> language, ParseNode grammarNode, HashMap<ParseNode, Integer> nodeMap, int index, SynchronousGrammar<String> grammar, int command) {
        if (grammarNode == null) {
            if (command == 0) {
                return grammar.getEnglishList(nodeMap.get(language.get(index)));
            } else {
                return grammar.getTurkishList(nodeMap.get(language.get(index)));
            }
        }
        ParseNode child = new ParseNode(new Symbol(language.get(index).getData().getName()));
        if (!language.get(index).isLeaf()) {
            if (!nodeMap.containsKey(language.get(index))) {
                int grammarIndex = findIndex(language, nodeMap.keySet(), index);
                grammarNode.addChild(grammarIndex, child);
                nodeMap.put(language.get(index), grammarIndex);
            }
        }
        if (!nodeMap.containsKey(language.get(index))) {
            return grammarNode;
        }
        return grammarNode.getChild(nodeMap.get(language.get(index)));
    }

    private static void fillGrammarsForNotMatched(SynchronousGrammar<String> grammar, HashMap<ParseNode, Integer> nodeMap, ArrayList<ParseNode> turkish, ArrayList<ParseNode> english, ParseNode grammarTurkish, ParseNode grammarEnglish, HashMap<String, String> turkishEnglishMap) {
        for (int i = 0; i < english.size(); i++) {
            if (!nodeMap.containsKey(english.get(i)) && specialState(english.get(i), turkishEnglishMap)) {
                ParseNode child = addForSpecialState(english, i);
                int index = findIndex(english, nodeMap.keySet(), i);
                nodeMap.put(english.get(i), index);
                grammarEnglish.addChild(index, child);
            } else {
                for (int j = 0; j < turkish.size(); j++) {
                    int state = contains(turkish.get(j), english.get(i), turkishEnglishMap);
                    ParseNode currentTurkishChild;
                    ParseNode currentEnglishChild;
                    switch (state) {
                        case 0:
                            currentTurkishChild = addChildToParseNode(turkish, grammarTurkish, nodeMap, j, grammar, 1);
                            currentEnglishChild = addChildToParseNode(english, grammarEnglish, nodeMap, i, grammar, 0);
                            grammar.put(currentTurkishChild, currentEnglishChild);
                            break;
                        case 1:
                            currentTurkishChild = addChildToParseNode(turkish, grammarTurkish, nodeMap, j, grammar, 1);
                            fillGrammarsForNotMatched(grammar, nodeMap, createList(turkish.get(j), nodeMap.keySet()), english, currentTurkishChild, grammarEnglish, turkishEnglishMap);
                            break;
                        case 2:
                            currentEnglishChild = addChildToParseNode(english, grammarEnglish, nodeMap, i, grammar, 0);
                            fillGrammarsForNotMatched(grammar, nodeMap, turkish, createList(english.get(i), nodeMap.keySet()), grammarTurkish, currentEnglishChild, turkishEnglishMap);
                            break;
                    }
                }
            }
        }
    }

    private static boolean suitable(ParseNode turkish, ParseNode english, HashMap<String, String> turkishEnglishMap) {
        int count = 0;
        ArrayList<ParseNodeDrawable> turkishLeaves = new NodeDrawableCollector((ParseNodeDrawable) turkish, new IsTurkishLeafNode()).collect();
        ArrayList<ParseNodeDrawable> englishLeaves = new NodeDrawableCollector((ParseNodeDrawable) english, new IsEnglishLeafNode()).collect();
        for (ParseNodeDrawable turkishLeaf : turkishLeaves) {
            for (ParseNodeDrawable englishLeaf : englishLeaves) {
                if (turkishEnglishMap.get(turkishLeaf.getLayerData(ViewLayerType.TURKISH_WORD)).equals(englishLeaf.getData().getName())) {
                    count++;
                    break;
                }
            }
        }
        for (ParseNodeDrawable englishLeaf : englishLeaves) {
            if (!turkishEnglishMap.containsValue(englishLeaf.getData().getName())) {
                count++;
            }
        }
        return count == englishLeaves.size();
    }

    private static void addChildToGrammar(HashMap<ParseNode, Integer> nodeMap, SynchronousGrammar<String> grammar, ArrayList<ParseNode> language, int i, int command) {
        ParseNode languageNode = language.get(i);
        if (!nodeMap.containsKey(languageNode)) {
            ParseNode currentLanguage = new ParseNode(new Symbol(languageNode.getData().getName()));
            int index = findIndex(language, nodeMap.keySet(), i);
            nodeMap.put(languageNode, index);
            if (command == 0) {
                grammar.addEnglishList(index, currentLanguage);
            } else {
                grammar.addTurkishList(index, currentLanguage);
            }
        }
    }

    private static void fillGrammarsForMatched(ArrayList<ParseNode> turkish, ArrayList<ParseNode> english, ArrayList<SynchronousGrammar<String>> grammars, HashMap<String, String> turkishEnglishMap) {
        HashSet<ParseNode> used = new HashSet<>();
        HashMap<ParseNode, Integer> nodeMap = new HashMap<>();
        SynchronousGrammar<String> grammar = new SynchronousGrammar<>(turkish.get(0).getParent().getData().getName(), english.get(0).getParent().getData().getName());
        for (int i = 0; i < english.size(); i++) {
            ParseNode englishNode = english.get(i);
            if (specialState(englishNode, turkishEnglishMap)) {
                ParseNode child = addForSpecialState(english, i);
                if (!nodeMap.containsKey(englishNode)) {
                    int index = findIndex(english, used, i);
                    nodeMap.put(englishNode, index);
                    grammar.addEnglishList(index, child);
                    used.add(englishNode);
                }
            } else {
                for (int j = 0; j < turkish.size(); j++) {
                    ParseNode turkishNode = turkish.get(j);
                    int containing = contains(turkishNode, englishNode, turkishEnglishMap);
                    if (containing == 0 || (containing == 2 && suitable(turkishNode, englishNode, turkishEnglishMap))) {
                        used.add(turkishNode);
                        used.add(englishNode);
                        ParseNode currentTurkish, currentEnglish;
                        if (turkishNode.isLeaf()) {
                            currentTurkish = new ParseNode(new Symbol(((ParseNodeDrawable)turkishNode).getLayerData()));
                        } else {
                            currentTurkish = new ParseNode(new Symbol(turkishNode.getData().getName()));
                        }
                        grammar.addTurkishList(findIndex(turkish, used, j), currentTurkish);
                        currentEnglish = new ParseNode(new Symbol(englishNode.getData().getName()));
                        grammar.addEnglishList(findIndex(english, used, i), currentEnglish);
                        grammar.put(currentTurkish, currentEnglish);
                        ArrayList<ParseNode> turkishList = createList(turkishNode, used);
                        ArrayList<ParseNode> englishList = createList(englishNode, used);
                        if (!turkishList.isEmpty() && !englishList.isEmpty()) {
                            fillGrammarsForMatched(turkishList, englishList, grammars, turkishEnglishMap);
                        }
                        break;
                    }
                }
            }
        }
        for (int i = 0; i < turkish.size(); i++) {
            if (!used.contains(turkish.get(i))) {
                addChildToGrammar(nodeMap, grammar, turkish, i, 1);
            }
        }
        for (int i = 0; i < english.size(); i++) {
            if (!used.contains(english.get(i))) {
                addChildToGrammar(nodeMap, grammar, english, i, 0);
            }
        }
        fillGrammarsForNotMatched(grammar, nodeMap, createList(turkish, used), createList(english, used), null, null, turkishEnglishMap);
        grammars.add(grammar);
    }

    private static HashMap<String, String> generateTurkishEnglishMap(ParseNode root) {
        HashMap<String, String> turkishEnglishMap = new HashMap<>();
        ArrayList<ParseNodeDrawable> list = new NodeDrawableCollector((ParseNodeDrawable) root, new IsTurkishLeafNode()).collect();
        for (ParseNodeDrawable parseNodeDrawable : list) {
            turkishEnglishMap.put(parseNodeDrawable.getLayerData(ViewLayerType.TURKISH_WORD), parseNodeDrawable.getData().getName());
        }
        return turkishEnglishMap;
    }

    public static ArrayList<SynchronousGrammar<String>> generate(ParseNode turkish, ParseNode english) {
        HashMap<String, String> turkishEnglishMap = generateTurkishEnglishMap(turkish);
        ArrayList<SynchronousGrammar<String>> grammars = new ArrayList<>();
        ArrayList<ParseNode> turkishChildren = new ArrayList<>();
        for (int i = 0; i < turkish.numberOfChildren(); i++) {
            turkishChildren.add(turkish.getChild(i));
        }
        ArrayList<ParseNode> englishChildren = new ArrayList<>();
        for (int i = 0; i < english.numberOfChildren(); i++) {
            englishChildren.add(english.getChild(i));
        }
        fillGrammarsForMatched(turkishChildren, englishChildren, grammars, turkishEnglishMap);
        return grammars;
    }
}
