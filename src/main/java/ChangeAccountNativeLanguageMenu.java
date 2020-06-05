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
		} else if (List.of("yes", "y").contains(continueChanging)) {
			getAndSetNewLanguage();
		} else {
			System.out.println("Please type yes or no.");
			run();
		}
	}
	
	private void getAndSetNewLanguage() {
		String newNativeLanguage = askUserAQuestion("Specify the new native language for this account:");
		if (newNativeLanguage.toLowerCase().equals("exit")) {
			System.exit(0);
		} else if (newNativeLanguage.toLowerCase().equals("back")) {
			previousMenu.run();
		} else {
			String canonicalLanguageName = LanguageCodeHandler.getCanonicalName(newNativeLanguage);
			if (canonicalLanguageName == null) {
				System.out.println("That language is not recognised. Make sure you typed it correctly.");
				getAndSetNewLanguage();
			} else {
				SetNativeLanguageMenu setLanguageMenu = new SetNativeLanguageMenu(account, canonicalLanguageName, nextMenu);
				new IfNecessaryDownloadSentencesMenu(canonicalLanguageName, this, setLanguageMenu).run();
			}
		}
	}
}
