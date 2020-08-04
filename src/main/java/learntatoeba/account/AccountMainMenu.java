package learntatoeba.account;

import learntatoeba.account.settings.AccountSettingsMenu;
import learntatoeba.account.addlanguage.AskForNewPracticeLanguageMenu;
import learntatoeba.account.practice.LanguageSelectionMenu;
import learntatoeba.Menu;
import learntatoeba.Terminal;

import java.util.List;

public class AccountMainMenu extends Menu {
	private Account account;
	private Menu previousMenu;
	
	public AccountMainMenu(Account account, Menu previousMenu) {
		this.account = account;
		this.previousMenu = previousMenu;
	}
	
	@Override
	public void run() {
		Terminal.println("\n" + account.getName() + "'s account:");
		List<String> options = List.of("Practice a language", "Add a new language", "Account settings");
		giveUserAChoice(options,
				previousMenu::run,
				userChoice -> {
					if (userChoice.equals("1")) {
						new LanguageSelectionMenu(account, this).run();
					} else if (userChoice.equals("2")) {
						new AskForNewPracticeLanguageMenu(account, this, this).run();
					} else {
						new AccountSettingsMenu(account, this).run();
					}
				}
		);
	}
}
