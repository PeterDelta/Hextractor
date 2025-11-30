package com.wave.hextractor;

import com.wave.hextractor.gui.HexViewer;
import com.wave.hextractor.util.*;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Main class that routes all the options.
 * @author slcantero
 */
public class Hextractor {

	/** Enum for application modes. */
	public enum Mode {
		FIX_MEGADRIVE_CHECKSUM,
		FIX_GAMEBOY_CHECKSUM,
		FIX_SNES_CHECKSUM,
		FIX_ZXTAP_CHECKSUM,
		FIX_ZXTZX_CHECKSUM,
		FIX_SMS_CHECKSUM,
		CLEAN_ASCII,
		INSERT_HEX,
		INSERT_FILE,
		ASCII_TO_HEX,
		EXTRACT_ASCII,
		SEARCH_RELATIVE_8,
		SEARCH_ALL,
		CLEAN_EXTRACTED_FILE,
		CREATE_IPS_PATCH,
		VERIFY_IPS_PATCH,
		APPLY_IPS_PATCH,
		EXTRACT_HEX,
		HEX_VIEW,
		CHECK_LINE_LENGTH,
		EXTRACT_ASCII_3_4,
		INSERT_ASCII_4_3,
		SEPARATE_CHAR_LENGTH,
		GENERATE_FILE_DIGESTS,
		FILL_READ_ME,
		PRINT_MESSAGE
	}

	/** The Constant MODE_FIX_MEGADRIVE_CHECKSUM. */
	public static final String MODE_FIX_MEGADRIVE_CHECKSUM = "-fcm";

	/** The Constant MODE_FIX_GAMEBOY_CHECKSUM. */
	public static final String MODE_FIX_GAMEBOY_CHECKSUM = "-fcg";

	/** The Constant MODE_FIX_SNES_CHECKSUM. */
	public static final String MODE_FIX_SNES_CHECKSUM = "-fcs";

	/** The Constant MODE_FIX_ZXTAP_CHECKSUM. */
	public static final String MODE_FIX_ZXTAP_CHECKSUM = "-fctap";

	/** The Constant MODE_FIX_ZXTZX_CHECKSUM. */
	public static final String MODE_FIX_ZXTZX_CHECKSUM = "-fctzx";

	/** The Constant MODE_FIX_SMS_CHECKSUM. */
	public static final String MODE_FIX_SMS_CHECKSUM = "-fcsms";

	/** The Constant MODE_CLEAN_ASCII. */
	public static final String MODE_CLEAN_ASCII = "-ca";

	/** The Constant MODE_INSERT_HEX. */
	public static final String MODE_INSERT_HEX = "-ih";

	/** The Constant MODE_INSERT_FILE. */
	public static final String MODE_INSERT_FILE = "-if";

	/** The Constant MODE_ASCII_TO_HEX. */
	public static final String MODE_ASCII_TO_HEX = "-h";

	/** The Constant MODE_EXTRACT_ASCII. */
	public static final String MODE_EXTRACT_ASCII = "-a";

	/** The Constant MODE_SEARCH_RELATIVE_8. */
	public static final String MODE_SEARCH_RELATIVE_8 = "-sr8";

	/** The Constant MODE_SEARCH_ALL. */
	public static final String MODE_SEARCH_ALL = "-sa";

	/** The Constant MODE_CLEAN_EXTRACTED_FILE. */
	public static final String MODE_CLEAN_EXTRACTED_FILE = "-cef";

	/** The Constant CREATE_IPS_PATCH. */
	public static final String CREATE_IPS_PATCH = "-cip";

	/** The Constant MODE_VERIFY_IPS_PATCH. */
	public static final String MODE_VERIFY_IPS_PATCH = "-vip";

	/** The Constant MODE_APPLY_IPS_PATCH. */
	public static final String MODE_APPLY_IPS_PATCH = "-aip";

	/** The Constant MODE_EXTRACT_HEX. */
	public static final String MODE_EXTRACT_HEX = "-eh";

	/** The Constant MODE_HEX_VIEW. */
	public static final String MODE_HEX_VIEW = "-hv";

	/** The Constant MODE_CHECK_LINE_LENGTH. */
	public static final String MODE_CHECK_LINE_LENGTH = "-cll";

	/** The Constant MODE_EXTRACT_ASCII_3_4. */
	public static final String MODE_EXTRACT_ASCII_3_4 = "-a34";

	/** The Constant MODE_INSERT_ASCII_4_3. */
	public static final String MODE_INSERT_ASCII_4_3 = "-h43";

	/** The Constant MODE_SEPARATE_CHAR_LENGTH. */
	public static final String MODE_SEPARATE_CHAR_LENGTH = "-scl";
	
	/** The Constant MODE_GENERATE_FILE_DIGESTS. */
	public static final String MODE_GENERATE_FILE_DIGESTS = "-gd";
	
	/** The Constant MODE_FILL_READ_ME. */
	public static final String MODE_FILL_READ_ME = "-frm";

	/** The Constant MODE_PRINT_MESSAGE. */
	public static final String MODE_PRINT_MESSAGE = "-msg";

	/**
	 * Parses the mode string to enum.
	 * @param modeStr the mode string
	 * @return the mode enum, or null if invalid
	 */
	public static Mode parseMode(String modeStr) {
		return switch (modeStr) {
			case MODE_FIX_MEGADRIVE_CHECKSUM -> Mode.FIX_MEGADRIVE_CHECKSUM;
			case MODE_FIX_GAMEBOY_CHECKSUM -> Mode.FIX_GAMEBOY_CHECKSUM;
			case MODE_FIX_SNES_CHECKSUM -> Mode.FIX_SNES_CHECKSUM;
			case MODE_FIX_ZXTAP_CHECKSUM -> Mode.FIX_ZXTAP_CHECKSUM;
			case MODE_FIX_ZXTZX_CHECKSUM -> Mode.FIX_ZXTZX_CHECKSUM;
			case MODE_FIX_SMS_CHECKSUM -> Mode.FIX_SMS_CHECKSUM;
			case MODE_CLEAN_ASCII -> Mode.CLEAN_ASCII;
			case MODE_INSERT_HEX -> Mode.INSERT_HEX;
			case MODE_INSERT_FILE -> Mode.INSERT_FILE;
			case MODE_ASCII_TO_HEX -> Mode.ASCII_TO_HEX;
			case MODE_EXTRACT_ASCII -> Mode.EXTRACT_ASCII;
			case MODE_SEARCH_RELATIVE_8 -> Mode.SEARCH_RELATIVE_8;
			case MODE_SEARCH_ALL -> Mode.SEARCH_ALL;
			case MODE_CLEAN_EXTRACTED_FILE -> Mode.CLEAN_EXTRACTED_FILE;
			case CREATE_IPS_PATCH -> Mode.CREATE_IPS_PATCH;
			case MODE_VERIFY_IPS_PATCH -> Mode.VERIFY_IPS_PATCH;
			case MODE_APPLY_IPS_PATCH -> Mode.APPLY_IPS_PATCH;
			case MODE_EXTRACT_HEX -> Mode.EXTRACT_HEX;
			case MODE_HEX_VIEW -> Mode.HEX_VIEW;
			case MODE_CHECK_LINE_LENGTH -> Mode.CHECK_LINE_LENGTH;
			case MODE_EXTRACT_ASCII_3_4 -> Mode.EXTRACT_ASCII_3_4;
			case MODE_INSERT_ASCII_4_3 -> Mode.INSERT_ASCII_4_3;
			case MODE_SEPARATE_CHAR_LENGTH -> Mode.SEPARATE_CHAR_LENGTH;
			case MODE_GENERATE_FILE_DIGESTS -> Mode.GENERATE_FILE_DIGESTS;
			case MODE_FILL_READ_ME -> Mode.FILL_READ_ME;
			case MODE_PRINT_MESSAGE -> Mode.PRINT_MESSAGE;
			default -> null;
		};
	}

	/**
	 * Prints the usage.
	 */
	private static void printUsage(ResourceBundle rb) {
		Utils.log(rb.getString(KeyConstants.KEY_CONSOLE_HELP));
	}

	/**
	 * Main program start.
	 *
	 * @param args the arguments
	 * @throws IOException if an I/O error occurs
	 */
	public static void main(String[] args) throws IOException {
		ResourceBundle rb = ResourceBundle.getBundle(Constants.RB_NAME, Locale.getDefault(), new com.wave.hextractor.util.UTF8Control());
		// Avoid printing header for lightweight message mode (-msg)
		boolean isPrintMessage = args.length > 0 && MODE_PRINT_MESSAGE.equals(args[0]);
		if (!isPrintMessage) {
			Utils.log(rb.getString(KeyConstants.KEY_CONSOLE_HEADER));
		}
		if (args.length > 0) {
			// Restore legacy behavior: if all args are files, create projects for each
			if (FileUtils.allFilesExist(args)) {
				for (String file : args) {
					try {
						String projectName = ProjectUtils.getProjectName(new File(file).getName());
						Utils.log(Utils.getMessage("consoleGeneratingProjects", projectName));
						ProjectUtils.createProject(file);
						Utils.log("------------------");
					} catch (Exception e) {
						Utils.logException(e);
					}
				}
				Utils.log(Utils.getMessage("consoleProjectsGenerated"));
			} else {
				// Otherwise, treat first argument as a mode/command as per new CLI
				manageModes(args, rb);
			}
		} else {
			// Detectar resolución de pantalla física y ajustar escala
			int dpi = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
			String scale = (dpi > 120) ? "1.25" : "1.0";
			System.setProperty("hextractor.ui.scale", scale);
			HexViewer.view();
		}
	}

	/**
	 * Manage app modes.
	 *
	 * @param args the args
	 * @param rb the rb
	 * @throws IOException if an I/O error occurs
	 */
	private static void manageModes(String[] args, ResourceBundle rb) throws IOException {
		Mode mode = parseMode(args[0]);
		if (mode == null) {
			printUsage(rb);
			return;
		}
		// Lightweight handler: print localized message by key with optional args
		if (mode == Mode.PRINT_MESSAGE) {
			if (args.length >= 2) {
				String key = args[1];
				String msg;
				try {
					msg = rb.getString(key);
				} catch (Exception e) {
					msg = key;
				}
				for (int i = 2; i < args.length; i++) {
					msg = msg.replace("{" + (i - 2) + "}", args[i]);
				}
				System.out.println(msg);
			} else {
				System.out.println("Missing message key");
			}
			return;
		}
		switch (args.length) {
		case 2:
			manageModes2Args(args, rb, mode);
			break;
		case 3:
			manageModes3Args(args, rb, mode);
			break;
		case 4:
			manageModes4Args(args, rb, mode);
			break;
		case 5:
			manageModes5Args(args, rb, mode);
			break;
		case 6:
			if (mode == Mode.SEARCH_ALL) {
				FileUtils.searchAllStrings(args[1], args[2], Integer.parseInt(args[3]), args[4], args[5]);
			} else {
				printUsage(rb);
			}
			break;
		default:
		case 1:
			if (mode == Mode.HEX_VIEW) {
				HexViewer.view();
			} else {
				printUsage(rb);
			}
			break;
		}
	}

	private static void manageModes5Args(String[] args, ResourceBundle rb, Mode mode) throws IOException {
		switch (mode) {
			case EXTRACT_ASCII -> FileUtils.extractAsciiFile(args[1], args[2], args[3], args[4]);
			case SEARCH_ALL -> FileUtils.searchAllStrings(args[1], args[2], Integer.parseInt(args[3]), args[4]);
			case EXTRACT_ASCII_3_4 -> FileUtils.extractAscii3To4Data(args[1], args[2], args[3], args[4]);
			default -> printUsage(rb);
		}
	}

	private static void manageModes4Args(String[] args, ResourceBundle rb, Mode mode) throws IOException {
		switch (mode) {
			case ASCII_TO_HEX -> FileUtils.insertAsciiAsHex(args[1], args[2], args[3]);
			case SEARCH_RELATIVE_8 -> FileUtils.searchRelative8Bits(args[1], args[2], args[3]);
			case CREATE_IPS_PATCH -> IpsPatchUtils.createIpsPatch(args[1], args[2], args[3]);
			case APPLY_IPS_PATCH -> IpsPatchUtils.applyIpsPatch(args[1], args[2], args[3]);
			case VERIFY_IPS_PATCH -> IpsPatchUtils.validateIpsPatch(args[1], args[2], args[3]);
			case EXTRACT_HEX -> FileUtils.extractHexData(args[1], args[2], args[3]);
			case INSERT_ASCII_4_3 -> FileUtils.insertHex4To3Data(args[1], args[2], args[3]);
			case SEPARATE_CHAR_LENGTH -> FileUtils.separateCharLength(args[1], args[2], args[3]);
			case INSERT_FILE -> FileUtils.replaceFileData(args[1], args[2],
					Integer.valueOf(args[3], Constants.HEX_RADIX));
			case FILL_READ_ME -> FileUtils.fillGameData(args[1], args[2], args[3]);
			default -> printUsage(rb);
		}
	}

	private static void manageModes3Args(String[] args, ResourceBundle rb, Mode mode) throws IOException {
		switch (mode) {
			case CLEAN_ASCII -> FileUtils.cleanAsciiFile(args[1], args[2]);
			case INSERT_HEX -> FileUtils.insertHexData(args[1], args[2]);
			case CLEAN_EXTRACTED_FILE -> FileUtils.cleanExtractedFile(args[1], args[2]);
			case HEX_VIEW -> HexViewer.view(args[1], args[2]);
			case FIX_ZXTAP_CHECKSUM -> TAPChecksumUtils.checkUpdateZxTapChecksum(args[1], args[2]);
			case FIX_ZXTZX_CHECKSUM -> TAPChecksumUtils.checkUpdateZxTzxChecksum(args[1], args[2]);
			default -> printUsage(rb);
		}
	}

	private static void manageModes2Args(String[] args, ResourceBundle rb, Mode mode) throws IOException {
		switch (mode) {
			case FIX_MEGADRIVE_CHECKSUM -> SMDChecksumUtils.checkUpdateMegaDriveChecksum(args[1]);
			case FIX_GAMEBOY_CHECKSUM -> GBChecksumUtils.checkUpdateGameBoyChecksum(args[1]);
			case FIX_SNES_CHECKSUM -> SNESChecksumUtils.checkUpdateSnesChecksum(args[1]);
			case HEX_VIEW -> HexViewer.view(args[1]);
			case FIX_ZXTAP_CHECKSUM -> TAPChecksumUtils.checkUpdateZxTapChecksum(args[1]);
			case FIX_ZXTZX_CHECKSUM -> TAPChecksumUtils.checkUpdateZxTzxChecksum(args[1]);
			case FIX_SMS_CHECKSUM -> SMSChecksumUtils.checkUpdateSMSChecksum(args[1]);
			case CHECK_LINE_LENGTH -> FileUtils.checkLineLength(args[1]);
			case GENERATE_FILE_DIGESTS -> FileUtils.outputFileDigests(args[1]);
			default -> printUsage(rb);
		}
	}

}
