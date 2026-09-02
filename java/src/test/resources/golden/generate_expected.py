#!/usr/bin/env python3
"""Derive the expected CBACT01C output datasets from the input account file.

The expected bytes are computed ONLY from the rules documented in
docs/MIGRATION_DESIGN.md (record layouts §3, zoned/packed/RDW encodings §4,
per-record flow §5, date reformatting §6). Nothing here imports or executes the
Java implementation, so the fixtures stay an independent oracle for
GoldenMasterTest.

Usage (from anywhere):
    python3 java/src/test/resources/golden/generate_expected.py
writes acctdata.ebcdic.in's expected outputs next to this script:
    OUTFILE.expected.bin   5350 bytes  (50 x 107, RECFM=FB)
    ARRYFILE.expected.bin  5500 bytes  (50 x 110, RECFM=FB)
    VBRCFILE.expected.bin  2950 bytes  (50 x (4+12 + 4+39), RECFM=VB)
"""

from decimal import Decimal
from pathlib import Path

EBCDIC = "cp037"  # IBM037

ACCT_LRECL = 300
POSITIVE_OVERPUNCH = "{ABCDEFGHI"
NEGATIVE_OVERPUNCH = "}JKLMNOPQR"

# Hand-built COMP-3 bytes for the only packed values CBACT01C ever stores:
# S9(10)V99 COMP-3 is 7 bytes = 13 digit nibbles + sign nibble (C=+/0, D=-).
# The hex digits below are literally the 13 digit nibbles followed by the sign nibble.
PACKED_2525_00 = bytes.fromhex("0000000252500C")        # 00 00 00 02 52 50 0C
PACKED_1005_00 = bytes.fromhex("0000000100500C")        # 00 00 00 01 00 50 0C
PACKED_1525_00 = bytes.fromhex("0000000152500C")        # 00 00 00 01 52 50 0C
PACKED_MINUS_2500_00 = bytes.fromhex("0000000250000D")  # 00 00 00 02 50 00 0D
PACKED_ZERO = bytes.fromhex("0000000000000C")           # 00 00 00 00 00 00 0C

ARR_CURR_BAL_3 = Decimal("-1025.00")


def text(raw: bytes, off: int, length: int) -> str:
    return raw[off:off + length].decode(EBCDIC)


def decode_zoned(raw: bytes, off: int, digits: int = 12, scale: int = 2) -> Decimal:
    """PIC S9(10)V99 DISPLAY -> Decimal. Accepts C/D overpunch and plain F digits."""
    field = raw[off:off + digits].decode(EBCDIC)
    last = field[-1]
    if last in POSITIVE_OVERPUNCH:
        sign, low = 1, POSITIVE_OVERPUNCH.index(last)
    elif last in NEGATIVE_OVERPUNCH:
        sign, low = -1, NEGATIVE_OVERPUNCH.index(last)
    elif last.isdigit():
        sign, low = 1, int(last)
    else:
        raise ValueError(f"invalid zoned sign byte {last!r} at offset {off + digits - 1}")
    unscaled = int(field[:-1] + str(low))
    return Decimal(sign * unscaled).scaleb(-scale)


def encode_zoned(value: Decimal, digits: int = 12, scale: int = 2) -> bytes:
    """PIC S9(10)V99 DISPLAY: sign overpunched in the last byte, C for +/0, D for -."""
    unscaled = int(value.scaleb(scale).to_integral_value())
    body = str(abs(unscaled)).rjust(digits, "0")[-digits:]
    table = NEGATIVE_OVERPUNCH if unscaled < 0 else POSITIVE_OVERPUNCH
    return (body[:-1] + table[int(body[-1])]).encode(EBCDIC)


def encode_unsigned(digits_text: str) -> bytes:
    """PIC 9(n) DISPLAY: plain digits, zone F."""
    if not digits_text.isdigit():
        raise ValueError(f"not a digit string: {digits_text!r}")
    return digits_text.encode(EBCDIC)


def reformat_reissue(reissue: str) -> str:
    """COBDATFT type '2'/'2': YYYY-MM-DD -> YYYYMMDD positionally, + 2 fill spaces (§6)."""
    return reissue[0:4] + reissue[5:7] + reissue[8:10] + "  "


def rdw(data: bytes) -> bytes:
    """RECFM=VB record descriptor word: big-endian (len + 4), then two zero bytes (§4.1)."""
    total = len(data) + 4
    return bytes([(total >> 8) & 0xFF, total & 0xFF, 0x00, 0x00]) + data


def outfile_record(raw: bytes, carried_debit: bytes):
    """OUT-ACCT-REC, 107 bytes (§3.2 / §5.1)."""
    debit = PACKED_2525_00 if decode_zoned(raw, 90) == 0 else carried_debit
    rec = (
        encode_unsigned(text(raw, 0, 11))                       # OUT-ACCT-ID
        + text(raw, 11, 1).encode(EBCDIC)                       # OUT-ACCT-ACTIVE-STATUS
        + encode_zoned(decode_zoned(raw, 12))                   # OUT-ACCT-CURR-BAL
        + encode_zoned(decode_zoned(raw, 24))                   # OUT-ACCT-CREDIT-LIMIT
        + encode_zoned(decode_zoned(raw, 36))                   # OUT-ACCT-CASH-CREDIT-LIMIT
        + text(raw, 48, 10).encode(EBCDIC)                      # OUT-ACCT-OPEN-DATE
        + text(raw, 58, 10).encode(EBCDIC)                      # OUT-ACCT-EXPIRAION-DATE
        + reformat_reissue(text(raw, 68, 10)).encode(EBCDIC)     # OUT-ACCT-REISSUE-DATE
        + encode_zoned(decode_zoned(raw, 78))                   # OUT-ACCT-CURR-CYC-CREDIT
        + debit                                                 # OUT-ACCT-CURR-CYC-DEBIT COMP-3
        + text(raw, 112, 10).encode(EBCDIC)                     # OUT-ACCT-GROUP-ID
    )
    assert len(rec) == 107, len(rec)
    return rec, debit


def arryfile_record(raw: bytes) -> bytes:
    """ARR-ARRAY-REC, 110 bytes: INITIALIZE + 1400-POPUL-ARRAY-RECORD (§3.3 / §5.2)."""
    bal = decode_zoned(raw, 12)
    occurrences = [
        (encode_zoned(bal), PACKED_1005_00),
        (encode_zoned(bal), PACKED_1525_00),
        (encode_zoned(ARR_CURR_BAL_3), PACKED_MINUS_2500_00),
        (encode_zoned(Decimal("0.00")), PACKED_ZERO),
        (encode_zoned(Decimal("0.00")), PACKED_ZERO),
    ]
    rec = encode_unsigned(text(raw, 0, 11))
    for zoned, packed in occurrences:
        rec += zoned + packed
    rec += "    ".encode(EBCDIC)  # ARR-FILLER X(4)
    assert len(rec) == 110, len(rec)
    return rec


def vbrcfile_records(raw: bytes) -> bytes:
    """VBRC-REC1 (12) then VBRC-REC2 (39), each with its own RDW (§3.4 / §3.5 / §4.1)."""
    vb1 = encode_unsigned(text(raw, 0, 11)) + text(raw, 11, 1).encode(EBCDIC)
    vb2 = (
        encode_unsigned(text(raw, 0, 11))
        + encode_zoned(decode_zoned(raw, 12))     # VB2-ACCT-CURR-BAL
        + encode_zoned(decode_zoned(raw, 24))     # VB2-ACCT-CREDIT-LIMIT
        + text(raw, 68, 4).encode(EBCDIC)         # VB2-ACCT-REISSUE-YYYY (original date)
    )
    assert len(vb1) == 12 and len(vb2) == 39
    return rdw(vb1) + rdw(vb2)


def generate(acct_bytes: bytes):
    if len(acct_bytes) % ACCT_LRECL != 0:
        raise ValueError(f"input is not a multiple of {ACCT_LRECL} bytes: {len(acct_bytes)}")
    outfile = bytearray()
    arryfile = bytearray()
    vbrcfile = bytearray()
    carried = PACKED_ZERO  # first record's carried OUT-ACCT-CURR-CYC-DEBIT is packed +0 (§5.1)
    for off in range(0, len(acct_bytes), ACCT_LRECL):
        raw = acct_bytes[off:off + ACCT_LRECL]
        rec, carried = outfile_record(raw, carried)
        outfile += rec
        arryfile += arryfile_record(raw)
        vbrcfile += vbrcfile_records(raw)
    return bytes(outfile), bytes(arryfile), bytes(vbrcfile)


def main() -> None:
    here = Path(__file__).resolve().parent
    acct = here / "acctdata.ebcdic.in"
    outfile, arryfile, vbrcfile = generate(acct.read_bytes())
    for name, data in (
        ("OUTFILE.expected.bin", outfile),
        ("ARRYFILE.expected.bin", arryfile),
        ("VBRCFILE.expected.bin", vbrcfile),
    ):
        (here / name).write_bytes(data)
        print(f"{name}: {len(data)} bytes")
    print("OUTFILE  rec  1:", outfile[0:107].hex(" ").upper())
    print("OUTFILE  rec 50:", outfile[49 * 107:50 * 107].hex(" ").upper())
    print("ARRYFILE rec  1:", arryfile[0:110].hex(" ").upper())
    print("ARRYFILE rec 50:", arryfile[49 * 110:50 * 110].hex(" ").upper())
    print("VBRCFILE rec  1:", vbrcfile[0:59].hex(" ").upper())
    print("VBRCFILE rec 50:", vbrcfile[49 * 59:50 * 59].hex(" ").upper())


if __name__ == "__main__":
    main()
