package learntatoeba.account.settings;

import learntatoeba.account.Account;
import learntatoeba.MainMenu;
import learntatoeba.Menu;
import learntatoeba.Terminal;

import java.util.List;

public class AccountSettingsMenu extends Menu {
	private final Account account;
	private final Menu previousMenu;
	
	public AccountSettingsMenu(Account account, Menu previousMenu) {
		this.account = account;
		this.previousMenu = previousMenu;
	}
	
	@Override
	public void run() {
		Terminal.println("\nModify account:");
		List<String> options = List.of(
				"Change native language (" + account.getNativeLanguage().getName() + ")",
				"Change vocab directory (" + account.getVocabDirectory() + ")",
				"Change autoblacklist duration (" + account.getAutoblacklistDuration().toPrintString() + ")",
				"Change sentence recurrence probability (" + account.getRecurrenceProbability() + ")",
				"Change session length (" + account.getSessionLength() + ")",
				"Delete account"
		);
		giveUserAChoice(options,
				previousMenu::run,
				userChoice -> {
					if (userChoice.equals("1")) {
						new ChangeAccountNativeLanguageMenu(account, this, this).run();
					} else if (userChoice.equals("2")) {
						new ChangeAccountVocabDirMenu(account, this, this).run();
					} else if (userChoice.equals("3")) {
						new ChangeAccountAutoblacklistMenu(account, this, this).run();
					} else if (userChoice.equals("4")) {
						new ChangeAccountRecurrenceProbabilityMenu(account, this, this).run();
					} else if (userChoice.equals("5")) {
						new ChangeAccountSessionLengthMenu(account, this, this).run();
					} else {
						new DeleteAccountMenu(account, this, new MainMenu()).run();
					}
				}
		);
	}
}
