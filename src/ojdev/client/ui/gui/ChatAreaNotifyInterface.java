package ojdev.client.ui.gui;

public interface ChatAreaNotifyInterface {
	/**
	 * Used by ChatArea to notify of command being submitted.
	 * @param source the ChatArea source of the text
	 * @param text the text entered by the user
	 * @return boolean indicating if the text entered was successfully processed
	 */
	public boolean notifyTextEntered(ChatArea source, String text);
	
	public String getChatDisplayName();
}
