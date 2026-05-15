#!/usr/bin/env python3
"""
BMS Map Processor - Converts BMS map definitions to COBOL copybooks.

Parses IBM BMS (Basic Mapping Support) map source files and generates:
1. COBOL copybooks with input/output data structures for each map
2. A C header with map field definitions for the CICS runtime

This enables the CICS simulator to correctly handle SEND MAP and
RECEIVE MAP operations without a mainframe BMS assembler.

Usage:
    python3 bms2cpy.py <bms-dir> <output-cpy-dir> [--c-header <file>]
"""

import sys
import os
import re
import argparse
from dataclasses import dataclass, field
from typing import List, Optional


@dataclass
class BMSField:
    name: str = ''
    pos_row: int = 0
    pos_col: int = 0
    length: int = 0
    attrb: List[str] = field(default_factory=list)
    color: str = ''
    initial: str = ''
    picin: str = ''
    picout: str = ''
    is_input: bool = False
    is_output: bool = True


@dataclass
class BMSMap:
    name: str = ''
    mapset: str = ''
    rows: int = 24
    cols: int = 80
    fields: List[BMSField] = field(default_factory=list)
    tioapfx: bool = True
    lang: str = 'COBOL'


class BMSParser:
    """Parse BMS macro source into structured map definitions."""

    def __init__(self):
        self.mapsets = []  # List of (mapset_name, [BMSMap])

    def parse_file(self, filepath):
        """Parse a single BMS file."""
        with open(filepath, 'r') as f:
            lines = f.readlines()

        # Join continuation lines (ending with -)
        joined_lines = []
        current = ''
        for line in lines:
            # Skip comment lines
            stripped = line.rstrip('\n')
            if stripped.startswith('*'):
                continue

            # Check for continuation (column 72 has '-' or line ends with -)
            if len(stripped) > 71 and stripped[71] == '-':
                current += stripped[:71].rstrip()
                continue
            elif stripped.rstrip().endswith('-'):
                current += stripped.rstrip()[:-1]
                continue
            else:
                current += stripped
                joined_lines.append(current)
                current = ''

        if current:
            joined_lines.append(current)

        # Parse macros
        current_mapset = None
        current_map = None
        maps = []

        for line in joined_lines:
            line = line.strip()
            if not line or line.startswith('*'):
                continue

            # Parse DFHMSD (mapset definition)
            m = re.match(r'^(\w+)\s+DFHMSD\s+(.*)', line, re.IGNORECASE)
            if m:
                current_mapset = m.group(1)
                params = self._parse_macro_params(m.group(2))
                if params.get('TYPE', '') == '&&SYSPARM':
                    continue
                if 'FINAL' in params.get('TYPE', ''):
                    continue
                continue

            # Parse DFHMDI (map definition)
            m = re.match(r'^(\w+)\s+DFHMDI\s+(.*)', line, re.IGNORECASE)
            if m:
                if current_map:
                    maps.append(current_map)
                current_map = BMSMap()
                current_map.name = m.group(1)
                current_map.mapset = current_mapset or ''
                params = self._parse_macro_params(m.group(2))
                size = params.get('SIZE', '(24,80)')
                size_m = re.match(r'\((\d+),(\d+)\)', size)
                if size_m:
                    current_map.rows = int(size_m.group(1))
                    current_map.cols = int(size_m.group(2))
                continue

            # Parse DFHMDF (field definition)
            m = re.match(r'^(\w*)\s+DFHMDF\s+(.*)', line, re.IGNORECASE)
            if m:
                if not current_map:
                    continue
                field_obj = BMSField()
                field_obj.name = m.group(1) if m.group(1) else ''
                params = self._parse_macro_params(m.group(2))

                # Position
                pos = params.get('POS', '(1,1)')
                pos_m = re.match(r'\((\d+),(\d+)\)', pos)
                if pos_m:
                    field_obj.pos_row = int(pos_m.group(1))
                    field_obj.pos_col = int(pos_m.group(2))

                # Length
                field_obj.length = int(params.get('LENGTH', '0'))

                # Attributes
                attrb = params.get('ATTRB', '')
                if attrb.startswith('(') and attrb.endswith(')'):
                    attrb = attrb[1:-1]
                field_obj.attrb = [a.strip() for a in attrb.split(',')]

                # Determine if input field
                if 'UNPROT' in field_obj.attrb or 'FSET' in field_obj.attrb:
                    if 'PROT' not in field_obj.attrb and \
                       'ASKIP' not in field_obj.attrb:
                        field_obj.is_input = True

                # If no protect attrs at all, could be input
                has_protect = any(a in ('PROT', 'ASKIP')
                                  for a in field_obj.attrb)
                if not has_protect and field_obj.name:
                    field_obj.is_input = True

                # Color
                field_obj.color = params.get('COLOR', '')

                # Initial value
                field_obj.initial = params.get('INITIAL', '').strip("'")

                # PIC clauses
                field_obj.picin = params.get('PICIN', '')
                field_obj.picout = params.get('PICOUT', '')

                current_map.fields.append(field_obj)
                continue

        if current_map:
            maps.append(current_map)

        if maps:
            self.mapsets.append((current_mapset or 'UNKNOWN', maps))

        return maps

    def _parse_macro_params(self, text):
        """Parse BMS macro parameters (key=value pairs)."""
        params = {}
        text = text.strip().rstrip(',')

        # Handle nested parentheses
        i = 0
        while i < len(text):
            # Skip whitespace and commas
            while i < len(text) and text[i] in ' ,\t':
                i += 1
            if i >= len(text):
                break

            # Find key
            key_start = i
            while i < len(text) and text[i] not in '=, \t':
                i += 1
            key = text[key_start:i].strip()

            if i >= len(text) or text[i] != '=':
                # Flag parameter (no value)
                if key:
                    params[key.upper()] = key
                continue

            i += 1  # skip =

            # Find value
            if i < len(text) and text[i] == '(':
                # Parenthesized value
                depth = 0
                val_start = i
                while i < len(text):
                    if text[i] == '(':
                        depth += 1
                    elif text[i] == ')':
                        depth -= 1
                        if depth == 0:
                            i += 1
                            break
                    i += 1
                value = text[val_start:i]
            elif i < len(text) and text[i] == "'":
                # Quoted value
                i += 1
                val_start = i
                while i < len(text) and text[i] != "'":
                    i += 1
                value = text[val_start:i]
                if i < len(text):
                    i += 1
            else:
                # Unquoted value
                val_start = i
                while i < len(text) and text[i] not in ', \t':
                    i += 1
                value = text[val_start:i]

            if key:
                params[key.upper()] = value

        return params


class CopybookGenerator:
    """Generate COBOL copybooks from parsed BMS maps."""

    def generate_copybook(self, bms_map):
        """Generate a COBOL copybook for a BMS map (input and output areas)."""
        lines = []
        mapset = bms_map.mapset
        map_name = bms_map.name

        # Output area (xxxO)
        lines.append(f'      * Generated by CICS Simulator BMS processor\n')
        lines.append(f'      * Map: {map_name} Mapset: {mapset}\n')
        lines.append(f'       01  {map_name}O.\n')
        lines.append(f'         05  FILLER              PIC X(12).\n')

        for fld in bms_map.fields:
            if not fld.name:
                continue  # Skip unnamed (label-only) fields
            name = fld.name.upper()
            length = fld.length if fld.length > 0 else 1

            # BMS output structure: length + attr + color + data
            lines.append(
                f'         05  {name}L     PIC S9(04) COMP VALUE 0.\n')
            lines.append(
                f'         05  {name}A     PIC X(01) VALUE SPACES.\n')
            lines.append(
                f'         05  {name}C     PIC X(01) VALUE SPACES.\n')

            # Use PICOUT if defined (numeric-edited output)
            if fld.picout:
                pic_clause = fld.picout.strip("'")
                lines.append(
                    f'         05  {name}O     PIC {pic_clause}.\n')
            else:
                lines.append(
                    f'         05  {name}O     PIC X({length:02d})'
                    f' VALUE SPACES.\n')

        lines.append(f'      *\n')

        # Input area (xxxI)
        lines.append(f'       01  {map_name}I.\n')
        lines.append(f'         05  FILLER              PIC X(12).\n')

        for fld in bms_map.fields:
            if not fld.name:
                continue
            name = fld.name.upper()
            length = fld.length if fld.length > 0 else 1

            lines.append(
                f'         05  {name}L     PIC S9(04) COMP VALUE 0.\n')
            lines.append(
                f'         05  {name}A     PIC X(01) VALUE SPACES.\n')
            lines.append(
                f'         05  {name}C     PIC X(01) VALUE SPACES.\n')

            # Use PICIN if defined
            if fld.picin:
                pic_clause = fld.picin.strip("'")
                lines.append(
                    f'         05  {name}I     PIC {pic_clause}.\n')
            else:
                lines.append(
                    f'         05  {name}I     PIC X({length:02d})'
                    f' VALUE SPACES.\n')

        lines.append(f'      *\n')
        return lines

    def generate_c_map_init(self, bms_map):
        """Generate C code to register this map with the runtime."""
        lines = []
        mapset = bms_map.mapset
        map_name = bms_map.name
        named_fields = [f for f in bms_map.fields if f.name]

        lines.append(f'  /* Map: {map_name} in mapset {mapset} */\n')
        lines.append(f'  {{\n')
        lines.append(f'    static MapField fields_{map_name}[] = {{\n')

        for fld in named_fields:
            is_input = 1 if fld.is_input else 0
            initial = fld.initial.replace('"', '\\"')[:255]
            color = fld.color[:7] if fld.color else ''
            lines.append(
                f'      {{"{fld.name}", {fld.pos_row}, {fld.pos_col}, '
                f'{fld.length}, 0, "{color}", "{initial}", {is_input}}},\n')

        lines.append(f'    }};\n')
        lines.append(
            f'    cics_register_map("{mapset}", "{map_name}", '
            f'{bms_map.rows}, {bms_map.cols}, '
            f'fields_{map_name}, {len(named_fields)});\n')
        lines.append(f'  }}\n')

        return lines


def main():
    parser = argparse.ArgumentParser(
        description='BMS Map to COBOL Copybook converter')
    parser.add_argument('bms_dir', help='Directory containing .bms files')
    parser.add_argument('output_dir', help='Output directory for copybooks')
    parser.add_argument('--c-header', '-c', default=None,
                        help='Output C header with map registrations')

    args = parser.parse_args()

    os.makedirs(args.output_dir, exist_ok=True)

    bms_parser = BMSParser()
    cpy_gen = CopybookGenerator()
    all_c_init_lines = []

    bms_files = sorted(f for f in os.listdir(args.bms_dir)
                       if f.lower().endswith('.bms'))

    print(f"=== BMS Map Processor ===")
    print(f"Input:  {args.bms_dir}")
    print(f"Output: {args.output_dir}")
    print(f"")

    total_maps = 0
    total_fields = 0

    for bms_file in bms_files:
        filepath = os.path.join(args.bms_dir, bms_file)
        maps = bms_parser.parse_file(filepath)

        for bms_map in maps:
            # Generate COBOL copybook
            cpy_lines = cpy_gen.generate_copybook(bms_map)
            cpy_name = bms_map.mapset.upper()
            cpy_path = os.path.join(args.output_dir, f'{cpy_name}.cpy')

            # Append to existing file (mapsets can have multiple maps)
            mode = 'a' if os.path.exists(cpy_path) else 'w'
            with open(cpy_path, mode) as f:
                f.writelines(cpy_lines)

            # Generate C map registration
            c_lines = cpy_gen.generate_c_map_init(bms_map)
            all_c_init_lines.extend(c_lines)

            named_count = sum(1 for f in bms_map.fields if f.name)
            total_maps += 1
            total_fields += named_count
            print(f"  {bms_file} -> {cpy_name}.cpy "
                  f"(map={bms_map.name}, {named_count} fields)")

    # Generate C header for map initialization
    if args.c_header:
        with open(args.c_header, 'w') as f:
            f.write('/* Auto-generated map definitions for CICS simulator */\n')
            f.write('/* Do not edit - regenerate with bms2cpy.py */\n\n')
            f.write('#include "cics_runtime.h"\n\n')
            f.write('void cics_register_all_maps(void) {\n')
            f.writelines(all_c_init_lines)
            f.write('}\n')
        print(f"\n  C header: {args.c_header}")

    print(f"\n=== Results: {total_maps} maps, {total_fields} fields ===")


if __name__ == '__main__':
    main()
