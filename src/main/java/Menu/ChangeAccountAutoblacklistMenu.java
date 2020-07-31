package Menu;

import Account.Account;
import Account.AccountManager;
import Terminal.Terminal;

public class ChangeAccountAutoblacklistMenu extends Menu {
	private Account account;
	private Menu previousMenu;
	private Menu nextMenu;
	
	public ChangeAccountAutoblacklistMenu(Account account, Menu previousMenu, Menu nextMenu) {
		this.account = account;
		this.previousMenu = previousMenu;
		this.nextMenu = nextMenu;
	}
	
	@Override
	public void run() {
		Terminal.println("\nThe autoblacklist duration for your account specifies how long to automatically blacklist a foreign language sentence after you read it. For example, if your autoblacklist duration is 2 days, then after reading a sentence you will not see it again until 2 days have elapsed. Autoblacklisting can be overridden for individual sentences by using the \"!b\" command when practicing.");
		askUserAYesNoQuestion("The current autoblacklist duration for this account is " + account.getAutoblacklistDuration().toPrintString() + ". Would you like to change it?",
				nextMenu::run,
				this::askUserToChangeAutoblacklist
		);
	}
	
	private void askUserToChangeAutoblacklist() {
		askUserAQuestion("Specify a new autoblacklist duration in whole days. If you would prefer to specify a precise number of hours, follow your number with the letter h. For example, \"12h\". To set an infinite duration, type \"infinite\".",
				previousMenu::run,
				newDuration -> {
					boolean setDurationSuccessful = AccountManager.setAutoblacklistDuration(account, newDuration);
					if (setDurationSuccessful) {
						Terminal.println("Your account's autoblacklist duration has been changed!");
						nextMenu.run();
					} else {
						Terminal.println("You did not enter a valid duration. Please try again.");
						run();
					}
				}
		);
	}
}
