package SentenceAlignment;

import Corpus.Paragraph;

public class SentenceAlignment {

    final int PENALTY21 = 230;	/* -i00 * log([prob of 2-1 match] / [prob of 1-1 match]) */
    final int PENALTY22 = 440;	/*	-100	* Iog([prob of 2-2 match] / [prob of 1-1 match]) */
    final int PENALTY01 = 450;	/*	-100	* log([prob of 0-i match] / [prob of I-i match]) */
    private Paragraph firstParagraph;
    private Paragraph secondParagraph;

    private class Cell{
        int distance;
        SentenceAlignmentType from;

        public Cell(int distance, int from){
            this.distance = distance;
            switch (from){
                case 0:this.from = SentenceAlignmentType.SUBSTITUTION;
                    break;
                case 1:this.from = SentenceAlignmentType.DELETION;
                    break;
                case 2:this.from = SentenceAlignmentType.INSERTION;
                    break;
                case 3:this.from = SentenceAlignmentType.CONTRACTION;
                    break;
                case 4:this.from = SentenceAlignmentType.EXPANSION;
                    break;
                case 5:this.from = SentenceAlignmentType.MERGE;
                    break;
            }
        }

    }

    public SentenceAlignment(Paragraph firstParagraph, Paragraph secondParagraph){
        this.firstParagraph = firstParagraph;
        this.secondParagraph = secondParagraph;
    }

    private double normal(double z){
        double t, pd;
        t = 1 / (1 + 0.2316419 * z);
        pd	=	1 - 0.3989423 * Math.exp(-z * z/2) * ((((1.330274429 * t - 1.821255978) * t + 1.781477937) * t - 0.356563782) * t + 0.319381530) * t;
        return(pd);
    }

    private int match(int len1, int len2){
        double z, pd, mean;
        double s2 = 6.8;
        if (len1 == 0 && len2 == 0)
            return 0;
        mean = (len1 + len2) / 2.0;
        z = (len1 - len2) / Math.sqrt(s2 * mean);
        if (z < 0)
            z = -z;
        pd = 2 * (1 - normal(z));
        if (pd > 0)
            return (int)(-100 * Math.log(pd));
        else
            return Integer.MAX_VALUE;
    }

    private int substitutionCost(int x1, int y1){
        return match(x1, y1);
    }

    private int insertionCost(int y1){
        return match(0, y1) + PENALTY01;
    }

    private int deletionCost(int x1){
        return match(x1, 0) + PENALTY01;
    }

    private int expansionCost(int x1, int y1, int y2){
        return (match(x1, y1 + y2) + PENALTY21);
    }

    private int contractionCost(int x1, int y1, int x2){
        return (match(x1 + x2, y1) + PENALTY21);
    }

    private int mergeCost(int x1, int y1, int x2, int y2){
        return (match(x1 + x2, y1 + y2) + PENALTY22);
    }

    public SentenceAlignmentType[] galeChurch(){
        SentenceAlignmentType[] alignment;
        int i, j, k, minCost, minIndex, n, moveCount;
        int[] costs = new int[6];
        Cell[][] dist = new Cell[firstParagraph.sentenceCount() + 1][secondParagraph.sentenceCount() + 1];
        for (j = 0; j <= secondParagraph.sentenceCount(); j++)
            for (i = 0; i <= firstParagraph.sentenceCount(); i++){
                if (i > 0 && j > 0)
                    costs[0] = dist[i - 1][j - 1].distance + substitutionCost(firstParagraph.getSentence(i - 1).charCount(), secondParagraph.getSentence(j - 1).charCount());
                else
                    costs[0] = Integer.MAX_VALUE;
                if (i > 0)
                    costs[1] = dist[i - 1][j].distance + deletionCost(firstParagraph.getSentence(i - 1).charCount());
                else
                    costs[1] = Integer.MAX_VALUE;
                if (j > 0)
                    costs[2] = dist[i][j - 1].distance + insertionCost(secondParagraph.getSentence(j - 1).charCount());
                else
                    costs[2] = Integer.MAX_VALUE;
                if (i > 1 && j > 0)
                    costs[3] = dist[i - 2][j - 1].distance + contractionCost(firstParagraph.getSentence(i - 2).charCount(), secondParagraph.getSentence(j - 1).charCount(), firstParagraph.getSentence(i - 1).charCount());
                else
                    costs[3] = Integer.MAX_VALUE;
                if (i > 0 && j > 1)
                    costs[4] = dist[i - 1][j - 2].distance + expansionCost(firstParagraph.getSentence(i - 1).charCount(), secondParagraph.getSentence(j - 2).charCount(), secondParagraph.getSentence(j - 1).charCount());
                else
                    costs[4] = Integer.MAX_VALUE;
                if (i > 1 && j > 1)
                    costs[5] = dist[i - 2][j - 2].distance + mergeCost(firstParagraph.getSentence(i - 2).charCount(), secondParagraph.getSentence(j - 2).charCount(), firstParagraph.getSentence(i - 1).charCount(), secondParagraph.getSentence(j - 1).charCount());
                else
                    costs[5] = Integer.MAX_VALUE;
                minCost = costs[0];
                minIndex = 0;
                for (k = 1; k < 6; k++)
                    if (costs[k] < minCost){
                        minCost = costs[k];
                        minIndex = k;
                    }
                if (minCost == Integer.MAX_VALUE)
                    dist[i][j] = new Cell(0, minIndex);
                else
                    dist[i][j] = new Cell(minCost, minIndex);
            }
        i = firstParagraph.sentenceCount();
        j = secondParagraph.sentenceCount();
        moveCount = 0;
        while (i > 0 || j > 0){
            moveCount++;
            switch (dist[i][j].from){
                case SUBSTITUTION:i = i - 1;
                    j = j - 1;
                    break;
                case DELETION:i = i - 1;
                    break;
                case INSERTION:j = j - 1;
                    break;
                case CONTRACTION:i = i - 2;
                    j = j - 1;
                    break;
                case EXPANSION:i = i - 1;
                    j = j - 2;
                    break;
                case MERGE:i = i - 2;
                    j = j - 2;
                    break;
            }
        }
        alignment = new SentenceAlignmentType[moveCount];
        n = moveCount - 1;
        while (i > 0 || j > 0){
            alignment[n] = dist[i][j].from;
            n--;
            switch (dist[i][j].from){
                case SUBSTITUTION:i = i - 1;
                    j = j - 1;
                    break;
                case DELETION:i = i - 1;
                    break;
                case INSERTION:j = j - 1;
                    break;
                case CONTRACTION:i = i - 2;
                    j = j - 1;
                    break;
                case EXPANSION:i = i - 1;
                    j = j - 2;
                    break;
                case MERGE:i = i - 2;
                    j = j - 2;
                    break;
            }
        }
        return alignment;
    }

}
