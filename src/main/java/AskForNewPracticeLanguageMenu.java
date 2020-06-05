public class AskForNewPracticeLanguageMenu extends Menu {
	private Account account;
	private Menu previousMenu;
	private Menu nextMenu;
	
	public AskForNewPracticeLanguageMenu(Account account, Menu previousMenu, Menu nextMenu) {
		this.account = account;
		this.previousMenu = previousMenu;
		this.nextMenu = nextMenu;
	}
	
	@Override
	void run() {
		String newLanguage = askUserAQuestion("\nSpecify the new language you would like to practice:");
		if (newLanguage.toLowerCase().equals("back")) {
			previousMenu.run();
		} else {
			String canonicalLanguageName = LanguageCodeHandler.getCanonicalName(newLanguage);
			if (canonicalLanguageName == null) {
				System.out.println("That language is not recognised. Make sure you typed it correctly.");
				run();
			} else {
				AddPracticeLanguageMenu addPracticeLanguageMenu = new AddPracticeLanguageMenu(account, canonicalLanguageName, nextMenu);
				new IfNecessaryDownloadSentencesMenu(canonicalLanguageName, this, addPracticeLanguageMenu).run();
			}
		}
	}
}
