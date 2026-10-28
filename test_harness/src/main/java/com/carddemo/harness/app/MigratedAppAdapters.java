package com.carddemo.harness.app;

public final class MigratedAppAdapters {
  private MigratedAppAdapters() {}

  public static MigratedAppAdapter resolve() {
    String adapterClass = System.getProperty("carddemo.app.adapter");
    if (adapterClass == null || adapterClass.isBlank()) {
      return new NotYetAvailableAdapter();
    }
    try {
      return (MigratedAppAdapter) Class.forName(adapterClass).getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException | ClassCastException exception) {
      throw new IllegalStateException("Could not load migrated app adapter " + adapterClass, exception);
    }
  }
}
