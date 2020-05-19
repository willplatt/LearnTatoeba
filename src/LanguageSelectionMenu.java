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
		System.out.println("Choose a language to practice:");
		List<String> languages = AccountManager.getLanguagesForAccount(account);
		String userChoice = giveUserAChoice(languages);
		if (userChoice.toLowerCase().equals("exit")) {
			System.exit(0);
		} else if (userChoice.toLowerCase().equals("back")) {
			previousMenu.run();
		} else {
			int languageIndex = Integer.parseInt(userChoice) - 1;
			// TODO: implement language practice
		}
	}
}
