public class CreateAccountMenu extends Menu {
	private Menu previousMenu;
	
	public CreateAccountMenu(Menu previousMenu) {
		this.previousMenu = previousMenu;
	}
	
	@Override
	void run() {
		System.out.println("Okay, let's create a new account.");
		String newAccountName = askUserAQuestion("What do you want to name it?");
		if (newAccountName.toLowerCase().equals("exit")) {
			System.exit(0);
		} else if (newAccountName.toLowerCase().equals("back")) {
			previousMenu.run();
		} else {
			boolean accountCreationSuccessful = AccountManager.createNewAccount(newAccountName);
			if (accountCreationSuccessful) {
				System.out.println("Your new account has been created!");
			}
			new MainMenu().run();
		}
	}
}
