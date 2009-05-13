package sneer.pulp.log;

public interface Worker {

	void log(String message, Object... messageInsets);
	void logShort(Exception e, String message, Object... insets);
	void log(Throwable throwable, String message, Object... messageInsets) ;
	void log(Throwable throwable);

}
