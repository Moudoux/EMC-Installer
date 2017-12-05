package me.deftware.emc.ui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class Progressbar extends JPanel {

	JProgressBar pbar;

	public Progressbar(String title, int max) {
		pbar = new JProgressBar();
		pbar.setMinimum(0);
		pbar.setMaximum(max);
		pbar.setPreferredSize(new Dimension(500, 20));
		add(pbar);
		JFrame frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(this);
		frame.setSize(new Dimension(500, 65));
		frame.setVisible(true);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
	}

	public void updateBar(int newValue) {
		pbar.setValue(newValue);
	}

}
