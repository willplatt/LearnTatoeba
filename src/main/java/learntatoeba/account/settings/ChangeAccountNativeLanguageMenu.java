package learntatoeba.account.settings;

import learntatoeba.account.Account;
import learntatoeba.account.IfNecessaryDownloadSentencesMenu;
import learntatoeba.Menu;
import learntatoeba.language.Language;

import java.io.IOException;

import static learntatoeba.language.LanguageManager.getLanguage;

public class ChangeAccountNativeLanguageMenu extends Menu {
	private final Account account;
	private final Menu previousMenu;
	private final Menu nextMenu;
	
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
