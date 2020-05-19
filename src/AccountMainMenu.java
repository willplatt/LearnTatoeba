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
		List<String> options = new ArrayList<>();
		options.add("My courses");
		options.add("Add new course");
		options.add("Account settings");
		String userChoice = giveUserAChoice(options);
		if (userChoice.toLowerCase().equals("exit")) {
			System.exit(0);
		} else if (userChoice.toLowerCase().equals("back")) {
			previousMenu.run();
		} else if (userChoice.equals("1")) {
			// TODO: implement course broswer
		} else if (userChoice.equals("2")) {
			// TODO: implement adding new courses
		} else {
			// TODO: implement account settings
		}
	}
}
