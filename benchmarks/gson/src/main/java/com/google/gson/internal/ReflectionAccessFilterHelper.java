

package com.google.gson.internal;

import com.google.gson.ReflectionAccessFilter;
import com.google.gson.ReflectionAccessFilter.FilterResult;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.List;


public class ReflectionAccessFilterHelper {
  private ReflectionAccessFilterHelper() {}

  // Platform type detection is based on Moshi's Util.isPlatformType(Class)
  // See
  // https://github.com/square/moshi/blob/3c108919ee1cce88a433ffda04eeeddc0341eae7/moshi/src/main/java/com/squareup/moshi/internal/Util.java#L141

  public static boolean isJavaType(Class<?> c) {
    return isJavaType(c.getName());
  }

  private static boolean isJavaType(String className) {
    return className.startsWith("java.") || className.startsWith("javax.");
  }

  public static boolean isAndroidType(Class<?> c) {
    return isAndroidType(c.getName());
  }

  private static boolean isAndroidType(String className) {
    return className.startsWith("android.")
        || className.startsWith("androidx.")
        || isJavaType(className);
  }

  public static boolean isAnyPlatformType(Class<?> c) {
    String className = c.getName();
    return isAndroidType(className) // Covers Android and Java
        || className.startsWith("kotlin.")
        || className.startsWith("kotlinx.")
        || className.startsWith("scala.");
  }

  
  public static FilterResult getFilterResult(
      List<ReflectionAccessFilter> reflectionFilters, Class<?> c) {
    for (ReflectionAccessFilter filter : reflectionFilters) {
      FilterResult result = filter.check(c);
      if (result != FilterResult.INDECISIVE) {
        return result;
      }
    }
    return FilterResult.ALLOW;
  }

  
  public static boolean canAccess(AccessibleObject accessibleObject, Object object) {
    return AccessChecker.INSTANCE.canAccess(accessibleObject, object);
  }

  private abstract static class AccessChecker {
    public static final AccessChecker INSTANCE;

    static {
      AccessChecker accessChecker = null;
      // TODO: Ideally should use Multi-Release JAR for this version specific code
      if (JavaVersion.isJava9OrLater()) {
        try {
          final Method canAccessMethod =
              AccessibleObject.class.getDeclaredMethod("canAccess", Object.class);
          accessChecker =
              new AccessChecker() {
                @Override
                public boolean canAccess(AccessibleObject accessibleObject, Object object) {
                  try {
                    return (Boolean) canAccessMethod.invoke(accessibleObject, object);
                  } catch (Exception e) {
                    throw new RuntimeException("Failed invoking canAccess", e);
                  }
                }
              };
        } catch (NoSuchMethodException ignored) {
          // OK: will assume everything is accessible
        }
      }

      if (accessChecker == null) {
        accessChecker =
            new AccessChecker() {
              @Override
              public boolean canAccess(AccessibleObject accessibleObject, Object object) {
                // Cannot determine whether object can be accessed, so assume it can be accessed
                return true;
              }
            };
      }
      INSTANCE = accessChecker;
    }

    public abstract boolean canAccess(AccessibleObject accessibleObject, Object object);
  }
}
