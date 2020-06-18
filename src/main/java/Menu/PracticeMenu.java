package Menu;

import Account.Account;
import Language.Language;
import Language.SentenceChooser;
import Language.Sentence;
import Terminal.Terminal;

import java.io.IOException;
import java.util.List;

public class PracticeMenu extends Menu {
	private final Account account;
	private final Language language;
	private final Menu previousMenu;
	
	public PracticeMenu(Account account, Language language, Menu previousMenu) {
		this.account = account;
		this.language = language;
		this.previousMenu = previousMenu;
	}
	
	@Override
	public void run() {
		Terminal.println("\nYou will now be presented with sentences in " + language.getName() + " until you type 'back' or 'exit'.");
		Terminal.println("After reading a sentence, you can do one of three things:\n" +
				"1) Enter anything beginning with the '#' symbol. This will give you the translation(s) of the sentence into your native language. Then you can proceed by steps 2 or 3.\n" +
				"2) Press enter without typing anything to move on to the next sentence.\n" +
				"3) Enter a command to change the status of phrases in your vocabulary and move on to the next sentence. A command must be of the form \"phrase1: status1, phrase2: status2, ... phraseN: statusN\"." +
				" A status must be a whole number from 1 to 5, or 98 or 99. These have the following meanings:\n" +
				"\t1: Unknown\n" +
				"\t2: A bit better than unknown\n" +
				"\t3: Still learning\n" +
				"\t4: I nearly know it\n" +
				"\t5: Known\n" +
				"\t98: Ignored (this can be used for names)\n" +
				"\t99: Well-known (this can be used for words that are the same in your native language)\n" +
				"So for example, \"chair: 2, coffee table: 3, stool: 1\" will record that you recognise the word chair (even if you don't know what it means), you are on your way to learning the phrase coffee table, and the word stool is unknown to you.");
		SentenceChooser sentenceChooser = null;
		try {
			sentenceChooser = new SentenceChooser(account, language);
			if (sentenceChooser.vocabIsEmpty()) {
				Terminal.println("\nIt looks like you don't have any " + language.getName() + " vocab yet, so you might want to enter some commands now to add words you know (or partly know). When you're done and want to start practicing, just press enter a blank line.");
				processVocabUpdatesAndGoToNextSentenceWhenUserInputIsBlank(sentenceChooser);
			} else {
				doAnotherSentence(sentenceChooser);
			}
		} catch (IOException e) {
			if (sentenceChooser != null) {
				sentenceChooser.close();
			}
			e.printStackTrace();
			previousMenu.run();
		}
	}
	
	private void doAnotherSentence(SentenceChooser sentenceChooser) throws IOException {
		Sentence sentence = sentenceChooser.getNextSentence();
		if (sentence == null) {
			Terminal.println("Wow! You've been through all of the sentences! Take a well-deserved break.");
			sentenceChooser.close();
			previousMenu.run();
		} else {
			String sentenceText = sentence.getText();
			List<Sentence> translations = sentence.getTranslations();
			printSentence(sentenceText);
			Terminal.println("\t" + sentenceChooser.getSentenceAnnotation(sentenceText));
			askUserHowToProceed(sentenceChooser, translations);
		}
	}
	
	private void printSentence(String sentence) {
		Terminal.print("\n\t");
		if (language.isRightToLeft()) {
			Terminal.printlnRtl(sentence);
		} else {
			Terminal.println(sentence);
		}
	}
	
	private void askUserHowToProceed(SentenceChooser sentenceChooser, List<Sentence> translations) throws IOException {
		String nextSentenceOrTranslate = getNextLine();
		if (nextSentenceOrTranslate.startsWith("#")) {
			for (Sentence translation : translations) {
				printTranslation(translation.getText());
			}
			String userInput = getNextLine();
			processVocabUpdateAndGoToNextSentence(userInput, sentenceChooser, translations);
		} else {
			processVocabUpdateAndGoToNextSentence(nextSentenceOrTranslate, sentenceChooser, translations);
		}
	}
	
	private void printTranslation(String translation) {
		Terminal.print("\t");
		if (account.getNativeLanguage().isRightToLeft()) {
			Terminal.printlnRtl(translation);
		} else {
			Terminal.println(translation);
		}
	}
	
	private void processVocabUpdatesAndGoToNextSentenceWhenUserInputIsBlank(SentenceChooser sentenceChooser) throws IOException {
		String userInput = getNextLine();
		doAnotherSentenceOrDoSomethingElse(userInput, sentenceChooser,
				() -> {
					boolean updateVocabSuccessful = sentenceChooser.updateVocab(userInput);
					if (!updateVocabSuccessful) {
						Terminal.println("Your request was improperly formatted. Please make sure you typed correctly and try again:");
					}
					processVocabUpdatesAndGoToNextSentenceWhenUserInputIsBlank(sentenceChooser);
				}
		);
	}
	
	private void processVocabUpdateAndGoToNextSentence(String updateCommand, SentenceChooser sentenceChooser, List<Sentence> translations) throws IOException {
		doAnotherSentenceOrDoSomethingElse(updateCommand, sentenceChooser,
				() -> {
					boolean updateVocabSuccessful = sentenceChooser.updateVocab(updateCommand);
					if (updateVocabSuccessful) {
						doAnotherSentence(sentenceChooser);
					} else {
						Terminal.println("Your request was improperly formatted. Please make sure you typed correctly and try again:");
						askUserHowToProceed(sentenceChooser, translations);
					}
				}
		);
	}
	
	private void doAnotherSentenceOrDoSomethingElse(String userInput, SentenceChooser sentenceChooser, RunnableWithIOException somethingElseProcedure) throws IOException {
		if (userInput.toLowerCase().equals("exit")) {
			sentenceChooser.close();
			System.exit(0);
		} else if (userInput.toLowerCase().equals("back")) {
			sentenceChooser.close();
			previousMenu.run();
		} else if (userInput.equals("")){
			doAnotherSentence(sentenceChooser);
		} else {
			somethingElseProcedure.run();
		}
	}
	
	private interface RunnableWithIOException {
		void run() throws IOException;
	}
}
