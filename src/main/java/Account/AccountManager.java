package Account;

import FileHandling.AccountDirManager;
import FileHandling.DirCreator;
import Language.BlacklistDuration;
import Language.Language;
import Terminal.Terminal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static Constants.Constants.*;
import static FileHandling.AccountSettingsFileManager.*;
import static FileHandling.DefaultsFileManager.getDefaultLanguage;

public class AccountManager {
	private static List<Account> accounts;
	
	public static void loadAccounts() throws IOException {
		AccountDirManager.createAccountsDirIfNecessary();
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
		String accountDirName = AccountDirManager.generateUniqueDirectoryName(accountName);
		String vocabDirPath = AccountDirManager.getPath(accountDirName);
		Account newAccount = new Account(accountName, accountDirName, getDefaultLanguage(), vocabDirPath, DEFAULT_AUTOBLACKLIST_DURATION, DEFAULT_RECURRENCE_PROBABILITY, DEFAULT_SESSION_LENGTH);
		boolean setUpAccountDirSuccessful = AccountDirManager.setUpAccountDir(newAccount);
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
		boolean dirNowExists = DirCreator.createDirIfNecessary(newVocabDir);
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
		return AccountDirManager.readLanguagesForAccount(account);
	}
	
	public static void addPracticeLanguageToAccount(Account account, Language newLanguage) throws IOException {
		AccountDirManager.createLanguageFilesForAccount(account, newLanguage);
	}
	
	public static void deleteAccount(Account account) throws IOException {
		AccountDirManager.deleteDirForAccount(account);
		accounts.remove(account);
	}
	
	private static void loadAccountsFromSubDirs() throws IOException {
		String[] accountDirNames = AccountDirManager.getAccountDirNames();
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
}
