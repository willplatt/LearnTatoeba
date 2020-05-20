import java.util.List;

public class ChangeAccountNativeLanguageMenu extends Menu {
	private Account account;
	private Menu previousMenu;
	private Menu nextMenu;
	
	public ChangeAccountNativeLanguageMenu(Account account, Menu previousMenu, Menu nextMenu) {
		this.account = account;
		this.previousMenu = previousMenu;
		this.nextMenu = nextMenu;
	}
	
	@Override
	void run() {
		String continueChanging = askUserAQuestion("\nThe current native language of this account is '" + account.getNativeLanguage() + "'. Would you like to change it?");
		continueChanging = continueChanging.toLowerCase();
		if (continueChanging.equals("exit")) {
			System.exit(0);
		} else if (continueChanging.equals("back")) {
			previousMenu.run();
		} else if (List.of("no", "n").contains(continueChanging)) {
			nextMenu.run();
		} else if (!List.of("yes", "y").contains(continueChanging)) {
			run();
		} else {
			String newNativeLanguage = askUserAQuestion("Specify the new native language for this account:");
			if (newNativeLanguage.toLowerCase().equals("exit")) {
				System.exit(0);
			} else if (newNativeLanguage.toLowerCase().equals("back")) {
				previousMenu.run();
			} else {
				AccountManager.setNativeLanguage(account, newNativeLanguage);
				System.out.println("Your account's native language has been changed!");
				nextMenu.run();
			}
		}
	}
}
