package com.fajianchen.sensitive.core;

import java.util.List;

public class MaskingConverter {

    private boolean isdebug = false;
    private boolean isEnable = false;

    public SensitiveDataConverter start(List<String> optionList) {
        SensitiveDataConverter sensitiveDataConverter = new SensitiveDataConverter();

        try {
            sensitiveDataConverter.init(optionList);
        } catch (Exception e) {
            System.err.println(e);
        }
        return sensitiveDataConverter;
    }


    public void setIsdebug(boolean isdebug) {
        this.isdebug = isdebug;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }
}
