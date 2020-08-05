package learntatoeba.migration;

import learntatoeba.defaults.DefaultsFileManager;
import learntatoeba.language.Language;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static learntatoeba.Constants.*;
import static learntatoeba.account.AccountDirManager.ACCOUNTS_DIR;
import static learntatoeba.account.settings.AccountSettingsFileManager.SETTINGS_FILE_NAME;
import static learntatoeba.account.settings.AccountSettingsFileManager.writeSettingsToFile;
import static learntatoeba.language.LanguageManager.getLanguage;
import static java.nio.charset.StandardCharsets.UTF_8;

public class MigrationTo0_1_0dev {
	static final File DEFAULT_LANGUAGE_FILE = new File(ACCOUNTS_DIR, "defaultLanguage.txt");
	
	public static boolean migrate() {
		try {
			migrateDefaultsFile();
			migrateSettingsOfAllAccounts();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static void migrateDefaultsFile() throws IOException {
		Language defaultNativeLanguage = getLanguage(Files.readAllLines(DEFAULT_LANGUAGE_FILE.toPath(), UTF_8).get(0));
		DefaultsFileManager.setDefaultLanguage(defaultNativeLanguage);
		Files.delete(DEFAULT_LANGUAGE_FILE.toPath());
	}
	
	private static void migrateSettingsOfAllAccounts() throws IOException {
		String[] accountDirNames = ACCOUNTS_DIR.list(DIRECTORY_FILTER);
		assert(accountDirNames != null);
		for (String accountDirName : accountDirNames) {
			migrateSettingsOfAccount(accountDirName);
		}
	}
	
	private static void migrateSettingsOfAccount(String accountDirName) throws IOException {
		File infoFile = new File(new File(ACCOUNTS_DIR, accountDirName), "info.txt");
		if (infoFile.isFile()) {
			List<String> lines = Files.readAllLines(infoFile.toPath(), UTF_8);
			String accountName = lines.get(0);
			Language nativeLanguage = getLanguage(lines.get(1));
			String vocabDir = lines.get(2);
			File settingsFile = new File(new File(ACCOUNTS_DIR, accountDirName), SETTINGS_FILE_NAME);
			writeSettingsToFile(settingsFile, accountName, nativeLanguage, vocabDir, DEFAULT_AUTOBLACKLIST_DURATION, DEFAULT_RECURRENCE_PROBABILITY, DEFAULT_SESSION_LENGTH);
			Files.delete(infoFile.toPath());
		}
	}
}
