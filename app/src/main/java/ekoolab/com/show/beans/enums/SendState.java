package ekoolab.com.show.beans.enums;

public enum SendState {
    SENDING(0), SENT(1), REACHED(2), READ(3), FAILED(4), NOT_ANSWERED(5), ACCEPTED(6), DECLINED(7), ENDED(8);

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
