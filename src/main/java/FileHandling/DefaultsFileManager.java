package FileHandling;

import Language.Language;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static Constants.Constants.VERSION;
import static FileHandling.AccountDirManager.ACCOUNTS_DIR;
import static FileHandling.KeyValueFileManager.readValueFromFile;
import static Language.LanguageManager.getLanguage;
import static java.nio.charset.StandardCharsets.UTF_8;

public class DefaultsFileManager {
	private static final File DEFAULTS_FILE = new File(ACCOUNTS_DIR, "defaults.tsv");
	private static final String VERSION_KEY = "version";
	private static final String DEFAULT_LANGUAGE_KEY = "defaultNativeLanguage";
	
	private static Language defaultNativeLanguage;
	
	public static void setDefaultLanguage(Language newDefaultLanguage) {
		AccountDirManager.createAccountsDirIfNecessary();
		createDefaultsFile(newDefaultLanguage);
		defaultNativeLanguage = newDefaultLanguage;
	}
	
	public static Language getDefaultLanguage() {
		if (defaultNativeLanguage == null) {
			defaultNativeLanguage = readDefaultLanguage();
		}
		return defaultNativeLanguage;
	}
	
	public static String readVersion() {
		return readValueFromFile(DEFAULTS_FILE, VERSION_KEY);
	}
	
	private static void createDefaultsFile(Language defaultLanguage) {
		try {
			String fileContents = VERSION_KEY + "\t" + VERSION + "\n" +
					DEFAULT_LANGUAGE_KEY + "\t" + defaultLanguage.getName();
			Files.write(DEFAULTS_FILE.toPath(), fileContents.getBytes(UTF_8));
		} catch (IOException e) {
			System.err.println("Could not create " + DEFAULTS_FILE.getPath() + " for this account. Terminating program.");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private static Language readDefaultLanguage() {
		String defaultLanguageName = readValueFromFile(DEFAULTS_FILE, DEFAULT_LANGUAGE_KEY);
		if (defaultLanguageName == null) {
			return null;
		}
		try {
			return getLanguage(defaultLanguageName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
