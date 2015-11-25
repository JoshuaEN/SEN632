package ojdev.client.ui.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import ojdev.client.WarriorFolder;

import javax.swing.JTextField;
import javax.swing.JTextArea;
import java.awt.Font;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;

@SuppressWarnings("serial")
public class ClientFauxConsoleWindow extends JFrame {

	private transient JPanel contentPane;
	private transient JTextField textField;
	private transient JTextArea textArea;
	private transient final ClientConsoleInterfaceController commandHandler;
	private JPopupMenu popupMenu;
	private JMenuItem mntmWordWrap;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientFauxConsoleWindow frame = new ClientFauxConsoleWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ClientFauxConsoleWindow() {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 680, 350);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 0));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		textArea = new JTextArea();
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textArea.setText("Hello Master, how may I serve you?\r\n");
		textArea.setBorder(new EmptyBorder(0, 0, 0, 0));
		textArea.setEditable(false);
		textArea.setFocusable(false);
		textArea.setLineWrap(true);
		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		JScrollPane textAreaScrollPane = new JScrollPane(textArea);
		textAreaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		textAreaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		textAreaScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		contentPane.add(textAreaScrollPane, BorderLayout.CENTER);
		
		JTextAreaWriter writer = new JTextAreaWriter(textArea);
		
		popupMenu = new JPopupMenu();
		addPopup(textArea, popupMenu);
		
		mntmWordWrap = new JMenuItem("Disable Word Wrap");
		mntmWordWrap.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				textArea.setLineWrap(!textArea.getLineWrap());
				
				if(textArea.getLineWrap()) {
					mntmWordWrap.setText("Disable Word Wrap");
				} else {
					mntmWordWrap.setText("Enable Word Wrap");
				}
			}
		});
		popupMenu.add(mntmWordWrap);
		
		
		commandHandler = new ClientConsoleInterfaceController(writer, null, new WarriorFolder(Paths.get(".")));
		
		textField = new JTextField();
		textField.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textField.setBorder(new EmptyBorder(0, 0, 0, 0));
		textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JScrollBar scrollBar = textAreaScrollPane.getVerticalScrollBar(); 
				int extent = scrollBar.getModel().getExtent();
				boolean atBottom = scrollBar.getValue() + extent == scrollBar.getMaximum();
				
				if(atBottom) {
					caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
				} else {
					caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
				}
				
				try {
					writer.append(String.format("> %s%n", e.getActionCommand()));
				} catch (IOException e1) { /* impossible */ }

				if(commandHandler.parse(e.getActionCommand())) {
					((JTextField)e.getSource()).setText("");
				}
			}
		});
		contentPane.add(textField, BorderLayout.SOUTH);
	}

	public class JTextAreaWriter extends Writer {

		private final StringWriter writer = new StringWriter();
		private final JTextArea textArea;
		
		public JTextAreaWriter(JTextArea textArea) {
			this.textArea = textArea;
		}
		
		@Override
		public void close() throws IOException {

		}

		@Override
		public synchronized void flush() throws IOException {
			textArea.append(writer.toString());
			writer.getBuffer().setLength(0);
		}

		@Override
		public synchronized void write(char[] cbuf, int off, int len) throws IOException {
			writer.write(cbuf, off, len);
		}
		
	}
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
