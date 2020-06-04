import java.util.ArrayList;
import java.util.List;

public class AccountMainMenu extends Menu {
	private Account account;
	private Menu previousMenu;
	
	public AccountMainMenu(Account account, Menu previousMenu) {
		this.account = account;
		this.previousMenu = previousMenu;
	}
	
	@Override
	void run() {
		System.out.println("\n" + account.getName() + "'s account:");
		List<String> options = new ArrayList<>();
		options.add("Practice a language");
		options.add("Add a new language");
		options.add("Account settings");
		String userChoice = giveUserAChoice(options);
		if (userChoice.toLowerCase().equals("exit")) {
			System.exit(0);
		} else if (userChoice.toLowerCase().equals("back")) {
			previousMenu.run();
		} else if (userChoice.equals("1")) {
			new LanguageSelectionMenu(account, this).run();
		} else if (userChoice.equals("2")) {
			new AddPracticeLanguageMenu(account, this, this).run();
		} else {
			new AccountSettingsMenu(account, this).run();
		}
	}
}
