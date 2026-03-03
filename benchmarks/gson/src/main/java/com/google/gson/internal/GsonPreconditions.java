

package com.google.gson.internal;


public final class GsonPreconditions {
  private GsonPreconditions() {
    throw new UnsupportedOperationException();
  }

  
  // Only deprecated for now because external projects might be using this by accident
  @Deprecated
  public static <T> T checkNotNull(T obj) {
    if (obj == null) {
      throw new NullPointerException();
    }
    return obj;
  }

  public static void checkArgument(boolean condition) {
    if (!condition) {
      throw new IllegalArgumentException();
    }
  }
}
