package com.carddemo.harness.report;

import com.carddemo.harness.comparator.CompareResult;

/**
 * Single field comparison detail within a record.
 */
public class FieldComparisonDetail {

    private final String fieldName;
    private final String cobolValue;
    private final String javaValue;
    private final CompareResult result;
    private final String annotation;

    public FieldComparisonDetail(String fieldName, String cobolValue, String javaValue,
                                  CompareResult result, String annotation) {
        this.fieldName = fieldName;
        this.cobolValue = cobolValue;
        this.javaValue = javaValue;
        this.result = result;
        this.annotation = annotation;
    }

    public FieldComparisonDetail(String fieldName, String cobolValue, String javaValue,
                                  CompareResult result) {
        this(fieldName, cobolValue, javaValue, result, null);
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getCobolValue() {
        return cobolValue;
    }

    public String getJavaValue() {
        return javaValue;
    }

    public CompareResult getResult() {
        return result;
    }

    public String getAnnotation() {
        return annotation;
    }

    @Override
    public String toString() {
        String base = String.format("  %-30s: %s vs %s [%s]", fieldName, cobolValue, javaValue, result);
        if (annotation != null && !annotation.isEmpty()) {
            base += " (" + annotation + ")";
        }
        return base;
    }
}
