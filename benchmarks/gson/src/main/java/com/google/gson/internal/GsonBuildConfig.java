

package com.google.gson.internal;


public final class GsonBuildConfig {
  // Based on https://stackoverflow.com/questions/2469922/generate-a-version-java-file-in-maven

  
  public static final String VERSION = "${project.version}";

  private GsonBuildConfig() {}
}
