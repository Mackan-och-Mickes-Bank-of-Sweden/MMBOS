package MMBOS;

import java.util.Date;

public class BlockCheck {

    public String hash;
    public String previousHash;
    private String data;
    public long settimeStamp;
    public int setNonce;

    /**
     * hash controller of logged transfers, timestamp and nonce are set from log file
     * @author Michael
     * @param data - transfer data
     * @param previousHash - hash from previous log
     * @param timeStamp - custom timestamp (i.e. from log file)
     * @param setNonce - custom nonce (i.e. from log file)
     */
    public BlockCheck(String data, String previousHash, long timeStamp, int setNonce) {
        this.data = data;
        this.previousHash = previousHash;
        this.settimeStamp = timeStamp;
        this.setNonce = setNonce;
        this.hash = calculateHashChecker();
    }

    /**
     * hash of new transfer
     * @author Michael
     * @param data - transfer data
     * @param previousHash - hash from previous log
     */
    public BlockCheck(String data, String previousHash ) {
        this.data = data;
        this.previousHash = previousHash;
        this.settimeStamp = new Date().getTime();
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

    /**
     * sets a  number of zeros in the beginning of every hash
     * @param difficulty - number of zeros
     */
    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        while(!hash.substring( 0, difficulty).equals(target)) {
            setNonce ++;
            hash = calculateHashChecker();
        }
    }
    public String getData() {
        return data;
    }

}
