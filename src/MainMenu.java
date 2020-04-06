import java.util.ArrayList;
import java.util.List;

public class MainMenu extends Menu {
	@Override
	public void run() {
		int numberOfAccounts = AccountManager.getNumberOfAccounts();
		if (numberOfAccounts > 0) {
			System.out.println("Choose an account to practice with:");
		} else {
			System.out.println("No accounts could be found on this machine.");
		}
		List<List<String>> finalOptions = new ArrayList<>(1);
		List<String> createAccountOption = new ArrayList<>(1);
		createAccountOption.add("*");
		createAccountOption.add("Create a new account");
		finalOptions.add(createAccountOption);
		String userChoice = giveUserAChoice(AccountManager.getAccountNames(), finalOptions);
		if (userChoice.toLowerCase().equals("exit")) {
			System.exit(0);
		} else if (userChoice.toLowerCase().equals("back")) {
			System.out.println("You cannot go back from here.");
			run();
		} else if (userChoice.equals("*")) {
			new CreateAccountMenu(this).run();
		} else {
			System.out.println("This path is under development."); // TODO: Implement account selection
		}
	}
}
