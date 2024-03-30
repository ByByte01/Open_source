package com.fajianchen.sensitive;

import com.fajianchen.sensitive.core.MaskingConverter;
import com.fajianchen.sensitive.core.SensitiveDataConverter;
import com.fajianchen.sensitive.property.CustomPropertiesMasked;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
@ConfigurationProperties(prefix = "sensitive",ignoreInvalidFields = true,ignoreUnknownFields = true)
@ConditionalOnProperty(name = "sensitive.enable", havingValue = "true")
public class SensitiveAutoConfiguration
 {
    /**
     * 是否启用日志脱敏
     */
    private boolean enable = false;

    /**
     * 调试模式
     */
    private boolean debug = false;

    private List optionList;

/*    @Bean
    public SensitiveDataConverter getMaskingConverter() {

    }*/

     @Bean
     public SensitiveDataConverter getMaskingConverter1() {
         MaskingConverter maskingConverter = new MaskingConverter();
         maskingConverter.setIsdebug(debug);
         SensitiveDataConverter sensitiveDataConverter=  maskingConverter.start(optionList);
         return  sensitiveDataConverter;
     }

    @NestedConfigurationProperty
    public CustomPropertiesMasked customPropertiesMasked = new CustomPropertiesMasked();



    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public List getOptionList() {
        return optionList;
    }

    public void setOptionList(List optionList) {
        this.optionList = optionList;
    }

    public CustomPropertiesMasked getCustomPropertiesMasked() {
        return customPropertiesMasked;
    }

    public void setCustomPropertiesMasked(CustomPropertiesMasked customPropertiesMasked) {
        this.customPropertiesMasked = customPropertiesMasked;
    }
}
