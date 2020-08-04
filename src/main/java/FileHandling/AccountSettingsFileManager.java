package FileHandling;

import Account.Account;
import Language.BlacklistDuration;
import Language.Language;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static Constants.Constants.*;
import static FileHandling.KeyValueFileManager.readValueFromFile;
import static Language.LanguageManager.getLanguage;
import static java.nio.charset.StandardCharsets.UTF_8;

public class AccountSettingsFileManager {
	public static final String SETTINGS_FILE_NAME = "settings.tsv";
	
	private static final String ACCOUNT_NAME_KEY = "accountName";
	private static final String NATIVE_LANGUAGE_KEY = "nativeLanguage";
	private static final String VOCAB_DIR_KEY = "vocabDirectory";
	private static final String AUTOBLACKLIST_DURATION_KEY = "autoblacklistDuration";
	private static final String RECURRENCE_PROBABILITY_KEY = "recurrenceProbability";
	private static final String SESSION_LENGTH_KEY = "sessionLength";
	
	public static String readAccountName(String accountDirName) {
		return readValueFromSettingsFile(accountDirName, ACCOUNT_NAME_KEY);
	}
	
	public static Language readAccountNativeLanguage(String accountDirName) throws IOException {
		return getLanguage(readValueFromSettingsFile(accountDirName, NATIVE_LANGUAGE_KEY));
	}
	
	public static String readVocabDir(String accountDirName) {
		return readValueFromSettingsFile(accountDirName, VOCAB_DIR_KEY);
	}
	
	public static BlacklistDuration readAutoblacklistDuration(String accountDirName) {
		String autoblacklistString = readValueFromSettingsFile(accountDirName, AUTOBLACKLIST_DURATION_KEY);
		if (autoblacklistString == null) {
			return DEFAULT_AUTOBLACKLIST_DURATION;
		} else {
			return new BlacklistDuration(autoblacklistString);
		}
	}
	
	public static double readRecurrenceProbability(String accountDirName) {
		String recurrenceProbabilityString = readValueFromSettingsFile(accountDirName, RECURRENCE_PROBABILITY_KEY);
		if (recurrenceProbabilityString == null) {
			return DEFAULT_RECURRENCE_PROBABILITY;
		} else {
			double recurrenceProbability = Double.parseDouble(recurrenceProbabilityString);
			if (recurrenceProbability <= 0 || recurrenceProbability > 1) {
				throw new IllegalArgumentException("Sentence recurrence probability must be greater than 0 and less than or equal to 1.");
			}
			return recurrenceProbability;
		}
	}
	
	public static int readSessionLength(String accountDirName) {
		String sessionLengthString = readValueFromSettingsFile(accountDirName, SESSION_LENGTH_KEY);
		if (sessionLengthString == null) {
			return DEFAULT_SESSION_LENGTH;
		} else {
			int sessionLength = Integer.parseInt(sessionLengthString);
			if (sessionLength < 1 || sessionLength > 500) {
				throw new IllegalArgumentException("Session length must be between 1 and 500 inclusive.");
			}
			return sessionLength;
		}
	}
	
	public static void updateSettingsFile(Account account) {
		File settingsFile = new File(AccountDirManager.getDir(account), SETTINGS_FILE_NAME);
		writeSettingsToFile(settingsFile, account.getName(), account.getNativeLanguage(), account.getVocabDirectory(), account.getAutoblacklistDuration(), account.getRecurrenceProbability(), account.getSessionLength());
	}
	
	public static void writeSettingsToFile(File settingsFile, String accountName, Language accountNativeLanguage, String vocabDir, BlacklistDuration autoblacklistDuration, double recurrenceProbability, int sessionLength) {
		try {
			String fileContents = ACCOUNT_NAME_KEY + "\t" + accountName + "\n" +
					NATIVE_LANGUAGE_KEY + "\t" + accountNativeLanguage.getName() + "\n" +
					VOCAB_DIR_KEY + "\t" + vocabDir + "\n" +
					AUTOBLACKLIST_DURATION_KEY + "\t" + autoblacklistDuration + "\n" +
					RECURRENCE_PROBABILITY_KEY + "\t" + recurrenceProbability + "\n" +
					SESSION_LENGTH_KEY + "\t" + sessionLength;
			Files.write(settingsFile.toPath(), fileContents.getBytes(UTF_8));
		} catch (IOException e) {
			System.err.println("Could not write to settings file for this account. Terminating program.");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private static String readValueFromSettingsFile(String accountDirName, String desiredKey) {
		File settingsFile = new File(AccountDirManager.getDir(accountDirName), SETTINGS_FILE_NAME);
		return readValueFromFile(settingsFile, desiredKey);
	}
}
