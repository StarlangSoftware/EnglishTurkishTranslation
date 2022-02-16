package Translation.Tree.Rule;

import AnnotatedTree.ParseNodeDrawable;
import AnnotatedTree.Processor.Condition.NodeDrawableCondition;
import ContextFreeGrammar.ContextFreeGrammar;
import ContextFreeGrammar.Rule;

public class IsNodeWithRule implements NodeDrawableCondition {
    private Rule rule;

    public IsNodeWithRule(Rule rule){
        this.rule = rule;
    }

    public boolean satisfies(ParseNodeDrawable parseNode) {
        if (parseNode.numberOfChildren() > 0){
            Rule rule = ContextFreeGrammar.toRule(parseNode, true);
            if (rule != null){
                return rule.toString().equals(this.rule.toString());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
