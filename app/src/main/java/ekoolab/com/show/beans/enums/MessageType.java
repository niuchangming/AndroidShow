package ekoolab.com.show.beans.enums;

import ekoolab.com.show.utils.Utils;

public enum MessageType {
    TEXT (0, "text"),
    ADMIN (1, "admin"),
    AUDIO (2, "audio"),
    PHOTO (3, "photo"),
    VIDEO (4, "video"),
    LINK (5, "link"),
    VOICE_CALL (6, "voice_call"),
    VIDEO_CALL (7, "video_call"),
    OTHER (8, "other");

    private int index;
    private String name;


    MessageType(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public static MessageType getMessageType(String strValue) {
        for (MessageType type : MessageType.values()) {
            if (Utils.equals(type.name, strValue)) return type;
        }
        return TEXT;
    }

    public static MessageType getMessageType(int index) {
        for (MessageType type : MessageType.values()) {
            if (type.index == index) return type;
        }
        return TEXT;
    }


}
