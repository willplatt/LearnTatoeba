import java.util.ArrayList;
import java.util.List;

public class MainMenu extends Menu {
	@Override
	public void run() {
		int numberOfAccounts = AccountManager.getNumberOfAccounts();
		if (numberOfAccounts > 0) {
			System.out.println("\nChoose an account to practice with:");
		} else {
			System.out.println("\nNo accounts could be found on this machine.");
		}
		List<List<String>> finalOptions = new ArrayList<>(1);
		List<String> createAccountOption = new ArrayList<>(1);
		createAccountOption.add("*");
		createAccountOption.add("Create a new account");
		finalOptions.add(createAccountOption);
		List<String> accountNames = AccountManager.getAccountNames();
		String userChoice = giveUserAChoice(accountNames, finalOptions);
		if (userChoice.toLowerCase().equals("back")) {
			System.out.println("You cannot go back from here.");
			run();
		} else if (userChoice.equals("*")) {
			new NameNewAccountMenu(this).run();
		} else {
			int accountIndex = Integer.parseInt(userChoice) - 1;
			String accountName = accountNames.get(accountIndex);
			System.out.println("Opening account " + accountName + ".");
			Account account = AccountManager.getAccountFromName(accountName);
			new AccountMainMenu(account, this).run();
		}
	}
}
