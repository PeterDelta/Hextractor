package com.wave.hextractor.pojo;

import java.io.Serializable;

/**
 * Represents the mapping between a table file and an extraction file with an optional suffix.
 * This allows multiple extraction files to be associated with their corresponding table files.
 */
public class TableMapping implements Serializable, Comparable<TableMapping> {
	
	private static final long serialVersionUID = 1L;
	
	/** The numeric suffix for this mapping (e.g., "1", "2", "3"). Empty string for main table. */
	private String suffix;
	
	/** The extraction file name (e.g., "rom_1.ext"). */
	private String extFileName;
	
	/** The table file name (e.g., "rom_1.tbl"). */
	private String tableFileName;
	
	/** Optional description of this mapping. */
	private String description;
	
	/**
	 * Creates a new TableMapping.
	 */
	public TableMapping() {
		this("", "", "");
	}
	
	/**
	 * Creates a new TableMapping with suffix and file names.
	 * 
	 * @param suffix the numeric suffix (e.g., "1", "2", "3", or empty string for main)
	 * @param extFileName the extraction file name
	 * @param tableFileName the table file name
	 */
	public TableMapping(String suffix, String extFileName, String tableFileName) {
		this(suffix, extFileName, tableFileName, "");
	}
	
	/**
	 * Creates a new TableMapping with all parameters.
	 * 
	 * @param suffix the numeric suffix
	 * @param extFileName the extraction file name
	 * @param tableFileName the table file name
	 * @param description optional description
	 */
	public TableMapping(String suffix, String extFileName, String tableFileName, String description) {
		this.suffix = suffix != null ? suffix : "";
		this.extFileName = extFileName != null ? extFileName : "";
		this.tableFileName = tableFileName != null ? tableFileName : "";
		this.description = description != null ? description : "";
	}
	
	/**
	 * Gets the suffix.
	 * @return the suffix (empty string for main table)
	 */
	public String getSuffix() {
		return suffix;
	}
	
	/**
	 * Sets the suffix.
	 * @param suffix the suffix to set
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix != null ? suffix : "";
	}
	
	/**
	 * Gets the extraction file name.
	 * @return the extraction file name
	 */
	public String getExtFileName() {
		return extFileName;
	}
	
	/**
	 * Sets the extraction file name.
	 * @param extFileName the extraction file name to set
	 */
	public void setExtFileName(String extFileName) {
		this.extFileName = extFileName != null ? extFileName : "";
	}
	
	/**
	 * Gets the table file name.
	 * @return the table file name
	 */
	public String getTableFileName() {
		return tableFileName;
	}
	
	/**
	 * Sets the table file name.
	 * @param tableFileName the table file name to set
	 */
	public void setTableFileName(String tableFileName) {
		this.tableFileName = tableFileName != null ? tableFileName : "";
	}
	
	/**
	 * Gets the description.
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description.
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description != null ? description : "";
	}
	
	/**
	 * Checks if this is the main table mapping (no suffix).
	 * @return true if suffix is empty
	 */
	public boolean isMainTable() {
		return suffix.isEmpty();
	}
	
	/**
	 * Compares mappings by suffix for sorting.
	 * Main table (empty suffix) comes first, then numeric suffixes in order.
	 */
	@Override
	public int compareTo(TableMapping other) {
		if (this.isMainTable() && !other.isMainTable()) {
			return -1;
		}
		if (!this.isMainTable() && other.isMainTable()) {
			return 1;
		}
		// Both are non-main, compare by suffix number
		try {
			int thisSuffixNum = Integer.parseInt(this.suffix);
			int otherSuffixNum = Integer.parseInt(other.suffix);
			return Integer.compare(thisSuffixNum, otherSuffixNum);
		} catch (NumberFormatException e) {
			// If not numeric, compare lexicographically
			return this.suffix.compareTo(other.suffix);
		}
	}
	
	@Override
	public String toString() {
		if (isMainTable()) {
			return "TableMapping [Main: " + tableFileName + "]";
		}
		return "TableMapping [_" + suffix + ": " + extFileName + " -> " + tableFileName + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		TableMapping other = (TableMapping) obj;
		return suffix.equals(other.suffix) && 
		       extFileName.equals(other.extFileName) && 
		       tableFileName.equals(other.tableFileName);
	}
	
	@Override
	public int hashCode() {
		return (suffix + extFileName + tableFileName).hashCode();
	}
}
