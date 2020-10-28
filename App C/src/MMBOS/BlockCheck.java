package MMBOS;
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
    public String settimeStamp;
    public String setNonce;

    public BlockCheck(String data, String previousHash, String timeStamp, String setNonce) {
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

    public String getData() {
        return data;
    }

}
