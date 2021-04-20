package net.blueoxygen.guard.core.util;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

@UtilityClass
public class ReflectionUtil {

    private static final Field MODIFIERS_FIELD;

    static {
        try {
            MODIFIERS_FIELD = getModifiersField();
            MODIFIERS_FIELD.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getPrivateField(Class<?> clazz, String name)
            throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static Field getModifiersField()
            throws NoSuchFieldException {
        try {
            return Field.class.getDeclaredField("modifiers");
        } catch (NoSuchFieldException e) {
            // http://mail-archives.apache.org/mod_mbox/wicket-commits/202012.mbox/%3C160758367101.25620.10531204496154114178@gitbox.apache.org%3E
            try {
                Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
                getDeclaredFields0.setAccessible(true);
                Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);

                return Arrays.stream(fields)
                        .filter(field -> field.getName().equals("modifiers"))
                        .findFirst().orElse(null);
            } catch (ReflectiveOperationException e1) {
                e.addSuppressed(e1);
            }

            throw e;
        }
    }

    public static void modifyFinalField(Object object,
                                        Field field,
                                        Object value)
            throws Exception {
        MODIFIERS_FIELD.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(object, value);
    }

}
