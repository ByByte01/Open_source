package com.fajianchen.sensitive.provider;


import com.fajianchen.sensitive.core.SensitiveProvider;
public class IPSensitiveProvider implements SensitiveProvider {
    @Override
    public int execute(StringBuilder builder, char maskChar, int startPos, int buffLength) {
        int pos = startPos;
        Character character = builder.charAt(pos);
        if (Character.isDigit(character)) {
            int noDigits = 1;
            int noDots = 0;
            pos++;
            while (pos < buffLength && !SensitiveProvider.isDelimiter(builder.charAt(pos))) {
                character = builder.charAt(pos);
                pos++;
                if (Character.isDigit(character)) {
                    noDigits++;
                    if (noDigits > 3) {
                        return startPos;
                    }
                } else if ('.' == character) {
                    noDots++;
                    noDigits = 0;
                } else {
                    return startPos;
                }
            }

            if (noDots == 3 || isDotAtEnd(noDots, builder, pos, buffLength)) {
                StringBuilder masked = new StringBuilder();
                int consecutiveDigits = 0;
                for (int charPos = startPos; charPos < pos; charPos++) {
                    if ('.' == builder.charAt(charPos)) {
                        masked.append('.');
                        consecutiveDigits = 0;
                    } else if (consecutiveDigits < 2) {
                        masked.append(builder.charAt(charPos));
                        consecutiveDigits++;
                    } else {
                        masked.append(maskChar);
                    }
                }

                builder.replace(startPos, pos, masked.toString());
                return pos;
            }
        }

        return startPos;
    }

    private boolean isDotAtEnd(int noDots, StringBuilder builder, int charPos, int buffLength) {
        return noDots == 4 &&
                (charPos == buffLength || SensitiveProvider.isDelimiter(builder.charAt(charPos)));
    }
}
