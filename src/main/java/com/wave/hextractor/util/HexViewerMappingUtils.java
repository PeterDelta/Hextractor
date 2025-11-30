package com.wave.hextractor.util;

/**
 * Utility class for mapping between document positions and byte indices in hex viewer.
 */
public class HexViewerMappingUtils {

    /**
     * Map ASCII document position (including newlines) to byte index within current view.
     * @param docPos the document position
     * @param visibleColumns the number of visible columns
     * @param visibleRows the number of visible rows
     * @return the byte index
     */
    public static int mapAsciiDocPosToByteIndex(int docPos, int visibleColumns, int visibleRows) {
        int bytesPerRow = visibleColumns;
        int fullRows = visibleRows - 1; // rows with newline
        int rowLenWithNl = bytesPerRow + 1;
        int threshold = rowLenWithNl * fullRows; // start docPos of last row
        if (docPos < threshold) {
            int row = docPos / rowLenWithNl;
            int colInRow = docPos % rowLenWithNl;
            if (colInRow >= bytesPerRow) colInRow = bytesPerRow - 1; // newline maps to previous byte
            return row * bytesPerRow + colInRow;
        } else {
            int col = docPos - threshold;
            if (col >= bytesPerRow) col = bytesPerRow - 1;
            return fullRows * bytesPerRow + col;
        }
    }

    /**
     * Map byte index within current view to ASCII document position.
     * @param byteIndex the byte index
     * @param visibleColumns the number of visible columns
     * @param visibleRows the number of visible rows
     * @return the document position
     */
    public static int mapByteIndexToAsciiDocPos(int byteIndex, int visibleColumns, int visibleRows) {
        int bytesPerRow = visibleColumns;
        int fullRows = visibleRows - 1;
        int rowLenWithNl = bytesPerRow + 1;
        int row = byteIndex / bytesPerRow;
        int col = byteIndex % bytesPerRow;
        if (row < fullRows) {
            return row * rowLenWithNl + col;
        } else {
            return rowLenWithNl * fullRows + col;
        }
    }

    /**
     * Map byte index within current view to hex document position (3 chars per byte, newline per row except last).
     * @param byteIndex the byte index
     * @param visibleColumns the number of visible columns
     * @param visibleRows the number of visible rows
     * @return the document position
     */
    public static int mapByteIndexToHexDocPos(int byteIndex, int visibleColumns, int visibleRows) {
        int bytesPerRow = visibleColumns;
        int fullRows = visibleRows - 1; // rows with newline
        int row = byteIndex / bytesPerRow;
        int col = byteIndex % bytesPerRow;
        int rowLen = bytesPerRow * Constants.HEX_VALUE_SIZE; // 3 chars per byte
        if (row < fullRows) {
            // Full row with newline: each row occupies rowLen + 1 chars
            return row * (rowLen + 1) + col * Constants.HEX_VALUE_SIZE;
        } else {
            // Last row (no newline)
            return fullRows * (rowLen + 1) + col * Constants.HEX_VALUE_SIZE;
        }
    }

    /**
     * Map hex document position to byte index within current view.
     * @param docPos the document position
     * @param visibleColumns the number of visible columns
     * @param visibleRows the number of visible rows
     * @return the byte index
     */
    public static int mapHexDocPosToByteIndex(int docPos, int visibleColumns, int visibleRows) {
        int bytesPerRow = visibleColumns;
        int rowLen = bytesPerRow * Constants.HEX_VALUE_SIZE; // 3 chars per byte
        int fullRows = visibleRows - 1; // rows that have newline
        int perFullRow = rowLen + 1; // row content + newline
        int threshold = perFullRow * fullRows; // start position of last row
        int row;
        int colPos;
        if (docPos < threshold) {
            row = docPos / perFullRow;
            colPos = docPos % perFullRow;
            if (colPos >= rowLen) colPos = rowLen - 1; // newline maps to last byte cell
        } else {
            row = fullRows;
            colPos = docPos - threshold;
            if (colPos >= rowLen) colPos = rowLen - 1;
        }
        int byteInRow = colPos / Constants.HEX_VALUE_SIZE;
        return row * bytesPerRow + byteInRow;
    }
}