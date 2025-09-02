package org.vf.sbCriteriaPager.utils;

import jakarta.persistence.Tuple;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FieldsUtils {

    public static boolean validIdClassHasField(Class pClass, String pField) {
        var hasfield = false;
        final var levels = pField.split("\\.");
        Class tempClass = pClass;
        for (final var field : levels) {
            try {
                final var tempField = tempClass.getDeclaredField(field);
                if (isPrimitive(tempField)) {
                    hasfield = true;
                } else {
                    tempClass = tempField.getType();
                }

            } catch (NoSuchFieldException e) {
                hasfield = false;
            }
        }
        return hasfield;
    }


    public static boolean isPrimitive(Field pField) {
        final var clazz = pField.getType();
        return clazz == String.class ||  Number.class.isAssignableFrom(clazz) || clazz == Boolean.class || clazz == Date.class;
    }

    public static List<String> getFieldsNames(Class clazz) {
        return Arrays.stream(clazz.getDeclaredFields()).map(Field::getName).toList();
    }

}
