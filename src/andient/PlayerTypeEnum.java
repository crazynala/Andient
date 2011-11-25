package andient;

/**
 * User: dan
 * Date: 11/22/11
 */
public enum PlayerTypeEnum {
    NULL("--"),
    STRUMMER("Strummer"),
    FLOATER("Floater"),
    DOODLER("Doodler"),
    CHUNKER("Chunker"),
    BASS("Bass");

    private final String label;

    PlayerTypeEnum(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
