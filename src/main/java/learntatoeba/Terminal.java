package learntatoeba;

import java.io.Console;
import java.io.IOException;
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
		try {
			while (System.in.available() > 0) {
				System.in.read(new byte[System.in.available()]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (CONSOLE == null) {
			return SCANNER.nextLine();
		} else {
			return CONSOLE.readLine();
		}
	}
	
	private static String getRtlPrintString(String str) {
		UnicodeString unicodeStr = new UnicodeString(str);
		String printString = "";
		String ltrString = "";
		for (int i = 0; i < unicodeStr.length(); i++) {
			String character = unicodeStr.getCharacter(i);
			if (character.matches(LTR_CHAR_REGEX)) {
				ltrString += character;
			} else if (character.matches(RTL_CHAR_REGEX)) {
				printString = character + moveWeakCharacterSuffixToBeginningInReverse(ltrString) + printString;
				ltrString = "";
			} else if (ltrString.equals("")) {
				printString = character + printString;
			} else {
				ltrString += character;
			}
		}
		printString = moveWeakCharacterSuffixToBeginningInReverse(ltrString) + printString;
		return printString;
	}
	
	private static String moveWeakCharacterSuffixToBeginningInReverse(String ltrString) {
		UnicodeString unicodeLtrString = new UnicodeString(ltrString);
		if (unicodeLtrString.length() > 0) {
			String ltrPrefix = "";
			while (unicodeLtrString.finalCharacter().matches(WEAK_CHAR_REGEX)) {
				ltrPrefix += unicodeLtrString.finalCharacter();
				unicodeLtrString.removeFinalCharacter();
			}
			unicodeLtrString.prepend(ltrPrefix);
		}
		return unicodeLtrString.toString();
	}
}
