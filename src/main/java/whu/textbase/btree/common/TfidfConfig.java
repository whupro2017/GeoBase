package whu.textbase.btree.common;

public class TfidfConfig {
    private String indexPath;
    private int tokenCacheSize, tokenBlockSize, tokenNodeNumOfBlock, tokenNodeCacheSize;
    private int reverseCacheSize, reverseBlockSize, reverseNodeNumOfBlock, reverseNodeCacheSize;
    private int recordCacheSize, recordDataCacheSize, recordBlockSize, recordNodeNumOfBlock, recordNodeCacheSize;
    private int lengthCacheSize, lengthBlockSize, lengthNodeNumOfBlock, lengthNodeCacheSize;
    private int tfmaxCacheSize, tfmaxBlockSize, tfmaxNodeNumOfBlock, tfmaxNodeCacheSize;
    private float cachefac;

    public TfidfConfig(String indexPath, int tokenCacheSize, int tokenBlockSize, int tokenNodeNumOfBlock,
            int reverseCacheSize, int reverseBlockSize, int reverseNodeNumOfBlock, int recordCacheSize,
            int recordDataCacheSize, int recordBlockSize, int recordNodeNumOfBlock, int lengthCacheSize,
            int lengthBlockSize, int lengthNodeNumOfBlock, int tfmaxCacheSize, int tfmaxBlockSize,
            int tfmaxNodeNumOfBlock, float cachefac) {
        super();
        this.indexPath = indexPath;
        this.tokenCacheSize = tokenCacheSize;
        this.tokenBlockSize = tokenBlockSize;
        this.tokenNodeNumOfBlock = tokenNodeNumOfBlock;
        this.reverseCacheSize = reverseCacheSize;
        this.reverseBlockSize = reverseBlockSize;
        this.reverseNodeNumOfBlock = reverseNodeNumOfBlock;
        this.recordCacheSize = recordCacheSize;
        this.recordDataCacheSize = recordDataCacheSize;
        this.recordBlockSize = recordBlockSize;
        this.recordNodeNumOfBlock = recordNodeNumOfBlock;
        this.lengthCacheSize = lengthCacheSize;
        this.lengthBlockSize = lengthBlockSize;
        this.lengthNodeNumOfBlock = lengthNodeNumOfBlock;
        this.tfmaxCacheSize = tfmaxCacheSize;
        this.tfmaxBlockSize = tfmaxBlockSize;
        this.tfmaxNodeNumOfBlock = tfmaxNodeNumOfBlock;
        this.cachefac = cachefac;
    }

    public String getIndexPath() {
        return indexPath;
    }

    public int getTokenCacheSize() {
        return tokenCacheSize;
    }

    public int getTokenBlockSize() {
        return tokenBlockSize;
    }

    public int getTokenNodeNumOfBlock() {
        return tokenNodeNumOfBlock;
    }

    public int getReverseCacheSize() {
        return reverseCacheSize;
    }

    public int getReverseBlockSize() {
        return reverseBlockSize;
    }

    public int getReverseNodeNumOfBlock() {
        return reverseNodeNumOfBlock;
    }

    public int getRecordCacheSize() {
        return recordCacheSize;
    }

    public int getRecordDataCacheSize() {
        return recordDataCacheSize;
    }

    public int getRecordBlockSize() {
        return recordBlockSize;
    }

    public int getRecordNodeNumOfBlock() {
        return recordNodeNumOfBlock;
    }

    public int getLengthCacheSize() {
        return lengthCacheSize;
    }

    public int getLengthBlockSize() {
        return lengthBlockSize;
    }

    public int getLengthNodeNumOfBlock() {
        return lengthNodeNumOfBlock;
    }

    public float getCachefac() {
        return cachefac;
    }

    public int getTokenNodeCacheSize() {
        return tokenNodeCacheSize;
    }

    public void setTokenNodeCacheSize(int tokenNodeCacheSize) {
        this.tokenNodeCacheSize = tokenNodeCacheSize;
    }

    public int getReverseNodeCacheSize() {
        return reverseNodeCacheSize;
    }

    public void setReverseNodeCacheSize(int reverseNodeCacheSize) {
        this.reverseNodeCacheSize = reverseNodeCacheSize;
    }

    public int getRecordNodeCacheSize() {
        return recordNodeCacheSize;
    }

    public void setRecordNodeCacheSize(int recordNodeCacheSize) {
        this.recordNodeCacheSize = recordNodeCacheSize;
    }

    public int getLengthNodeCacheSize() {
        return lengthNodeCacheSize;
    }

    public void setLengthNodeCacheSize(int lengthNodeCacheSize) {
        this.lengthNodeCacheSize = lengthNodeCacheSize;
    }

    public int getTfmaxCacheSize() {
        return tfmaxCacheSize;
    }

    public int getTfmaxBlockSize() {
        return tfmaxBlockSize;
    }

    public int getTfmaxNodeNumOfBlock() {
        return tfmaxNodeNumOfBlock;
    }

    public int getTfmaxNodeCacheSize() {
        return tfmaxNodeCacheSize;
    }

    public void setTfmaxNodeCacheSize(int tfmaxNodeCacheSize) {
        this.tfmaxNodeCacheSize = tfmaxNodeCacheSize;
    }

}
