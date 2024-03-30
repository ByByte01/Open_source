package com.fajianchen.sensitive.provider;

import com.fajianchen.sensitive.core.SensitiveProvider;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class UserNameSensitiveProvider implements SensitiveProvider {
    private final List<String> keywords = Arrays.asList("password", "passphrase", "pwd", "pass");
    private final List<String> separators = Arrays.asList(":", "=", "\": \"", "\":\"", ">");

    @Override
    public int execute(StringBuilder builder, char maskChar, int startPos, int buffLength) {
        int charPos = startPos;
        if (charPos + 5 > buffLength) {
            return startPos;
        }

        Character character = builder.charAt(charPos);
        if (isPasswordStart(character, builder, charPos)) {
            int keywordStart = 0;
            int keywordLength = 0;
            String keywordUsed = null;
            for (String keyword:keywords) {
                keywordStart = StringUtils.indexOfIgnoreCase(builder, keyword, charPos);
                if (keywordStartAtRightPosition(keywordStart, charPos)) {
                    keywordLength = keyword.length();
                    keywordUsed = keyword;
                    break;
                }
            }

            if (keywordStart != startPos && keywordStart != startPos + 1) {
                return startPos;
            }

            int idxSeparator;
            for (String separator:separators) {
                idxSeparator = StringUtils.indexOf(builder, separator, keywordStart + keywordLength);
                if (idxSeparator == keywordStart + keywordLength) {
                    charPos = passwordStartPosition(keywordStart, keywordLength, separator, builder);

                    int endPos = detectEnd(builder, buffLength, charPos, keywordUsed, keywordLength, separator);
                    
                    if (endPos > charPos) {
                        return mask(builder, maskChar, charPos, endPos);
                    }
                }
            }
        }

        return startPos;
    }

    private int detectEnd(StringBuilder builder, int buffLength, int startPos, String keyword, int keywordLength, String separator) {
        if (separator.charAt(0) == '>') {
            return detectEndXml(builder, buffLength, startPos, keyword, keywordLength);
        } else if (separator.contains("\"")) {
            return detectEndJson(builder, buffLength, startPos);
        } else {
            return detectEndNoXml(builder, buffLength, startPos);
        }
    }

    private int detectEndNoXml(StringBuilder builder, int buffLength, int startPos) {
        while (startPos < buffLength && !SensitiveProvider.isDelimiter(builder.charAt(startPos))) {
            startPos++;
        }

        return startPos;
    }

    private int detectEndJson(StringBuilder builder, int buffLength, int startPos) {
        while (startPos < buffLength && !isEndOfJson(builder, startPos)) {
            startPos++;
        }

        return startPos;
    }

    private boolean isEndOfJson(StringBuilder builder, int pos) {
        return builder.charAt(pos) == '"' && builder.charAt(pos - 1) != '\\';
    }

    private int detectEndXml(StringBuilder builder, int buffLength, int startPos, String keyword, int keywordLength) {
        if ( buffLength < startPos + keywordLength + 3) {
            return -1;
        }

        int passwordEnd = StringUtils.indexOfIgnoreCase(builder, keyword, startPos);
        if (passwordEnd > 0 && builder.charAt(passwordEnd - 1) == '/' && builder.charAt(passwordEnd - 2) == '<') {
            return passwordEnd - 2;
        }

        return -1;
    }

    private boolean isPasswordStart(Character character, StringBuilder builder, int pos) {
        return 'p' == character || 'P' == character ||
                ('<' == character && 'p' == builder.charAt(pos+1)) ||
                ('<' == character && 'P' == builder.charAt(pos+1));
    }

    private boolean keywordStartAtRightPosition(int keywordStart, int pos) {
        return keywordStart >= 0 && (keywordStart == pos || keywordStart == pos + 1);
    }

    private int passwordStartPosition(int keywordStart, int keywordLength, String separator, StringBuilder builder) {
        int charPos = keywordStart + keywordLength + separator.length();
        if (Character.isWhitespace(builder.charAt(charPos))) {
            charPos++;
        }
        return charPos;
    }

    private int mask(StringBuilder builder, char maskChar, int startPos, int endPos) {
        final String masked = "" + maskChar + maskChar + maskChar + maskChar + maskChar + maskChar;
        builder.replace(startPos, endPos, masked);
        return startPos + 6;
    }
}
