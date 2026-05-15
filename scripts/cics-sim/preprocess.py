#!/usr/bin/env python3
"""
CICS Preprocessor for GnuCOBOL

Converts EXEC CICS ... END-EXEC statements in COBOL source into
CALL statements that invoke the CICS simulator runtime library.

This eliminates the need for a commercial CICS emulator (UniKix or
Micro Focus) by transforming CICS API calls into standard COBOL
CALL statements that link against the C-based CICS simulator.

Usage:
    python3 preprocess.py <input.cbl> <output.cbl> [--copydir <dir>]

The preprocessor also:
  - Injects the EIB (Execute Interface Block) data definition
  - Replaces COPY DFHAID / DFHBMSCA with local versions
  - Handles DFHCOMMAREA linkage section
"""

import sys
import re
import os
import argparse


class CICSPreprocessor:
    """Transforms EXEC CICS statements into CALL statements."""

    def __init__(self, copydir=None):
        self.copydir = copydir or '.'
        self.eib_injected = False
        self.ws_injection_needed = True

    def preprocess(self, source_lines):
        """Process all lines, handling multi-line EXEC CICS blocks."""
        output = []
        i = 0
        in_exec_cics = False
        exec_cics_lines = []

        while i < len(source_lines):
            line = source_lines[i]

            # Check for start of EXEC CICS block
            if self._is_exec_cics_start(line):
                in_exec_cics = True
                exec_cics_lines = [line]
                # Check if END-EXEC is on same line
                if self._has_end_exec(line):
                    in_exec_cics = False
                    call_lines = self._transform_exec_cics(exec_cics_lines)
                    output.extend(call_lines)
                    exec_cics_lines = []
                i += 1
                continue

            if in_exec_cics:
                exec_cics_lines.append(line)
                if self._has_end_exec(line):
                    in_exec_cics = False
                    call_lines = self._transform_exec_cics(exec_cics_lines)
                    output.extend(call_lines)
                    exec_cics_lines = []
                i += 1
                continue

            # Inject EIB and working storage additions
            if self._is_working_storage(line) and self.ws_injection_needed:
                output.append(line)
                output.extend(self._get_eib_definition())
                self.ws_injection_needed = False
                i += 1
                continue

            # Replace DFHRESP(xxx) with numeric values
            line = self._replace_dfhresp(line)

            output.append(line)
            i += 1

        return output

    def _replace_dfhresp(self, line):
        """Replace DFHRESP(xxx) with numeric response codes."""
        dfhresp_map = {
            'NORMAL': '0',
            'ERROR': '1',
            'FILENOTFOUND': '12',
            'NOTFND': '13',
            'DUPREC': '14',
            'DUPKEY': '15',
            'INVREQ': '16',
            'NOSPACE': '18',
            'NOTOPEN': '19',
            'ENDFILE': '20',
            'LENGERR': '22',
            'PGMIDERR': '27',
            'ITEMERR': '26',
            'QIDERR': '44',
            'DISABLED': '84',
        }

        def replace_match(m):
            code = m.group(1).upper()
            return dfhresp_map.get(code, '0')

        return re.sub(r'DFHRESP\s*\(\s*(\w+)\s*\)', replace_match, line,
                      flags=re.IGNORECASE)

    def _is_exec_cics_start(self, line):
        """Check if line starts an EXEC CICS block."""
        stripped = line[6:72] if len(line) > 6 else line
        return bool(re.search(r'EXEC\s+CICS', stripped, re.IGNORECASE))

    def _has_end_exec(self, line):
        """Check if line contains END-EXEC."""
        stripped = line[6:72] if len(line) > 6 else line
        return bool(re.search(r'END-EXEC', stripped, re.IGNORECASE))

    def _is_working_storage(self, line):
        """Check if this is the WORKING-STORAGE SECTION line."""
        stripped = line[6:72] if len(line) > 6 else line
        return bool(re.search(r'WORKING-STORAGE\s+SECTION', stripped,
                              re.IGNORECASE))

    def _get_eib_definition(self):
        """Return EIB COBOL data definition lines."""
        return [
            '      *================================================================*\n',
            '      * CICS Simulator - Execute Interface Block (EIB)\n',
            '      *================================================================*\n',
            '       01  DFHEIBLK.\n',
            '         05  EIBAID                 PIC X(01) VALUE SPACES.\n',
            '         05  EIBCALEN              PIC S9(04) COMP VALUE 0.\n',
            '         05  EIBTRNID              PIC X(04) VALUE SPACES.\n',
            '         05  EIBRSRCE              PIC X(08) VALUE SPACES.\n',
            '         05  EIBRESP               PIC S9(09) COMP VALUE 0.\n',
            '         05  EIBRESP2              PIC S9(09) COMP VALUE 0.\n',
            '         05  EIBDATE               PIC X(08) VALUE SPACES.\n',
            '         05  EIBTIME               PIC X(08) VALUE SPACES.\n',
            '         05  EIBTASKN              PIC S9(09) COMP VALUE 0.\n',
            '       01  CICS-SIM-WORK-AREAS.\n',
            '         05  WS-CICS-RESP         PIC S9(09) COMP VALUE 0.\n',
            '         05  WS-CICS-RESP2        PIC S9(09) COMP VALUE 0.\n',
            '         05  WS-CICS-ERASE        PIC S9(09) COMP VALUE 0.\n',
            '         05  WS-CICS-CURSOR       PIC S9(09) COMP VALUE 0.\n',
            '         05  WS-CICS-FREEKB       PIC S9(09) COMP VALUE 0.\n',
            '         05  WS-CICS-COMMLEN      PIC S9(09) COMP VALUE 0.\n',
            '         05  WS-CICS-DATALEN      PIC S9(09) COMP VALUE 0.\n',
            '         05  WS-CICS-KEYLEN       PIC S9(09) COMP VALUE 0.\n',
            '         05  WS-CICS-MAPSET       PIC X(08) VALUE SPACES.\n',
            '         05  WS-CICS-MAP          PIC X(08) VALUE SPACES.\n',
            '         05  WS-CICS-FILENAME     PIC X(08) VALUE SPACES.\n',
            '         05  WS-CICS-PROGRAM      PIC X(08) VALUE SPACES.\n',
            '         05  WS-CICS-TRANSID      PIC X(04) VALUE SPACES.\n',
            '         05  WS-CICS-QUEUE        PIC X(08) VALUE SPACES.\n',
            '         05  WS-CICS-ABCODE       PIC X(04) VALUE SPACES.\n',
            '         05  WS-CICS-FIELD        PIC X(16) VALUE SPACES.\n',
            '         05  WS-CICS-ABSTIME      PIC X(16) VALUE SPACES.\n',
            '         05  WS-CICS-FMTOUT       PIC X(20) VALUE SPACES.\n',
            '         05  WS-CICS-FMTLEN       PIC S9(09) COMP VALUE 0.\n',
            '         05  WS-CICS-FORMAT       PIC X(16) VALUE SPACES.\n',
            '         05  WS-CICS-LABEL        PIC X(30) VALUE SPACES.\n',
            '         05  WS-CICS-ASSIGN-VAL   PIC X(08) VALUE SPACES.\n',
            '         05  WS-CICS-ASSIGN-LEN   PIC S9(09) COMP VALUE 0.\n',
            '\n',
        ]

    def _transform_exec_cics(self, lines):
        """Transform an EXEC CICS block into CALL statements."""
        # Check if original END-EXEC had a period (sentence terminator)
        has_period = False
        for line in lines:
            if re.search(r'END-EXEC\s*\.', line, re.IGNORECASE):
                has_period = True
                break

        # Join all lines and extract the CICS command
        full_text = ''
        for line in lines:
            # Extract columns 7-72 (COBOL area B)
            if len(line) > 6:
                content = line[6:min(72, len(line))]
                # Skip continuation lines (char in col 7)
                if len(line) > 6 and line[6] == '-':
                    content = content[1:].strip()
                full_text += ' ' + content
            else:
                full_text += ' ' + line

        # Clean up
        full_text = re.sub(r'EXEC\s+CICS\s+', '', full_text, flags=re.IGNORECASE)
        full_text = re.sub(r'END-EXEC\.?', '', full_text, flags=re.IGNORECASE)
        full_text = full_text.strip()

        # Parse the command
        result = self._generate_call(full_text)

        # Add period to last line if original had one
        if has_period and result:
            last = result[-1].rstrip('\n')
            if not last.rstrip().endswith('.'):
                result[-1] = last + '.\n'

        return result

    def _generate_call(self, cics_text):
        """Generate COBOL CALL statements from parsed CICS command."""
        # Determine the CICS verb
        parts = cics_text.split()
        if not parts:
            return [self._comment('Empty EXEC CICS block')]

        verb = parts[0].upper()

        # Parse parameters into dict
        params = self._parse_params(cics_text)

        if verb == 'SEND':
            if 'MAP' in params:
                return self._gen_send_map(params)
            elif 'TEXT' in params:
                return self._gen_send_text(params)
            else:
                return self._gen_send_map(params)
        elif verb == 'RECEIVE':
            return self._gen_receive_map(params)
        elif verb == 'RETURN':
            return self._gen_return(params)
        elif verb == 'XCTL':
            return self._gen_xctl(params)
        elif verb == 'READ':
            return self._gen_read(params)
        elif verb == 'WRITE':
            return self._gen_write(params)
        elif verb == 'REWRITE':
            return self._gen_rewrite(params)
        elif verb == 'DELETE':
            return self._gen_delete(params)
        elif verb == 'STARTBR':
            return self._gen_startbr(params)
        elif verb == 'READNEXT':
            return self._gen_readnext(params)
        elif verb == 'READPREV':
            return self._gen_readprev(params)
        elif verb == 'ENDBR':
            return self._gen_endbr(params)
        elif verb == 'ASSIGN':
            return self._gen_assign(params)
        elif verb == 'HANDLE':
            return self._gen_handle_abend(params)
        elif verb == 'ABEND':
            return self._gen_abend(params)
        elif verb == 'WRITEQ':
            return self._gen_writeq_td(params)
        elif verb == 'ASKTIME':
            return self._gen_asktime(params)
        elif verb == 'FORMATTIME':
            return self._gen_formattime(params)
        elif verb == 'INQUIRE':
            return self._gen_inquire(params)
        else:
            return [self._comment(f'Unsupported CICS verb: {verb}')]

    def _parse_params(self, text):
        """Parse CICS parameters into a dictionary."""
        params = {}
        # Remove the verb
        text = re.sub(r'^\w+\s*', '', text)

        # Match parameters: NAME(value) or just NAME
        pattern = r'(\w+)\s*\(([^)]*)\)|(\w+)'
        for m in re.finditer(pattern, text):
            if m.group(1):
                params[m.group(1).upper()] = m.group(2).strip()
            elif m.group(3):
                word = m.group(3).upper()
                if word not in ('MAP', 'TEXT', 'TD'):
                    params[word] = True

        # Handle "SEND TEXT" vs "SEND MAP" vs "WRITEQ TD"
        if re.search(r'\bTEXT\b', text, re.IGNORECASE) and 'TEXT' not in params:
            # SEND TEXT FROM(x) case
            params['TEXT'] = True
        if re.search(r'\bTD\b', text, re.IGNORECASE):
            params['TD'] = True

        return params

    def _gen_send_map(self, params):
        """Generate CALL for SEND MAP."""
        lines = []
        mapset = params.get('MAPSET', "' '")
        map_name = params.get('MAP', "' '")
        from_area = params.get('FROM', 'SPACES')
        erase = '1' if 'ERASE' in params else '0'

        lines.append(self._cobol(
            f"MOVE {self._lit(mapset)} TO WS-CICS-MAPSET"))
        lines.append(self._cobol(
            f"MOVE {self._lit(map_name)} TO WS-CICS-MAP"))
        lines.append(self._cobol(
            f"MOVE {erase} TO WS-CICS-ERASE"))
        lines.append(self._cobol(
            f"MOVE -1 TO WS-CICS-CURSOR"))

        if 'CURSOR' in params and params['CURSOR'] is not True:
            lines.append(self._cobol(
                f"MOVE {params['CURSOR']} TO WS-CICS-CURSOR"))

        from_ref = self._deref(from_area)
        length_expr = f"LENGTH OF {from_ref}"
        lines.append(self._cobol(
            f"MOVE {length_expr} TO WS-CICS-DATALEN"))

        lines.append(self._cobol(
            f"CALL 'CICSSNDM' USING"))
        lines.append(self._cobol(
            f"    WS-CICS-MAPSET"))
        lines.append(self._cobol(
            f"    WS-CICS-MAP"))
        lines.append(self._cobol(
            f"    {from_ref}"))
        lines.append(self._cobol(
            f"    WS-CICS-DATALEN"))
        lines.append(self._cobol(
            f"    WS-CICS-RESP"))
        lines.append(self._cobol(
            f"    WS-CICS-RESP2"))
        lines.append(self._cobol(
            f"    WS-CICS-ERASE"))
        lines.append(self._cobol(
            f"    WS-CICS-CURSOR"))

        if 'RESP' in params and params['RESP'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP TO {params['RESP']}"))
        if 'RESP2' in params and params['RESP2'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP2 TO {params['RESP2']}"))

        return lines

    def _gen_send_text(self, params):
        """Generate CALL for SEND TEXT."""
        lines = []
        from_area = params.get('FROM', 'SPACES')
        length = params.get('LENGTH', '80')
        erase = '1' if 'ERASE' in params else '0'
        freekb = '1' if 'FREEKB' in params else '0'

        from_ref = self._deref(from_area)
        lines.append(self._cobol(
            f"MOVE {self._eval_length(length, from_ref)} TO WS-CICS-DATALEN"))
        lines.append(self._cobol(f"MOVE {erase} TO WS-CICS-ERASE"))
        lines.append(self._cobol(f"MOVE {freekb} TO WS-CICS-FREEKB"))
        lines.append(self._cobol(
            f"CALL 'CICSSNTX' USING"))
        lines.append(self._cobol(f"    {from_ref}"))
        lines.append(self._cobol(f"    WS-CICS-DATALEN"))
        lines.append(self._cobol(f"    WS-CICS-ERASE"))
        lines.append(self._cobol(f"    WS-CICS-FREEKB"))
        return lines

    def _gen_receive_map(self, params):
        """Generate CALL for RECEIVE MAP."""
        lines = []
        mapset = params.get('MAPSET', "' '")
        map_name = params.get('MAP', "' '")
        into_area = params.get('INTO', '')

        lines.append(self._cobol(
            f"MOVE {self._lit(mapset)} TO WS-CICS-MAPSET"))
        lines.append(self._cobol(
            f"MOVE {self._lit(map_name)} TO WS-CICS-MAP"))

        # If no INTO specified, use the BMS input area (map name + "I")
        if into_area:
            into_ref = self._deref(into_area)
        else:
            # Derive input area name from map name (e.g., COSGN0A -> COSGN0AI)
            raw_map = self._deref(map_name).strip("'")
            into_ref = f"{raw_map}I"

        lines.append(self._cobol(
            f"MOVE LENGTH OF {into_ref} TO WS-CICS-DATALEN"))
        lines.append(self._cobol(
            f"CALL 'CICSRCVM' USING"))
        lines.append(self._cobol(f"    WS-CICS-MAPSET"))
        lines.append(self._cobol(f"    WS-CICS-MAP"))
        lines.append(self._cobol(f"    {into_ref}"))
        lines.append(self._cobol(f"    WS-CICS-DATALEN"))
        lines.append(self._cobol(f"    WS-CICS-RESP"))
        lines.append(self._cobol(f"    WS-CICS-RESP2"))

        if 'RESP' in params and params['RESP'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP TO {params['RESP']}"))
        if 'RESP2' in params and params['RESP2'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP2 TO {params['RESP2']}"))

        return lines

    def _gen_return(self, params):
        """Generate CALL for RETURN."""
        lines = []
        transid = params.get('TRANSID', '')
        commarea = params.get('COMMAREA', '')
        length = params.get('LENGTH', '0')

        if transid:
            lines.append(self._cobol(
                f"MOVE {self._lit(transid)} TO WS-CICS-TRANSID"))
        else:
            lines.append(self._cobol(
                f"MOVE SPACES TO WS-CICS-TRANSID"))

        if commarea:
            comm_ref = self._deref(commarea)
            lines.append(self._cobol(
                f"MOVE {self._eval_length(length, comm_ref)} "
                f"TO WS-CICS-COMMLEN"))
            lines.append(self._cobol(
                f"CALL 'CICSRETN' USING"))
            lines.append(self._cobol(f"    WS-CICS-TRANSID"))
            lines.append(self._cobol(f"    {comm_ref}"))
            lines.append(self._cobol(f"    WS-CICS-COMMLEN"))
        else:
            lines.append(self._cobol(
                f"MOVE 0 TO WS-CICS-COMMLEN"))
            lines.append(self._cobol(
                f"CALL 'CICSRETN' USING"))
            lines.append(self._cobol(f"    WS-CICS-TRANSID"))
            lines.append(self._cobol(f"    WS-CICS-TRANSID"))
            lines.append(self._cobol(f"    WS-CICS-COMMLEN"))

        # Only generate STOP RUN for bare RETURN (no TRANSID)
        # RETURN with TRANSID is pseudo-conversational - paragraphs
        # are still reachable via PERFORM
        if not transid:
            lines.append(self._cobol(f"STOP RUN"))

        return lines

    def _gen_xctl(self, params):
        """Generate CALL for XCTL."""
        lines = []
        program = params.get('PROGRAM', "' '")
        commarea = params.get('COMMAREA', '')
        length = params.get('LENGTH', '0')

        lines.append(self._cobol(
            f"MOVE {self._lit(program)} TO WS-CICS-PROGRAM"))

        if commarea:
            comm_ref = self._deref(commarea)
            lines.append(self._cobol(
                f"MOVE {self._eval_length(length, comm_ref)} "
                f"TO WS-CICS-COMMLEN"))
            lines.append(self._cobol(
                f"CALL 'CICSXCTL_' USING"))
            lines.append(self._cobol(f"    WS-CICS-PROGRAM"))
            lines.append(self._cobol(f"    {comm_ref}"))
            lines.append(self._cobol(f"    WS-CICS-COMMLEN"))
            lines.append(self._cobol(f"    WS-CICS-RESP"))
            lines.append(self._cobol(f"    WS-CICS-RESP2"))
        else:
            lines.append(self._cobol(
                f"MOVE 0 TO WS-CICS-COMMLEN"))
            lines.append(self._cobol(
                f"CALL 'CICSXCTL_' USING"))
            lines.append(self._cobol(f"    WS-CICS-PROGRAM"))
            lines.append(self._cobol(f"    WS-CICS-PROGRAM"))
            lines.append(self._cobol(f"    WS-CICS-COMMLEN"))
            lines.append(self._cobol(f"    WS-CICS-RESP"))
            lines.append(self._cobol(f"    WS-CICS-RESP2"))

        # XCTL transfers control - but paragraphs after it may be
        # reachable via PERFORM, so don't STOP RUN
        return lines

    def _gen_read(self, params):
        """Generate CALL for READ."""
        lines = []
        filename = params.get('DATASET', params.get('FILE', "' '"))
        into = params.get('INTO', 'SPACES')
        ridfld = params.get('RIDFLD', 'SPACES')
        keylength = params.get('KEYLENGTH', '0')

        lines.append(self._cobol(
            f"MOVE {self._lit(filename)} TO WS-CICS-FILENAME"))

        into_ref = self._deref(into)
        ridfld_ref = self._deref(ridfld)

        lines.append(self._cobol(
            f"MOVE LENGTH OF {into_ref} TO WS-CICS-DATALEN"))
        lines.append(self._cobol(
            f"MOVE {self._eval_length(keylength, ridfld_ref)} "
            f"TO WS-CICS-KEYLEN"))
        lines.append(self._cobol(
            f"CALL 'CICSREAD_' USING"))
        lines.append(self._cobol(f"    WS-CICS-FILENAME"))
        lines.append(self._cobol(f"    {into_ref}"))
        lines.append(self._cobol(f"    WS-CICS-DATALEN"))
        lines.append(self._cobol(f"    {ridfld_ref}"))
        lines.append(self._cobol(f"    WS-CICS-KEYLEN"))
        lines.append(self._cobol(f"    WS-CICS-RESP"))
        lines.append(self._cobol(f"    WS-CICS-RESP2"))

        if 'RESP' in params and params['RESP'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP TO {params['RESP']}"))
        if 'RESP2' in params and params['RESP2'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP2 TO {params['RESP2']}"))

        return lines

    def _gen_write(self, params):
        """Generate CALL for WRITE."""
        lines = []
        filename = params.get('DATASET', params.get('FILE', "' '"))
        from_area = params.get('FROM', 'SPACES')
        ridfld = params.get('RIDFLD', 'SPACES')
        keylength = params.get('KEYLENGTH', '0')

        lines.append(self._cobol(
            f"MOVE {self._lit(filename)} TO WS-CICS-FILENAME"))

        from_ref = self._deref(from_area)
        ridfld_ref = self._deref(ridfld)

        lines.append(self._cobol(
            f"MOVE LENGTH OF {from_ref} TO WS-CICS-DATALEN"))
        lines.append(self._cobol(
            f"MOVE {self._eval_length(keylength, ridfld_ref)} "
            f"TO WS-CICS-KEYLEN"))
        lines.append(self._cobol(
            f"CALL 'CICSWRIT' USING"))
        lines.append(self._cobol(f"    WS-CICS-FILENAME"))
        lines.append(self._cobol(f"    {from_ref}"))
        lines.append(self._cobol(f"    WS-CICS-DATALEN"))
        lines.append(self._cobol(f"    {ridfld_ref}"))
        lines.append(self._cobol(f"    WS-CICS-KEYLEN"))
        lines.append(self._cobol(f"    WS-CICS-RESP"))
        lines.append(self._cobol(f"    WS-CICS-RESP2"))

        if 'RESP' in params and params['RESP'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP TO {params['RESP']}"))
        if 'RESP2' in params and params['RESP2'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP2 TO {params['RESP2']}"))

        return lines

    def _gen_rewrite(self, params):
        """Generate CALL for REWRITE."""
        lines = []
        filename = params.get('DATASET', params.get('FILE', "' '"))
        from_area = params.get('FROM', 'SPACES')

        lines.append(self._cobol(
            f"MOVE {self._lit(filename)} TO WS-CICS-FILENAME"))

        from_ref = self._deref(from_area)
        lines.append(self._cobol(
            f"MOVE LENGTH OF {from_ref} TO WS-CICS-DATALEN"))
        lines.append(self._cobol(
            f"CALL 'CICSRWRT' USING"))
        lines.append(self._cobol(f"    WS-CICS-FILENAME"))
        lines.append(self._cobol(f"    {from_ref}"))
        lines.append(self._cobol(f"    WS-CICS-DATALEN"))
        lines.append(self._cobol(f"    WS-CICS-RESP"))
        lines.append(self._cobol(f"    WS-CICS-RESP2"))

        if 'RESP' in params and params['RESP'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP TO {params['RESP']}"))
        if 'RESP2' in params and params['RESP2'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP2 TO {params['RESP2']}"))

        return lines

    def _gen_delete(self, params):
        """Generate CALL for DELETE."""
        lines = []
        filename = params.get('DATASET', params.get('FILE', "' '"))
        ridfld = params.get('RIDFLD', 'SPACES')
        keylength = params.get('KEYLENGTH', '0')

        lines.append(self._cobol(
            f"MOVE {self._lit(filename)} TO WS-CICS-FILENAME"))

        ridfld_ref = self._deref(ridfld)
        lines.append(self._cobol(
            f"MOVE {self._eval_length(keylength, ridfld_ref)} "
            f"TO WS-CICS-KEYLEN"))
        lines.append(self._cobol(
            f"CALL 'CICSDELT' USING"))
        lines.append(self._cobol(f"    WS-CICS-FILENAME"))
        lines.append(self._cobol(f"    {ridfld_ref}"))
        lines.append(self._cobol(f"    WS-CICS-KEYLEN"))
        lines.append(self._cobol(f"    WS-CICS-RESP"))
        lines.append(self._cobol(f"    WS-CICS-RESP2"))

        if 'RESP' in params and params['RESP'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP TO {params['RESP']}"))
        if 'RESP2' in params and params['RESP2'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP2 TO {params['RESP2']}"))

        return lines

    def _gen_startbr(self, params):
        """Generate CALL for STARTBR."""
        lines = []
        filename = params.get('DATASET', params.get('FILE', "' '"))
        ridfld = params.get('RIDFLD', 'SPACES')
        keylength = params.get('KEYLENGTH', '0')

        lines.append(self._cobol(
            f"MOVE {self._lit(filename)} TO WS-CICS-FILENAME"))

        ridfld_ref = self._deref(ridfld)
        lines.append(self._cobol(
            f"MOVE {self._eval_length(keylength, ridfld_ref)} "
            f"TO WS-CICS-KEYLEN"))
        lines.append(self._cobol(
            f"CALL 'CICSSTBR' USING"))
        lines.append(self._cobol(f"    WS-CICS-FILENAME"))
        lines.append(self._cobol(f"    {ridfld_ref}"))
        lines.append(self._cobol(f"    WS-CICS-KEYLEN"))
        lines.append(self._cobol(f"    WS-CICS-RESP"))
        lines.append(self._cobol(f"    WS-CICS-RESP2"))

        if 'RESP' in params and params['RESP'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP TO {params['RESP']}"))
        if 'RESP2' in params and params['RESP2'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP2 TO {params['RESP2']}"))

        return lines

    def _gen_readnext(self, params):
        """Generate CALL for READNEXT."""
        lines = []
        filename = params.get('DATASET', params.get('FILE', "' '"))
        into = params.get('INTO', 'SPACES')
        ridfld = params.get('RIDFLD', 'SPACES')
        keylength = params.get('KEYLENGTH', '0')

        lines.append(self._cobol(
            f"MOVE {self._lit(filename)} TO WS-CICS-FILENAME"))

        into_ref = self._deref(into)
        ridfld_ref = self._deref(ridfld)

        lines.append(self._cobol(
            f"MOVE LENGTH OF {into_ref} TO WS-CICS-DATALEN"))
        lines.append(self._cobol(
            f"MOVE {self._eval_length(keylength, ridfld_ref)} "
            f"TO WS-CICS-KEYLEN"))
        lines.append(self._cobol(
            f"CALL 'CICSRDNX' USING"))
        lines.append(self._cobol(f"    WS-CICS-FILENAME"))
        lines.append(self._cobol(f"    {into_ref}"))
        lines.append(self._cobol(f"    WS-CICS-DATALEN"))
        lines.append(self._cobol(f"    {ridfld_ref}"))
        lines.append(self._cobol(f"    WS-CICS-KEYLEN"))
        lines.append(self._cobol(f"    WS-CICS-RESP"))
        lines.append(self._cobol(f"    WS-CICS-RESP2"))

        if 'RESP' in params and params['RESP'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP TO {params['RESP']}"))
        if 'RESP2' in params and params['RESP2'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP2 TO {params['RESP2']}"))

        return lines

    def _gen_readprev(self, params):
        """Generate CALL for READPREV."""
        lines = []
        filename = params.get('DATASET', params.get('FILE', "' '"))
        into = params.get('INTO', 'SPACES')
        ridfld = params.get('RIDFLD', 'SPACES')
        keylength = params.get('KEYLENGTH', '0')

        lines.append(self._cobol(
            f"MOVE {self._lit(filename)} TO WS-CICS-FILENAME"))

        into_ref = self._deref(into)
        ridfld_ref = self._deref(ridfld)

        lines.append(self._cobol(
            f"MOVE LENGTH OF {into_ref} TO WS-CICS-DATALEN"))
        lines.append(self._cobol(
            f"MOVE {self._eval_length(keylength, ridfld_ref)} "
            f"TO WS-CICS-KEYLEN"))
        lines.append(self._cobol(
            f"CALL 'CICSRDPV' USING"))
        lines.append(self._cobol(f"    WS-CICS-FILENAME"))
        lines.append(self._cobol(f"    {into_ref}"))
        lines.append(self._cobol(f"    WS-CICS-DATALEN"))
        lines.append(self._cobol(f"    {ridfld_ref}"))
        lines.append(self._cobol(f"    WS-CICS-KEYLEN"))
        lines.append(self._cobol(f"    WS-CICS-RESP"))
        lines.append(self._cobol(f"    WS-CICS-RESP2"))

        if 'RESP' in params and params['RESP'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP TO {params['RESP']}"))
        if 'RESP2' in params and params['RESP2'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP2 TO {params['RESP2']}"))

        return lines

    def _gen_endbr(self, params):
        """Generate CALL for ENDBR."""
        lines = []
        filename = params.get('DATASET', params.get('FILE', "' '"))

        lines.append(self._cobol(
            f"MOVE {self._lit(filename)} TO WS-CICS-FILENAME"))
        lines.append(self._cobol(
            f"CALL 'CICSENDB' USING"))
        lines.append(self._cobol(f"    WS-CICS-FILENAME"))
        lines.append(self._cobol(f"    WS-CICS-RESP"))
        lines.append(self._cobol(f"    WS-CICS-RESP2"))

        if 'RESP' in params and params['RESP'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP TO {params['RESP']}"))
        if 'RESP2' in params and params['RESP2'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP2 TO {params['RESP2']}"))

        return lines

    def _gen_assign(self, params):
        """Generate CALL for ASSIGN."""
        lines = []

        for field_name, target in params.items():
            if field_name in ('RESP', 'RESP2'):
                continue
            if target is True:
                continue
            target_ref = self._deref(target)
            lines.append(self._cobol(
                f"MOVE '{field_name}' TO WS-CICS-FIELD"))
            lines.append(self._cobol(
                f"MOVE LENGTH OF {target_ref} TO WS-CICS-ASSIGN-LEN"))
            lines.append(self._cobol(
                f"CALL 'CICSASGN' USING"))
            lines.append(self._cobol(f"    WS-CICS-FIELD"))
            lines.append(self._cobol(f"    {target_ref}"))
            lines.append(self._cobol(f"    WS-CICS-ASSIGN-LEN"))

        return lines

    def _gen_handle_abend(self, params):
        """Generate CALL for HANDLE ABEND."""
        lines = []
        label = params.get('LABEL', params.get('ABEND', 'ABEND-RTN'))
        if label is True:
            label = 'ABEND-RTN'

        lines.append(self._cobol(
            f"MOVE '{label}' TO WS-CICS-LABEL"))
        lines.append(self._cobol(
            f"CALL 'CICSHABA' USING WS-CICS-LABEL"))
        return lines

    def _gen_abend(self, params):
        """Generate CALL for ABEND."""
        lines = []
        abcode = params.get('ABCODE', '????')
        if abcode is True:
            abcode = '????'

        lines.append(self._cobol(
            f"MOVE {self._lit(abcode)} TO WS-CICS-ABCODE"))
        lines.append(self._cobol(
            f"CALL 'CICSABND' USING WS-CICS-ABCODE"))
        return lines

    def _gen_writeq_td(self, params):
        """Generate CALL for WRITEQ TD."""
        lines = []
        queue = params.get('QUEUE', "' '")
        from_area = params.get('FROM', 'SPACES')
        length = params.get('LENGTH', '0')

        lines.append(self._cobol(
            f"MOVE {self._lit(queue)} TO WS-CICS-QUEUE"))

        from_ref = self._deref(from_area)
        lines.append(self._cobol(
            f"MOVE {self._eval_length(length, from_ref)} "
            f"TO WS-CICS-DATALEN"))
        lines.append(self._cobol(
            f"CALL 'CICSWQTD' USING"))
        lines.append(self._cobol(f"    WS-CICS-QUEUE"))
        lines.append(self._cobol(f"    {from_ref}"))
        lines.append(self._cobol(f"    WS-CICS-DATALEN"))
        lines.append(self._cobol(f"    WS-CICS-RESP"))
        lines.append(self._cobol(f"    WS-CICS-RESP2"))

        if 'RESP' in params and params['RESP'] is not True:
            lines.append(self._cobol(
                f"MOVE WS-CICS-RESP TO {params['RESP']}"))
        return lines

    def _gen_asktime(self, params):
        """Generate CALL for ASKTIME."""
        lines = []
        abstime = params.get('ABSTIME', 'WS-CICS-ABSTIME')
        if abstime is True:
            abstime = 'WS-CICS-ABSTIME'
        lines.append(self._cobol(
            f"CALL 'CICSASKT' USING {self._deref(abstime)}"))
        return lines

    def _gen_formattime(self, params):
        """Generate CALL for FORMATTIME."""
        lines = []
        abstime = params.get('ABSTIME', 'WS-CICS-ABSTIME')
        if abstime is True:
            abstime = 'WS-CICS-ABSTIME'

        # Determine output fields
        out_field = 'WS-CICS-FMTOUT'
        fmt = 'YYYYMMDD'

        for key in ('YYYYMMDD', 'MMDDYY', 'DDMMYY', 'DATESEP', 'TIME',
                    'TIMESEP', 'YYDDD', 'DATEFORM'):
            if key in params:
                val = params[key]
                if val is not True:
                    out_field = self._deref(val)
                fmt = key
                break

        lines.append(self._cobol(
            f"MOVE '{fmt}' TO WS-CICS-FORMAT"))
        lines.append(self._cobol(
            f"MOVE LENGTH OF {out_field} TO WS-CICS-FMTLEN"))
        lines.append(self._cobol(
            f"CALL 'CICSFMTT' USING"))
        lines.append(self._cobol(f"    {self._deref(abstime)}"))
        lines.append(self._cobol(f"    {out_field}"))
        lines.append(self._cobol(f"    WS-CICS-FMTLEN"))
        lines.append(self._cobol(f"    WS-CICS-FORMAT"))
        return lines

    def _gen_inquire(self, params):
        """Generate CALL for INQUIRE (stub)."""
        lines = []
        lines.append(self._comment('INQUIRE - stub (no-op in simulator)'))
        lines.append(self._cobol(
            f"MOVE 0 TO WS-CICS-RESP"))
        return lines

    # --- Helper methods ---

    def _cobol(self, text):
        """Format a line in COBOL Area B (columns 12-72)."""
        # Ensure we don't exceed column 72
        prefix = '           '  # 11 spaces to start at column 12
        line = prefix + text
        if len(line) > 72:
            line = line[:72]
        return line + '\n'

    def _comment(self, text):
        """Generate a COBOL comment line."""
        return f'      * {text}\n'

    def _lit(self, value):
        """Handle literal vs variable reference."""
        if not value or value is True:
            return 'SPACES'
        value = str(value).strip()
        # Already a quoted literal
        if value.startswith("'") and value.endswith("'"):
            return value
        # A COBOL variable reference
        if value.startswith('LIT-') or value.startswith('WS-'):
            return value
        # Looks like a COBOL identifier
        if re.match(r'^[A-Z][A-Z0-9\-]*$', value, re.IGNORECASE):
            return value
        # Must be a literal
        return f"'{value}'"

    def _deref(self, value):
        """Dereference a parameter value to a COBOL data reference."""
        if not value or value is True:
            return 'SPACES'
        value = str(value).strip()
        # Remove surrounding quotes if it's a variable name in quotes
        if value.startswith("'") and value.endswith("'"):
            inner = value[1:-1]
            # If it looks like a COBOL identifier, use it directly
            if re.match(r'^[A-Z][A-Z0-9\-]*$', inner, re.IGNORECASE):
                return inner
            return value
        return value

    def _eval_length(self, length_expr, ref_name):
        """Evaluate a LENGTH expression."""
        if not length_expr or length_expr == '0':
            return f'LENGTH OF {ref_name}'
        expr = str(length_expr).strip()
        # "LENGTH OF X" style
        if expr.upper().startswith('LENGTH'):
            return expr
        # Numeric
        if expr.isdigit():
            return expr
        # COBOL variable
        return expr


def main():
    parser = argparse.ArgumentParser(
        description='CICS Preprocessor for GnuCOBOL')
    parser.add_argument('input', help='Input COBOL source file')
    parser.add_argument('output', help='Output preprocessed file')
    parser.add_argument('--copydir', '-I', default='.',
                        help='Copybook directory')

    args = parser.parse_args()

    with open(args.input, 'r') as f:
        lines = f.readlines()

    pp = CICSPreprocessor(copydir=args.copydir)
    output_lines = pp.preprocess(lines)

    with open(args.output, 'w') as f:
        f.writelines(output_lines)

    print(f"Preprocessed: {args.input} -> {args.output}")


if __name__ == '__main__':
    main()
