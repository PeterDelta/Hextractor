package com.wave.hextractor.util;

import com.wave.hextractor.pojo.TableMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TableMappingManager class.
 */
public class TableMappingManagerTest {
	
	private TableMappingManager manager;
	
	@BeforeEach
	public void setUp() {
		manager = new TableMappingManager();
	}
	
	@Test
	public void testAddMapping() {
		manager.addMapping("1", "rom_1.ext", "rom_1.tbl");
		assertEquals(1, manager.size());
		assertNotNull(manager.getMapping("1"));
	}
	
	@Test
	public void testRemoveMapping() {
		manager.addMapping("1", "rom_1.ext", "rom_1.tbl");
		manager.addMapping("2", "rom_2.ext", "rom_2.tbl");
		assertEquals(2, manager.size());
		
		boolean removed = manager.removeMapping("1");
		assertTrue(removed);
		assertEquals(1, manager.size());
		assertNull(manager.getMapping("1"));
	}
	
	@Test
	public void testGetMainMapping() {
		manager.addMapping("", "rom.tbl", "rom.tbl");
		manager.addMapping("1", "rom_1.ext", "rom_1.tbl");
		
		TableMapping main = manager.getMainMapping();
		assertNotNull(main);
		assertTrue(main.isMainTable());
	}
	
	@Test
	public void testGetAdditionalMappings() {
		manager.addMapping("", "rom.tbl", "rom.tbl");
		manager.addMapping("1", "rom_1.ext", "rom_1.tbl");
		manager.addMapping("2", "rom_2.ext", "rom_2.tbl");
		
		var additional = manager.getAdditionalMappings();
		assertEquals(2, additional.size());
		assertTrue(additional.stream().allMatch(m -> !m.isMainTable()));
	}
	
	@Test
	public void testMappingOrdering() {
		manager.addMapping("3", "rom_3.ext", "rom_3.tbl");
		manager.addMapping("1", "rom_1.ext", "rom_1.tbl");
		manager.addMapping("2", "rom_2.ext", "rom_2.tbl");
		manager.addMapping("", "rom.tbl", "rom.tbl");
		
		var mappings = manager.getAllMappings();
		assertEquals(4, mappings.size());
		
		// Verify main table is first
		assertTrue(mappings.get(0).isMainTable());
		
		// Verify suffixes are in numeric order
		assertEquals("1", mappings.get(1).getSuffix());
		assertEquals("2", mappings.get(2).getSuffix());
		assertEquals("3", mappings.get(3).getSuffix());
	}
	
	@Test
	public void testGenerateInsertionCommands() {
		manager.addMapping("1", "rom_1.ext", "rom_1.tbl");
		manager.addMapping("2", "rom_2.ext", "rom_2.tbl");
		
		String commands = manager.generateInsertionCommands("rom", "%T_FILENAME%");
		assertNotNull(commands);
		assertFalse(commands.isEmpty());
		assertTrue(commands.contains("rom_1.tbl"));
		assertTrue(commands.contains("rom_1.ext"));
		assertTrue(commands.contains("rom_2.tbl"));
		assertTrue(commands.contains("rom_2.ext"));
		assertTrue(commands.contains("-h")); // Should have -h flag
		// Should include inline validation with if/else
		assertTrue(commands.contains("if not exist"));
		assertTrue(commands.contains("WARNING"));
		assertTrue(commands.contains(") else ("));
	}
	
	@Test
	public void testGenerateValidationCommands() {
		manager.addMapping("", "rom.tbl", "rom.tbl");
		manager.addMapping("1", "rom_1.ext", "rom_1.tbl");
		
		String commands = manager.generateValidationCommands();
		assertNotNull(commands);
		// Should validate table files
		assertTrue(commands.contains("rom.tbl"));
		assertTrue(commands.contains("rom_1.tbl"));
		assertTrue(commands.contains("ERROR"));
	}
	
	@Test
	public void testGenerateCommandsWithSpacesInFilenames() {
		manager.addMapping("1", "Fantasy Zone (World)_1.ext", "Fantasy Zone (World)_1.tbl");
		
		String insertionCommands = manager.generateInsertionCommands("Fantasy Zone (World)", "%T_FILENAME%");
		String validationCommands = manager.generateValidationCommands();
		
		// Verify quotes are present in insertion commands (includes inline .ext validation)
		assertTrue(insertionCommands.contains("\"Fantasy Zone (World)_1.tbl\""));
		assertTrue(insertionCommands.contains("\"Fantasy Zone (World)_1.ext\""));
		
		// Verify quotes in validation commands (only .tbl files)
		assertTrue(validationCommands.contains("\"Fantasy Zone (World)_1.tbl\""));
		// .ext files are validated inline during insertion, not at the beginning
		assertFalse(validationCommands.contains("\"Fantasy Zone (World)_1.ext\""));
	}
	
	@Test
	public void testAutoDetectMappings(@TempDir Path tempDir) throws Exception {
		// Create test files
		File rom_tbl = new File(tempDir.toFile(), "rom.tbl");
		File rom_1_ext = new File(tempDir.toFile(), "rom_1.ext");
		File rom_1_tbl = new File(tempDir.toFile(), "rom_1.tbl");
		File rom_2_ext = new File(tempDir.toFile(), "rom_2.ext");
		File rom_2_tbl = new File(tempDir.toFile(), "rom_2.tbl");
		
		Files.writeString(rom_tbl.toPath(), "test");
		Files.writeString(rom_1_ext.toPath(), "test");
		Files.writeString(rom_1_tbl.toPath(), "test");
		Files.writeString(rom_2_ext.toPath(), "test");
		Files.writeString(rom_2_tbl.toPath(), "test");
		
		// Auto-detect
		manager.autoDetectMappings(tempDir.toFile(), "rom");
		
		assertEquals(3, manager.size(), "Should find main table and 2 additional mappings");
		assertNotNull(manager.getMainMapping());
		assertNotNull(manager.getMapping("1"));
		assertNotNull(manager.getMapping("2"));
	}
	
	@Test
	public void testAutoDetectMappingsIgnoresMissingTables(@TempDir Path tempDir) throws Exception {
		// Create extraction files but not their corresponding tables
		File rom_1_ext = new File(tempDir.toFile(), "rom_1.ext");
		File rom_2_ext = new File(tempDir.toFile(), "rom_2.ext");
		
		Files.writeString(rom_1_ext.toPath(), "test");
		Files.writeString(rom_2_ext.toPath(), "test");
		
		// Auto-detect
		manager.autoDetectMappings(tempDir.toFile(), "rom");
		
		assertEquals(0, manager.size(), "Should find nothing if table files don't exist");
	}
	
	@Test
	public void testClear() {
		manager.addMapping("1", "rom_1.ext", "rom_1.tbl");
		manager.addMapping("2", "rom_2.ext", "rom_2.tbl");
		assertEquals(2, manager.size());
		
		manager.clear();
		assertEquals(0, manager.size());
		assertTrue(manager.isEmpty());
	}
	
	@Test
	public void testDuplicateRemoval() {
		manager.addMapping("1", "rom_1.ext", "rom_1.tbl");
		manager.addMapping("1", "rom_1_new.ext", "rom_1_new.tbl");
		
		assertEquals(1, manager.size(), "Duplicate suffix should be replaced");
		assertEquals("rom_1_new.ext", manager.getMapping("1").getExtFileName());
	}
}
