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
		askUserAYesNoQuestion("\nThe current native language of this account is '" + account.getNativeLanguage() + "'. Would you like to change it?",
				previousMenu::run,
				nextMenu::run,
				this::getAndSetNewLanguage
		);
	}
	
	private void getAndSetNewLanguage() {
		askUserAQuestion("Specify the new native language for this account:",
				previousMenu::run,
				newNativeLanguage -> {
					String canonicalLanguageName = LanguageCodeHandler.getCanonicalName(newNativeLanguage);
					if (canonicalLanguageName == null) {
						System.out.println("That language is not recognised. Make sure you typed it correctly.");
						getAndSetNewLanguage();
					} else {
						SetNativeLanguageMenu setLanguageMenu = new SetNativeLanguageMenu(account, canonicalLanguageName, nextMenu);
						new IfNecessaryDownloadSentencesMenu(canonicalLanguageName, this, setLanguageMenu).run();
					}
				}
		);
	}
}
