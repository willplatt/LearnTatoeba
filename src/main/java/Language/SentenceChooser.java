package Language;

import Account.Account;
import Terminal.Terminal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Language.FileIdSearcher.*;
import static Language.SentencesDirManager.SUFFIX_OF_SENTENCE_FILES;
import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.text.StringEscapeUtils.unescapeJava;

public class SentenceChooser {
	private static final int MAX_SCORE_UPPER_LIMIT = 300;
	private static final int MAX_NUMBER_OF_SENTENCES = 50;
	
	private final VocabManager vocabManager;
	private final BlacklistManager blacklistManager;
	private final File sentencesFile;
	private final List<Sentence> nextSentences = new ArrayList<>();
	private final RandomAccessFile nativeTranslationReader;
	private final String wordCharRegex;
	private final boolean isRightToLeft;
	
	private BufferedReader sentencesReader;
	private int sentenceScoreUpperLimit = 20;
	private int sentencesChosen = 0;
	
	public SentenceChooser(Account account, Language practiceLanguage) throws IOException {
		this.vocabManager =  new VocabManager(account, practiceLanguage);
		this.blacklistManager =  new BlacklistManager(account, practiceLanguage);
		File translationsFile = new File(SentencesDirManager.SENTENCES_DIR, account.getNativeLanguage().getTatoebaCode() + SUFFIX_OF_SENTENCE_FILES);
		this.nativeTranslationReader = new RandomAccessFile(translationsFile, "r");
		this.sentencesFile =  new File(SentencesDirManager.SENTENCES_DIR, practiceLanguage.getTatoebaCode() + SUFFIX_OF_SENTENCE_FILES);
		this.sentencesReader = Files.newBufferedReader(sentencesFile.toPath(), UTF_8);
		this.wordCharRegex = "[" + unescapeJava(practiceLanguage.getWordCharRegExp()) + "]";
		this.isRightToLeft = practiceLanguage.isRightToLeft();
	}
	
	public boolean vocabIsEmpty() {
		return vocabManager.isEmpty();
	}
	
	public Sentence getNextSentence() throws IOException {
		while (nextSentences.isEmpty() && sentencesChosen < MAX_NUMBER_OF_SENTENCES && sentenceScoreUpperLimit < MAX_SCORE_UPPER_LIMIT) {
			Terminal.println("Computing more sentences...");
			computeNextSentencesWithScoresBetween(Math.max(1, sentenceScoreUpperLimit - 20), sentenceScoreUpperLimit);
		}
		if (nextSentences.isEmpty()) {
			return null;
		} else {
			int indexOfFinalSentence = nextSentences.size() - 1;
			Sentence sentence = nextSentences.get(indexOfFinalSentence);
			nextSentences.remove(indexOfFinalSentence);
			return sentence;
		}
	}
	
	public String getSentenceAnnotation(String sentence) {
		if (isRightToLeft) {
			return getRightToLeftSentenceAnnotation(sentence);
		} else {
			return getLeftToRightSentenceAnnotation(sentence);
		}
	}
	
	public boolean updateBlacklistAndVocab(Sentence sentence, String updateCommand) {
		String vocabCommand = updateCommand;
		try {
			if (updateCommand.toLowerCase().startsWith("!b")) {
				int indexOfFirstSpace = updateCommand.indexOf(' ');
				int endOfBlacklistCommand = indexOfFirstSpace == -1 ? updateCommand.length() : indexOfFirstSpace;
				vocabCommand = updateCommand.substring(Math.min(updateCommand.length(), endOfBlacklistCommand + 1));
				String blacklistDurationString = updateCommand.substring(2, endOfBlacklistCommand);
				blacklistManager.blacklist(sentence, new BlacklistDuration(blacklistDurationString));
			} else {
				blacklistManager.autoblacklist(sentence);
			}
		} catch (IllegalArgumentException e) {
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("There was a problem modifying the blacklist file.");
		}
		return updateVocab(vocabCommand);
	}
	
	public boolean updateVocab(String updateCommand) {
		return vocabManager.updateVocab(updateCommand);
	}
	
	public void close() {
		try {
			vocabManager.pushUpdatesToFile();
			Terminal.println("Updates saved to file.");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("There was a problem writing your vocab updates to the file. Your vocab changes for this session may not have been saved.");
		}
		try {
			nativeTranslationReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			sentencesReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void computeNextSentencesWithScoresBetween(int minScore, int maxScore) throws IOException {
		String line = null;
		while (nextSentences.size() < 5 && sentencesChosen < MAX_NUMBER_OF_SENTENCES && (line = sentencesReader.readLine()) != null) {
			if (Math.random() < 0.1) {
				Sentence sentence = new Sentence(line);
				if (!blacklistManager.isBlacklisted(sentence)) {
					int score = getScoreForSentence(sentence.getText());
					if (score >= minScore && score < maxScore) {
						List<Sentence> translations = getNativeTranslations(line);
						if (!translations.isEmpty()) {
							sentence.addTranslations(translations);
							nextSentences.add(sentence);
							sentencesChosen++;
						}
					}
				}
			}
		}
		if (line == null) {
			sentencesReader.close();
			sentencesReader = Files.newBufferedReader(sentencesFile.toPath(), UTF_8);
			sentenceScoreUpperLimit += 20;
		}
	}
	
	private int getScoreForSentence(String sentence) {
		String trimmedSentence = trimOuterPunctuation(sentence);
		int score = 0;
		String remainingToScore = trimmedSentence;
		while (remainingToScore.length() > 0) {
			String phrase = getLongestPhrasePrefix(remainingToScore);
			score += getScoreForPhrase(phrase);
			remainingToScore = remainingToScore.substring(phrase.length());
			int wordSeparatorLength = indexOfFirstWordCharOrZero(remainingToScore);
			remainingToScore = remainingToScore.substring(wordSeparatorLength);
		}
		return score;
	}
	
	private String getRightToLeftSentenceAnnotation(String sentence) {
		if (Terminal.isEmulatingBidi()) {
			String reversedLeftToRightAnnotation = new StringBuilder(getLeftToRightSentenceAnnotation(sentence)).reverse().toString();
			return reversedLeftToRightAnnotation.replace("89", "98");
		} else {
			return "\u200F" + getLeftToRightSentenceAnnotation(sentence) + "\u200F";
		}
	}
	
	private String getLeftToRightSentenceAnnotation(String sentence) {
		String trimmedSentence = trimOuterPunctuation(sentence);
		int charsTrimmedFromStart = sentence.indexOf(trimmedSentence);
		String annotation = " ".repeat(charsTrimmedFromStart);
		String remainingToAnnotate = trimmedSentence;
		while (remainingToAnnotate.length() > 0) {
			String phrase = getLongestPhrasePrefix(remainingToAnnotate);
			String phraseAnnotation = getPhraseAnnotation(phrase);
			annotation += phraseAnnotation;
			remainingToAnnotate = remainingToAnnotate.substring(phrase.length());
			int wordSeparatorLength = indexOfFirstWordCharOrZero(remainingToAnnotate);
			int annotationOverrun = phraseAnnotation.length() - phrase.length();
			int numberOfSpaces = Math.max(0, wordSeparatorLength - annotationOverrun);
			annotation += " ".repeat(numberOfSpaces);
			remainingToAnnotate = remainingToAnnotate.substring(wordSeparatorLength);
		}
		annotation += " ".repeat(Math.max(0, sentence.length() - annotation.length()));
		return annotation;
	}
	
	private String trimOuterPunctuation(String sentence) {
		int start = 0;
		int end = sentence.length();
		while (start < end && !sentence.substring(start, start + 1).matches(wordCharRegex)) {
			start++;
		}
		while (start < end && !sentence.substring(end - 1, end).matches(wordCharRegex)) {
			end--;
		}
		return sentence.substring(start, end);
	}
	
	private String getLongestPhrasePrefix(String sentenceFragment) {
		int wordLength = lengthOfFirstWordIn(sentenceFragment);
		String phrase = sentenceFragment;
		while (phrase.length() != wordLength && vocabManager.getStatusOfPhrase(phrase) == 0) {
			phrase = removeLastWordFrom(phrase);
		}
		return phrase;
	}
	
	private int getScoreForPhrase(String phrase) {
		int status = vocabManager.getStatusOfPhrase(phrase);
		if (status == 98 || status == 99) {
			status = 5;
		}
		int difference = Math.max(0, 4 - status);
		int differenceSquared = difference * difference;
		return 5 * differenceSquared;
	}
	
	private int lengthOfFirstWordIn(String phrase) {
		int index = 0;
		while (index < phrase.length() && phrase.substring(index, index + 1).matches(wordCharRegex)) {
			index++;
		}
		return index;
	}
	
	private String removeLastWordFrom(String phrase) {
		int index = phrase.length();
		while (index > 0 && phrase.substring(index - 1, index).matches(wordCharRegex)) {
			index--;
		}
		while (index > 0 && !phrase.substring(index - 1, index).matches(wordCharRegex)) {
			index--;
		}
		return phrase.substring(0, index);
	}
	
	private int indexOfFirstWordCharOrZero(String str) {
		Pattern pattern = Pattern.compile(wordCharRegex);
		Matcher matcher = pattern.matcher(str);
		return matcher.find() ? matcher.start() : 0;
	}
	
	private List<Sentence> getNativeTranslations(String sentenceLine) throws IOException {
		return getNativeTranslationsForIds(getTranslationIds(sentenceLine));
	}
	
	private List<Integer> getTranslationIds(String sentenceLine) {
		String[] ids = sentenceLine.split("\t");
		List<Integer> translationIds = new ArrayList<>();
		for (int i = 4; i < ids.length; i++) {
			translationIds.add(parseInt(ids[i]));
		}
		return translationIds;
	}
	
	private List<Sentence> getNativeTranslationsForIds(List<Integer> ids) throws IOException {
		Collections.sort(ids);
		List<Sentence> translations = new ArrayList<>();
		long startIndex = 0;
		long fileLength = nativeTranslationReader.length();
		int idsIndex = 0;
		while (idsIndex < ids.size() && startIndex < fileLength - 1) {
			int desiredId = ids.get(idsIndex);
			startIndex = getByteIndexOnLineWithId(desiredId, startIndex, fileLength, nativeTranslationReader);
			Sentence sentence = new Sentence(readLineAt(startIndex, nativeTranslationReader));
			if (desiredId == sentence.getId()) {
				translations.add(sentence);
			}
			idsIndex++;
		}
		return translations;
	}
	
	private String getPhraseAnnotation(String phrase) {
		String status = String.valueOf(vocabManager.getStatusOfPhrase(phrase));
		if (status.equals("0")) {
			status = "-";
		}
		return status + "-".repeat(Math.max(0, phrase.length() - status.length()));
	}
}
