package Account;

import Language.BlacklistDuration;
import Language.Language;
import Terminal.Terminal;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static Constants.Constants.*;
import static FileHandling.KeyValueFileManager.readValueFromFile;
import static FileHandling.DefaultsFileManager.getDefaultLanguage;
import static Language.LanguageManager.getLanguage;
import static java.nio.charset.StandardCharsets.UTF_8;

public class AccountManager {
	public static final File ACCOUNTS_DIR = new File(INSTALL_DIR, "accounts");
	public static final String SETTINGS_FILE_NAME = "settings.tsv";
	
	private static final String ACCOUNT_NAME_KEY = "accountName";
	private static final String NATIVE_LANGUAGE_KEY = "nativeLanguage";
	private static final String VOCAB_DIR_KEY = "vocabDirectory";
	private static final String AUTOBLACKLIST_DURATION_KEY = "autoblacklistDuration";
	private static final String RECURRENCE_PROBABILITY_KEY = "recurrenceProbability";
	private static final String SESSION_LENGTH_KEY = "sessionLength";
	
	private static List<Account> accounts;
	
	public static void loadAccounts() throws IOException {
		createAccountsDirIfNecessary();
		loadAccountsFromSubDirs();
	}
	
	public static int getNumberOfAccounts() {
		return accounts.size();
	}
	
	public static List<String> getAccountNames() {
		List<String> accountNames = new ArrayList<>();
		for (Account account : accounts) {
			accountNames.add(account.getName());
		}
		return accountNames;
	}
	
	public static boolean createNewAccount(String accountName) {
		List<String> accountNames = getAccountNames();
		if (!isAccountEligibleForCreation(accountName, accountNames)) {
			return false;
		}
		String accountDirName = generateUniqueDirectoryName(accountName);
		String vocabDirPath = new File(ACCOUNTS_DIR, accountDirName).getPath();
		Account newAccount = new Account(accountName, accountDirName, getDefaultLanguage(), vocabDirPath, DEFAULT_AUTOBLACKLIST_DURATION, DEFAULT_RECURRENCE_PROBABILITY, DEFAULT_SESSION_LENGTH);
		boolean setUpAccountDirSuccessful = setUpAccountDir(newAccount);
		if (!setUpAccountDirSuccessful) {
			return false;
		}
		accounts.add(newAccount);
		return true;
	}
	
	public static void setNativeLanguage(Account account, Language newNativeLanguage) {
		account.setNativeLanguage(newNativeLanguage);
		updateSettingsFile(account);
	}
	
	public static boolean setVocabDir(Account account, String vocabDir) {
		File newVocabDir = new File(vocabDir);
		boolean dirNowExists = createDirIfNecessary(newVocabDir);
		if (!dirNowExists) {
			return false;
		}
		account.setVocabDirectory(vocabDir);
		updateSettingsFile(account);
		return true;
	}
	
	public static boolean setAutoblacklistDuration(Account account, String duration) {
		try {
			BlacklistDuration newDuration = new BlacklistDuration(duration);
			account.setAutoblacklistDuration(newDuration);
		} catch (IllegalArgumentException e) {
			return false;
		}
		updateSettingsFile(account);
		return true;
	}
	
	public static boolean setRecurrenceProbability(Account account, String probability) {
		try {
			double newProbability = Double.parseDouble(probability);
			if (newProbability <= 0 || newProbability > 1) {
				return false;
			} else {
				account.setRecurrenceProbability(newProbability);
			}
		} catch (NumberFormatException e) {
			return false;
		}
		updateSettingsFile(account);
		return true;
	}
	
	public static boolean setSessionLength(Account account, String sessionLength) {
		try {
			int newSessionLength = Integer.parseInt(sessionLength);
			if (newSessionLength < 1 || newSessionLength > 500) {
				return false;
			} else {
				account.setSessionLength(newSessionLength);
			}
		} catch (NumberFormatException e) {
			return false;
		}
		updateSettingsFile(account);
		return true;
	}
	
	public static Account getAccountFromName(String accountName) {
		for (Account account : accounts) {
			if (account.getName().equals(accountName)) {
				return account;
			}
		}
		return null;
	}
	
	public static List<Language> getLanguagesForAccount(Account account) throws IOException {
		String[] fileNames = new File(account.getVocabDirectory()).list(FILE_FILTER);
		List<Language> languages = new ArrayList<>();
		if (fileNames != null) {
			for (String fileName : fileNames) {
				if (fileName.endsWith("_Words.csv")) {
					String languageName = fileName.substring(0, fileName.length() - "_Words.csv".length());
					try {
						languages.add(getLanguage(languageName));
					} catch (IllegalArgumentException e) {
						System.err.println(e.getMessage());
					}
				}
			}
		}
		return languages;
	}
	
	public static void addPracticeLanguageToAccount(Account account, Language newLanguage) throws IOException {
		File newTextsDir = new File(account.getVocabDirectory(), newLanguage.getName() + "_Texts");
		if (!newTextsDir.exists() || !newTextsDir.isDirectory()) {
			boolean dirCreationSuccessful = newTextsDir.mkdir();
			if (!dirCreationSuccessful) {
				throw new IOException("Directory \"" + newTextsDir + "\" could not be created.");
			}
		}
		File newVocabFile = new File(account.getVocabDirectory(), newLanguage.getName() + "_Words.csv");
		newVocabFile.createNewFile();
		File newLanguageSettingsFile = new File(account.getVocabDirectory(), newLanguage.getName() + "_Settings.csv");
		Language nativeLanguage = account.getNativeLanguage();
		try (BufferedWriter settingsWriter = Files.newBufferedWriter(newLanguageSettingsFile.toPath(), UTF_8)) {
			settingsWriter.write(
					"FLTRLANGPREFS\n" +
					"charSubstitutions\t´='|`='|’='|‘='|′='|‵='\n" +
					"wordCharRegExp\t" + newLanguage.getWordCharRegExp().replace("\\\\-", "\\-") + "\n" +
					"makeCharacterWord\t0\n" +
					"removeSpaces\t0\n" +
					"rightToLeft\t" + (newLanguage.isRightToLeft() ? "1" : "0") + "\n" +
					"fontName\tDialog\n" +
					"fontSize\t20\n" +
					"statusFontName\tDialog\n" +
					"statusFontSize\t15\n" +
					"dictionaryURL1\thttps://translate.google.com/?ie=UTF-8&sl=" + getGoogleTranslateCode(newLanguage) + "&tl=" + getGoogleTranslateCode(nativeLanguage) + "&text=###\n" +
					"wordEncodingURL1\tUTF-8\n" +
					"openAutomaticallyURL1\t1\n" +
					"dictionaryURL2\thttps://glosbe.com/" + newLanguage.getTatoebaCode() + "/" + nativeLanguage.getTatoebaCode() + "/###\n" +
					"wordEncodingURL2\tUTF-8\n" +
					"openAutomaticallyURL2\t0\n" +
					"dictionaryURL3\t\n" +
					"wordEncodingURL3\tUTF-8\n" +
					"openAutomaticallyURL3\t0\n" +
					"exportTemplate\t$w\\t$t\\t$s\\t$r\\t$a\\t$k\n" +
					"exportStatuses\t1|2|3|4\n" +
					"doExport\t1\n");
		}
	}
	
	public static void deleteAccount(Account account) throws IOException {
		FileUtils.deleteDirectory(new File(ACCOUNTS_DIR, account.getDirectoryName()));
		accounts.remove(account);
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
	
	public static void createAccountsDirIfNecessary() {
		if (!ACCOUNTS_DIR.exists() || !ACCOUNTS_DIR.isDirectory()) {
			boolean dirCreationSuccessful = ACCOUNTS_DIR.mkdir();
			if (!dirCreationSuccessful) {
				System.err.println("Could not create account directory. Terminating program.");
				System.exit(0);
			}
		}
	}
	
	private static void loadAccountsFromSubDirs() throws IOException {
		String[] accountDirNames = ACCOUNTS_DIR.list(DIRECTORY_FILTER);
		assert(accountDirNames != null);
		accounts = new ArrayList<>(accountDirNames.length);
		for (String accountDirName : accountDirNames) {
			String accountName = readAccountName(accountDirName);
			Language nativeLanguage = readAccountNativeLanguage(accountDirName);
			String vocabDir = readVocabDir(accountDirName);
			BlacklistDuration autoblacklistDuration = readAutoblacklistDuration(accountDirName);
			double recurrenceProbability = readRecurrenceProbability(accountDirName);
			int sessionLength = readSessionLength(accountDirName);
			if (isValidAccountName(accountName)) {
				accounts.add(new Account(accountName, accountDirName, nativeLanguage, vocabDir, autoblacklistDuration, recurrenceProbability, sessionLength));
			}
		}
	}
	
	private static String readAccountName(String accountDirName) {
		return readValueFromSettingsFile(accountDirName, ACCOUNT_NAME_KEY);
	}
	
	private static Language readAccountNativeLanguage(String accountDirName) throws IOException {
		return getLanguage(readValueFromSettingsFile(accountDirName, NATIVE_LANGUAGE_KEY));
	}
	
	private static String readVocabDir(String accountDirName) {
		return readValueFromSettingsFile(accountDirName, VOCAB_DIR_KEY);
	}
	
	private static BlacklistDuration readAutoblacklistDuration(String accountDirName) {
		String autoblacklistString = readValueFromSettingsFile(accountDirName, AUTOBLACKLIST_DURATION_KEY);
		if (autoblacklistString == null) {
			return DEFAULT_AUTOBLACKLIST_DURATION;
		} else {
			return new BlacklistDuration(autoblacklistString);
		}
	}
	
	private static double readRecurrenceProbability(String accountDirName) {
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
	
	private static int readSessionLength(String accountDirName) {
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
	
	private static String readValueFromSettingsFile(String accountDirName, String desiredKey) {
		File settingsFile = new File(new File(ACCOUNTS_DIR, accountDirName), SETTINGS_FILE_NAME);
		return readValueFromFile(settingsFile, desiredKey);
	}
	
	private static boolean isValidAccountName(String accountName) {
		return accountName != null && !accountName.equals("");
	}
	
	private static boolean isAccountEligibleForCreation(String accountName, List<String> accountNames) {
		if (accountNames.size() > 999) {
			Terminal.println("You already have the maximum number of accounts. You must delete one if you wish to create a new one.");
			return false;
		} else if (accountNames.contains(accountName)) {
			Terminal.println("There is already an account with this name. Please choose another name.");
			return false;
		}
		return true;
	}
	
	private static String generateUniqueDirectoryName(String accountName) {
		String accountDirName = getShortAlphaNumericFromAccountName(accountName);
		accountDirName = appendSuffixToDirNameIfNecessary(accountDirName);
		return accountDirName;
	}
	
	private static String getShortAlphaNumericFromAccountName(String accountName) {
		String alphaNumericName = "";
		for (char ch : accountName.toCharArray()) {
			if (Character.isLetterOrDigit(ch)) {
				alphaNumericName += ch;
			}
		}
		return alphaNumericName.substring(0, Math.min(10, alphaNumericName.length()));
	}
	
	private static String appendSuffixToDirNameIfNecessary(String accountDirName) {
		int maxSuffix = 0;
		for (String dirName : getAccountDirNames()) {
			if (dirName.equals(accountDirName)) {
				maxSuffix = Math.max(maxSuffix, 1);
			} else if (dirName.startsWith(accountDirName)) {
				String suffix = dirName.substring(accountDirName.length());
				try {
					maxSuffix = Math.max(maxSuffix, Integer.parseInt(suffix));
				} catch (NumberFormatException ignored) {}
			}
		}
		if (maxSuffix > 0) {
			return accountDirName + (maxSuffix + 1);
		} else {
			return accountDirName;
		}
	}
	
	private static List<String> getAccountDirNames() {
		List<String> accountDirNames = new ArrayList<>();
		for (Account account : accounts) {
			accountDirNames.add(account.getDirectoryName());
		}
		return accountDirNames;
	}
	
	private static boolean setUpAccountDir(Account account) {
		File newAccountDir = new File(ACCOUNTS_DIR, account.getDirectoryName());
		boolean dirCreationSuccessful = createAccountDir(newAccountDir);
		if (!dirCreationSuccessful) {
			return false;
		}
		updateSettingsFile(account);
		return true;
	}
	
	private static boolean createAccountDir(File newAccountDir) {
		boolean dirCreationSuccessful = newAccountDir.mkdir();
		if (!dirCreationSuccessful) {
			Terminal.println(
					"For an unknown reason, the directory '" + newAccountDir.getPath() + "' could not be created. " +
					"Try checking that the program has permission to write to '" + ACCOUNTS_DIR.getAbsolutePath() + "'."
			);
			return false;
		}
		return true;
	}
	
	private static void updateSettingsFile(Account account) {
		File settingsFile = new File(new File(ACCOUNTS_DIR, account.getDirectoryName()), SETTINGS_FILE_NAME);
		writeSettingsToFile(settingsFile, account.getName(), account.getNativeLanguage(), account.getVocabDirectory(), account.getAutoblacklistDuration(), account.getRecurrenceProbability(), account.getSessionLength());
	}
	
	private static boolean createDirIfNecessary(File dir) {
		if (dir.exists()) {
			return true;
		}
		boolean dirCreationSuccessful = dir.mkdir();
		if (!dirCreationSuccessful) {
			Terminal.println(
					"For an unknown reason, the directory '" + dir.getPath() + "' could not be created. " +
							"Try checking that the program has permission to write to '" + dir.getAbsolutePath() + "'."
			);
			return false;
		}
		return true;
	}
	
	private static String getGoogleTranslateCode(Language language) {
		String twoCharCode = language.getTwoCharCode();
		return twoCharCode.equals("") ? language.getTatoebaCode() : twoCharCode;
	}
}
