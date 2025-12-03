package com.wave.hextractor.pojo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TableMapping class.
 */
public class TableMappingTest {
	
	private TableMapping mapping1;
	private TableMapping mapping2;
	
	@BeforeEach
	public void setUp() {
		mapping1 = new TableMapping("1", "rom_1.ext", "rom_1.tbl", "First extraction");
		mapping2 = new TableMapping("2", "rom_2.ext", "rom_2.tbl", "Second extraction");
	}
	
	@Test
	public void testTableMappingCreation() {
		assertEquals("1", mapping1.getSuffix());
		assertEquals("rom_1.ext", mapping1.getExtFileName());
		assertEquals("rom_1.tbl", mapping1.getTableFileName());
		assertEquals("First extraction", mapping1.getDescription());
		assertFalse(mapping1.isMainTable());
	}
	
	@Test
	public void testMainTableMapping() {
		TableMapping mainMapping = new TableMapping();
		assertTrue(mainMapping.isMainTable());
		assertEquals("", mainMapping.getSuffix());
	}
	
	@Test
	public void testTableMappingComparison() {
		TableMapping main = new TableMapping("", "rom.tbl", "rom.tbl");
		assertTrue(main.compareTo(mapping1) < 0, "Main table should come before numbered tables");
		assertTrue(mapping1.compareTo(mapping2) < 0, "Suffix 1 should come before suffix 2");
	}
	
	@Test
	public void testTableMappingEquality() {
		TableMapping copy = new TableMapping("1", "rom_1.ext", "rom_1.tbl", "Different description");
		assertEquals(mapping1, copy);
	}
	
	@Test
	public void testNullHandling() {
		TableMapping nullMapping = new TableMapping(null, null, null, null);
		assertEquals("", nullMapping.getSuffix());
		assertEquals("", nullMapping.getExtFileName());
		assertEquals("", nullMapping.getTableFileName());
		assertEquals("", nullMapping.getDescription());
	}
	
	@Test
	public void testSerialization() {
		// Test that it can be serialized
		assertNotNull(mapping1);
		assertTrue(mapping1 instanceof java.io.Serializable);
	}
}
