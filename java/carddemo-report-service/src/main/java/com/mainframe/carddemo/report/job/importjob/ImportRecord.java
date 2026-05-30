package com.mainframe.carddemo.report.job.importjob;

public class ImportRecord {

    private final String type;
    private final String[] fields;

    public ImportRecord(String type, String[] fields) {
        this.type = type;
        this.fields = fields;
    }

    public String getType() {
        return type;
    }

    public String[] getFields() {
        return fields;
    }

    public String field(int index) {
        if (index < 0 || index >= fields.length) {
            return "";
        }
        return fields[index];
    }
}
