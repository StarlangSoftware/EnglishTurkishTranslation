package Translation.Tree.Rule.Grammar;/* Created by oguzkeremyildiz on 12.05.2021 */

import ParseTree.ParseNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class SynchronousGrammar<Symbol> {

    private Symbol turkish;
    private Symbol english;
    private ArrayList<ParseNode> turkishList;
    private ArrayList<ParseNode> englishList;
    private HashMap<ParseNode, ParseNode> parseNodeDrawableMap;

    public SynchronousGrammar(Symbol turkish, Symbol english) {
        this.turkish = turkish;
        this.english = english;
        turkishList = new ArrayList<>();
        englishList = new ArrayList<>();
        parseNodeDrawableMap = new HashMap<>();
    }

    private void addToMap(String data, Integer hashCode, HashMap<Integer, ParseNode> nodeMap) {
        if (!nodeMap.containsKey(hashCode)) {
            ParseNode node = new ParseNode(new ParseTree.Symbol(data));
            nodeMap.put(hashCode, node);
        }
    }

    private void addChild(String line, HashMap<Integer, ParseNode> nodeMap) {
        String[] split = line.split(" ");
        int parentHash = Integer.parseInt(split[0].substring(split[0].lastIndexOf("=") + 1));
        addToMap(split[0].substring(0, split[0].lastIndexOf("=")), parentHash, nodeMap);
        ParseNode parent = nodeMap.get(parentHash);
        for (int i = 1; i < split.length; i++) {
            int childHash = Integer.parseInt(split[i].substring(split[i].lastIndexOf("=") + 1));
            addToMap(split[i].substring(0, split[i].lastIndexOf("=")), childHash, nodeMap);
            ParseNode child = nodeMap.get(childHash);
            parent.addChild(child);
        }
    }

    public SynchronousGrammar(FileInputStream file) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(file, StandardCharsets.UTF_8));
            String line = br.readLine();
            String turkish = line.substring(0, line.indexOf("/"));
            String english = line.substring(line.indexOf("/") + 1);
            this.turkish = (Symbol) turkish;
            this.english = (Symbol) english;
            HashMap<Integer, ParseNode> nodeMap = new HashMap<>();
            line = br.readLine();
            turkishList = new ArrayList<>();
            int listSize = Integer.parseInt(line);
            for (int i = 0; i < listSize; i++) {
                String[] split = br.readLine().split(" ");
                addToMap(split[0], Integer.parseInt(split[1]), nodeMap);
                turkishList.add(nodeMap.get(Integer.parseInt(split[1])));
                int size = Integer.parseInt(split[2]);
                for (int j = 0; j < size; j++) {
                    addChild(br.readLine(), nodeMap);
                }
            }
            line = br.readLine();
            englishList = new ArrayList<>();
            listSize = Integer.parseInt(line);
            for (int i = 0; i < listSize; i++) {
                String[] split = br.readLine().split(" ");
                addToMap(split[0], Integer.parseInt(split[1]), nodeMap);
                englishList.add(nodeMap.get(Integer.parseInt(split[1])));
                int size = Integer.parseInt(split[2]);
                for (int j = 0; j < size; j++) {
                    addChild(br.readLine(), nodeMap);
                }
            }
            parseNodeDrawableMap = new HashMap<>();
            String[] split = br.readLine().split(" ");
            for (int i = 0; i < split.length; i += 2) {
                int turkishHash = Integer.parseInt(split[i]);
                int englishHash = Integer.parseInt(split[i + 1]);
                parseNodeDrawableMap.put(nodeMap.get(turkishHash), nodeMap.get(englishHash));
            }
        } catch (IOException ignored) {}
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

    private void generate(HashMap<SimpleEntry<String, Integer>, ArrayList<SimpleEntry<String, Integer>>> map, ParseNode root) {
        SimpleEntry<String, Integer> entry = new SimpleEntry<>(root.getData().getName(), root.hashCode());
        map.put(entry, new ArrayList<>());
        for (int i = 0; i < root.numberOfChildren(); i++) {
            ParseNode child = root.getChild(i);
            map.get(entry).add(new SimpleEntry<>(child.getData().getName(), child.hashCode()));
            if (child.numberOfChildren() > 0) {
                generate(map, child);
            }
        }
    }

    private HashMap<SimpleEntry<String, Integer>, ArrayList<SimpleEntry<String, Integer>>> generateMap(ParseNode root) {
        HashMap<SimpleEntry<String, Integer>, ArrayList<SimpleEntry<String, Integer>>> map = new HashMap<>();
        generate(map, root);
        return map;
    }

    private void saveNodes(BufferedWriter writer, ArrayList<ParseNode> list) throws IOException {
        writer.write(Integer.toString(list.size()));
        writer.newLine();
        for (ParseNode parseNode : list) {
            HashMap<SimpleEntry<String, Integer>, ArrayList<SimpleEntry<String, Integer>>> map = generateMap(parseNode);
            writer.write(parseNode.getData().getName() + " " + parseNode.hashCode() + " " + map.size());
            writer.newLine();
            for (SimpleEntry<String, Integer> key : map.keySet()) {
                writer.write(key + " ");
                for (int j = 0; j < map.get(key).size(); j++) {
                    writer.write(map.get(key).get(j).toString());
                    if (j + 1 != map.get(key).size()) {
                        writer.write(" ");
                    }
                }
                writer.newLine();
            }
        }
    }

    public void save(String fileName) {
        try {
            BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8));
            fw.write(this.toString());
            fw.newLine();
            saveNodes(fw, turkishList);
            saveNodes(fw, englishList);
            int iterate = 0;
            for (ParseNode key : parseNodeDrawableMap.keySet()) {
                iterate++;
                fw.write(key.hashCode() + " " + parseNodeDrawableMap.get(key).hashCode());
                if (iterate != parseNodeDrawableMap.size()) {
                    fw.write(" ");
                }
            }
            fw.close();
        } catch (IOException ignored) {}
    }

    @Override
    public String toString() {
        return turkish.toString() + "/" + english.toString();
    }
}
