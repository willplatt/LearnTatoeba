public class SetNativeLanguageMenu extends Menu {
	private Account account;
	private String newNativeLanguage;
	private Menu nextMenu;
	
	public SetNativeLanguageMenu(Account account, String newNativeLanguage, Menu nextMenu) {
		this.account = account;
		this.newNativeLanguage = newNativeLanguage;
		this.nextMenu = nextMenu;
	}
	
	@Override
	void run() {
		AccountManager.setNativeLanguage(account, newNativeLanguage);
		System.out.println("Your account's native language has been changed!");
		nextMenu.run();
	}
}
