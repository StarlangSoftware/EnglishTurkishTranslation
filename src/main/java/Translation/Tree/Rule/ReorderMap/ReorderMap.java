package Translation.Tree.Rule.ReorderMap;

import AnnotatedTree.NodePermutation;
import AnnotatedTree.ParseNodeDrawable;
import AnnotatedTree.ParseTreeDrawable;
import AnnotatedTree.TreeBankDrawable;
import ContextFreeGrammar.ContextFreeGrammar;
import ContextFreeGrammar.Rule;
import ParseTree.ParseNode;
import Translation.Phrase.ScoredSentence;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ReorderMap {

    private HashMap <Rule,NodePermutationSet> reorders;

    public ReorderMap(){
        reorders = new HashMap<Rule, NodePermutationSet>();
    }

    public void constructReorderMap(TreeBankDrawable fromTreeBank, TreeBankDrawable toTreeBank){
        reorders = new HashMap<Rule, NodePermutationSet>();
        for (int i = 0; i < fromTreeBank.size(); i++){
            ParseTreeDrawable fromTree = fromTreeBank.get(i);
            ParseTreeDrawable toTree = toTreeBank.get(i);
            if (fromTree.isPermutation(toTree))
                addReorder(toTree, fromTree);
            else
                System.out.println("Permutation Error with file name: " + fromTree.getName());
        }
        for (Map.Entry<Rule,NodePermutationSet> entry:reorders.entrySet()){
            NodePermutationSet ps = entry.getValue();
            int total = 0;
            for (NodePermutation p:ps.nodePermutations)
                total += p.count;
        }
    }

    public ReorderMap(String fromDirectory, String toDirectory, String pattern, boolean includePunctuation){
        TreeBankDrawable fromTreeBank = new TreeBankDrawable(new File(fromDirectory), pattern);
        TreeBankDrawable toTreeBank = new TreeBankDrawable(new File(toDirectory), pattern);
        if (!includePunctuation){
            fromTreeBank.stripPunctuation();
            toTreeBank.stripPunctuation();
        }
        constructReorderMap(fromTreeBank, toTreeBank);
    }

    public ArrayList<ScoredSentence> allPermutations(ParseNodeDrawable parseNodeDrawable){
        if (parseNodeDrawable.numberOfChildren() < 2){
            if (parseNodeDrawable.toSentence().trim().length() > 0)
                return new ArrayList<>(Arrays.asList(new ScoredSentence(parseNodeDrawable.toSentence().trim())));
            return new ArrayList<>(Arrays.asList(new ScoredSentence()));
        }
        ArrayList<ArrayList<ScoredSentence>> listOfChildrenSentences = new ArrayList<>();
        for (int i = 0; i < parseNodeDrawable.numberOfChildren(); i++)
            listOfChildrenSentences.add(allPermutations(((ParseNodeDrawable)parseNodeDrawable.getChild(i))));
        Rule rule = ContextFreeGrammar.toRule(parseNodeDrawable, true);
        NodePermutationSet ps = permutationSet(rule);
        if (ps == null) {
            ps = new NodePermutationSet();
            ps.nodePermutations = new ArrayList<>(Arrays.asList(new NodePermutation(parseNodeDrawable.numberOfChildren())));
        }
        ArrayList<ScoredSentence> sentences = new ArrayList<>();
        for (NodePermutation nodePermutation :ps.nodePermutations){
            ArrayList<ScoredSentence> currentChildSentences = listOfChildrenSentences.get(nodePermutation.nodePermutation.get(0));
            for (int i = 1; i < nodePermutation.nodePermutation.size(); i++) {
                ArrayList<ScoredSentence> nextChildSentences = listOfChildrenSentences.get(nodePermutation.nodePermutation.get(i));
                ArrayList<ScoredSentence> tmp = new ArrayList<>();
                for (ScoredSentence currentChildSentence : currentChildSentences)
                    for (ScoredSentence nextChildSentence : nextChildSentences)
                        tmp.add(currentChildSentence.join(nextChildSentence));
                currentChildSentences = tmp;
            }
            for (ScoredSentence s : currentChildSentences) {
                sentences.add(s);
            }
        }
        return sentences;
    }

    public void addReorder(ParseTreeDrawable toTree, ParseTreeDrawable fromTree){
        addReorder((ParseNodeDrawable)toTree.getRoot(), ((ParseNodeDrawable)fromTree.getRoot()));
    }

    public void addReorder(ParseNodeDrawable toNode, ParseNodeDrawable fromNode){
        if (fromNode.numberOfChildren() < 2)
            return;
        if (!fromNode.isPermutation(toNode))
            return;
        NodePermutation nodePermutation = new NodePermutation(fromNode, toNode);
        Rule rule = ContextFreeGrammar.toRule(toNode, true);
        addReorder(rule, nodePermutation);
        for (int i = 0; i < fromNode.numberOfChildren(); i++)
            addReorder((ParseNodeDrawable)toNode.getChild(nodePermutation.nodePermutation.get(i)), ((ParseNodeDrawable)fromNode.getChild(i)));
    }

    public void addReorder(Rule rule, NodePermutation nodePermutation){
        if (!reorders.containsKey(rule))
            reorders.put(rule, new NodePermutationSet());
        reorders.get(rule).insert(nodePermutation);
    }

    public NodePermutationSet permutationSet(Rule rule){
        if (reorders.containsKey(rule))
            return reorders.get(rule);
        return null;
    }

    public ArrayList<ScoredSentence> allPermutations(ParseTreeDrawable parseTreeDrawable){
        if (parseTreeDrawable.getRoot() != null){
            return allPermutations(((ParseNodeDrawable)parseTreeDrawable.getRoot()));
        } else {
            return null;
        }
    }

    public void mlTranslate(ParseTreeDrawable parseTreeDrawable){
        mlTranslate(((ParseNodeDrawable)parseTreeDrawable.getRoot()));
    }

    public void mlTranslate(ParseNodeDrawable parseNodeDrawable){
        if (parseNodeDrawable.numberOfChildren() < 2)
            return;
        Rule rule = ContextFreeGrammar.toRule(parseNodeDrawable, true);
        NodePermutationSet ps = permutationSet(rule);
        ArrayList<ParseNode> children = new ArrayList<>();
        for (int i = 0; i < parseNodeDrawable.numberOfChildren(); i++){
            children.add(parseNodeDrawable.getChild(i));
        }
        if (ps != null)
            ps.mlPermutation().apply(children);
        for (ParseNode child: children)
            mlTranslate(((ParseNodeDrawable)child));
    }

    public String toString(){
        String result = "";
        for (Map.Entry<Rule,NodePermutationSet> entry:reorders.entrySet()){
            NodePermutationSet ps = entry.getValue();
            result = result + entry.getKey().toString() + "\n";
            result = result + ps.toString();
        }
        return result;
    }
}


