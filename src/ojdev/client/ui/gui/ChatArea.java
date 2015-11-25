package ojdev.client.ui.gui;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

@SuppressWarnings("serial")
public class ChatArea extends JPanel {
	protected JTextField textField;
	protected JTextArea textArea;
	protected DefaultCaret caret;
	protected JScrollPane textAreaScrollPane;
	protected final ChatAreaNotifyInterface mainWindow;
	protected boolean closed = false;
	
	/**
	 * Create the panel.
	 * 
	 * @param notifyInterface the object to notify when the user submits entered text; if null, text entry is disallowed.
	 */
	public ChatArea(ChatAreaNotifyInterface notifyInterface) {
		this.mainWindow = notifyInterface;
		
		setLayout(new BorderLayout(0, 0));
		setBorder(new EmptyBorder(0, 0, 0, 0));
		
		textField = new JTextField();
		add(textField, BorderLayout.SOUTH);
		
		if(this.mainWindow == null) {
			textField.setEnabled(false);
		} else {
			textField.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					textAreaAction(e);
				}
			});
		}
		
		textArea = new JTextArea();
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textArea.setText("");
		textArea.setBorder(null);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		caret = (DefaultCaret) textArea.getCaret();
		textAreaScrollPane = new JScrollPane(textArea);
		textAreaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		textAreaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		textAreaScrollPane.setBorder(null);
		add(textAreaScrollPane, BorderLayout.CENTER);

	}

	protected void textAreaAction(ActionEvent e) {
		JScrollBar scrollBar = textAreaScrollPane.getVerticalScrollBar(); 
		int extent = scrollBar.getModel().getExtent();
		boolean atBottom = scrollBar.getValue() + extent == scrollBar.getMaximum();
		
		if(atBottom) {
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		} else {
			caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		}

		if(mainWindow.notifyTextEntered(ChatArea.this, e.getActionCommand())) {
			appendText("> %s%n", e.getActionCommand());
			textField.setText("");
		}
	}
	
	public void appendText(String text, Object... args) {
		assert SwingUtilities.isEventDispatchThread() : "Must be called from Event Dispatch Thread";
		textArea.append(String.format(text, args));
	}
	
	public void closed() {
		closed = true;
		
		textField.setEnabled(false);
	}

	public boolean isClosed() {
		return closed;
	}

}
