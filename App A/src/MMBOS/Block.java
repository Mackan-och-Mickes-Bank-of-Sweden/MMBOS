package MMBOS;

import java.util.Date;

public class Block {

    public String hash;
    public String previousHash;
    private String data;
    private long timeStamp;
    private int nonce;
    public String settimeStamp;
    public String setNonce;

    public Block(String data,String previousHash ) {
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }
    public void BlockCheck(String data, String previousHash, String timeStamp, String setNonce ) {
        this.data = data;
        this.previousHash = previousHash;
        this.settimeStamp = timeStamp;
        this.setNonce = setNonce;
        this.hash = calculateHashChecker();
    }
    public String calculateHashChecker() {
        String calculatedhashCheck = StringUtil.applySha256(
                previousHash +
                        settimeStamp +
                        setNonce +
                        data
        );
        return calculatedhashCheck;
    }
    public String calculateHash() {
        String calculatedhash = StringUtil.applySha256(
                previousHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce) +
                        data
        );
        return calculatedhash;
    }

    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0'); //Create a string with difficulty * "0"
        while(!hash.substring( 0, difficulty).equals(target)) {
            nonce ++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }
}
