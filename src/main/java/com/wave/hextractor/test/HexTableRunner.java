package com.wave.hextractor.test;

import com.wave.hextractor.object.HexTable;
import com.wave.hextractor.util.Utils;

import java.io.File;

public class HexTableRunner {
    public static void main(String[] args) throws Exception {
        File cwd = new File(System.getProperty("user.dir"));
        File tbl = new File(cwd, "Last Battle (USA, Europe, Korea)_1.tbl");
        if (!tbl.exists()) {
            System.out.println("Table file not found: " + tbl.getAbsolutePath());
            return;
        }
        HexTable ht = new HexTable(tbl.getAbsolutePath());
        System.out.println("Loaded table size: " + (ht.toAsciiTable() != null ? ht.toAsciiTable().length() : 0));

        byte[] data1 = new byte[] {(byte)0xFF, (byte)0xFF};
        System.out.println("Data FF FF -> toAscii: '" + ht.toAscii(data1, true) + "'");

        byte[] data2 = new byte[] {(byte)0xA5, (byte)0xC2};
        System.out.println("Data A5 C2 -> toAscii: '" + ht.toAscii(data2, true) + "'");

        byte[] sample = new byte[] {0x20, 0x41, (byte)0xA5, (byte)0xC2, (byte)0xFF, (byte)0xFF};
        System.out.println("Sample -> toAscii: '" + ht.toAscii(sample, true) + "'");
    }
}
