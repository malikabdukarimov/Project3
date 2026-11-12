import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Starter test suite for Project 3 (Huffman Encoding/Decoding).
 * Write more tests!
 * These tests assume the test files are in the same directory as HuffmanTest.java
 */
public class HuffmanTest {

    @Test
    public void testCntFreq() throws IOException {
        int[] freqList = Huffman.cntFreq("src/test/java/file2.txt");
        int[] ansList = {2, 4, 8, 16, 0, 2, 0}; // counts for ascii 97..103
        for (int i = 0; i < ansList.length; i++) {
            assertEquals(ansList[i], freqList[97 + i], "mismatch at ascii " + (97 + i));
        }
    }

    @Test
    public void testComesBefore() {
        HuffmanLeaf d = new HuffmanLeaf(5, 'd');
        HuffmanLeaf k = new HuffmanLeaf(5, 'k');
        assertTrue(d.comesBefore(k));
        assertFalse(k.comesBefore(d));
    }

    @Test
    public void testCreateHeader() throws IOException {
        int[] freqList = Huffman.cntFreq("src/test/java/file2.txt");
        assertEquals("97 2 98 4 99 8 100 16 102 2", Huffman.createHeader(freqList));
    }

    @Test
    public void test01Textfile() throws IOException {
        Huffman.huffmanEncode("src/test/java/file1.txt", "file1_out.txt");
        assertTrue(compareFiles("file1_out.txt", "src/test/java/file1_soln.txt"));
    }

    @Test
    public void testCombine(){
        HuffmanLeaf a = new HuffmanLeaf(2, 'a');
        HuffmanLeaf b = new HuffmanLeaf(3, 'b');

        HuffmanNode combined = (HuffmanNode) a.combine(b);

        assertEquals(a, combined.left());
        assertEquals(b, combined.right());
        assertEquals(5, combined.freq());
    }

    @Test
    public void testCreateHuffTree(){
        int[] freq = new int[Huffman.NUM_CHARS];
        freq['a'] = 3;
        freq['b'] = 2;
        freq['c'] = 1;

        HuffmanTree root = Huffman.createHuffTree(freq);

        assertNotNull(root);
        assertTrue(root instanceof HuffmanNode);
    }

    @Test
    public void testCreateHuffTreeEmpty(){
        int[] freq = new int[Huffman.NUM_CHARS];
        assertNull(Huffman.createHuffTree(freq));
    }

    @Test
    public void testCreateCode(){
        int[] freq = new int[Huffman.NUM_CHARS];
        freq['a'] = 3;
        freq['b'] = 2;
        freq['c'] = 1;

        HuffmanTree root = Huffman.createHuffTree(freq);
        String[] codes = Huffman.createCode(root);

        assertEquals("0", codes['a']);
        assertEquals("11", codes['b']);
        assertEquals("10", codes['c']);
        assertEquals("", codes['z']);
    }

    @Test
    public void testParseHeader(){
        int[] freq = Huffman.parseHeader("97 3 98 4 99 2");

        assertEquals(3, freq['a']);
        assertEquals(4, freq['b']);
        assertEquals(2, freq['c']);
        assertEquals(0, freq['d']);
    }

    @Test
    public void testEncodeAndDecode() throws IOException{
        Path input = Files.createTempFile("huffman-input", ".txt");
        Path encoded = Files.createTempFile("huffman-encoded", ".txt");
        Path decoded = Files.createTempFile("huffman-decoded", ".txt");

        String originalText = "abcd abc ab a";
        Files.writeString(input, originalText);

        Huffman.huffmanEncode(input.toString(), encoded.toString());
        Huffman.huffmanDecode(encoded.toString(), decoded.toString());

        assertEquals(originalText, Files.readString(decoded));
    }

    @Test
    public void testHQueue(){
        HQueue queue = new Huffman.PriorityHQueue();

        HuffmanLeaf c = new HuffmanLeaf(4, 'c');
        HuffmanLeaf b = new HuffmanLeaf(2, 'b');
        HuffmanLeaf a = new HuffmanLeaf(2, 'a');

        queue.enqueue(c);
        queue.enqueue(a);
        queue.enqueue(b);

        assertEquals(3, queue.count());
        assertEquals(a, queue.dequeue());
        assertEquals(b, queue.dequeue());
        assertEquals(c, queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testOneUniqueCharacter() throws IOException{
        Path input = Files.createTempFile("one-character", ".txt");
        Path encoded = Files.createTempFile("one-character-encoded", ".txt");
        Path decoded = Files.createTempFile("one-character-decoded", ".txt");

        Files.writeString(input, "aaaaa");

        Huffman.huffmanEncode(input.toString(), encoded.toString());
        assertEquals("97 5\n", Files.readString(encoded));

        Huffman.huffmanDecode(encoded.toString(), decoded.toString());
        assertEquals("aaaaa", Files.readString(decoded));
    }

    @Test
    public void testEmptyFile() throws IOException{
        Path input = Files.createTempFile("empty-input", ".txt");
        Path encoded = Files.createTempFile("empty-encoded", ".txt");

        Huffman.huffmanEncode(input.toString(), encoded.toString());
        assertEquals("", Files.readString(encoded));
    }

    @Test
    public void testMissingFile(){
        assertThrows(FileNotFoundException.class, () -> Huffman.huffmanEncode("not-a-real-file.txt", "output.txt"));
    }

    @Test
    public void testMissingEncodedFile(){
        assertThrows(FileNotFoundException.class, () -> Huffman.huffmanDecode("not-a-real-encoded-file.txt", "decoded.txt"));
    }

    /**
     * Compares two files line by line, trimming trailing CR/LF so the
     * comparison works the same on Windows and Unix-style line endings.
     */
    private static boolean compareFiles(String file1, String file2) throws IOException {
        try (BufferedReader r1 = new BufferedReader(new FileReader(file1));
             BufferedReader r2 = new BufferedReader(new FileReader(file2))) {
            String line1, line2;
            while (true) {
                line1 = r1.readLine();
                line2 = r2.readLine();
                if (line1 == null && line2 == null) {
                    return true;
                }
                if (line1 == null || line2 == null) {
                    return false;
                }
                if (!line1.trim().equals(line2.trim())) {
                    return false;
                }
            }
        }
    }
}
