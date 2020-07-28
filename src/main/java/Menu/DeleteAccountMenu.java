package Menu;

import Account.Account;
import Account.AccountManager;
import Terminal.Terminal;

import java.io.File;
import java.io.IOException;

public class DeleteAccountMenu extends Menu {
	private Account account;
	private Menu previousMenu;
	private Menu nextMenu;
	
	public DeleteAccountMenu(Account account, Menu previousMenu, Menu nextMenu) {
		this.account = account;
		this.previousMenu = previousMenu;
		this.nextMenu = nextMenu;
	}
	
	@Override
	public void run() {
		File accountDir = new File(AccountManager.ACCOUNTS_DIR, account.getDirectoryName());
		File vocabDir = new File(account.getVocabDirectory());
		if (accountDir.equals(vocabDir)) {
			Terminal.println("\nThis account's vocab directory is \"" + vocabDir + "\"; which is also the account directory. If you continue the vocab data will be deleted.");
		} else {
			Terminal.println("\nThis account's vocab directory is \"" + vocabDir + "\" and will not be affected by deleting the account.");
		}
		askUserAYesNoQuestion("Are you sure you would like to delete this account?",
				previousMenu::run,
				() -> {
					tryToDeleteAccount();
					nextMenu.run();
				}
		);
	}
	
	private void tryToDeleteAccount() {
		try {
			AccountManager.deleteAccount(account);
			Terminal.println("Account successfully deleted.");
		} catch (IOException e) {
			Terminal.println("Something went wrong trying to delete the account directory:");
			e.printStackTrace();
		}
	}
}