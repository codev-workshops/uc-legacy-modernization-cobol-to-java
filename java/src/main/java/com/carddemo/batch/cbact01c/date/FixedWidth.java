package com.carddemo.batch.cbact01c.date;

/** PIC X(n) MOVE semantics: right-pad with a fill char or truncate on the right. */
final class FixedWidth {

    private FixedWidth() {
    }

    static String fit(String value, int length, char fill) {
        String s = value == null ? "" : value;
        if (s.length() == length) {
            return s;
        }
        if (s.length() > length) {
            return s.substring(0, length);
        }
        StringBuilder sb = new StringBuilder(length).append(s);
        while (sb.length() < length) {
            sb.append(fill);
        }
        return sb.toString();
    }

    static boolean isAll(String value, char c) {
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) != c) {
                return false;
            }
        }
        return true;
    }
}
