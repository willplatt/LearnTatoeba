package Account;

import Language.Language;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static Language.LanguageManager.getLanguage;

public class AccountManager {
	public static final File ACCOUNTS_DIR = new File("accounts");
	
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
		boolean setUpAccountDirSuccessful = setUpAccountDir(accountName, defaultNativeLanguage, accountDirName);
		if (!setUpAccountDirSuccessful) {
			return false;
		}
		accounts.add(new Account(accountName, accountDirName, defaultNativeLanguage, new File(ACCOUNTS_DIR, accountDirName).getPath()));
		return true;
	}
	
	public static void setNativeLanguage(Account account, Language newNativeLanguage) {
		changeNativeLanguageOfInfoFile(account, newNativeLanguage);
		account.setNativeLanguage(newNativeLanguage);
	}
	
	public static boolean setVocabDir(Account account, String vocabDir) {
		File newVocabDir = new File(vocabDir);
		boolean dirNowExists = createDirIfNecessary(newVocabDir);
		if (!dirNowExists) {
			return false;
		}
		changeVocabDirOfInfoFile(account, newVocabDir);
		account.setVocabDirectory(vocabDir);
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
		File newVocabFile = new File(account.getVocabDirectory(), newLanguage.getName() + "_Words.csv");
		newVocabFile.createNewFile();
		File newLanguageSettingsFile = new File(account.getVocabDirectory(), newLanguage.getName() + "_Settings.csv");
		Language nativeLanguage = account.getNativeLanguage();
		try (BufferedWriter settingsWriter = Files.newBufferedWriter(newLanguageSettingsFile.toPath())) {
			settingsWriter.write(
					"FLTRLANGPREFS\n" +
					"charSubstitutions\t´='|`='|’='|‘='|′='|‵='\n" +
					"wordCharRegExp\t" + newLanguage.getWordCharRegExp().replace("\\\\-", "\\-") + "\n" +
					"makeCharacterWord\t0\n" +
					"removeSpaces\t0\n" +
					"rightToLeft\t0\n" +
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
			if (isValidAccountName(accountName)) {
				accounts.add(new Account(accountName, accountDirName, nativeLanguage, vocabDir));
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
	
	private static String readLineFromInfoFile(String accountDirName, int lineNumber) {
		File accountInfoFile = new File(ACCOUNTS_DIR, accountDirName + "/info.txt");
		if (accountInfoFile.exists() && accountInfoFile.isFile()) {
			try {
				return Files.readAllLines(accountInfoFile.toPath()).get(lineNumber);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private static void createDefaultLanguageFile(Language defaultLanguage) {
		try {
			Files.write(DEFAULT_LANGUAGE_FILE.toPath(), defaultLanguage.getName().getBytes());
		} catch (IOException e) {
			System.err.println("Could not create defaultLanguage.txt for this account. Terminating program.");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private static Language readDefaultLanguage() {
		if (DEFAULT_LANGUAGE_FILE.exists() && DEFAULT_LANGUAGE_FILE.isFile()) {
			try {
				return getLanguage(Files.readAllLines(DEFAULT_LANGUAGE_FILE.toPath()).get(0));
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
			System.out.println("You already have the maximum number of accounts. You must delete one if you wish to create a new one.");
			return false;
		} else if (accountNames.contains(accountName)) {
			System.out.println("There is already an account with this name. Please choose another name.");
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
	
	private static boolean setUpAccountDir(String accountName, Language accountNativeLanguage, String accountDirName) {
		File newAccountDir = new File(ACCOUNTS_DIR, accountDirName);
		boolean dirCreationSuccessful = createAccountDir(newAccountDir);
		if (!dirCreationSuccessful) {
			return false;
		}
		writeInfoFile(accountName, accountNativeLanguage, newAccountDir);
		return true;
	}
	
	private static boolean createAccountDir(File newAccountDir) {
		boolean dirCreationSuccessful = newAccountDir.mkdir();
		if (!dirCreationSuccessful) {
			System.out.println(
					"For an unknown reason, the directory '" + newAccountDir.getPath() + "' could not be created. " +
					"Try checking that the program has permission to write to '" + ACCOUNTS_DIR.getAbsolutePath() + "'."
			);
			return false;
		}
		return true;
	}
	
	private static void writeInfoFile(String accountName, Language accountNativeLanguage, File newAccountDir) {
		File infoFile = new File(newAccountDir, "info.txt");
		try {
			String fileContents = accountName + "\n" + accountNativeLanguage.getName() + "\n" + newAccountDir.getPath();
			Files.write(infoFile.toPath(), fileContents.getBytes());
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
			System.out.println(
					"For an unknown reason, the directory '" + dir.getPath() + "' could not be created. " +
							"Try checking that the program has permission to write to '" + dir.getAbsolutePath() + "'."
			);
			return false;
		}
		return true;
	}
	
	private static void changeNativeLanguageOfInfoFile(Account account, Language newNativeLanguage) {
		File infoFile = new File(new File(ACCOUNTS_DIR, account.getDirectoryName()), "info.txt");
		try {
			String fileContents = account.getName() + "\n" + newNativeLanguage.getName() + "\n" + account.getVocabDirectory();
			Files.write(infoFile.toPath(), fileContents.getBytes());
		} catch (IOException e) {
			System.err.println("Could not write info.txt for this account. Terminating program.");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private static void changeVocabDirOfInfoFile(Account account, File newVocabDir) {
		File infoFile = new File(new File(ACCOUNTS_DIR, account.getDirectoryName()), "info.txt");
		try {
			String fileContents = account.getName() + "\n" + account.getNativeLanguage().getName() + "\n" + newVocabDir;
			Files.write(infoFile.toPath(), fileContents.getBytes());
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
