package com.wave.hextractor.gui;

import com.wave.hextractor.util.HexViewerMappingUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for HexViewer private mapping methods (ASCII/HEX position conversions).
 */
public class HexViewerMappingTest {

    @Test
    public void testAsciiDocPosToByteIndexBoundaries() {
        int visibleColumns = 16; // default bytes per row
        int visibleRows = 16;    // fixed
        int rowLenWithNl = visibleColumns + 1;
        int fullRows = visibleRows - 1;
        int threshold = rowLenWithNl * fullRows;

        // First char
        int b0 = HexViewerMappingUtils.mapAsciiDocPosToByteIndex(0, visibleColumns, visibleRows);
        assertEquals(0, b0);

        // Last char of first row (before newline)
        int b1 = HexViewerMappingUtils.mapAsciiDocPosToByteIndex(visibleColumns - 1, visibleColumns, visibleRows);
        assertEquals(visibleColumns - 1, b1);

        // Newline position of first row (maps to previous byte)
        int bNl = HexViewerMappingUtils.mapAsciiDocPosToByteIndex(visibleColumns, visibleColumns, visibleRows);
        assertEquals(visibleColumns - 1, bNl);

        // First char second row
        int docSecondRow = rowLenWithNl; // index after newline
        int b2 = HexViewerMappingUtils.mapAsciiDocPosToByteIndex(docSecondRow, visibleColumns, visibleRows);
        assertEquals(visibleColumns, b2);

        // Last newline before last row
        int lastFullRowNewlineDocPos = rowLenWithNl * fullRows - 1; // last char of last full row
        int bBeforeLastRow = HexViewerMappingUtils.mapAsciiDocPosToByteIndex(lastFullRowNewlineDocPos, visibleColumns, visibleRows);
        assertEquals(visibleColumns * fullRows - 1, bBeforeLastRow);

        // Threshold start of last row
        int bStartLastRow = HexViewerMappingUtils.mapAsciiDocPosToByteIndex(threshold, visibleColumns, visibleRows);
        assertEquals(visibleColumns * fullRows, bStartLastRow);

        // Last char overall
        int lastDocPos = threshold + visibleColumns - 1;
        int bLast = HexViewerMappingUtils.mapAsciiDocPosToByteIndex(lastDocPos, visibleColumns, visibleRows);
        assertEquals(visibleColumns * fullRows + visibleColumns - 1, bLast);
    }

    @Test
    public void testByteIndexToAsciiDocPosRoundTrip() {
        int visibleColumns = 16;
        int visibleRows = 16;
        int totalBytes = visibleColumns * visibleRows;
        for (int byteIndex = 0; byteIndex < totalBytes; byteIndex += 11) { // sample points
            int docPos = HexViewerMappingUtils.mapByteIndexToAsciiDocPos(byteIndex, visibleColumns, visibleRows);
            int back = HexViewerMappingUtils.mapAsciiDocPosToByteIndex(docPos, visibleColumns, visibleRows);
            assertEquals(byteIndex, back, "Round trip failed for byteIndex=" + byteIndex);
        }
    }

    @Test
    public void testByteIndexToHexDocPosPattern() {
        // Adjacent bytes in same row differ by 3 chars
        int pos0 = HexViewerMappingUtils.mapByteIndexToHexDocPos(0, 16, 16);
        int pos1 = HexViewerMappingUtils.mapByteIndexToHexDocPos(1, 16, 16);
        assertEquals(3, pos1 - pos0);

        // First byte of second row should be rowLen + 1 (newline) more than first of first row
        int visibleColumns = 16;
        int rowLen = visibleColumns * 3;
        int posRow0 = HexViewerMappingUtils.mapByteIndexToHexDocPos(0, 16, 16);
        int posRow1 = HexViewerMappingUtils.mapByteIndexToHexDocPos(visibleColumns, 16, 16);
        assertEquals(rowLen + 1, posRow1 - posRow0); // +1 for newline
    }
}
