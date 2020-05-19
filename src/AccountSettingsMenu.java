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
		System.out.println("Modify account:");
		List<String> options = new ArrayList<>();
		options.add("Change vocab directory");
		options.add("Delete account");
		String userChoice = giveUserAChoice(options);
		if (userChoice.toLowerCase().equals("exit")) {
			System.exit(0);
		} else if (userChoice.toLowerCase().equals("back")) {
			previousMenu.run();
		} else if (userChoice.equals("1")) {
			new ChangeAccountVocabDirMenu(account, this).run();
		} else {
			// TODO: implement account deletion
			new MainMenu().run();
		}
	}
}
