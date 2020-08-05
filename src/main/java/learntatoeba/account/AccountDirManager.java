package learntatoeba.account;

import learntatoeba.fileutil.DirCreator;
import learntatoeba.language.Language;
import learntatoeba.Terminal;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static learntatoeba.Constants.*;
import static learntatoeba.account.settings.AccountSettingsFileManager.updateSettingsFile;
import static learntatoeba.language.LanguageManager.getLanguage;
import static java.nio.charset.StandardCharsets.UTF_8;

public class AccountDirManager {
	public static final File ACCOUNTS_DIR = new File(INSTALL_DIR, "accounts");
	
	public static void createAccountsDirIfNecessary() {
		boolean dirNowExists = DirCreator.createDirIfNecessary(ACCOUNTS_DIR);
		if (!dirNowExists) {
			System.err.println("Directory \"" + ACCOUNTS_DIR + "\" could not be found or created. Terminating program.");
			System.exit(0);
		}
	}
	
	public static String[] getAccountDirNames() {
		String[] accountDirNames = ACCOUNTS_DIR.list(DIRECTORY_FILTER);
		assert(accountDirNames != null);
		return accountDirNames;
	}
	
	public static String generateUniqueDirectoryName(String accountName) {
		String accountDirName = getShortAlphaNumericFromAccountName(accountName);
		accountDirName = appendSuffixToDirNameIfNecessary(accountDirName);
		return accountDirName;
	}
	
	public static boolean setUpAccountDir(Account account) {
		File newAccountDir = getDir(account);
		boolean dirCreationSuccessful = createAccountDir(newAccountDir);
		if (!dirCreationSuccessful) {
			return false;
		}
		updateSettingsFile(account);
		return true;
	}
	
	public static String getPath(String accountDirName) {
		return getDir(accountDirName).getPath();
	}
	
	public static File getDir(Account account) {
		return getDir(account.getDirectoryName());
	}
	
	public static File getDir(String accountDirName) {
		return new File(ACCOUNTS_DIR, accountDirName);
	}
	
	public static List<Language> readLanguagesForAccount(Account account) throws IOException {
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
	
	public static void createLanguageFilesForAccount(Account account, Language newLanguage) throws IOException {
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
	
	public static void deleteDirForAccount(Account account) throws IOException {
		FileUtils.deleteDirectory(getDir(account));
	}
	
	public static boolean backupAccountsDir(File target) {
		if (target.exists()) {
			System.err.println("Could not backup accounts directory to \"" + target + "\" because this file/directory already exists.");
			return false;
		}
		try {
			FileUtils.copyDirectory(ACCOUNTS_DIR, target);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	public static boolean restoreBackupAccountsDir(File backupDir) {
		try {
			FileUtils.deleteDirectory(ACCOUNTS_DIR);
			FileUtils.moveDirectory(backupDir, ACCOUNTS_DIR);
		} catch (IOException e) {
			return false;
		}
		return true;
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
	
	private static boolean createAccountDir(File newAccountDir) {
		boolean dirCreationSuccessful = newAccountDir.mkdir();
		if (!dirCreationSuccessful) {
			Terminal.println("For an unknown reason, the directory '" + newAccountDir.getPath() + "' could not be created. Try checking that the program has permission to write to '" + ACCOUNTS_DIR.getAbsolutePath() + "'.");
			return false;
		}
		return true;
	}
	
	private static String getGoogleTranslateCode(Language language) {
		String twoCharCode = language.getTwoCharCode();
		return twoCharCode.equals("") ? language.getTatoebaCode() : twoCharCode;
	}
}
