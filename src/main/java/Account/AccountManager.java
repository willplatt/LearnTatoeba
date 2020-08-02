package Account;

import Language.Language;
import Language.BlacklistDuration;
import Terminal.Terminal;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static Constants.Constants.*;
import static Language.LanguageManager.getLanguage;
import static java.nio.charset.StandardCharsets.UTF_8;

public class AccountManager {
	public static final File ACCOUNTS_DIR = new File(INSTALL_DIR, "accounts");
	
	private static final File DEFAULT_LANGUAGE_FILE = new File(ACCOUNTS_DIR, "defaultLanguage.txt");
	private static final FilenameFilter DIRECTORY_FILTER = (current, name) -> new File(current, name).isDirectory();
	private static final FilenameFilter FILE_FILTER = (current, name) -> !new File(current, name).isDirectory();
	
	private static Language defaultNativeLanguage;
	private static List<Account> accounts;
	
	public static void loadAccounts() throws IOException {
		createAccountsDirIfNecessary();
		loadAccountsFromSubDirs();
	}
	
	public static void setDefaultLanguage(Language newDefaultLanguage) {
		createAccountsDirIfNecessary();
		createDefaultLanguageFile(newDefaultLanguage);
		defaultNativeLanguage = newDefaultLanguage;
	}
	
	public static Language getDefaultLanguage() {
		if (defaultNativeLanguage == null) {
			defaultNativeLanguage = readDefaultLanguage();
		}
		return defaultNativeLanguage;
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
		boolean setUpAccountDirSuccessful = setUpAccountDir(accountName, defaultNativeLanguage, accountDirName, DEFAULT_AUTOBLACKLIST_DURATION, DEFAULT_RECURRENCE_PROBABILITY, DEFAULT_SESSION_LENGTH);
		if (!setUpAccountDirSuccessful) {
			return false;
		}
		String vocabDirPath = new File(ACCOUNTS_DIR, accountDirName).getPath();
		Account newAccount = new Account(accountName, accountDirName, defaultNativeLanguage, vocabDirPath, DEFAULT_AUTOBLACKLIST_DURATION, DEFAULT_RECURRENCE_PROBABILITY, DEFAULT_SESSION_LENGTH);
		accounts.add(newAccount);
		return true;
	}
	
	public static void setNativeLanguage(Account account, Language newNativeLanguage) {
		account.setNativeLanguage(newNativeLanguage);
		updateInfoFile(account);
	}
	
	public static boolean setVocabDir(Account account, String vocabDir) {
		File newVocabDir = new File(vocabDir);
		boolean dirNowExists = createDirIfNecessary(newVocabDir);
		if (!dirNowExists) {
			return false;
		}
		account.setVocabDirectory(vocabDir);
		updateInfoFile(account);
		return true;
	}
	
	public static boolean setAutoblacklistDuration(Account account, String duration) {
		try {
			BlacklistDuration newDuration = new BlacklistDuration(duration);
			account.setAutoblacklistDuration(newDuration);
		} catch (IllegalArgumentException e) {
			return false;
		}
		updateInfoFile(account);
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
		updateInfoFile(account);
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
		updateInfoFile(account);
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
	
	private static void createAccountsDirIfNecessary() {
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
		return readLineFromInfoFile(accountDirName, 0);
	}
	
	private static Language readAccountNativeLanguage(String accountDirName) throws IOException {
		return getLanguage(readLineFromInfoFile(accountDirName, 1));
	}
	
	private static String readVocabDir(String accountDirName) {
		return readLineFromInfoFile(accountDirName, 2);
	}
	
	private static BlacklistDuration readAutoblacklistDuration(String accountDirName) {
		String autoblacklistString = readLineFromInfoFile(accountDirName, 3);
		if (autoblacklistString == null) {
			return DEFAULT_AUTOBLACKLIST_DURATION;
		} else {
			return new BlacklistDuration(autoblacklistString);
		}
	}
	
	private static double readRecurrenceProbability(String accountDirName) {
		String recurrenceProbabilityString = readLineFromInfoFile(accountDirName, 4);
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
		String sessionLengthString = readLineFromInfoFile(accountDirName, 5);
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
	
	private static String readLineFromInfoFile(String accountDirName, int lineNumber) {
		File accountInfoFile = new File(ACCOUNTS_DIR, accountDirName + "/info.txt");
		if (accountInfoFile.exists() && accountInfoFile.isFile()) {
			try {
				return Files.readAllLines(accountInfoFile.toPath(), UTF_8).get(lineNumber);
			} catch (IOException | IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private static void createDefaultLanguageFile(Language defaultLanguage) {
		try {
			Files.write(DEFAULT_LANGUAGE_FILE.toPath(), defaultLanguage.getName().getBytes(UTF_8));
		} catch (IOException e) {
			System.err.println("Could not create defaultLanguage.txt for this account. Terminating program.");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private static Language readDefaultLanguage() {
		if (DEFAULT_LANGUAGE_FILE.exists() && DEFAULT_LANGUAGE_FILE.isFile()) {
			try {
				return getLanguage(Files.readAllLines(DEFAULT_LANGUAGE_FILE.toPath(), UTF_8).get(0));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
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
	
	private static boolean setUpAccountDir(String accountName, Language accountNativeLanguage, String accountDirName, BlacklistDuration autoblacklistDuration, double recurrenceProbability, int sessionLength) {
		File newAccountDir = new File(ACCOUNTS_DIR, accountDirName);
		boolean dirCreationSuccessful = createAccountDir(newAccountDir);
		if (!dirCreationSuccessful) {
			return false;
		}
		writeInfoFile(accountName, accountNativeLanguage, newAccountDir, autoblacklistDuration, recurrenceProbability, sessionLength);
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
	
	private static void writeInfoFile(String accountName, Language accountNativeLanguage, File newAccountDir, BlacklistDuration autoblacklistDuration, double recurrenceProbability, int sessionLength) {
		File infoFile = new File(newAccountDir, "info.txt");
		try {
			String fileContents = accountName + "\n" + accountNativeLanguage.getName() + "\n" + newAccountDir.getPath() + "\n" + autoblacklistDuration + "\n" + recurrenceProbability + "\n" + sessionLength;
			Files.write(infoFile.toPath(), fileContents.getBytes(UTF_8));
		} catch (IOException e) {
			System.err.println("Could not create info.txt for this account. Terminating program.");
			e.printStackTrace();
			System.exit(0);
		}
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
	
	private static void updateInfoFile(Account account) {
		File infoFile = new File(new File(ACCOUNTS_DIR, account.getDirectoryName()), "info.txt");
		try {
			String fileContents = account.getName() + "\n" + account.getNativeLanguage().getName() + "\n" + account.getVocabDirectory() + "\n" + account.getAutoblacklistDuration() + "\n" + account.getRecurrenceProbability() + "\n" + account.getSessionLength();
			Files.write(infoFile.toPath(), fileContents.getBytes(UTF_8));
		} catch (IOException e) {
			System.err.println("Could not write info.txt for this account. Terminating program.");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private static String getGoogleTranslateCode(Language language) {
		String twoCharCode = language.getTwoCharCode();
		return twoCharCode.equals("") ? language.getTatoebaCode() : twoCharCode;
	}
}
