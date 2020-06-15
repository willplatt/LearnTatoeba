package Menu;

import Account.Account;
import Account.AccountManager;
import Language.Language;
import Terminal.Terminal;

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
