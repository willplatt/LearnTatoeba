public class SetAccountVocabDirMenu extends Menu {
	private Menu previousMenu;
	private Account account;
	
	public SetAccountVocabDirMenu(Menu previousMenu, Account account) {
		this.previousMenu = previousMenu;
		this.account = account;
	}
	
	@Override
	void run() {
		String newVocabDir = askUserAQuestion("Specify the directory for storing this account's vocabulary.");
		if (newVocabDir.toLowerCase().equals("exit")) {
			System.exit(0);
		} else if (newVocabDir.toLowerCase().equals("back")) {
			previousMenu.run();
		} else {
			boolean setDirSuccessful = AccountManager.setVocabDir(account, newVocabDir);
			if (setDirSuccessful) {
				System.out.println("Your account's vocab directory has been set!");
				new MainMenu().run();
			} else {
				System.out.println("Unfortunately, that directory could not be accessed or created. Please try again.");
				run();
			}
		}
	}
}
