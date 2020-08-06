package learntatoeba.account.settings;

import learntatoeba.account.Account;
import learntatoeba.account.AccountManager;
import learntatoeba.Menu;
import learntatoeba.language.Language;
import learntatoeba.Terminal;

public class SetNativeLanguageMenu extends Menu {
	private final Account account;
	private final Language newNativeLanguage;
	private final Menu nextMenu;
	
	public SetNativeLanguageMenu(Account account, Language newNativeLanguage, Menu nextMenu) {
		this.account = account;
		this.newNativeLanguage = newNativeLanguage;
		this.nextMenu = nextMenu;
	}
	
	@Override
	public void run() {
		AccountManager.setNativeLanguage(account, newNativeLanguage);
		Terminal.println("Your account's native language has been changed to " + newNativeLanguage.getName() + ".");
		nextMenu.run();
	}
}
