package ekoolab.com.show.beans.enums;

public enum FileType {
    IMAGE("image", 0), VIDEO("video", 1), AUDIO("audio", 2), DOCUMENT("document", 3);

    private String name;
    private int index;

    private FileType(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public static FileType getMediaType(int index){
        for(FileType v : FileType.values()){
            if(v.getIndex() == index){
                return v;
            }
        }
        return null;
    }

    public static FileType getMediaType(String name){
        for(FileType v : values()){
            if(v.getName().equals(name)){
                return v;
            }
        }
        return null;
    }

    public static String getName(int index) {
        for (FileType a : FileType.values()) {
            if (a.getIndex() == index) {
                return a.name;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }
}
