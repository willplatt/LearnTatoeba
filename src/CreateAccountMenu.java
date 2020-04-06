public class CreateAccountMenu extends Menu {
	@Override
	void run() {
		System.out.println("Okay, let's create a new account.");
		String newAccountName = askUserAQuestion("What do you want to name it?");
		boolean accountCreationSuccessful = AccountManager.createNewAccount(newAccountName);
		if (accountCreationSuccessful) {
			System.out.println("Your new account has been created!");
		}
		new MainMenu().run();
	}
}
