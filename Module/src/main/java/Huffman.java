import java.io.*;
import java.util.*;

/**
 * Project 3: Huffman Encoding / Decoding.
 *
 * Implement the methods below to compress and decompress text files using
 * Huffman coding. See the assignment write-up for the full specification.
 *
 *  - Treat characters as 8-bit values in the range [0, 255].
 *  - When writing output files, write the newline character explicitly as
 *    '\n' (do not rely on System.lineSeparator() / println()), so your
 *    output is identical across Windows/Mac/Linux. A BufferedWriter over
 *    a FileWriter, writing "\n" yourself, works well for this.
 *  - Encoded files store the code as literal '0'/'1' characters (one byte
 *    each), not as packed bits, so the "encoded" file will actually be
 *    larger than the original -- that's expected for this assignment.
 *  - createHuffTree() is built on top of addNode()/removeMin(), which
 *    operate generically over a java.util.Collection<HuffmanNode>.
 *  - HuffmanNode is a record, so its components are read with
 *    node.charAscii(), node.freq(), node.left(), and node.right() -- no
 *    "get" prefix.
 */
public class Huffman {

    /** Number of possible 8-bit character values. */
    public static final int NUM_CHARS = 256;

    /**
     * Opens the text file named filename and counts the frequency of
     * occurrence of every character in it.
     * Returns an int array of size NUM_CHARS, indexed by ASCII value, with
     * the count of each character (0 for characters that do not occur).
     */
    public static int[] cntFreq(String filename) throws IOException {
        int[] freq = new int[NUM_CHARS];

        try (FileInputStream input = new FileInputStream(filename)){
            int character;

            while((character = input.read()) != -1){
                freq[character]++;
            }
        }
        return freq;
    }


    /**
     * Builds a Huffman tree from an array of character frequencies (as
     * produced by cntFreq()) and returns the root HuffmanNode of the tree.
     *
     * Use an HQueue to manage the collection of
     * not-yet-combined nodes as you build the tree.
     *
     * Special cases:
     *  - If freq contains no non-zero counts (empty input file), return null
     *  - If freq contains exactly one non-zero count (input file has only
     *    one unique character), return a single leaf HuffmanNode for that
     *    character (no combine() call needed).
     */
    public static HuffmanTree createHuffTree(int[] freq) {
        HQueue queue = new PriorityHQueue();

        for(int ascii = 0; ascii < NUM_CHARS; ascii++){
            if(freq[ascii] > 0){
                HuffmanLeaf leaf = new HuffmanLeaf(freq[ascii], (char) ascii);
                queue.enqueue(leaf);
            }
        }

        if(queue.isEmpty()){
            return null;
        }

        while(queue.count() > 1){
            HuffmanTree smallestTree = queue.dequeue();
            HuffmanTree secondSmallestTree = queue.dequeue();

            HuffmanTree combinedTree = smallestTree.combine(secondSmallestTree);
            queue.enqueue(combinedTree);
        }

        return queue.dequeue();
    }


    /**
     * Traverses the Huffman tree rooted at root and builds the Huffman
     * code for every character that appears in it. Going left appends
     * '0' to the code, going right appends '1'.
     *
     * Returns a String array of size NUM_CHARS, indexed by ASCII value.
     * Characters that do not appear in the tree have an empty string ""
     * at their index. The same is true of the lone character in a
     * single-node tree (the only-one-unique-character case) - it has no
     * left/right branch to build a code out of; huffmanEncode/huffmanDecode
     * handle that case entirely through the header.
     */
    public static String[] createCode(HuffmanTree root) {
        String[] codes = new String[NUM_CHARS];
        Arrays.fill(codes, "");

        if(root == null){
            return codes;
        }

        Stack<HuffmanTree> trees = new Stack<>();
        Stack<String> paths = new Stack<>();

        trees.push(root);
        paths.push("");

        while(! trees.isEmpty()){
            HuffmanTree currentTree = trees.pop();
            String currentCode = paths.pop();

            if(currentTree instanceof HuffmanLeaf leaf){
                codes[leaf.charASCII()] = currentCode;

            } else {
                HuffmanNode node = (HuffmanNode) currentTree;

                trees.push(node.right());
                paths.push(currentCode + "1");
                trees.push(node.left());
                paths.push(currentCode + "0");
            }
        }

        return codes;
    }

    /**
     * Given a frequency array (as produced by cntFreq()), returns a
     * header string listing the ASCII values and their associated
     * frequencies, separated by single spaces, in ascending order of
     * ASCII value. Only characters with a non-zero count are included.
     *
     * Example: for the text "aaabbbbcc" this returns "97 3 98 4 99 2".
     */
    public static String createHeader(int[] freq) {
        StringBuilder header = new StringBuilder();

        for(int ascii = 0; ascii < NUM_CHARS; ascii++){
            if(freq[ascii] > 0){

                if(header.length() > 0){
                    header.append(" ");
                }

                header.append(ascii);
                header.append(" ");
                header.append(freq[ascii]);
            }
        }

        if(header.length() > 0){
            return header.toString();
        }

        return "";
    }

    /**
     * Reads the input text file inFile and writes to outFile:
     *   1. A header line (see createHeader), followed by a newline
     *   2. The Huffman-encoded contents of inFile, using the codes from
     *      createCode() -- UNLESS inFile has only one unique character,
     *      see below.
     * If outFile already exists, its old contents are overwritten.
     *
     * Special cases:
     *  - Empty input file: outFile should end up empty too
     *  - Input file with only one unique character: should contain only the
     *    header line (e.g. "97 5") and nothing else - no second line of code bits.
     */
    public static void huffmanEncode(String inFile, String outFile) throws IOException {
        int[] freq = cntFreq(inFile);
        HuffmanTree root = createHuffTree(freq);

        if(root == null){
            try(FileOutputStream output = new FileOutputStream(outFile)){
            }
            return;
        }

        String header = createHeader(freq);

        try(FileOutputStream output = new FileOutputStream(outFile)){
            output.write(header.getBytes());
            output.write('\n');

            if(root instanceof HuffmanLeaf){
                return;
            }

            String[] codes = createCode(root);

            try(FileInputStream input = new FileInputStream(inFile)){
                int character;

                while((character = input.read()) != -1){
                    output.write(codes[character].getBytes());
                }
            }
        }
    }

    /**
     * Parses a header string (in the format produced by createHeader) and
     * returns the corresponding frequency array of size NUM_CHARS, indexed
     * by ASCII value, i.e. the same format returned by cntFreq().
     *
     * Example: for the header "97 3 98 4 99 2", freq[97, 98, 99] == {3, 4, 2}.
     */
    public static int[] parseHeader(String headerString) {
        int[] freq = new int[NUM_CHARS];
        if(headerString.isEmpty()){
            return freq;
        }

        String[] values = headerString.split(" ");

        for(int index = 0; index < values.length; index += 2){
            int ascii = Integer.parseInt(values[index]);
            int count = Integer.parseInt(values[index + 1]);

            freq[ascii] = count;
        }
        return freq;
    }

    /**
     * Reads an encoded file (in the format written by huffmanEncode) and
     * writes the decoded text to decodeFile.
     *
     * Recreate the frequency array from the header with parseHeader(),
     * rebuild the Huffman tree with createHuffTree(), then walk the tree
     * for each bit of the second line: '0' -> left child, '1' -> right
     * child. When a leaf is reached, emit its character and restart at
     * the root.
     *
     * Special case: if the recreated tree is a single leaf (the
     * only-one-unique-character case), there is no second line to read
     * The header's count alone tells you how many times that character occurred,
     * so write it out that many times directly.
     */
    public static void huffmanDecode(String encodedFile, String decodeFile) throws IOException {
        try(BufferedReader input = new BufferedReader(new FileReader(encodedFile));
            FileOutputStream output = new FileOutputStream(decodeFile)){

                String header = input.readLine();

                if(header == null || header.isEmpty()){
                    return;
                }

                int[] freq = parseHeader(header);
                HuffmanTree root = createHuffTree(freq);

                if(root instanceof HuffmanLeaf leaf){

                    for(int count = 0; count < leaf.freq(); count++){
                        output.write(leaf.charASCII());
                    }

                    return;
                }

                String encodedText = input.readLine();
                HuffmanTree currentTree = root;

                for(int index = 0; index < encodedText.length(); index++){
                    HuffmanNode currentNode = (HuffmanNode) currentTree;

                    if(encodedText.charAt(index) == '0'){
                        currentTree = currentNode.left();

                    } else {
                        currentTree = currentNode.right();
                    }

                    if(currentTree instanceof HuffmanLeaf leaf){
                        output.write(leaf.charASCII());
                        currentTree = root;
                    }
                }
            }
        }

    static class PriorityHQueue implements HQueue{
        private PriorityQueue<HuffmanTree> trees = new PriorityQueue<>();

        @Override
        public boolean isEmpty(){
            return trees.isEmpty();
        }

        @Override
        public int count(){
            return trees.size();
        }

        @Override
        public void enqueue(HuffmanTree tree){
            trees.add(tree);
        }

        @Override
        public HuffmanTree dequeue(){
            return trees.remove();
        }
    }
}