package learntatoeba;

import learntatoeba.account.Account;
import learntatoeba.account.AccountMainMenu;
import learntatoeba.account.AccountManager;
import learntatoeba.account.NameNewAccountMenu;

import java.util.List;

public class MainMenu extends Menu {
	@Override
	public void run() {
		int numberOfAccounts = AccountManager.getNumberOfAccounts();
		if (numberOfAccounts > 0) {
			Terminal.println("\nChoose an account to practice with:");
		} else {
			Terminal.println("\nNo accounts could be found on this machine.");
		}
		List<String> accountNames = AccountManager.getAccountNames();
		List<List<String>> finalOptions = List.of(List.of("*", "Create a new account"));
		giveUserAChoice(accountNames, finalOptions,
				() -> {
					Terminal.println("You cannot go back from here.");
					run();
				},
				userChoice -> {
					if (userChoice.equals("*")) {
						new NameNewAccountMenu(this).run();
					} else {
						int accountIndex = Integer.parseInt(userChoice) - 1;
						String accountName = accountNames.get(accountIndex);
						Terminal.println("Opening account " + accountName + ".");
						Account account = AccountManager.getAccountFromName(accountName);
						new AccountMainMenu(account, this).run();
					}
				}
		);
	}
}
