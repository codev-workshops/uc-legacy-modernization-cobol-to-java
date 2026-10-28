package com.carddemo.harness.loader;

import java.nio.file.Path;

public final class RepoPaths {
  private final Path root;

  public RepoPaths() {
    this.root = Path.of(System.getProperty("carddemo.repoRoot", "..")).toAbsolutePath().normalize();
  }

  public Path root() {
    return root;
  }

  public Path resolve(String repositoryRelativePath) {
    return root.resolve(repositoryRelativePath);
  }
}
