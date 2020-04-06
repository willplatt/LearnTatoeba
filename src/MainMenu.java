import java.util.ArrayList;
import java.util.List;

public class MainMenu extends Menu {
	@Override
	public void run() {
		int numberOfAccounts = AccountManager.getNumberOfAccounts();
		if (numberOfAccounts > 0) {
			System.out.println("Choose an account to practice with:");
			List<List<String>> finalOptions = new ArrayList<>(1);
			List<String> createAccountOption = new ArrayList<>(1);
			createAccountOption.add("*");
			createAccountOption.add("Create a new account");
			finalOptions.add(createAccountOption);
			String userChoice = giveUserAChoice(AccountManager.getAccountNames(), finalOptions);
			if (userChoice.equals("*")) {
				new CreateAccountMenu().run();
			} else {
				System.out.println("This path is under development."); // TODO: Implement account selection
			}
		} else {
			System.out.println("No accounts could be found on this machine.");
			List<String> numberedOptions = new ArrayList<>(1);
			numberedOptions.add("Create a new account");
			List<List<String>> finalOptions = new ArrayList<>(1);
			List<String> exitOption = new ArrayList<>(1);
			exitOption.add("*");
			exitOption.add("Exit");
			finalOptions.add(exitOption);
			String userChoice = giveUserAChoice(numberedOptions, finalOptions);
			if (userChoice.equals("1")) {
				new CreateAccountMenu().run();
			}
		}
	}
}
