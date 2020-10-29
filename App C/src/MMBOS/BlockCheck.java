package MMBOS;

import java.util.Date;

/**
 * Kontrollerar loggade hashar med alla överföringas-hashar
 *
 * @author Michael
 *
 */
public class BlockCheck {

    public String hash;
    public String previousHash;
    private String data;
    public long settimeStamp;
    public int setNonce;

    public BlockCheck(String data, String previousHash, long timeStamp, int setNonce) {
        this.data = data;
        this.previousHash = previousHash;
        this.settimeStamp = timeStamp;
        this.setNonce = setNonce;
        this.hash = calculateHashChecker();
    }
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

    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0'); //Create a string with difficulty * "0"
        while(!hash.substring( 0, difficulty).equals(target)) {
            setNonce ++;
            hash = calculateHashChecker();
        }
    }
    public String getData() {
        return data;
    }

}
