import java.util.ArrayList;
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
			System.out.println("Choose a language to practice:");
			String userChoice = giveUserAChoice(languages);
			if (userChoice.toLowerCase().equals("exit")) {
				System.exit(0);
			} else if (userChoice.toLowerCase().equals("back")) {
				previousMenu.run();
			} else {
				int languageIndex = Integer.parseInt(userChoice) - 1;
				new PracticeMenu(account, languages.get(languageIndex), this).run();
			}
		}
	}
}
