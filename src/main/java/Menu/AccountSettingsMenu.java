package Menu;

import Account.Account;
import Terminal.Terminal;

import java.util.List;

public class AccountSettingsMenu extends Menu {
	private Account account;
	private Menu previousMenu;
	
	public AccountSettingsMenu(Account account, Menu previousMenu) {
		this.account = account;
		this.previousMenu = previousMenu;
	}
	
	@Override
	public void run() {
		Terminal.println("\nModify account:");
		List<String> options = List.of(
				"Change native language",
				"Change vocab directory",
				"Change autoblacklist duration",
				"Change sentence recurrence probability",
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
					} else {
						new DeleteAccountMenu(account, this, new MainMenu()).run();
					}
				}
		);
	}
}
