package dto;

import data.LoggingUtility;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 27.01.13
 * Time: 20:49
 */
public class ReflectionUtility {
  private static final Logger LOGGER =
      LoggingUtility.getLogger(ReflectionUtility.class.getName());

  /** Returns the getter methods of a class. The sorted sequence is the sequence of the
   * corresponding fields declared in the class. */
  public static <T>List<Method> getSortedGetters(Class<T> aClass){
    Method[] declaredMethods = aClass.getDeclaredMethods();
    List<Field> declaredFields = new ArrayList<Field>();
    Collections.addAll(declaredFields, aClass.getDeclaredFields());
    final List<Method> methods = new ArrayList<Method>(declaredFields.size());

    for (Field field : declaredFields) {
      for(Method method : declaredMethods){
        if(matchesGetter(method, field))
          methods.add(method);
      }
    }

    return methods;
  }

  private static boolean matchesGetter(Method method, Field field) {
    final String methodName = method.getName();

    if(!isGetter(method)) return false;
    if(!method.getReturnType().equals(field.getType())) return false;
    if(field.getType().equals(Boolean.class)) {
      if(!methodName.equalsIgnoreCase("get" + field.getName()) &&
         !methodName.equalsIgnoreCase("is" + field.getName())) return false;
    } else {
      if(!methodName.equalsIgnoreCase("get" + field.getName())) return false;
    }
    return true;
  }

  private static boolean matchesSetter(Method method, Field field) {
    final String methodName = method.getName();

    if(!isSetter(method)) return false;
    if(!method.getParameterTypes()[0].equals(field.getType())) return false;
    if(!methodName.equalsIgnoreCase("set" + field.getName())) return false;
    return true;
  }

  /** Returns the setter methods of a class. The sorted sequence is the sequence of the
   * corresponding fields declared in the class. */
  public static <T>List<Method> getSortedSetters(Class<T> aClass) {
    Method[] declaredMethods = aClass.getDeclaredMethods();
    List<Field> declaredFields = new ArrayList<Field>();
    Collections.addAll(declaredFields, aClass.getDeclaredFields());
    final List<Method> methods = new ArrayList<Method>(declaredFields.size());

    for (Field field : declaredFields) {
      for(Method method : declaredMethods){
        if(matchesSetter(method, field))
          methods.add(method);
      }
    }

    return methods;
  }

  public static boolean isGetter(Method method){
    if(!(method.getName().startsWith("get")
      || method.getName().startsWith("is"))) return false;
    if(method.getParameterTypes().length != 0) return false;
    if(void.class.equals(method.getReturnType())) return false;
    return true;
  }

  public static boolean isSetter(Method method){
    if(!method.getName().startsWith("set")) return false;
    if(method.getParameterTypes().length != 1) return false;
    return true;
  }

  /** Returns a list of field that have at least a getter and a setter method */
  public static <T>List<Field> getterSetterFields(Class<T> aClass) {
    Field[] declaredFields = aClass.getDeclaredFields();
    List<Field> getterSetterFields = new ArrayList<Field>(declaredFields.length);
    List<Method> getter = getSortedGetters(aClass);
    List<Method> setter = getSortedSetters(aClass);

    for (Field field : declaredFields) {
      if (findGetter(getter, field) && findSetter(setter, field))
        getterSetterFields.add(field);
    }
    return getterSetterFields;
  }

  /**
   * Returns null if the comparator never returned 0 for the compare method.
   * The compare method will be called with a method as the first and the field as the
   * second parameter.
   */
  private static Method findMethodForField(List<Method> methods, Field field,
                                          Comparator<AccessibleObject> comparator) {
    for (Method method : methods) {
      if(comparator.compare(method, field) == 0)
        return method;
    }
    return null;
  }

  public static boolean findGetter(List<Method> getter, Field field) {
    final Comparator<AccessibleObject> comparator = new Comparator<AccessibleObject>() {
      public int compare(AccessibleObject method, AccessibleObject field) {
        if(method instanceof Method && field instanceof Field)
          if(matchesGetter((Method) method, (Field) field))
            return 0;
        return -1;
      }
    };
    return findMethodForField(getter, field, comparator) != null;
  }

  public static boolean findSetter(List<Method> setter, Field field) {
    final Comparator<AccessibleObject> comparator = new Comparator<AccessibleObject>() {
      public int compare(AccessibleObject method, AccessibleObject field) {
        if(method instanceof Method && field instanceof Field)
          if(matchesSetter((Method) method, (Field) field))
            return 0;
        return -1;
      }
    };
    return findMethodForField(setter, field, comparator) != null;
  }

  public static String getValue(Object object, String getterMethodName) {
    try {
      final Object o = object.getClass().getMethod(getterMethodName).invoke(object);
      return o != null ? o.toString() : null;
    } catch (NoSuchMethodException e) {
      LOGGER.warning("Method " + getterMethodName + " could not be found!");
    } catch (InvocationTargetException e) {
      LOGGER.warning("Method invocation failed: " + e.getMessage());
    } catch (IllegalAccessException e) {
      LOGGER.warning("No access to method: "+e.getMessage());
    }
    return null;
  }
}
