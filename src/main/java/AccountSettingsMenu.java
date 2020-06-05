import java.util.ArrayList;
import java.util.List;

public class AccountSettingsMenu extends Menu {
	private Account account;
	private Menu previousMenu;
	
	public AccountSettingsMenu(Account account, Menu previousMenu) {
		this.account = account;
		this.previousMenu = previousMenu;
	}
	
	@Override
	void run() {
		System.out.println("\nModify account:");
		List<String> options = new ArrayList<>();
		options.add("Change native language");
		options.add("Change vocab directory");
		options.add("Delete account");
		String userChoice = giveUserAChoice(options);
		if (userChoice.toLowerCase().equals("back")) {
			previousMenu.run();
		} else if (userChoice.equals("1")) {
			new ChangeAccountNativeLanguageMenu(account, this, this).run();
		} else if (userChoice.equals("2")) {
			new ChangeAccountVocabDirMenu(account, this, this).run();
		} else {
			new DeleteAccountMenu(account, this, new MainMenu()).run();
		}
	}
}
