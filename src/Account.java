public class Account {
	private String name;
	private String directoryName;
	
	public Account(String name, String directoryName) {
		this.name = name;
		this.directoryName = directoryName;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDirectoryName() {
		return directoryName;
	}
}
