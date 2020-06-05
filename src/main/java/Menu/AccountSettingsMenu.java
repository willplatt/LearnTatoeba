package Menu;

import Account.Account;

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
		System.out.println("\nModify account:");
		List<String> options = List.of("Change native language", "Change vocab directory", "Delete account");
		giveUserAChoice(options,
				previousMenu::run,
				userChoice -> {
					if (userChoice.equals("1")) {
						new ChangeAccountNativeLanguageMenu(account, this, this).run();
					} else if (userChoice.equals("2")) {
						new ChangeAccountVocabDirMenu(account, this, this).run();
					} else {
						new DeleteAccountMenu(account, this, new MainMenu()).run();
					}
				}
		);
	}
}
