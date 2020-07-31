package Language;

public class BlacklistDuration {
	private static final String INFINITE = "infinite";
	private static final int SECONDS_PER_DAY = 86400;
	private static final int SECONDS_PER_6_HOURS = 21600;
	
	private String duration;
	
	public BlacklistDuration(String duration) {
		try {
			int numberOfDays = Integer.parseInt(duration);
			if (numberOfDays >= 0) {
				this.duration = String.valueOf(numberOfDays);
			} else {
				throw new IllegalArgumentException("A blacklist duration cannot be negative.");
			}
		} catch (NumberFormatException e) {
			if ("infinite".startsWith(duration.toLowerCase())) {
				this.duration = INFINITE;
			} else {
				throw new IllegalArgumentException("A blacklist duration must be an integer for infinite");
			}
		}
	}
	
	public boolean isNotInfinite() {
		return !isInfinite();
	}
	
	public boolean isInfinite() {
		return duration.equals(INFINITE);
	}
	
	public int toSeconds() {
		if (isInfinite()) {
			throw new IllegalStateException("Cannot convert an infinite duration to seconds.");
		}
		return (toDays() * SECONDS_PER_DAY) - SECONDS_PER_6_HOURS;
	}
	
	public int toDays() {
		if (isInfinite()) {
			throw new IllegalStateException("Cannot convert an infinite duration to days.");
		}
		return Integer.parseInt(duration);
	}
	
	public String toPrintString() {
		if (duration.equals(INFINITE)) {
			return duration;
		} else {
			if (duration.equals("1")) {
				return duration + " day";
			} else {
				return duration + " days";
			}
		}
	}
	
	@Override
	public String toString() {
		return duration;
	}
}
