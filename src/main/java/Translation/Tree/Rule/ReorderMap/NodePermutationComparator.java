package Translation.Tree.Rule.ReorderMap;

import AnnotatedTree.NodePermutation;

import java.util.Comparator;

public class NodePermutationComparator implements Comparator<NodePermutation> {

    public int compare(NodePermutation permA, NodePermutation permB){
        return permA.count - permB.count;
    }

}
