package me.deftware.emc.installer.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class DialogUI extends JDialog {

	private JPanel contentPane;
	private JButton buttonOK;
	private JLabel modalText;
	private DialogCallback cb;

	public DialogUI(String text, String title, boolean continueButton, DialogCallback cb) {
		this.cb = cb;
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);
		buttonOK.addActionListener((e) -> onContinue());
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				 onContinue();
			}
		});
		buttonOK.setVisible(continueButton);
		setTitle(title);
		modalText.setText(text);
		pack();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
	}

	public void onContinue() {
		if (cb != null) {
			cb.onCallback();
		}
		dispose();
	}

	@FunctionalInterface
	public interface DialogCallback {

		void onCallback();

	}

}
