package com.carddemo.batch.cbact01c.codec;

/** Test helper: hex string ("00 10 00 00" or "F0F0C0") to bytes and back. */
public final class Hex {

    private Hex() {
    }

    public static byte[] bytes(String hex) {
        String clean = hex.replace(" ", "");
        byte[] out = new byte[clean.length() / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(clean.substring(i * 2, i * 2 + 2), 16);
        }
        return out;
    }

    public static String of(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) {
            sb.append(String.format("%02X", x & 0xFF));
        }
        return sb.toString();
    }
}
