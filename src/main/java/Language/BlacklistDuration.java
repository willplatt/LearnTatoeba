package Language;

public class BlacklistDuration {
	private static final String INFINITE = "infinite";
	
	private String duration;
	private int hours;
	
	public BlacklistDuration(String duration) {
		duration = duration.toLowerCase();
		try {
			int numberOfDays = Integer.parseInt(duration);
			this.duration = String.valueOf(numberOfDays);
			if (numberOfDays == 0) {
				this.hours = 0;
			} else if (numberOfDays > 0) {
				this.hours = (numberOfDays * 24) - 6;
			} else {
				throw new IllegalArgumentException("A blacklist duration cannot be negative.");
			}
		} catch (NumberFormatException e) {
			if ("infinite".startsWith(duration)) {
				this.duration = INFINITE;
			} else if (duration.endsWith("h")) {
				try {
					int numberOfHours = Integer.parseInt(duration.substring(0, duration.length() - 1));
					if (numberOfHours >= 0) {
						this.duration = numberOfHours + "h";
						this.hours = numberOfHours;
					} else {
						throw new IllegalArgumentException("A blacklist duration cannot be negative.");
					}
				} catch (NumberFormatException ex) {
					throw new IllegalArgumentException("A blacklist duration in hours must be a whole number.");
				}
			} else {
				throw new IllegalArgumentException("A blacklist duration must be an integer or infinite or specified in hours");
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
		return hours * 3600;
	}
	
	public String toPrintString() {
		if (duration.equals(INFINITE)) {
			return duration;
		} else {
			if (duration.endsWith("h")) {
				if (hours == 1) {
					return hours + " hour";
				} else {
					return hours + " hours";
				}
			} else if (duration.equals("1")) {
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
