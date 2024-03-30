package com.fajianchen.sensitive.provider;

import com.fajianchen.sensitive.core.SensitiveProvider;
import com.fajianchen.sensitive.util.SensitiveUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;


public class IdCardNumberSensitiveProvider implements SensitiveProvider {
    private static final List<Character> KNOWN_PAN_START_DIGITS = Arrays.asList('1', '3', '4', '5', '6');

    private int startKeep = 1;
    private int endKeep = 6;

    @Override
    public void initialize(String args) {

    }

    @Override
    public int execute(StringBuilder builder, char maskChar, int startPos, int buffLength) {
        int pos = startPos;
        int panLength;
        Character checkChar = builder.charAt(pos);

        panLength = 0;
        pos++;
        while (pos < buffLength && Character.isDigit(builder.charAt(pos))) {
            panLength++;
            pos++;
        }

        if (validPan(builder, startPos, panLength, buffLength)) {
            builder.replace(startPos + startKeep,
                    startPos + panLength - endKeep,
                    StringUtils.repeat(maskChar, panLength - startKeep - endKeep));

            return startPos + panLength;
        }


        return startPos;
    }

    private boolean validPan(StringBuilder builder, int startPos, int panLength, int buffLength) {
        if (panLength == 17 &&
                (startPos + panLength+1 == buffLength || SensitiveProvider.isDelimiter(builder.charAt(startPos + panLength+1)))
                && (SensitiveUtil.checkIdCard(builder.toString().substring(startPos, panLength + startPos+1)))
        ) {
            return true;
        }
        return false;
    }



}
