public class NameNewAccountMenu extends Menu {
	private Menu previousMenu;
	
	public NameNewAccountMenu(Menu previousMenu) {
		this.previousMenu = previousMenu;
	}
	
	@Override
	void run() {
		System.out.println("\nOkay, let's create a new account.");
		String newAccountName = askUserAQuestion("What do you want to name it?");
		if (newAccountName.toLowerCase().equals("exit")) {
			System.exit(0);
		} else if (newAccountName.toLowerCase().equals("back")) {
			previousMenu.run();
		} else {
			boolean accountCreationSuccessful = AccountManager.createNewAccount(newAccountName);
			if (accountCreationSuccessful) {
				System.out.println("Your new account has been created!");
				new ChangeAccountVocabDirMenu(AccountManager.getAccountFromName(newAccountName), new MainMenu()).run();
			} else {
				System.out.println("Something went wrong trying to create your new account. Returning to the main menu.");
				new MainMenu().run();
			}
		}
	}
}
