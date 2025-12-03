/*
 *
 */
package com.wave.hextractor.object;

import com.wave.hextractor.pojo.OffsetEntry;
import com.wave.hextractor.util.Constants;
import com.wave.hextractor.util.FileUtils;
import com.wave.hextractor.util.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Class for the table operations.
 * @author slcantero
 */
public class HexTable implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2128637206318658102L;

	/** The Constant TABLE_KEY_A. */
	private static final String TABLE_KEY_A = "A";

	/** The Constant TABLE_KEY_LOWA. */
	private static final String TABLE_KEY_LOWA = "a";

	/** The Constant TABLE_KEY_ZERO. */
	private static final String TABLE_KEY_ZERO = "0";

	/** The Constant SPANISH_CHARS. */
	private static final Map<String, String> SPANISH_CHARS = new HashMap<>();
	static {
		SPANISH_CHARS.put("a", "á");
		SPANISH_CHARS.put("e", "é");
		SPANISH_CHARS.put("i", "í");
		SPANISH_CHARS.put("o", "ó");
		SPANISH_CHARS.put("u", "ú");
		SPANISH_CHARS.put("n", "ñ");
		SPANISH_CHARS.put("!", "¡");
		SPANISH_CHARS.put("?", "¿");
		SPANISH_CHARS.put("A", "Á");
		SPANISH_CHARS.put("E", "É");
		SPANISH_CHARS.put("I", "Í");
		SPANISH_CHARS.put("O", "Ó");
		SPANISH_CHARS.put("U", "Ú");
		SPANISH_CHARS.put("N", "Ñ");
	}

	/** The table for single-byte mappings (backward compatibility). */
	private Map<Byte, String> table = new HashMap<>();

	/** The reversed map for single-byte mappings (value -> byte). */
	private Map<String, Byte> reversedSingle = new HashMap<>();

	/** The reversed map for any-length mappings (value -> bytes). */
	private Map<String, byte[]> reversedMulti = new HashMap<>();

	/** Trie structure to support multi-byte keys with longest-match decoding. */
	private final TrieNode trieRoot = new TrieNode();

	/** The searchPercentCompleted. */
	private float searchPercent = 0;

	/**
	 * Transforms the byte into a String.
	 */
	public String toString(byte aByte, boolean expand, boolean decodeUnknown) {
		String res = table.get(aByte);
		if(res == null || (res.length() > 1 && !expand)) {
			if(decodeUnknown) {
				res = Constants.HEX_CHAR + Utils.intToHexString(aByte, 2) + Constants.HEX_CHAR;
			}
			else {
				res = Constants.HEX_VIEWER_UNKNOWN_CHAR;
			}
		}
		return res;
	}

	/**
	 * Transforms the byte into a String.
	 *
	 * @param aByte the a byte
	 * @return the string
	 */
	public String toString(byte aByte, boolean expand) {
		return toString(aByte, expand, false);
	}

	/**
	 * Converts the table selection to a line description.
	 *
	 * @return the string
	 */
	public String toSelectionString() {
		StringBuilder res = new StringBuilder();
		if(reversedSingle.containsKey(TABLE_KEY_A)) {
			res.append(TABLE_KEY_A).append(Constants.OFFSET_LENGTH_SEPARATOR).append(Constants.SPACE_STR);
			res.append(Utils.intToHexString(reversedSingle.get(TABLE_KEY_A), Constants.HEXSIZE_8BIT_VALUE)).append(Constants.SPACE_STR);
		}
		if(reversedSingle.containsKey(TABLE_KEY_LOWA)) {
			res.append(TABLE_KEY_LOWA).append(Constants.OFFSET_LENGTH_SEPARATOR).append(Constants.SPACE_STR);
			res.append(Utils.intToHexString(reversedSingle.get(TABLE_KEY_LOWA), Constants.HEXSIZE_8BIT_VALUE)).append(Constants.SPACE_STR);
		}
		if(reversedSingle.containsKey(TABLE_KEY_ZERO)) {
			res.append(TABLE_KEY_ZERO).append(Constants.OFFSET_LENGTH_SEPARATOR).append(Constants.SPACE_STR);
			res.append(Utils.intToHexString(reversedSingle.get(TABLE_KEY_ZERO), Constants.HEXSIZE_8BIT_VALUE)).append(Constants.SPACE_STR);
		}
		return res.toString();
	}

	/**
	 * Load lines.
	 *
	 * @param tableLines the table lines
	 */
	private void loadLines(List<String> tableLines) {
		table = new HashMap<>();
		reversedSingle = new HashMap<>();
		reversedMulti = new HashMap<>();
		trieRoot.clear();
		int singleByteCount = 0;
		int multiByteCount = 0;
		for(String s : tableLines) {
			if(s.length() >= 4 && s.contains(Constants.TABLE_SEPARATOR)) {
				boolean isEquals = s.contains(Constants.TABLE_SEPARATOR + Constants.TABLE_SEPARATOR);
				String[] items = s.split(Constants.TABLE_SEPARATOR, 2);
				String tablechar;
				if(isEquals) {
					tablechar = s.substring(s.indexOf(Constants.TABLE_SEPARATOR) + 1);
				}
				else {
					tablechar = items[1];
				}
				// Remove CR/LF
				tablechar = tablechar.replaceAll(Constants.S_NEWLINE, Constants.EMPTY).replaceAll(Constants.S_CRETURN, Constants.EMPTY);
				if(Constants.RESERVED_CHARS.contains(tablechar)) {
					Utils.log("WARNING - Table char \"" + tablechar + "\" will not be used because it is reserved.");
				}
				else {
					String keyHex = items[0].toUpperCase().trim().replace(" ", Constants.EMPTY);
					// Basic validation
					if(keyHex.length() >= 2 && keyHex.length() % 2 == 0) {
						try {
							byte[] keyBytes = Utils.hexStringToByteArray(keyHex);
							if(keyBytes.length == 1) {
								addToTable(Byte.valueOf(keyBytes[0]), tablechar);
								singleByteCount++;
							}
							else {
								addToTable(keyBytes, tablechar);
								multiByteCount++;
							}
						} catch (Exception e) {
							Utils.log("ERROR - Invalid hex key in line: '" + s + "'");
						}
					}
					else {
						Utils.log("ERROR - Invalid key length in line: '" + s + "'");
					}
				}
			}
			else {
				Utils.log("ERROR - Line not valid: '" + s + "'");
			}
		}
		Utils.log("Tabla cargada: " + singleByteCount + " de un byte, " + multiByteCount + " entradas multibyte ");
	}

	/**
	 * Based in a displacement, reconstruct full table from $20 to $7E (included).
	 *
	 * @param displacement the displacement
	 */
	public HexTable(int displacement) {
		List<String> tableLines = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		byte currChar = (byte) (Constants.MIN_PRINTABLE_CHAR - displacement & Constants.MASK_8BIT);
		for(int i = Constants.MIN_PRINTABLE_CHAR; i <= Constants.MAX_PRINTABLE_CHAR; i++) {
			if(!Constants.RESERVED_CHARS.contains(String.valueOf((char) i))) {
				sb.setLength(0);
				sb.append(String.format(Constants.HEX_16_FORMAT, currChar));
				sb.append(Constants.TABLE_SEPARATOR);
				sb.append((char) i);
				sb.append(Constants.S_NEWLINE);
				tableLines.add(sb.toString());
			}
			currChar++;
		}
		loadLines(tableLines);
	}

	/**
	 * Loads the table lines.
	 *
	 * @param tableLines the table lines
	 */
	public HexTable(List<String> tableLines) {
		loadLines(tableLines);
	}

	/**
	 * Empty table.
	 */
	public HexTable() {
	}

	/**
	 * Loads a table lines file.
	 *
	 * @param tableFile the table file
	 * @throws FileNotFoundException the exception
	 */
	public HexTable(String tableFile) throws IOException {
		loadLines(Arrays.asList(FileUtils.getAsciiFile(tableFile).replace(Constants.UTF_8_BOM_BE, Constants.EMPTY).replace(Constants.UTF_8_BOM_LE, Constants.EMPTY).split(String.valueOf(Constants.NEWLINE))));
	}

	/** Merge mappings from another HexTable into this one. Existing entries may be overwritten by addToTable semantics. */
	public void mergeFrom(HexTable other) {
		if (other == null) return;
		// Merge single-byte table entries
		for (Map.Entry<Byte, String> e : other.table.entrySet()) {
			this.addToTable(e.getKey(), e.getValue());
		}
		// Merge multi-byte entries by traversing other's trie
		List<Map.Entry<byte[], String>> multiList = new ArrayList<>();
		other.collectTrieMappings(other.trieRoot, new ArrayList<>(), multiList);
		for (Map.Entry<byte[], String> me : multiList) {
			byte[] key = me.getKey();
			if (key != null && key.length > 0) {
				this.addToTable(Arrays.copyOf(key, key.length), me.getValue());
			}
		}
	}

	/**
	 * Translates a hex string to ascii.
	 */
	public String toAscii(byte[] hexString, boolean expand, boolean decodeUnknown) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while(i < hexString.length) {
			Match m = findLongestMatch(hexString, i);
			if(m == null) {
				// Unknown byte
				if(decodeUnknown) {
					sb.append(Constants.HEX_CHAR)
					  .append(String.format(Constants.HEX_16_FORMAT, hexString[i]))
					  .append(Constants.HEX_CHAR);
				}
				else {
					sb.append(Constants.HEX_VIEWER_UNKNOWN_CHAR);
				}
				i++;
			}
			else {
				String val = m.value;
				if((val.length() > 1 || m.length > 1) && !expand) {
					// Respect expand flag: show raw bytes if requested or unknown dot otherwise
					if(decodeUnknown) {
						for(int j = 0; j < m.length; j++) {
							sb.append(Constants.HEX_CHAR)
							  .append(String.format(Constants.HEX_16_FORMAT, hexString[i + j]))
							  .append(Constants.HEX_CHAR);
						}
					} else {
						sb.append(Constants.HEX_VIEWER_UNKNOWN_CHAR);
					}
				}
				else {
					sb.append(val);
				}
				i += m.length;
			}
		}
		return sb.toString();
	}

	/**
	 * Translates a hex string to ascii.
	 */
	public String toAscii(byte[] hexString, boolean expand) {
		return toAscii(hexString, expand, false);
	}

	/**
	 * Simple string to hex using table.
	 */
	public byte[] toHex(String aString) {
		byte[] res = new byte[aString.length()];
		byte hexSpace;
		if(reversedSingle.containsKey(Constants.SPACE_STR)) {
			hexSpace = reversedSingle.get(Constants.SPACE_STR);
		}
		else {
			hexSpace = 0;
		}
		int i = 0;
		for(char c : aString.toCharArray()) {
			Byte b = reversedSingle.get(String.valueOf(c));
			if(b == null) {
				res[i] = hexSpace;
			}
			else {
				res[i] = b;
			}
			i++;
		}
		return res;
	}

	/**
	 * Adds the to table.
	 *
	 * @param entry the entry
	 * @param theChar the the char
	 */
	public void addToTable(Byte entry, String theChar) {
		table.put(entry, theChar);
		// For single-byte: allow overwriting (allows last definition to win for accents)
		// E.g., 41=A then 41=Á, the Á will override
		reversedSingle.put(theChar, entry);
		// also add into trie as single-byte mapping
		addToTable(new byte[]{entry}, theChar);
	}

	/**
	 * Adds a multi-byte mapping into the trie and reversed maps.
	 */
	public void addToTable(byte[] key, String theChar) {
		// Build trie path
		TrieNode node = trieRoot;
		for(byte b : key) {
			node = node.children.computeIfAbsent(b, k -> new TrieNode());
		}
		node.value = theChar;
		
		// Update reversedMulti based on key length
		if(key.length > 1) {
			// Multi-byte: prefer first definition (unless new one is shorter)
			if(!reversedMulti.containsKey(theChar)) {
				reversedMulti.put(theChar, Arrays.copyOf(key, key.length));
			}
			else if(key.length < reversedMulti.get(theChar).length) {
				reversedMulti.put(theChar, Arrays.copyOf(key, key.length));
			}
		}
		else {
			// Single-byte: always update (allows accent overrides like 41=A then 41=Á)
			reversedMulti.put(theChar, Arrays.copyOf(key, key.length));
		}
	}


	/**
	 * Translates to ascii entry to a hex string.
	 *
	 * @param hexString the hex string
	 * @param entry the entry
	 * @param showExtracting shows current extraction
	 * @return the string
	 */
	public String toAscii(byte[] hexString, OffsetEntry entry, boolean showExtracting, boolean splitLines) {
		StringBuilder sb = new StringBuilder();
		int bytesreaded = 0;
		int bytesreadedStart = 0;
		StringBuilder line = new StringBuilder();
		if(showExtracting) {
			Utils.log(Utils.getMessage("consoleExtracting", 
				Utils.fillLeft(Integer.toHexString(entry.getStart()), Constants.HEX_ADDR_SIZE).toUpperCase(),
				Utils.fillLeft(Integer.toHexString(entry.getEnd()), Constants.HEX_ADDR_SIZE).toUpperCase()));
		}
		sb.append(entry.toString()).append(Constants.NEWLINE);
		int i = entry.getStart();
		while(i <= entry.getEnd()) {
			Match m = findLongestMatch(hexString, i);
			if(m != null) {
				// Always append the value as-is from the table
				// Braces are only used if explicitly defined by the user in the table
				line.append(m.value);
				i += m.length;
				bytesreaded += m.length;
				if(splitLines) {
					// If any of the consumed bytes is an end char, close the line
					// Only treat the token as an end marker when the match itself is single-byte.
					// Multi-byte characters that *end* with the same byte as the terminator should
					// not prematurely close the line (e.g. codes like A7 8D where 8D is also the
					// line terminator).
					String lastByteHex = String.format(Constants.HEX_16_FORMAT, hexString[i - 1]);
					boolean endCharMatched = m.length == 1 && entry.getEndChars().contains(lastByteHex);
					if(endCharMatched || i - 1 == entry.getEnd()) {
						String originalLine = line.toString();
						String numChars = Utils.fillLeft(String.valueOf(originalLine.length()), Constants.LEN_NUM_CHARS);
						String numCharsHex = Utils.fillLeft(String.valueOf(bytesreaded - bytesreadedStart), Constants.LEN_NUM_CHARS);
						sb.append(Constants.COMMENT_LINE).append(Utils.fillLeft(Integer.toHexString(entry.getStart() + bytesreadedStart), Constants.HEX_ADDR_SIZE).toUpperCase());
						sb.append(Constants.ORG_STR_OPEN).append(originalLine).append(Constants.ORG_STR_CLOSE);
						sb.append(Constants.STR_NUM_CHARS).append(numChars).append(Constants.STR_NUM_CHARS).append(numCharsHex);
						sb.append(Constants.NEWLINE);
						sb.append(line).append(Constants.STR_NUM_CHARS).append(numCharsHex);
						sb.append(Constants.NEWLINE);
						line.setLength(0);
						bytesreadedStart = bytesreaded;
					}
				}
			} else {
				// Unknown byte at i
				String hexStr = String.format(Constants.HEX_16_FORMAT, hexString[i]);
				line.append(Constants.HEX_CHAR).append(hexStr).append(Constants.HEX_CHAR);
				i++;
				bytesreaded++;
				if(splitLines) {
					// Close line if end char or end of entry
					if(entry.getEndChars().contains(hexStr) || i - 1 == entry.getEnd()) {
						String originalLine = line.toString();
						String numChars = Utils.fillLeft(String.valueOf(originalLine.length()), Constants.LEN_NUM_CHARS);
						String numCharsHex = Utils.fillLeft(String.valueOf(bytesreaded - bytesreadedStart), Constants.LEN_NUM_CHARS);
						sb.append(Constants.COMMENT_LINE).append(Utils.fillLeft(Integer.toHexString(entry.getStart() + bytesreadedStart), Constants.HEX_ADDR_SIZE).toUpperCase());
						sb.append(Constants.ORG_STR_OPEN).append(originalLine).append(Constants.ORG_STR_CLOSE);
						sb.append(Constants.STR_NUM_CHARS).append(numChars).append(Constants.STR_NUM_CHARS).append(numCharsHex);
						sb.append(Constants.NEWLINE);
						sb.append(line).append(Constants.STR_NUM_CHARS).append(numCharsHex);
						sb.append(Constants.NEWLINE);
						line.setLength(0);
						bytesreadedStart = bytesreaded;
					}
				}
			}
		}
		// Close any remaining incomplete line at the end of the range
		if(line.length() > 0) {
			String originalLine = line.toString();
			String numChars = Utils.fillLeft(String.valueOf(originalLine.length()), Constants.LEN_NUM_CHARS);
			String numCharsHex = Utils.fillLeft(String.valueOf(bytesreaded - bytesreadedStart), Constants.LEN_NUM_CHARS);
			sb.append(Constants.COMMENT_LINE).append(Utils.fillLeft(Integer.toHexString(entry.getStart() + bytesreadedStart), Constants.HEX_ADDR_SIZE).toUpperCase());
			sb.append(Constants.ORG_STR_OPEN).append(originalLine).append(Constants.ORG_STR_CLOSE);
			sb.append(Constants.STR_NUM_CHARS).append(numChars).append(Constants.STR_NUM_CHARS).append(numCharsHex);
			sb.append(Constants.NEWLINE);
			sb.append(line).append(Constants.STR_NUM_CHARS).append(numCharsHex);
			sb.append(Constants.NEWLINE);
		}
		sb.append(Constants.MAX_BYTES).append(bytesreaded).append(Constants.NEWLINE);
		if(showExtracting) {
			Utils.log("TOTAL BYTES TO ASCII: " + bytesreaded);
		}
		return sb.toString();
	}

	/**
	 * Transforms the ascii string to hex byte[].
	 *
	 * @param string the string
	 * @param entry the entry
	 * @return the byte[]
	 */
	public byte[] toHex(String string, OffsetEntry entry) {
		int offset = 0;
		int offsetStart = 0;
		byte[] hex = new byte[string.length() * 16];
		int maxsize = 0;
		boolean end = false;
		char next;
		boolean incomment = false;
		byte hexSpace;
		if(reversedSingle.containsKey(Constants.SPACE_STR)) {
			hexSpace = reversedSingle.get(Constants.SPACE_STR);
		}
		else {
			hexSpace = 0;
		}
		int stringStart = 0;
		int i = 0;
		while(i <string.length() && !end) {
			next = string.substring(i, i+1).charAt(0);
			if(incomment) {
				if(Constants.NEWLINE == next) {
					incomment = false;
				}
			}
			else {
				switch(next) {
				case Constants.COMMENT_LINE:
					incomment = true;
					break;
				case Constants.MAX_BYTES:
					maxsize = Integer.parseInt(string.substring(i+1, string.length()-1));
					end = true;
					break;
				case Constants.HEX_CHAR:
					String hexchar = string.substring(i+1, i+3);
					i+=3;
					if(Constants.HEX_CHAR!= string.substring(i, i+1).charAt(0)) {
						int j = i - 100;
						if(j < 0) {
							j = 0;
						}
						Utils.log("ERROR! HEX CHAR NOT CLOSED AT: " + i + " -> " + string.substring(j, i+1));
					}
					if(entry.getEndChars().contains(hexchar)) {
						char nextchar = string.substring(i+1, i+2).charAt(0);
						while(Constants.ADDR_CHAR == nextchar) {
							i++;
							String hexTo = string.substring(i+1, i+1+8);
							Utils.log(Utils.getMessage("consoleInsertingOffset", 
									Utils.fillLeft(Integer.toHexString(offsetStart), Constants.HEX_ADDR_SIZE) + " TO " + hexTo));
							i+=8;
							nextchar = string.substring(i+1, i+2).charAt(0);
						}
						//Check size
						if(Constants.STR_NUM_CHARS == nextchar) {
							i++;
							//Search end char
							int j = i;
							char testEnd = nextchar;
							while(testEnd != Constants.NEWLINE) {
								j++;
								testEnd = string.substring(j, j+1).charAt(0);
							}
							int length = Integer.parseInt(string.substring(i+1, j));
							if(offset - offsetStart > length-1) {
								// Report an actual overflow: show context and truncate to declared length
								Utils.log("ERROR!!! STRING TOO LARGE (" +
										Utils.fillLeft(String.valueOf(offset - offsetStart+1), 4) + " - " +
										Utils.fillLeft(String.valueOf(length), 4) +
										")!!!");
								Utils.log(string.substring(stringStart, i));
								// Truncate extra bytes to fit declared length
								int allowed = offsetStart + (length - 1);
								if(allowed < offset) {
									offset = allowed;
								}
							}
							else {
								if(offset - offsetStart < length-1) {
									Utils.log("WARNING!!! STRING TOO SMALL (" +
											Utils.fillLeft(String.valueOf(offset - offsetStart+1), 4) + " - " +
											Utils.fillLeft(String.valueOf(length), 4) + ")!!!");
									Utils.log(string.substring(stringStart, i));
									while(offset - offsetStart < length-1) {
										hex[offset++] = hexSpace;
									}
								}
							}
							i += j - i - 1;
							// mark the start of the next logical string segment for size counting
							offsetStart = offset;
							stringStart = i + 2;
						}
						hex[offset++] = Utils.hexStringCharToByte(hexchar);
					}
					else {
						hex[offset++] = Utils.hexStringCharToByte(hexchar);
					}
					break;
				case Constants.STR_NUM_CHARS:
					//Search end char
					int j = i;
					char testEnd = next;
					while(testEnd != Constants.NEWLINE) {
						j++;
						testEnd = string.substring(j, j+1).charAt(0);
					}
					int length = Integer.parseInt(string.substring(i+1, j));
					if(offset - offsetStart - 1 > length-1) {
						// Unended string is longer than declared: report and trim
						Utils.log("ERROR!!! NOENDED STRING TOO LARGE (" +
								Utils.fillLeft(String.valueOf(offset - offsetStart), 4) + " - " +
								Utils.fillLeft(String.valueOf(length), 4) +
								")!!!");
						Utils.log(string.substring(stringStart, i));
						int allowed = offsetStart + (length - 1) + 1; // account for -1 in formula
						if(allowed < offset) {
							offset = allowed;
						}
					}
					else {
						if(offset - offsetStart - 1 < length-1) {
							while(offset - offsetStart - 1 < length-1) {
								hex[offset++] = hexSpace;
							}
						}
					}
					i += j - i - 1;
					// mark the start of the next logical string segment for size counting
					offsetStart = offset;
					stringStart = i + 2;
					break;
				case Constants.NEWLINE:
					break;
				case Constants.CODEWORD_START:
					int k = i;
					//Search CODEWORD_END if not end, space char
					boolean foundCodeWord = false;
					while(!foundCodeWord && k < string.length() - 2) {
						k++;
						foundCodeWord = Constants.S_CODEWORD_END.equals(string.substring(k, k+1));
					}
					byte[] codeWordValue = new byte[]{hexSpace};
					if(foundCodeWord) {
						//Get Key/value - try with braces first (for special functions like {space}, {line})
						//then without braces (for normal multibyte values like {ab})
						String keyWithBraces = string.substring(i, k + 1);
						String keyWithoutBraces = string.substring(i + 1, k);
						if(reversedMulti.containsKey(keyWithBraces)) {
							codeWordValue = reversedMulti.get(keyWithBraces);
						}
						else if(reversedMulti.containsKey(keyWithoutBraces)) {
							codeWordValue = reversedMulti.get(keyWithoutBraces);
						}
						else {
							Utils.log("WARNING!!! CODE WORD NOT IN TABLE: '" + keyWithBraces + "'");
						}
						i = k;
					}
					for(byte bval : codeWordValue) {
						hex[offset++] = bval;
					}
					break;
				default:
					String nextString = String.valueOf(next);
					
					// First try to find in reversedMulti (prioritizes longer mappings like 2-byte codes)
					if(reversedMulti.containsKey(nextString)) {
						byte[] multiValue = reversedMulti.get(nextString);
						for(byte bval : multiValue) {
							hex[offset++] = bval;
						}
					}
					// If not found, try reversedSingle
					else if(reversedSingle.containsKey(nextString)) {
						hex[offset++] = reversedSingle.get(nextString);
					}
					// Not found in either map
					else {
						Utils.log("WARNING!!! CHARACTER NOT IN TABLE: '" + nextString + "'");
						Utils.log(string.substring(stringStart, i));
						hex[offset++] = hexSpace;
					}
					break;
				}
			}
			i++;
		}
		if(offset > maxsize) {
			offset = maxsize;
		}
		//No dejemos que la siguiente cadena empiece tarde
		if(offset < maxsize) {
			// (final traces removed)
			Utils.log(Utils.getMessage("consoleWarningStringTooSmall"));
			Utils.log(string.substring(stringStart));
			for(int j = offset; j < maxsize; j++) {
				hex[j] = Constants.PAD_CHAR;
			}
		}
		if(maxsize == 0) {
			maxsize = offset;
		}
		if(Utils.isDebug()) {
			Utils.logNoNL("BYTES TO HEX: " + Utils.fillLeft(String.valueOf(offset), 5) + " / " +  Utils.fillLeft(String.valueOf(maxsize), 5));
		}
		return Arrays.copyOf(hex, maxsize);
	}

	/**
	 * The Enum ENTRIES_STATUS.
	 */
	enum ENTRIES_STATUS {
		/** The searching start of string. */
		SEARCHING_START_OF_STRING,
		/** The searching end of string. */
		SEARCHING_END_OF_STRING,
		/** The skipping chars. */
		SKIPPING_CHARS,
	}

	/**
	 * Get all entries from the file.
	 *
	 * @param secondFileBytes the second file bytes
	 * @param numMinChars the num min chars
	 * @param numIgnoredChars the num ignored chars
	 * @param endCharsList the end chars list
	 * @param dictFile the dict file
	 * @return the all entries
	 * @throws IOException the exception
	 */
	    public String getAllEntries(byte[] secondFileBytes, int numMinChars, int numIgnoredChars,
		    List<String> endCharsList, String dictFile) throws IOException {
	    	return getAllEntries(secondFileBytes, numMinChars, numIgnoredChars, endCharsList, dictFile, true);
	    }
	    
	    public String getAllEntries(byte[] secondFileBytes, int numMinChars, int numIgnoredChars,
		    List<String> endCharsList, String dictFile, boolean useMultibyte) throws IOException {
		searchPercent = 0;
		// Remove carriage returns to standardize line endings (CRLF -> LF) - but only for multibyte
		if(useMultibyte) {
			byte[] cleanedBytes = new byte[secondFileBytes.length];
			int cbIndex = 0;
			for(byte b : secondFileBytes) {
				if(b != 0x0D) { // ignore CR
					cleanedBytes[cbIndex++] = b;
				}
			}
			secondFileBytes = Arrays.copyOf(cleanedBytes, cbIndex);
		}
		List<OffsetEntry> offsetEntryList = new ArrayList<>();
		HashSet<String> dict = new HashSet<>(Arrays.asList(FileUtils.getAsciiFile(dictFile).split(Constants.S_NEWLINE)));
		int entryStart = 0;
		boolean validString = false;
		StringBuilder word = new StringBuilder();
		StringBuilder sentence = new StringBuilder();
		String dataChar;
		List<String> skippedChars = new ArrayList<>();
		ENTRIES_STATUS status = ENTRIES_STATUS.SEARCHING_START_OF_STRING;
		long lastTime = System.currentTimeMillis();
		for(int i = 0; i < secondFileBytes.length - numMinChars && !Thread.currentThread().isInterrupted(); i++) {
			searchPercent = i * 100f / secondFileBytes.length;
			if(System.currentTimeMillis() - lastTime > 1000) {
				lastTime = System.currentTimeMillis();
				Utils.log(searchPercent + "% completed.");
			}
			String dataCharHex = String.format(Constants.HEX_16_FORMAT, secondFileBytes[i]);
			Match m = useMultibyte ? findLongestMatch(secondFileBytes, i) : null;
			dataChar = m != null ? m.value : table.getOrDefault(secondFileBytes[i], null);
			switch(status) {
			case SEARCHING_START_OF_STRING:
				if(dataChar != null) {
					entryStart = i;
					word.setLength(0);
					sentence.setLength(0);
					sentence.append(dataChar);
					word.append(dataChar);
					validString = false;
					status = ENTRIES_STATUS.SEARCHING_END_OF_STRING;
					if(m != null && m.length > 1) {
						i += (m.length - 1);
					}
				}
				break;
			case SEARCHING_END_OF_STRING:
				if(dataChar != null) {
					sentence.append(dataChar);
					word.append(dataChar);
					if(m != null && m.length > 1) {
						i += (m.length - 1);
					}
				}
				else {
					if(Utils.getCleanedString(word.toString()).length() > 1) {
						if(!validString) {
							validString = Utils.stringHasWords(dict, word.toString());
						}
						sentence.append(Constants.SPACE_STR);
						word.append(Constants.SPACE_STR);
						skippedChars.clear();
						skippedChars.add(dataCharHex);
						status = ENTRIES_STATUS.SKIPPING_CHARS;
					}
					else {
						if(validString) {
							offsetEntryList.add(new OffsetEntry(entryStart, i, endCharsList));
						}
						entryStart = 0;
						status = ENTRIES_STATUS.SEARCHING_START_OF_STRING;
					}
				}
				break;
			case SKIPPING_CHARS:
				if(dataChar != null) {
					word.setLength(0);
					sentence.append(dataChar);
					word.append(dataChar);
					status = ENTRIES_STATUS.SEARCHING_END_OF_STRING;
					if(m != null && m.length > 1) {
						i += (m.length - 1);
					}
				}
				else {
					skippedChars.add(dataCharHex);
					boolean skippedAreEndings = endCharsList.stream().anyMatch(skippedChars::contains);
					if(skippedChars.size() > numIgnoredChars) {
						if(sentence.length() > numMinChars) {
							if(Utils.stringHasWords(dict, word.toString()) || validString && skippedAreEndings) {
								offsetEntryList.add(new OffsetEntry(entryStart, i, endCharsList));
							}
							else {
								if(validString) {
									offsetEntryList.add(new OffsetEntry(entryStart, i, endCharsList));
								}
							}
						}
						entryStart = 0;
						status = ENTRIES_STATUS.SEARCHING_START_OF_STRING;
					}
				}
				break;
			default:
				break;
			}
		}
		if(entryStart > 0) {
			offsetEntryList.add(new OffsetEntry(entryStart, secondFileBytes.length - 1, endCharsList));
		}
		word.setLength(0);
		for(OffsetEntry oe : offsetEntryList) {
			word.append(oe.toEntryString()).append(Constants.OFFSET_STR_SEPARATOR);
		}
		return word.toString();
	}

	/**
	 * Transforms the table into ascii.
	 *
	 * @return the string
	 */
	public String toAsciiTable() {
		StringBuilder sb = new StringBuilder();
		Utils.sortByValue(table).forEach((key, value) -> {
			if (SPANISH_CHARS.containsKey(value)) {
				String spaChar = SPANISH_CHARS.get(value);
				sb.append(String.format(Constants.HEX_16_FORMAT, key)).append(Constants.TABLE_SEPARATOR).append(spaChar);
				sb.append(Constants.S_NEWLINE);
			}
			sb.append(String.format(Constants.HEX_16_FORMAT, key)).append(Constants.TABLE_SEPARATOR).append(value);
			sb.append(Constants.S_NEWLINE);
		});
		// Append multi-byte entries that are not single-byte
		// Append multi-byte entries by traversing the trie so we list every key->value
		// even when multiple keys map to the same value string.
		List<Map.Entry<byte[], String>> multiList = new ArrayList<>();
		collectTrieMappings(trieRoot, new ArrayList<>(), multiList);
		for (Map.Entry<byte[], String> me : multiList) {
			byte[] key = me.getKey();
			if (key.length > 1) {
				StringBuilder khex = new StringBuilder();
				for (byte b : key) {
					khex.append(String.format(Constants.HEX_16_FORMAT, b));
				}
				sb.append(khex).append(Constants.TABLE_SEPARATOR).append(me.getValue()).append(Constants.S_NEWLINE);
			}
		}
		return sb.toString();
	}

	/** Collect all mappings from trie into list of (keyBytes, value). */
	private void collectTrieMappings(TrieNode node, List<Byte> path, List<Map.Entry<byte[], String>> out) {
		if (node == null) return;
		if (node.value != null) {
			byte[] key = new byte[path.size()];
			for (int i = 0; i < path.size(); i++) key[i] = path.get(i);
			out.add(new AbstractMap.SimpleEntry<>(key, node.value));
		}
		for (Map.Entry<Byte, TrieNode> e : node.children.entrySet()) {
			path.add(e.getKey());
			collectTrieMappings(e.getValue(), path, out);
			path.remove(path.size() - 1);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (table == null ? 0 : table.hashCode());
		return result;
	}

	/**
	 * Equals.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof HexTable)) {
			return false;
		}
		HexTable objHt = (HexTable) obj;
		return table.equals(objHt.table);
	}

	/**
	 * Current search completition percent.
	 * @return percent search
	 */
	public float getSearchPercent() {
		return searchPercent;
	}

	/** Trie node for multi-byte decoding. */
	private static class TrieNode {
		Map<Byte, TrieNode> children = new HashMap<>();
		String value; // non-null means a mapping ends here
		void clear() {
			children.clear();
			value = null;
		}
		
		/** Debug: count total entries in trie subtree */
		@SuppressWarnings("unused")
		int countEntries() {
			int count = value != null ? 1 : 0;
			for(TrieNode child : children.values()) {
				count += child.countEntries();
			}
			return count;
		}
	}

	   /** Represents a matched entry at a position. */
	   public static class Match {
		   public final int length;
		   public final String value;
		   public Match(int length, String value) { this.length = length; this.value = value; }
	   }

	/**
	 * Find the longest mapping starting at position 'pos'. Returns null if none.
	 */
	private Match findLongestMatch(byte[] data, int pos) {
		TrieNode node = trieRoot;
		String lastVal = null;
		int lastLen = 0;
		int i = pos;
		
		while(i < data.length && node != null) {
			node = node.children.get(data[i]);
			if(node == null) {
				break;
			}
			if(node.value != null) {
				lastVal = node.value;
				lastLen = i - pos + 1;
			}
			i++;
		}
		
		if(lastLen > 0) {
			return new Match(lastLen, lastVal);
		}
		
		// No match found in trie
		return null;
	}

}

