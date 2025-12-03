package com.wave.hextractor.util;

import com.wave.hextractor.pojo.TableMapping;
import java.io.File;
import java.util.*;

/**
 * Manages multiple table-to-extraction file mappings.
 * Allows organizing and retrieving associated table files for multiple extraction files.
 */
public class TableMappingManager {
	
	/** List of all table mappings, ordered by suffix */
	private final List<TableMapping> mappings = new ArrayList<>();
	
	/**
	 * Adds a table mapping.
	 * @param mapping the mapping to add
	 */
	public void addMapping(TableMapping mapping) {
		if (mapping != null) {
			// Remove existing mapping with same suffix to avoid duplicates
			mappings.removeIf(m -> m.getSuffix().equals(mapping.getSuffix()));
			mappings.add(mapping);
			Collections.sort(mappings);
		}
	}
	
	/**
	 * Adds a table mapping with parameters.
	 * @param suffix the numeric suffix
	 * @param extFileName the extraction file name
	 * @param tableFileName the table file name
	 */
	public void addMapping(String suffix, String extFileName, String tableFileName) {
		addMapping(new TableMapping(suffix, extFileName, tableFileName));
	}
	
	/**
	 * Removes a table mapping by suffix.
	 * @param suffix the suffix to remove
	 * @return true if removed, false if not found
	 */
	public boolean removeMapping(String suffix) {
		return mappings.removeIf(m -> m.getSuffix().equals(suffix));
	}
	
	/**
	 * Gets a mapping by suffix.
	 * @param suffix the suffix to search for
	 * @return the mapping, or null if not found
	 */
	public TableMapping getMapping(String suffix) {
		return mappings.stream()
			.filter(m -> m.getSuffix().equals(suffix))
			.findFirst()
			.orElse(null);
	}
	
	/**
	 * Gets the main table mapping (empty suffix).
	 * @return the main table mapping, or null if not defined
	 */
	public TableMapping getMainMapping() {
		return getMapping("");
	}
	
	/**
	 * Gets all mappings.
	 * @return unmodifiable list of all mappings
	 */
	public List<TableMapping> getAllMappings() {
		return Collections.unmodifiableList(mappings);
	}
	
	/**
	 * Gets all non-main mappings.
	 * @return list of non-main mappings
	 */
	public List<TableMapping> getAdditionalMappings() {
		return mappings.stream()
			.filter(m -> !m.isMainTable())
			.toList();
	}
	
	/**
	 * Gets the number of mappings.
	 * @return the total number of mappings
	 */
	public int size() {
		return mappings.size();
	}
	
	/**
	 * Checks if any mappings exist.
	 * @return true if at least one mapping is defined
	 */
	public boolean isEmpty() {
		return mappings.isEmpty();
	}
	
	/**
	 * Clears all mappings.
	 */
	public void clear() {
		mappings.clear();
	}
	
	/**
	 * Auto-detects table mappings from file names in a directory.
	 * Searches for files matching patterns:
	 * - Main table: rom.tbl with rom_*.ext
	 * - Additional tables: rom_1.tbl with rom_1.ext, etc.
	 * 
	 * @param projectFolder the directory to search
	 * @param baseName the base name (e.g., "rom")
	 */
	public void autoDetectMappings(File projectFolder, String baseName) {
		if (projectFolder == null || !projectFolder.isDirectory()) {
			return;
		}
		
		clear();
		
		// Look for main table
		File mainTable = new File(projectFolder, baseName + ".tbl");
		if (mainTable.exists()) {
			addMapping("", baseName + ".tbl", baseName + ".tbl");
		}
		
		// Look for numbered extraction files
		File[] files = projectFolder.listFiles();
		if (files != null) {
			Set<String> detectedSuffixes = new TreeSet<>((a, b) -> {
				try {
					return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
				} catch (NumberFormatException e) {
					return a.compareTo(b);
				}
			});
			
			for (File file : files) {
				String name = file.getName();
				// Match patterns like rom_1.ext
				if (name.startsWith(baseName + "_") && name.endsWith(".ext")) {
					String suffix = name.substring(baseName.length() + 1, name.length() - 4);
					detectedSuffixes.add(suffix);
				}
			}
			
			// For each detected suffix, look for corresponding table
			for (String suffix : detectedSuffixes) {
				String extFileName = baseName + "_" + suffix + ".ext";
				String tableFileName = baseName + "_" + suffix + ".tbl";
				File tableFile = new File(projectFolder, tableFileName);
				
				if (tableFile.exists()) {
					addMapping(suffix, extFileName, tableFileName);
				}
			}
		}
	}
	
	/**
	 * Generates batch script commands for inserting all extracted files.
	 * Each mapping gets its own command line.
	 * 
	 * @param scriptName the base script name (without extension)
	 * @param outputFileName the output file name variable
	 * @return string containing all insertion commands
	 */
	public String generateInsertionCommands(String scriptName, String outputFileName) {
		StringBuilder sb = new StringBuilder();
		String progCall = "java -jar .Hextractor.jar ";
		
		for (TableMapping mapping : mappings) {
			// Skip main table in commands (it's used in GUI, not in batch)
			if (!mapping.isMainTable()) {
				// Validate extraction file exists before attempting insertion
				sb.append("if not exist \"").append(mapping.getExtFileName()).append("\" (").append(Constants.NEWLINE);
				sb.append("    echo WARNING: Extraction file not found: ").append(mapping.getExtFileName()).append(Constants.NEWLINE);
				sb.append("    echo Skipping insertion for this file.").append(Constants.NEWLINE);
				sb.append(") else (").append(Constants.NEWLINE);
				sb.append("    ").append(progCall).append("-h ")
					.append("\"").append(mapping.getTableFileName()).append("\" ")
					.append("\"").append(mapping.getExtFileName()).append("\" ")
					.append(outputFileName).append(Constants.NEWLINE);
				sb.append("    echo.").append(Constants.NEWLINE);
				sb.append(")").append(Constants.NEWLINE);
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Generates validation commands for batch script.
	 * Only validates table files (.tbl) at the beginning.
	 * Extraction files (.ext) are validated inline during insertion commands.
	 * @return validation command lines
	 */
	public String generateValidationCommands() {
		StringBuilder sb = new StringBuilder();
		
		// Validate all table files exist
		for (TableMapping mapping : mappings) {
			sb.append("if not exist \"").append(mapping.getTableFileName()).append("\" (").append(Constants.NEWLINE);
			sb.append("    echo ERROR: Table file not found: ").append(mapping.getTableFileName()).append(Constants.NEWLINE);
			sb.append("    pause").append(Constants.NEWLINE);
			sb.append("    exit /b 1").append(Constants.NEWLINE);
			sb.append(")").append(Constants.NEWLINE);
		}
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("TableMappingManager[");
		for (int i = 0; i < mappings.size(); i++) {
			if (i > 0) sb.append(", ");
			sb.append(mappings.get(i));
		}
		sb.append("]");
		return sb.toString();
	}
}
