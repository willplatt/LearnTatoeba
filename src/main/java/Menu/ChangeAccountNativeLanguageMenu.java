package Menu;

import Account.Account;
import Language.Language;

import java.io.IOException;

import static Language.LanguageManager.getLanguage;

public class ChangeAccountNativeLanguageMenu extends Menu {
	private Account account;
	private Menu previousMenu;
	private Menu nextMenu;
	
	public ChangeAccountNativeLanguageMenu(Account account, Menu previousMenu, Menu nextMenu) {
		this.account = account;
		this.previousMenu = previousMenu;
		this.nextMenu = nextMenu;
	}
	
	@Override
	public void run() {
		askUserAYesNoQuestion("\nThe current native language of this account is '" + account.getNativeLanguage().getName() + "'. Would you like to change it?",
				previousMenu::run,
				nextMenu::run,
				this::getAndSetNewLanguage
		);
	}
	
	private void getAndSetNewLanguage() {
		askUserAQuestion("Specify the new native language for this account:",
				previousMenu::run,
				languageName -> {
					try {
						Language newNativeLanguage = getLanguage(languageName);
						SetNativeLanguageMenu setLanguageMenu = new SetNativeLanguageMenu(account, newNativeLanguage, nextMenu);
						new IfNecessaryDownloadSentencesMenu(newNativeLanguage, this, setLanguageMenu).run();
					} catch (IllegalArgumentException e) {
						System.err.println(e.getMessage());
						getAndSetNewLanguage();
					} catch (IOException e) {
						e.printStackTrace();
						previousMenu.run();
					}
				}
		);
	}
}
