package Menu;

import Account.Account;
import Account.AccountManager;

public class ChangeAccountVocabDirMenu extends Menu {
	private Account account;
	private Menu previousMenu;
	private Menu nextMenu;
	
	public ChangeAccountVocabDirMenu(Account account, Menu previousMenu, Menu nextMenu) {
		this.account = account;
		this.previousMenu = previousMenu;
		this.nextMenu = nextMenu;
	}
	
	@Override
	public void run() {
		askUserAYesNoQuestion("\nThe current vocab directory for this account is '" + account.getVocabDirectory() + "'. Would you like to change it?",
				nextMenu::run,
				this::askUserToChangeVocabDir
		);
	}
	
	private void askUserToChangeVocabDir() {
		askUserAQuestion("Specify the new directory for storing this account's vocabulary:",
				previousMenu::run,
				newVocabDir -> {
					boolean setDirSuccessful = AccountManager.setVocabDir(account, newVocabDir);
					if (setDirSuccessful) {
						System.out.println("Your account's vocab directory has been changed!");
						nextMenu.run();
					} else {
						System.out.println("Unfortunately, that directory could not be accessed or created. Please try again.");
						run();
					}
				}
		);
	}
}
