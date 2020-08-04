package learntatoeba.account.addlanguage;

import learntatoeba.account.Account;
import learntatoeba.account.AccountManager;
import learntatoeba.Menu;
import learntatoeba.language.Language;
import learntatoeba.Terminal;

import java.io.IOException;

public class AddPracticeLanguageMenu extends Menu {
	private Account account;
	private Language newPracticeLanguage;
	private Menu nextMenu;
	
	public AddPracticeLanguageMenu(Account account, Language newPracticeLanguage, Menu nextMenu) {
		this.account = account;
		this.newPracticeLanguage = newPracticeLanguage;
		this.nextMenu = nextMenu;
	}
	
	@Override
	public void run() {
		try {
			AccountManager.addPracticeLanguageToAccount(account, newPracticeLanguage);
			Terminal.println("You can now practice " + newPracticeLanguage.getName() + "!");
			nextMenu.run();
		} catch (IOException e) {
			Terminal.println("Something went wrong:");
			e.printStackTrace();
			Terminal.println("Terminating.");
			System.exit(0);
		}
	}
}
