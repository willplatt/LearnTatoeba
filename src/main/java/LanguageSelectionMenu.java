import java.util.List;

public class LanguageSelectionMenu extends Menu {
	private Account account;
	private Menu previousMenu;
	
	public LanguageSelectionMenu(Account account, Menu previousMenu) {
		this.account = account;
		this.previousMenu = previousMenu;
	}
	
	@Override
	void run() {
		List<String> languages = AccountManager.getLanguagesForAccount(account);
		if (languages.isEmpty()) {
			System.out.println("You don't haven't added any languages to practice with yet!");
			previousMenu.run();
		} else {
			System.out.println("\nChoose a language to practice:");
			String userChoice = giveUserAChoice(languages);
			if (userChoice.toLowerCase().equals("back")) {
				previousMenu.run();
			} else {
				int languageIndex = Integer.parseInt(userChoice) - 1;
				String language = languages.get(languageIndex);
				PracticeMenu practiceMenu = new PracticeMenu(account, language, this);
				IfNecessaryDownloadSentencesMenu ifNecessaryDownloadNativeSentencesMenu = new IfNecessaryDownloadSentencesMenu(
						account.getNativeLanguage(), this, practiceMenu
				);
				new IfNecessaryDownloadSentencesMenu(language, this, ifNecessaryDownloadNativeSentencesMenu).run();
			}
		}
	}
}
