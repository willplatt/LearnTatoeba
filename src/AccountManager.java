import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class AccountManager {
	private static final FilenameFilter DIRECTORY_FILTER = (current, name) -> new File(current, name).isDirectory();
	private static final File ACCOUNTS_DIR = new File("accounts");
	
	private static List<Account> accounts;
	
	public static void loadAccounts() {
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
		boolean setUpAccountDirSuccessful = setUpAccountDir(accountName, accountDirName);
		if (!setUpAccountDirSuccessful) {
			return false;
		}
		accounts.add(new Account(accountName, accountDirName));
		return true;
	}
	
	public static boolean setVocabDir(Account account, String vocabDir) {
		System.err.println(vocabDir);
		File newVocabDir = new File(vocabDir);
		boolean dirNowExists = createDirIfNecessary(newVocabDir);
		if (!dirNowExists) {
			return false;
		}
		changeVocabDirOfInfoFile(account, newVocabDir);
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
	
	private static void createAccountsDirIfNecessary() {
		if (!ACCOUNTS_DIR.exists() || !ACCOUNTS_DIR.isDirectory()) {
			boolean dirCreationSuccessful = ACCOUNTS_DIR.mkdir();
			if (!dirCreationSuccessful) {
				System.err.println("Could not create account directory. Terminating program.");
				System.exit(0);
			}
		}
	}
	
	private static void loadAccountsFromSubDirs() {
		String[] accountDirNames = ACCOUNTS_DIR.list(DIRECTORY_FILTER);
		assert(accountDirNames != null);
		accounts = new ArrayList<>(accountDirNames.length);
		for (String accountDirName : accountDirNames) {
			String accountName = readAccountName(accountDirName);
			if (isValidAccountName(accountName)) {
				accounts.add(new Account(accountName, accountDirName));
			}
		}
	}
	
	private static String readAccountName(String accountDirName) {
		File accountInfoFile = new File(ACCOUNTS_DIR, accountDirName + "/info.txt");
		if (accountInfoFile.exists() && accountInfoFile.isFile()) {
			try {
				return Files.readAllLines(accountInfoFile.toPath()).get(0);
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
	
	private static boolean setUpAccountDir(String accountName, String accountDirName) {
		File newAccountDir = new File(ACCOUNTS_DIR, accountDirName);
		boolean dirCreationSuccessful = createAccountDir(newAccountDir);
		if (!dirCreationSuccessful) {
			return false;
		}
		writeInfoFile(accountName, newAccountDir);
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
	
	private static void writeInfoFile(String accountName, File newAccountDir) {
		File infoFile = new File(newAccountDir, "info.txt");
		try {
			String fileContents = accountName + "\n" + newAccountDir.getPath();
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
	
	private static void changeVocabDirOfInfoFile(Account account, File newVocabDir) {
		File infoFile = new File(new File(ACCOUNTS_DIR, account.getDirectoryName()), "info.txt");
		try {
			String fileContents = account.getName() + "\n" + newVocabDir;
			Files.write(infoFile.toPath(), fileContents.getBytes());
		} catch (IOException e) {
			System.err.println("Could not write info.txt for this account. Terminating program.");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
