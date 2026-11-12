/**
 * A single node in a Huffman tree, implemented as a record.
 *
 * A HuffmanNode represents either a leaf (an actual character together
 * with its occurrence count) or an internal node created by combining two
 * subtrees during tree construction.
 *
 * Records are immutable and give you charAscii(), freq(), left(), and
 * right() accessor methods automatically, along with
 * equals()/hashCode()/toString(). Do not rename the components below.
 */
public record HuffmanNode(int charAscii, int freq, HuffmanTree left, HuffmanTree right)
        implements HuffmanTree{

     public boolean comesBefore(HuffmanTree other) {
         int otherFreq;
         int otherAscii;

         if(other instanceof HuffmanNode node){
             otherFreq = node.freq();
             otherAscii = node.charAscii();

         } else {
             HuffmanLeaf leaf = (HuffmanLeaf) other;
             otherFreq = leaf.freq();
             otherAscii = leaf.charASCII();
         }

         if(freq < otherFreq){
             return true;
         }

         if(freq == otherFreq && charAscii < otherAscii){
             return true;
         }

         return false;
    }

    public HuffmanTree combine(HuffmanTree other) {
         if(other == null){
             return null;
         }

        HuffmanTree leftChild;
        HuffmanTree rightChild;

        if (this.comesBefore(other)){
            leftChild = this;
            rightChild = other;

        } else {
            leftChild = other;
            rightChild = this;
        }

        int otherFreq;
        int otherAscii;

        if(other instanceof HuffmanNode node){
            otherFreq = node.freq();
            otherAscii = node.charAscii();

        } else {
            HuffmanLeaf leaf = (HuffmanLeaf) other;
            otherFreq = leaf.freq();
            otherAscii = leaf.charASCII();
        }

        return new HuffmanNode(Math.min(charAscii, otherAscii), freq + otherFreq, leftChild, rightChild);
    }

    /**
     * Natural ordering consistent with comesBefore(), so that HuffmanNode
     * objects can be stored directly in a sorted structure (e.g. a
     * PriorityQueue or a sorted List) while building the tree.
     */
    @Override
    public int compareTo(HuffmanTree other) {
        if (this.comesBefore(other)) return -1;
        if (other.comesBefore(this)) return 1;
        return 0;
    }
}
