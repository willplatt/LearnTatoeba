import java.util.ArrayList;
import java.util.List;

public class AccountManager {
	private static List<Account> accounts;
	
	public static void loadAccounts() {
		accounts = new ArrayList<>();
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
		return false;
	}
}
