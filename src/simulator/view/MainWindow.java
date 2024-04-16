package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;

import simulator.control.Controller;

public class MainWindow extends JFrame {

	public static final long serialVersionUID = 1L;

	private Controller ctrl;

	public MainWindow(Controller ctrl) {
		super("[ECOSYSTEM SIMULATOR");
		this.ctrl = ctrl;
		initGUI();
	}

	private void initGUI() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		setContentPane(mainPanel);
		
		// TODO crear ControlPanel y añadirlo en PAGE_START de mainPanel
		ControlPanel controlPanel = new ControlPanel(ctrl);
		mainPanel.add(controlPanel, BorderLayout.PAGE_START);
		// TODO crear StatusBar y añadirlo en PAGE_END de mainPanel
		JPanel statusBar = new JPanel();
		mainPanel.add(statusBar, BorderLayout.PAGE_END);
		
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		mainPanel.add(contentPanel, BorderLayout.CENTER);

		String[] stColumnNames = {
				"Species",
				"NORMAL",
				"MATE",
				"HUNGER",
				"DANGER",
				"DEAD"
		};
		
		DefaultTableModel stDtm = new DefaultTableModel(null, stColumnNames);
		JTable speciesTable = new JTable(stDtm);
		speciesTable.setPreferredSize(new Dimension(500, 250));
		tablePanel.add(speciesTable);
		
		String[] rtColumnNames = {
				"Row",
				"Col",
				"Desc.",
				"CARNIVORE",
				"HERVIBORE"
		};
		
		DefaultTableModel rtDtm = new DefaultTableModel(null, rtColumnNames);
		JTable regionsTable = new JTable(rtDtm);
		regionsTable.setPreferredSize(new Dimension(500, 250));
		tablePanel.add(regionsTable);

		contentPanel.add(tablePanel);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				ViewUtils.quit(MainWindow.this);
			}
		});
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		pack();
		setVisible(true);
	}
	
}
