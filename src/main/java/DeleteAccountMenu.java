import java.io.File;
import java.io.IOException;
import java.util.List;

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
	void run() {
		File accountDir = new File(AccountManager.ACCOUNTS_DIR, account.getDirectoryName());
		File vocabDir = new File(account.getVocabDirectory());
		if (accountDir.equals(vocabDir)) {
			System.out.println("\nThis account's vocab directory is \"" + vocabDir + "\"; which is also the account directory. If you continue the vocab data will be deleted.");
		} else {
			System.out.println("\nThis account's vocab directory is \"" + vocabDir + "\" and will not be affected by deleting the account.");
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
			System.out.println("Account successfully deleted.");
		} catch (IOException e) {
			System.out.println("Something went wrong trying to delete the account directory:");
			e.printStackTrace();
		}
	}
}
