package com.fajianchen.sensitive.core;

import com.fajianchen.sensitive.provider.*;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;


public class SensitiveDataConverter {
    private static final List<Character> STOP_CHARACTERS = Arrays.asList('\'', '"', '@', '>');

    private static final Map<String, SensitiveProvider> DEFAULT_SENSITIVE = initializeDefaultSensitiveProvider();

    private static final List<SensitiveProvider> ALL_MASKERS = DEFAULT_SENSITIVE.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());

    private List<SensitiveProvider> sensitiveProvides = new ArrayList<>();

    private List<SequentialLogSensitiveProvider> sequentialMaskers = new ArrayList<>();

    public void init(List<String> options) {
        if (options != null) {
            for (String option : options) {
                if (StringUtils.startsWith(option, "CUSTOM")) {
                    sensitiveProvides.addAll(buildCustomMaskersList(option));
                    sequentialMaskers.addAll(buildCustomSequentialMaskersList(option));
                } else if (option.equalsIgnoreCase("ALL")) {
                    sensitiveProvides.addAll(ALL_MASKERS);
                } else {
                    SensitiveProvider masker = getSensitiveProviderFromOptions(option);
                    sensitiveProvides.add(masker);
                }
            }
        }

        if (sensitiveProvides.isEmpty()) {
            sensitiveProvides.addAll(ALL_MASKERS);
        }
    }

    public void setSensitiveProvides(List<SensitiveProvider> sensitiveProvides) {
        this.sensitiveProvides.clear();
        this.sensitiveProvides.addAll(sensitiveProvides);
    }

    private SensitiveProvider getSensitiveProviderFromOptions(String option) {
        String args = null;
        int idxOfArgsSeparator = StringUtils.indexOf(option, ':');
        if (idxOfArgsSeparator > 0) {
            args = StringUtils.substring(option, idxOfArgsSeparator + 1);
            option = StringUtils.substring(option, 0, idxOfArgsSeparator);
        }
        SensitiveProvider sensitiveProvider = DEFAULT_SENSITIVE.get(option);
        if (sensitiveProvider == null) {
            throw new ExceptionInInitializerError("无效的脱敏器: " + option);
        }
        if (args != null) {
            sensitiveProvider.initialize(args);
        }

        return sensitiveProvider;
    }

    private static List<SensitiveProvider> buildCustomMaskersList(String params) {
        int idxOfArgsSeparator = StringUtils.indexOf(params, ':');
        if (idxOfArgsSeparator < 0) {
            return Collections.emptyList();
        }
        List<SensitiveProvider> maskers = new ArrayList<>();

        String args = StringUtils.substring(params, idxOfArgsSeparator + 1);
        String[] packages = StringUtils.split(args, '|');
        for (String pack : packages) {
            Reflections reflections = new Reflections(pack);
            initializeCustomLogSensitiveProvides(maskers, reflections);
        }

        return maskers;
    }

    private static void initializeCustomLogSensitiveProvides(List<SensitiveProvider> maskers, Reflections reflections) {
        Set<Class<? extends SensitiveProvider>> allClasses = reflections.getSubTypesOf(SensitiveProvider.class);
        for (Class<? extends SensitiveProvider> clazz : allClasses) {
            try {
                Constructor<? extends SensitiveProvider> SensitiveProviderConstructor = clazz.getConstructor();
                SensitiveProvider masker = SensitiveProviderConstructor.newInstance();
                maskers.add(masker);
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    private static List<SequentialLogSensitiveProvider> buildCustomSequentialMaskersList(String params) {
        int idxOfArgsSeparator = StringUtils.indexOf(params, ':');
        if (idxOfArgsSeparator < 0) {
            return Collections.emptyList();
        }
        List<SequentialLogSensitiveProvider> sensitiveProviders = new ArrayList<>();

        String args = StringUtils.substring(params, idxOfArgsSeparator + 1);
        String[] packages = StringUtils.split(args, '|');
        for (String pack : packages) {
            Reflections reflections = new Reflections(pack);
            initializeCustomSequentialLogSensitiveProvides(sensitiveProviders, reflections);
        }

        return sensitiveProviders;
    }

    private static void initializeCustomSequentialLogSensitiveProvides(List<SequentialLogSensitiveProvider> logSensitiveProviders, Reflections reflections) {
        Set<Class<? extends SequentialLogSensitiveProvider>> allClasses = reflections.getSubTypesOf(SequentialLogSensitiveProvider.class);
        for (Class<? extends SequentialLogSensitiveProvider> clazz : allClasses) {
            try {
                Constructor<? extends SequentialLogSensitiveProvider> maskerConstructor = clazz.getConstructor();
                SequentialLogSensitiveProvider logSensitiveProvider = maskerConstructor.newInstance();
                logSensitiveProviders.add(logSensitiveProvider);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public StringBuilder  execute(StringBuilder stringBuilder) {
        boolean maskedThisCharacter;
        int pos;
        int newPos;
        int length = stringBuilder.length();
        for (pos = 0; pos < length; pos++) {
            maskedThisCharacter = false;
            for (SensitiveProvider masker : sensitiveProvides) {
                newPos = masker.execute(stringBuilder, '*', pos, length);
                maskedThisCharacter = newPos != pos;
                if (maskedThisCharacter) {
                    length = stringBuilder.length();
                    maskedThisCharacter = true;
                    break;
                }
            }
            if (!maskedThisCharacter) {
                while (pos < length && !(Character.isWhitespace(stringBuilder.charAt(pos)) || STOP_CHARACTERS.contains(stringBuilder.charAt(pos)))) {
                    pos++;
                }
            }
        }

        maskSequential(stringBuilder);
        return stringBuilder;
    }

    private void maskSequential(StringBuilder builder) {
        StringBuilder stringBuilderTemp=new StringBuilder();
        for (SequentialLogSensitiveProvider masker : sequentialMaskers) {
            try {
                masker.execute(builder, '*');
            } catch (Exception e) {
                System.err.println("Error applying masker " + masker + ". Error: " + e.getMessage());
            }
        }
    }

    private static Map<String, SensitiveProvider> initializeDefaultSensitiveProvider() {

        Map<String, SensitiveProvider> maskers = new HashMap<>();
        maskers.put("email", new EmailSensitiveProvider());
        maskers.put("pass", new PasswordSensitiveProvider());
        maskers.put("ip", new IPSensitiveProvider());
        maskers.put("card", new CardNumberSensitiveProvider());
        maskers.put("iban", new IbanSensitiveProvider());
        maskers.put("idCard", new IdCardNumberSensitiveProvider());

        return maskers;
    }
}
