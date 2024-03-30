package com.fajianchen.sensitive.provider;

import com.fajianchen.sensitive.core.SensitiveProvider;
import org.apache.commons.lang3.StringUtils;


public class EmailSensitiveProvider implements SensitiveProvider {
    @Override
    public int execute(StringBuilder builder, char maskChar, int startPos, int buffLength) {
        if (startPos == 0) {
            return startPos;
        }

        int pos = startPos;
        int indexOfAt;
        int indexOfDot;
        int indexOfEnd;
        int emailStartPos;
        char character = builder.charAt(pos-1);
        if ('@' == character) {
            indexOfAt = pos-1;
            pos--;
            emailStartPos = indexOfStart(builder, pos, indexOfAt);
            indexOfEnd = indexOfEmailEnd(builder, indexOfAt + 1, emailStartPos, buffLength);
            indexOfDot = indexOfDot(indexOfAt, indexOfEnd, builder);

            if (emailStartPos < indexOfAt && indexOfAt < indexOfDot && indexOfDot < indexOfEnd) {
                builder.replace(emailStartPos + 1, indexOfAt - 1, StringUtils.repeat(maskChar, indexOfAt - emailStartPos - 2))
                        .replace(indexOfAt + 2, indexOfDot - 1, StringUtils.repeat(maskChar, indexOfDot - indexOfAt - 3));
                return indexOfEnd;
            }
        }

        return startPos;
    }

    private int indexOfStart(StringBuilder unmasked, int pos, int indexOfAt) {
        while (pos >= 0 && !(SensitiveProvider.isDelimiter(unmasked.charAt(pos)))) {
            pos--;
            if (pos > 0 && unmasked.charAt(pos) == '@') {
                return indexOfAt;
            }
        }
        return pos + 1;
    }

    private int indexOfEmailEnd(StringBuilder unmasked, int startPost, int emailStart, int buffLength) {
        while (startPost < buffLength) {
            if (SensitiveProvider.isDelimiter(unmasked.charAt(startPost))) {
                return startPost - 1;
            } else if ('@' == unmasked.charAt(startPost)) {
                // we discovered another '@' character, so we set the end at the start, since it is NOT a proper email
                return emailStart;
            }
            startPost++;
        }
        return startPost - 1;
    }

    private int indexOfDot(int startPos, int endPos, StringBuilder unmasked) {
        while (endPos > startPos && unmasked.charAt(endPos) != '.') {
            endPos--;
        }

        return endPos;
    }
}
