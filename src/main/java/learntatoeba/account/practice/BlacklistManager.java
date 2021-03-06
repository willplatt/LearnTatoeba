package learntatoeba.account.practice;

import learntatoeba.language.Language;
import learntatoeba.account.Account;
import learntatoeba.account.BlacklistDuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BlacklistManager {
	private static final String SEPARATOR = "\texpires ";
	private static final String NEVER_EXPIRE = "never";
	
	private final Account account;
	private final Set<Integer> blacklist = new HashSet<>();
	private final File blacklistFile;
	private final File tempBlacklistFile;
	
	public BlacklistManager(Account account, Language practiceLanguage) throws IOException {
		this.account = account;
		blacklistFile = new File(account.getVocabDirectory(), practiceLanguage.getName() + "_Blacklist.tsv");
		tempBlacklistFile = new File(account.getVocabDirectory(), "temp.tsv");
		cleanAndReadBlacklist();
	}
	
	public boolean isBlacklisted(Sentence sentence) {
		return blacklist.contains(sentence.getId());
	}
	
	public void autoblacklist(Sentence sentence) throws IOException {
		blacklist(sentence, account.getAutoblacklistDuration());
	}
	
	public void blacklist(Sentence sentence, BlacklistDuration duration) throws IOException {
		if (duration.isInfinite() || duration.toSeconds() != 0) {
			if (!blacklistFile.exists()) {
				blacklistFile.createNewFile();
			}
			try (BufferedWriter blacklistWriter = Files.newBufferedWriter(blacklistFile.toPath(), UTF_8, StandardOpenOption.APPEND)) {
				String expiryTime = NEVER_EXPIRE;
				if (duration.isNotInfinite()) {
					long currentSecond = Instant.now().getEpochSecond();
					long blacklistExpirySecond = currentSecond + duration.toSeconds();
					expiryTime = String.valueOf(blacklistExpirySecond);
				}
				blacklistWriter.write(sentence.getId() + SEPARATOR + expiryTime + "\n");
				blacklist.add(sentence.getId());
			}
		}
	}
	
	private void cleanAndReadBlacklist() throws IOException {
		if (blacklistFile.exists()) {
			long currentSecond = Instant.now().getEpochSecond();
			try (BufferedReader blacklistReader = Files.newBufferedReader(blacklistFile.toPath(), UTF_8);
			     BufferedWriter tempWriter = Files.newBufferedWriter(tempBlacklistFile.toPath(), UTF_8)) {
				String line;
				while ((line = blacklistReader.readLine()) != null) {
					ignoreOrCopyAndAcceptBlacklisting(line, tempWriter, currentSecond);
				}
			}
			Files.delete(blacklistFile.toPath());
			Files.move(tempBlacklistFile.toPath(), blacklistFile.toPath());
		}
	}
	
	private void ignoreOrCopyAndAcceptBlacklisting(String line, BufferedWriter tempWriter, long currentSecond) throws IOException {
		String[] values = line.toLowerCase().split(SEPARATOR);
		String expiryTime = values[1];
		boolean hasExpired;
		if (expiryTime.equals(NEVER_EXPIRE)) {
			hasExpired = false;
		} else {
			long expirySecond = Long.parseLong(expiryTime);
			hasExpired = currentSecond > expirySecond;
		}
		if (!hasExpired) {
			int sentenceId = Integer.parseInt(values[0]);
			copyAndAcceptBlacklisting(tempWriter, line, sentenceId);
		}
	}
	
	private void copyAndAcceptBlacklisting(BufferedWriter tempWriter, String line, int sentenceId) throws IOException {
		tempWriter.write(line + "\n");
		blacklist.add(sentenceId);
	}
}
