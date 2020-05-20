import java.util.List;

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
	void run() {
		String continueChanging = askUserAQuestion("\nThe current vocab directory for this account is '" + account.getVocabDirectory() + "'. Would you like to change it?");
		if (continueChanging.toLowerCase().equals("exit")) {
			System.exit(0);
		} else if (List.of("back", "no", "n").contains(continueChanging.toLowerCase())) {
			nextMenu.run();
		} else if (!List.of("yes", "y").contains(continueChanging.toLowerCase())) {
			run();
		} else {
			String newVocabDir = askUserAQuestion("Specify the new directory for storing this account's vocabulary:");
			if (newVocabDir.toLowerCase().equals("exit")) {
				System.exit(0);
			} else if (newVocabDir.toLowerCase().equals("back")) {
				previousMenu.run();
			} else {
				boolean setDirSuccessful = AccountManager.setVocabDir(account, newVocabDir);
				if (setDirSuccessful) {
					System.out.println("Your account's vocab directory has been changed!");
					nextMenu.run();
				} else {
					System.out.println("Unfortunately, that directory could not be accessed or created. Please try again.");
					run();
				}
			}
		}
	}
}
