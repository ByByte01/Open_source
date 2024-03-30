package com.fajianchen.sensitive.provider;

import com.fajianchen.sensitive.core.SensitiveProvider;
import com.fajianchen.sensitive.util.SensitiveUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class CardNumberSensitiveProvider implements SensitiveProvider {
    private static final List<Character> KNOWN_PAN_START_DIGITS = Arrays.asList('1', '3', '4', '5', '6');

    private int startKeep = 1;
    private int endKeep = 6;

    @Override
    public void initialize(String args) {
        if (StringUtils.isBlank(args)) {
            startKeep = 1;
            endKeep = 6;
        } else {
            String[] params = StringUtils.split(args, '|');
            if (params.length != 2) {
                throw new ExceptionInInitializerError("Invalid parameters supplied for CardNumber masker: " + args);
            }
            startKeep = Integer.valueOf(params[0]);
            endKeep = Integer.valueOf(params[1]);

            if (startKeep < 1 || startKeep > 6 ) {
                throw new ExceptionInInitializerError("The number of unmasked digits at the start of the pan can't be more than 6 or less than 1");
            }

            if (endKeep < 1 || endKeep > 8) {
                throw new ExceptionInInitializerError("The number of unmasked digits at the end of the pan can't be more than 8 or less than 1");
            }
        }
    }

    @Override
    public int execute(StringBuilder builder, char maskChar, int startPos, int buffLength) {
        int pos = startPos;
        int panLength;

        Character checkChar = builder.charAt(pos);
        if (KNOWN_PAN_START_DIGITS.contains(checkChar)) {
            panLength = 1;
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
        }
        return startPos;
    }

    private boolean validPan(StringBuilder builder, int startPos, int panLength, int buffLength) {
        return (!(panLength < 15 || panLength > 19))&&
                (startPos + panLength == buffLength || SensitiveProvider.isDelimiter(builder.charAt(startPos + panLength))
        )&&(SensitiveUtil.checkBankCard(builder.toString().substring(startPos,panLength+startPos)));
    }


}
