package learntatoeba.account;

import learntatoeba.account.settings.ChangeAccountNativeLanguageMenu;
import learntatoeba.account.settings.ChangeAccountVocabDirMenu;
import learntatoeba.MainMenu;
import learntatoeba.Menu;
import learntatoeba.Terminal;

public class NameNewAccountMenu extends Menu {
	private final Menu previousMenu;
	
	public NameNewAccountMenu(Menu previousMenu) {
		this.previousMenu = previousMenu;
	}
	
	@Override
	public void run() {
		Terminal.println("\nOkay, let's create a new account.");
		askUserAQuestion("What do you want to name it?",
				previousMenu::run,
				newAccountName -> {
					boolean accountCreationSuccessful = AccountManager.createNewAccount(newAccountName);
					if (accountCreationSuccessful) {
						Terminal.println("Your new account has been created!");
						Account newAccount = AccountManager.getAccountFromName(newAccountName);
						MainMenu mainMenu = new MainMenu();
						Menu menuAfterSettingNativeLanguage = new ChangeAccountVocabDirMenu(newAccount, mainMenu, mainMenu);
						new ChangeAccountNativeLanguageMenu(newAccount, mainMenu, menuAfterSettingNativeLanguage).run();
					} else {
						Terminal.println("Something went wrong trying to create your new account. Returning to the main menu.");
						new MainMenu().run();
					}
				}
		);
	}
}
