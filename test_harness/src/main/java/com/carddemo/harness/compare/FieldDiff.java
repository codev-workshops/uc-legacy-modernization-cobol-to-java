package com.carddemo.harness.compare;

public record FieldDiff(String path, Object expected, Object actual) {}
