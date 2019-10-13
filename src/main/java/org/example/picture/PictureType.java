package org.example.picture;

public enum PictureType {
    JPEG("image/jpeg", new int[]{0xFF, 0xD8, 0xFF}),
    GIF("image/gif", new int[]{'G', 'I', 'F', '8'}),
    PNG("image/png", new int[]{0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A});

    private final String type;
    private final byte[] signature;

    PictureType(String type, int[] signature) {
        this.type = type;
        this.signature = new byte[signature.length];
        for (int i = 0; i < signature.length; i++) {
            this.signature[i] = (byte)signature[i];
        }
    }

    public static PictureType detectType(byte[] picture) {
        for (PictureType pt : values()) {
            if (pt.signature.length > picture.length) {
                continue;
            }
            boolean signatureFound = true;
            for (int i = 0; i < pt.signature.length; i++) {
                if (pt.signature[i] != picture[i]) {
                    signatureFound = false;
                    break;
                }
            }
            if (signatureFound) {
                return pt;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return type;
    }
}
