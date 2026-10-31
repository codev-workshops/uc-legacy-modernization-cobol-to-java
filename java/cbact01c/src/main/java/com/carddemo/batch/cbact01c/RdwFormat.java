package com.carddemo.batch.cbact01c;

/** How each record of a variable-length ({@code RECFM=VB}) file is prefixed on disk. */
public enum RdwFormat {

  /**
   * z/OS record descriptor word: 2-byte big-endian length that includes the 4-byte RDW, then 2 zero
   * bytes. This is the layout of a VB dataset transferred in binary with RDWs kept.
   */
  ZOS(true),

  /**
   * GnuCOBOL's default variable-record header ({@code COB_VARSEQ_FORMAT=0}): 2-byte big-endian
   * length of the data only, then 2 zero bytes. Used to compare byte for byte with the GnuCOBOL
   * oracle.
   */
  GNUCOBOL(false);

  /** Length of the record prefix. */
  public static final int PREFIX_LENGTH = 4;

  private final boolean lengthIncludesPrefix;

  RdwFormat(boolean lengthIncludesPrefix) {
    this.lengthIncludesPrefix = lengthIncludesPrefix;
  }

  /** Returns the prefix for a record of {@code dataLength} bytes. */
  byte[] prefix(int dataLength) {
    int length = lengthIncludesPrefix ? dataLength + PREFIX_LENGTH : dataLength;
    return new byte[] {(byte) (length >> 8), (byte) length, 0, 0};
  }

  /** Returns the data length recorded in {@code prefix}. */
  int dataLength(byte[] prefix) {
    int length = (prefix[0] & 0xFF) << 8 | prefix[1] & 0xFF;
    return lengthIncludesPrefix ? length - PREFIX_LENGTH : length;
  }
}
