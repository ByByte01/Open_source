package com.fajianchen.sensitive.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public interface SensitiveProvider {

     List<Character> DEFAUTE_DELIMITERS =
             Collections.unmodifiableList(Arrays.asList('\'', '"', '<', '>',','));

    static boolean isDelimiter(char character) {
        return Character.isWhitespace(character) || DEFAUTE_DELIMITERS.contains(character);
    }

    static int findNextDelimiter(StringBuilder builder, int startPos, int buffLength) {
        while (startPos < buffLength && !isDelimiter(builder.charAt(startPos))) {
            startPos++;
        }
        return startPos;
    }

    default void initialize(String params){
    }


    int execute(StringBuilder builder, char maskChar, int startPos, int buffLength);
}
