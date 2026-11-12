public record HuffmanLeaf (int freq, char charASCII) implements HuffmanTree{

    @Override
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

        if(freq == otherFreq && charASCII < otherAscii){
            return true;
        }

        return false;
    }

    @Override
    public HuffmanTree combine(HuffmanTree other) {
        if(other == null){
            return this;
        }

        HuffmanTree leftChild;
        HuffmanTree rightChild;

        if(this.comesBefore(other)){
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

        return new HuffmanNode(Math.min(charASCII, otherAscii), freq + otherFreq, leftChild, rightChild);
    }

    @Override
    public int compareTo(HuffmanTree other) {
        if (this.comesBefore(other)){
            return -1;
        }

        if (other.comesBefore(this)){
            return 1;
        }

        return 0;
    }
}
