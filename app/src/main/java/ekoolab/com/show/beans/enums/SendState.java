package ekoolab.com.show.beans.enums;

public enum SendState {
    SENDING(0), SENT(1), READ(2), FAILED(3), NOT_ANSWERED(4), ACCEPTED(5), DECLINED(6), ENDED(7);

    private int index;

    SendState(final int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static SendState getSendState(int index) {
        for (SendState type : SendState.values()) {
            if (type.index == index) return type;
        }
        throw new IllegalArgumentException("Type not found. Amputated?");
    }
}
