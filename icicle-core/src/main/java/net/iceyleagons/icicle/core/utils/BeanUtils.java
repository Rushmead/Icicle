package net.iceyleagons.icicle.core.utils;

import net.iceyleagons.icicle.core.proxy.BeanProxyHandler;
import net.iceyleagons.icicle.core.exceptions.BeanCreationException;
import net.iceyleagons.icicle.utilities.Asserts;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Utility methods for the creating and autowiring of beans.
 *
 * @version 1.0.0
 * @since Aug. 23, 2021
 * @author TOTHTOMI
 */
public final class BeanUtils {

    /**
     * Instantiates a class using the supplied constructor and arguments.
     * If a {@link BeanProxyHandler} is present and not null, the object will be created via the proxy and not {@link Constructor#newInstance(Object...)}
     *
     * @param constructor the constructor to use (from {@link #getResolvableConstructor(Class)})
     * @param beanProxyHandler the {@link BeanProxyHandler} to use (can be null)
     * @param arguments the constructor parameters
     * @param <T> the type
     * @return the created bean
     * @throws BeanCreationException if any exception happens during the instantiation
     */
    public static <T> T instantiateClass(Constructor<T> constructor, @Nullable BeanProxyHandler beanProxyHandler, Object... arguments) throws BeanCreationException{
        Asserts.notNull(constructor, "Constructor must not be null!");

        try {
            constructor.setAccessible(true);

            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Asserts.isTrue(arguments.length <= parameterTypes.length, "Cannot specify more arguments than constructor parameters!");

            Object[] argObjects = new Object[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i] == null) {
                    Class<?> paramType = parameterTypes[i];
                    argObjects[i] = paramType.isPrimitive() ? Defaults.DEFAULT_TYPE_VALUES.get(paramType) : null;
                    continue;
                }
                argObjects[i] = arguments[i];
            }

            return beanProxyHandler == null ? constructor.newInstance(argObjects) : beanProxyHandler.createEnhancedBean(constructor, argObjects);
        } catch (InvocationTargetException e) {
            throw new BeanCreationException(constructor, "Constructor execution resulted in an exception.", e);
        } catch (InstantiationException e) {
            throw new BeanCreationException(constructor, "Could not instantiate class. (Is it an abstract class?)");
        } catch (IllegalAccessException e) {
            throw new BeanCreationException(constructor, "Constructor is not accessible! (Is it accessible/public?)");
        }
    }

    /**
     * Returns the resolvable constructor of the class:
     *  - if the class only has 1 constructor it will be used
     *  - if the class has more than 1 constructors, the one with the least parameters will be used (not implemented, an empty constructor is used currently)
     *
     * @param clazz the class
     * @param <T> the type of the class
     * @return the constructor
     * @throws IllegalStateException if no public constructors were found
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getResolvableConstructor(Class<T> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();

        if (constructors.length == 1) return (Constructor<T>) constructors[0];

        try {
            return clazz.getDeclaredConstructor(); //attempting to grab an empty constructor
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No default or single public constructor found for " + clazz);
        }
    }

    /**
     * Attempts to cast the supplied object to the required type.
     * If the object is instance of the required type it will get returned,
     * if the object is not instance of the required type, null will be returned.
     *
     * @param required the required type to cast to
     * @param object the object to cast
     * @param <T> the type wanted
     * @return the casted object or null
     */
    @Nullable
    public static <T> T castIfNecessary(Class<T> required, Object object) {
        return required.isInstance(object) ? required.cast(object) : null;
    }
}