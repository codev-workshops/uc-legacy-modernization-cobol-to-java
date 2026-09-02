package com.carddemo.batch.cbact01c.date;

/** Java counterpart of {@code CALL 'COBDATFT' USING CODATECN-REC}. */
public interface DateFormatter {

    DateConversionResponse convert(DateConversionRequest request);
}
