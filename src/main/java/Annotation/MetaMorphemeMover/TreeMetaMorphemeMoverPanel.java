package Annotation.MetaMorphemeMover;

import AnnotatedSentence.LayerNotExistsException;
import AnnotatedSentence.ViewLayerType;
import AnnotatedTree.LayerItemNotExistsException;
import AnnotatedTree.ParseNodeDrawable;
import AnnotatedTree.WordNotExistsException;
import DataCollector.ParseTree.TreeAction.MetaMorphemeMoveAction;
import DataCollector.ParseTree.TreeStructureEditorPanel;
import ParseTree.ParseNode;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;

public class TreeMetaMorphemeMoverPanel extends TreeStructureEditorPanel {

    private ParseNodeDrawable draggedNode = null;
    private int selectedIndex, dragX, dragY;
    private ParseNodeDrawable fromNode = null;
    private boolean dragged = false;

    public TreeMetaMorphemeMoverPanel(String path, String fileName) {
        super(path, fileName, ViewLayerType.META_MORPHEME_MOVED);
        widthDecrease = 30;
        heightDecrease = 120;
        setFocusable(false);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    private void clear(){
        selectedIndex = -1;
    }

    protected void nextTree(int count){
        clear();
        super.nextTree(count);
    }

    protected void previousTree(int count){
        clear();
        super.previousTree(count);
    }

    public void mouseClicked(MouseEvent mouseEvent) {
        ParseNodeDrawable node = currentTree.getNodeAt(mouseEvent.getX(), mouseEvent.getY());
        if (node != null){
            if (editableNode != null)
                editableNode.setEditable(false);
            editableNode = node;
            editableNode.setEditable(true);
            isEditing = false;
            this.repaint();
            this.setFocusable(true);
        }
    }

    public void mousePressed(MouseEvent mouseEvent) {
        ParseNode node = currentTree.getLeafNodeAt(mouseEvent.getX(), mouseEvent.getY());
        if (node == previousNode && previousNode != null){
            fromNode = previousNode;
        }
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        ParseNodeDrawable node = currentTree.getLeafNodeAt(mouseEvent.getX(), mouseEvent.getY());
        if (fromNode != null && node != null && fromNode != node && dragged && fromNode.numberOfChildren() == 0 && node.numberOfChildren() == 0 && node.getLayerData(ViewLayerType.TURKISH_WORD).equalsIgnoreCase("*NONE*")){
            draggedNode.setDragged(false);
            MetaMorphemeMoveAction action = new MetaMorphemeMoveAction(this, fromNode.getLayerInfo(), node.getLayerInfo(), selectedIndex);
            action.execute();
            actionList.add(action);
        }
        dragged = false;
        fromNode = null;
        this.repaint();
    }

    public void mouseDragged(MouseEvent mouseEvent) {
        ParseNodeDrawable node = currentTree.getLeafNodeAt(mouseEvent.getX(), mouseEvent.getY());
        dragged = true;
        if (node != null && node != previousNode && node.numberOfChildren() == 0 && node.getLayerData(ViewLayerType.TURKISH_WORD).equalsIgnoreCase("*NONE*")){
            draggedNode = node;
            draggedNode.setDragged(true);
            this.repaint();
        } else {
            if (node == null && draggedNode != null){
                draggedNode.setDragged(false);
                draggedNode = null;
                this.repaint();
            } else {
                if (previousNode != null){
                    dragX = mouseEvent.getX();
                    dragY = mouseEvent.getY();
                    this.repaint();
                }
            }
        }
    }

    public void mouseMoved(MouseEvent mouseEvent) {
        ParseNodeDrawable node = currentTree.getNodeAt(mouseEvent.getX(), mouseEvent.getY());
        if (node != null && node.isLeaf()){
            if (!dragged){
                if (node != null){
                    selectedIndex = currentTree.getSubItemAt(mouseEvent.getX(), mouseEvent.getY());
                    if (selectedIndex != 0){
                        node.setSelected(true, selectedIndex);
                        if (node != previousNode){
                            if (previousNode != null)
                                previousNode.setSelected(false);
                        }
                        previousNode = node;
                        this.repaint();
                    }
                } else {
                    if (previousNode != null){
                        previousNode.setSelected(false);
                        previousNode = null;
                        this.repaint();
                    }
                }
            }
        } else {
            super.mouseMoved(mouseEvent);
        }
    }

    public void mouseEntered(MouseEvent mouseEvent) {
    }

    public void mouseExited(MouseEvent mouseEvent) {
    }

    protected void paintComponent(Graphics g){
        int startX, startY;
        Point2D.Double pointCtrl1, pointCtrl2, pointStart, pointEnd;
        CubicCurve2D.Double cubicCurve;
        super.paintComponent(g);
        if (dragged && previousNode != null){
            startX = previousNode.getArea().getX() + previousNode.getArea().getWidth() / 2;
            startY = previousNode.getArea().getY() + 20 * (previousNode.getSelectedIndex() + 1);
            pointStart = new Point2D.Double(startX, startY);
            pointEnd = new Point2D.Double(dragX, dragY);
            if (dragY > startY){
                pointCtrl1 = new Point2D.Double(startX, (startY + dragY) / 2);
                pointCtrl2 = new Point2D.Double((startX + dragX) / 2, dragY);
            } else {
                pointCtrl1 = new Point2D.Double((startX + dragX) / 2, startY);
                pointCtrl2 = new Point2D.Double(dragX, (startY + dragY) / 2);
            }
            cubicCurve = new CubicCurve2D.Double(pointStart.x, pointStart.y, pointCtrl1.x, pointCtrl1.y, pointCtrl2.x, pointCtrl2.y, pointEnd.x, pointEnd.y);
            Graphics2D g2 = (Graphics2D)g;
            g2.setColor(Color.MAGENTA);
            g2.draw(cubicCurve);
        }
    }

    protected int getStringSize(ParseNodeDrawable parseNode, Graphics g) {
        int i, stringSize = 0;
        if (parseNode.numberOfChildren() == 0) {
            if (parseNode.getLayerInfo().getLayerSize(ViewLayerType.META_MORPHEME_MOVED) == 0){
                return g.getFontMetrics().stringWidth(parseNode.getLayerData(ViewLayerType.TURKISH_WORD));
            }
            for (i = 0; i < parseNode.getLayerInfo().getLayerSize(ViewLayerType.META_MORPHEME_MOVED); i++)
                try {
                    if (g.getFontMetrics().stringWidth(parseNode.getLayerInfo().getLayerInfoAt(ViewLayerType.META_MORPHEME_MOVED, i)) > stringSize){
                        stringSize = g.getFontMetrics().stringWidth(parseNode.getLayerInfo().getLayerInfoAt(ViewLayerType.META_MORPHEME_MOVED, i));
                    }
                } catch (LayerNotExistsException | LayerItemNotExistsException e) {
                    return g.getFontMetrics().stringWidth(parseNode.getData().getName());
                }
            return stringSize;
        } else {
            return g.getFontMetrics().stringWidth(parseNode.getData().getName());
        }
    }

    protected void drawString(ParseNodeDrawable parseNode, Graphics g, int x, int y){
        int i;
        if (parseNode.numberOfChildren() == 0){
            if (parseNode.getLayerInfo().getLayerSize(ViewLayerType.META_MORPHEME_MOVED) == 0){
                g.drawString(parseNode.getLayerData(ViewLayerType.TURKISH_WORD), x, y);
            }
            for (i = 0; i < parseNode.getLayerInfo().getLayerSize(ViewLayerType.META_MORPHEME_MOVED); i++){
                if (i > 0 && !parseNode.isGuessed()){
                    g.setColor(Color.RED);
                }
                try {
                    g.drawString(parseNode.getLayerInfo().getLayerInfoAt(ViewLayerType.META_MORPHEME_MOVED, i), x, y);
                    y += 20;
                } catch (LayerNotExistsException | LayerItemNotExistsException e) {
                    g.drawString(parseNode.getData().getName(), x, y);
                }
            }
        } else {
            g.drawString(parseNode.getData().getName(), x, y);
        }
    }

    protected void setArea(ParseNodeDrawable parseNode, int x, int y, int stringSize){
        if (parseNode.numberOfChildren() == 0){
            parseNode.setArea(x - 5, y - 15, stringSize + 10, 20 * (parseNode.getLayerInfo().getLayerSize(ViewLayerType.META_MORPHEME_MOVED) + 1));
        } else {
            parseNode.setArea(x - 5, y - 15, stringSize + 10, 20);
        }
    }

}
