package learntatoeba.account.settings;

import learntatoeba.account.Account;
import learntatoeba.account.AccountManager;
import learntatoeba.Menu;
import learntatoeba.language.Language;
import learntatoeba.Terminal;

public class SetNativeLanguageMenu extends Menu {
	private Account account;
	private Language newNativeLanguage;
	private Menu nextMenu;
	
	public SetNativeLanguageMenu(Account account, Language newNativeLanguage, Menu nextMenu) {
		this.account = account;
		this.newNativeLanguage = newNativeLanguage;
		this.nextMenu = nextMenu;
	}
	
	@Override
	public void run() {
		AccountManager.setNativeLanguage(account, newNativeLanguage);
		Terminal.println("Your account's native language has been changed!");
		nextMenu.run();
	}
}
