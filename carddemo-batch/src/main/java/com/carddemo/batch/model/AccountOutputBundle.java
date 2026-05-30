package com.carddemo.batch.model;

/**
 * Groups the three output records produced per account by CBACT01C.
 * The processor creates one of these; the writer writes all three files.
 */
public class AccountOutputBundle {

    private final OutfileRecord outfileRecord;
    private final ArryfileRecord arryfileRecord;
    private final VbrcRec1 vbrcRec1;
    private final VbrcRec2 vbrcRec2;

    public AccountOutputBundle(OutfileRecord outfileRecord,
                               ArryfileRecord arryfileRecord,
                               VbrcRec1 vbrcRec1,
                               VbrcRec2 vbrcRec2) {
        this.outfileRecord = outfileRecord;
        this.arryfileRecord = arryfileRecord;
        this.vbrcRec1 = vbrcRec1;
        this.vbrcRec2 = vbrcRec2;
    }

    public OutfileRecord getOutfileRecord() { return outfileRecord; }
    public ArryfileRecord getArryfileRecord() { return arryfileRecord; }
    public VbrcRec1 getVbrcRec1() { return vbrcRec1; }
    public VbrcRec2 getVbrcRec2() { return vbrcRec2; }
}
