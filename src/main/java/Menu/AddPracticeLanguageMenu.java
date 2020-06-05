package Menu;

import Account.Account;
import Account.AccountManager;

import java.io.IOException;

public class AddPracticeLanguageMenu extends Menu {
	private Account account;
	private String newPracticeLanguage;
	private Menu nextMenu;
	
	public AddPracticeLanguageMenu(Account account, String newPracticeLanguage, Menu nextMenu) {
		this.account = account;
		this.newPracticeLanguage = newPracticeLanguage;
		this.nextMenu = nextMenu;
	}
	
	@Override
	public void run() {
		try {
			AccountManager.addPracticeLanguageToAccount(account, newPracticeLanguage);
			System.out.println("You can now practice " + newPracticeLanguage + "!");
			nextMenu.run();
		} catch (IOException e) {
			System.out.println("Something went wrong:");
			e.printStackTrace();
			System.out.println("Terminating.");
			System.exit(0);
		}
	}
}
