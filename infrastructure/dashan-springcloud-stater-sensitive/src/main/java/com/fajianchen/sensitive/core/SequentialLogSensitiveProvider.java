package com.fajianchen.sensitive.core;


public interface SequentialLogSensitiveProvider {

    default void initialize(String args) {
    }


    boolean execute(StringBuilder unmasked, char maskChar);
}
