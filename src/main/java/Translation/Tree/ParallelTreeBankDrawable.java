package Translation.Tree;

import AnnotatedSentence.ViewLayerType;
import AnnotatedTree.ParseTreeDrawable;
import AnnotatedTree.Processor.LeafConverter.LeafToEnglish;
import AnnotatedTree.Processor.LeafConverter.LeafToTurkish;
import AnnotatedTree.Processor.TreeToStringConverter;
import AnnotatedTree.TreeBankDrawable;
import Corpus.Sentence;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import ParseTree.ParseTree;
import ParseTree.ParallelTreeBank;
import Sampling.KFoldCrossValidation;
import Translation.BleuMeasure;
import Translation.Phrase.*;
import Translation.Tree.Rule.ReorderMap.ReorderMap;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class ParallelTreeBankDrawable extends ParallelTreeBank {

    public ParallelTreeBankDrawable(File folder1, File folder2){
        fromTreeBank = new TreeBankDrawable(folder1);
        toTreeBank = new TreeBankDrawable(folder2);
        removeDifferentTrees();
    }

    public ParallelTreeBankDrawable(File folder1, File folder2, String pattern){
        fromTreeBank = new TreeBankDrawable(folder1, pattern);
        toTreeBank = new TreeBankDrawable(folder2, pattern);
        removeDifferentTrees();
    }

    public double interAnnotatorGlossAgreement(ViewLayerType viewLayerType){
        int agreement = 0, total = 0;
        for (int i = 0; i < fromTreeBank.size(); i++){
            ParseTreeDrawable parseTree1 = (ParseTreeDrawable) fromTreeBank.get(i);
            ParseTreeDrawable parseTree2 = (ParseTreeDrawable) toTreeBank.get(i);
            total += parseTree1.leafCount();
            agreement += parseTree1.glossAgreementCount(parseTree2, viewLayerType);
        }
        return agreement / (total + 0.0);
    }

    public double interAnnotatorStructureAgreement(){
        int agreement = 0, total = 0;
        for (int i = 0; i < fromTreeBank.size(); i++){
            ParseTreeDrawable parseTree1 = (ParseTreeDrawable) fromTreeBank.get(i);
            ParseTreeDrawable parseTree2 = (ParseTreeDrawable) toTreeBank.get(i);
            total += parseTree1.nodeCountWithMultipleChildren();
            agreement += parseTree1.structureAgreementCount(parseTree2);
        }
        return agreement / (total + 0.0);
    }

    public void stripPunctuation(){
        fromTreeBank.stripPunctuation();
        toTreeBank.stripPunctuation();
    }

    public void runTreeReorderExperiment(ReorderMap reorderMap, boolean includePunctuation){
        int countCorrect = 0;
        int countTotal = 0;
        if (!includePunctuation){
            stripPunctuation();
        }
        for (int i = 0; i < fromTreeBank.size(); i++){
            ParseTreeDrawable testTreeCandidate = (ParseTreeDrawable) fromTreeBank.get(i);
            ParseTreeDrawable testTreeCorrect = (ParseTreeDrawable) toTreeBank.get(i);
            reorderMap.mlTranslate(testTreeCandidate);
            countCorrect = countCorrect + testTreeCandidate.score(testTreeCorrect);
            countTotal = countTotal + testTreeCandidate.nodeCountWithMultipleChildren();
        }
        System.out.println(countCorrect + " out of " + countTotal);
    }



    public void runTranslationExperiment(int maxStackSize, boolean optimalTree, int seed){
        PartialTranslation bestTranslation;
        Sentence sentence;
        BufferedWriter fromLanguageFile, goldLanguageFile, learnedLanguageFile, bleuFile;
        String concatWith;
        try {
            FsmMorphologicalAnalyzer fsm = new FsmMorphologicalAnalyzer();
            ExhaustiveTranslation translation = new ExhaustiveTranslation(TranslationType.WORD_BASED, fsm, maxStackSize);
            BleuMeasure bleu = new BleuMeasure();
            IBMModel1Turkish model = new IBMModel1Turkish();
            KFoldCrossValidation<ParseTree> fromCrossValidation, toCrossValidation;
            fromCrossValidation = new KFoldCrossValidation<>(((TreeBankDrawable)fromTreeBank).getParseTrees(), 10, seed);
            toCrossValidation = new KFoldCrossValidation<>(((TreeBankDrawable)toTreeBank).getParseTrees(), 10, seed);
            if (optimalTree){
                concatWith = "optimal";
            } else {
                concatWith = "";
            }
            bleuFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("bleu"+concatWith+maxStackSize+".txt"), "UTF-8"));
            for (int k = 0; k < 10; k++){
                fromLanguageFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("from-"+concatWith+maxStackSize+"-"+k+".txt"), "UTF-8"));
                goldLanguageFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("gold-"+concatWith+maxStackSize+"-"+k+".txt"), "UTF-8"));
                learnedLanguageFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("learned-"+concatWith+maxStackSize+"-"+k+".txt"), "UTF-8"));
                TreeBankDrawable fromTrainTreeBank = new TreeBankDrawable(fromCrossValidation.getTrainFold(k));
                TreeBankDrawable fromTestTreeBank = new TreeBankDrawable(fromCrossValidation.getTestFold(k));
                TreeBankDrawable toTrainTreeBank = new TreeBankDrawable(toCrossValidation.getTrainFold(k));
                TreeBankDrawable toTestTreeBank = new TreeBankDrawable(toCrossValidation.getTestFold(k));
                AutomaticTranslationDictionary automaticTranslationDictionary = new AutomaticTranslationDictionary(toTrainTreeBank, ViewLayerType.ENGLISH_WORD, ViewLayerType.META_MORPHEME_MOVED, "tmpdictionary.xml");
                System.out.println("Translation Dictionary Prepared");
                ReorderMap reorderMap = new ReorderMap();
                reorderMap.constructReorderMap(fromTrainTreeBank, toTrainTreeBank);
                System.out.println("Reordermap Prepared");
                model.loadModel("tmpdictionary.xml");
                for (int i = 0; i < fromTestTreeBank.size(); i++){
                    ParseTreeDrawable fromTree = fromTestTreeBank.get(i);
                    ParseTreeDrawable toTree = toTestTreeBank.get(i);
                    if (optimalTree){
                        TreeToStringConverter treeToStringConverter = new TreeToStringConverter(toTree, new LeafToEnglish());
                        sentence = new Sentence(treeToStringConverter.convert());
                    } else {
                        ArrayList<ScoredSentence> sentences =  reorderMap.allPermutations(fromTree);
                        Collections.sort(sentences, new ScoredSentenceComparator());
                        sentence = new Sentence(sentences.get(0).toString());
                    }
                    bestTranslation = translation.bestTranslation(model, sentence);
                    fromLanguageFile.write(fromTree.toSentence() + "\n");
                    TreeToStringConverter treeToStringConverter = new TreeToStringConverter(toTree, new LeafToTurkish());
                    goldLanguageFile.write(treeToStringConverter.convert() + "\n");
                    if (bestTranslation != null){
                        learnedLanguageFile.write(bestTranslation.getTargetSentence() + "\n");
                    } else {
                        learnedLanguageFile.write(fromTree.toSentence() + "\n");
                    }
                }
                fromLanguageFile.close();
                goldLanguageFile.close();
                learnedLanguageFile.close();
                try {
                    bleuFile.write(bleu.execute("gold-"+concatWith+maxStackSize+"-"+k+".txt", "learned-"+concatWith+maxStackSize+"-"+k+".txt", false) + "\n");
                } catch (IOException e) {
                }
            }
            bleuFile.close();
        } catch (IOException e) {
        }
    }

}
