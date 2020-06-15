package Terminal;

import java.io.Console;
import java.util.Scanner;

public class Terminal {
	private static final Console CONSOLE = System.console();
	private static final Scanner SCANNER = new Scanner(System.in);
	private static final String LTR_CHAR_REGEX = "[A-Za-z\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u02B8\u0300-\u0590\u0800-\u1FFF\u2C00-\uFB1C\uFDFE-\uFE6F\uFEFD-\uFFFF]";
	private static final String RTL_CHAR_REGEX = "[\u0591-\u07FF\u200F\u202B\u202E\uFB1D-\uFDFD\uFE70-\uFEFC]";
	private static final String WEAK_CHAR_REGEX = "[\u0000-\u0040\\u005B-\u0060\u007B-\u00BF\u00D7\u00F7\u02B9-\u02FF\u2000-\u2BFF\u2010-\u2029\u202C\u202F-\u2BFF]";
	
	private static boolean emulateBidi = false;
	
	public static void activateBidiEmulation() {
		emulateBidi = true;
	}
	
	public static boolean isEmulatingBidi() {
		return emulateBidi;
	}
	
	public static void println(String str) {
		print(str + "\n");
	}
	
	public static void printlnRtl(String str) {
		if (emulateBidi) {
			println(getRtlPrintString(str));
		} else {
			println(str);
		}
	}
	
	public static void print(String str) {
		if (CONSOLE == null) {
			System.out.print(str);
		} else {
			CONSOLE.printf(str);
		}
	}
	
	public static String readLine() {
		return SCANNER.nextLine();
	}
	
	private static String getRtlPrintString(String str) {
		String printString = "";
		String ltrString = "";
		int strIndex = 0;
		while (strIndex < str.length()) {
			String unicodeCharacter = getUnicodeCharacterStartingAt(strIndex, str);
			if (unicodeCharacter.matches(LTR_CHAR_REGEX)) {
				ltrString += unicodeCharacter;
			} else if (unicodeCharacter.matches(RTL_CHAR_REGEX)) {
				ltrString = amendLtrString(ltrString);
				printString = ltrString + printString;
				ltrString = "";
				printString = unicodeCharacter + printString;
			} else if (ltrString.equals("")) {
				printString = unicodeCharacter + printString;
			} else {
				ltrString += unicodeCharacter;
			}
			strIndex += unicodeCharacter.length();
		}
		ltrString = amendLtrString(ltrString);
		printString = ltrString + printString;
		return printString;
	}
	
	private static String getUnicodeCharacterStartingAt(int index, String str) {
		int codepoint = str.codePointAt(index);
		return str.substring(index, index + Character.charCount(codepoint));
	}
	
	private static String amendLtrString(String ltrString) {
		if (ltrString.length() > 0) {
			String ltrPrefix = "";
			String endCharacter = getFinalUnicodeCharacter(ltrString);
			while (endCharacter.matches(WEAK_CHAR_REGEX)) {
				ltrPrefix += endCharacter;
				ltrString = ltrString.substring(0, ltrString.length() - endCharacter.length());
				endCharacter = getFinalUnicodeCharacter(ltrString);
			}
			ltrString = ltrPrefix + ltrString;
		}
		return ltrString;
	}
	
	private static String getFinalUnicodeCharacter(String str) {
		int codepoint = str.codePointAt(str.length() - 1);
		return str.substring(str.length() - Character.charCount(codepoint));
	}
}
