package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.json.JSONObject;

import simulator.control.Controller;

public class ControlPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private Controller ctrl;
	private ChangeRegionsDialog changeRegionsDialog;

	private JToolBar toolBar;
	private JFileChooser fc;

	private boolean stopped;
	private JButton quitButton;
	private JButton openButton;
	private JButton viewButton;
	private JButton regionButton;
	private JButton runButton;
	private JButton stopButton;

	private JLabel stepsLabel;
	private JSpinner stepsSpinner;

	private JLabel dtLabel;
	private JTextField dtField;

	ControlPanel(Controller ctrl) {
		this.ctrl = ctrl;
		initGUI();
	}

	private void initGUI() {
		setLayout(new BorderLayout());
		toolBar = new JToolBar();
		add(toolBar, BorderLayout.PAGE_START);
		// TODO crear los diferentes botones/atributos y añadirlos a toolBar.
		// Todos ellos han de tener su correspondiente tooltip. Puedes utilizar
		// toolBar.addSeparator() para añadir la línea de separación vertical
		// entre las componentes que lo necesiten.

		ActionListener al = new ToolBarChildrenListener();

		openButton = createToolBarButton("Open", "resources/icons/open.png", al);
		toolBar.add(openButton);

		toolBar.addSeparator();

		viewButton = createToolBarButton("Viewer", "resources/icons/viewer.png", al);
		toolBar.add(viewButton);

		regionButton = createToolBarButton("Regions", "resources/icons/regions.png", al);
		toolBar.add(regionButton);

		toolBar.addSeparator();

		runButton = createToolBarButton("Run", "resources/icons/run.png", al);
		toolBar.add(runButton);

		stopButton = createToolBarButton("Stop", "resources/icons/stop.png", al);
		toolBar.add(stopButton);

		stepsLabel = new JLabel("Steps");
		toolBar.add(stepsLabel);

		SpinnerModel sm = new SpinnerNumberModel(5000, 0, 10000, 1000);
		stepsSpinner = new JSpinner(sm);
		stepsSpinner.setMaximumSize(new Dimension(80, 40));
		stepsSpinner.setMinimumSize(new Dimension(80, 40));
		stepsSpinner.setPreferredSize(new Dimension(80, 40));

		dtLabel = new JLabel("Delta-Time");
		toolBar.add(dtLabel);

		dtField = new JTextField(7);
		dtField.setText("0.00");
		toolBar.add(dtField);

		// Quit Button
		toolBar.add(Box.createGlue()); // this aligns the button to the right
		toolBar.addSeparator();
		quitButton = createToolBarButton("Quit", "resources/icons/exit.png", al);
		toolBar.add(quitButton);
		// TODO Inicializar fc con una instancia de JFileChooser. Para que siempre
		// abre en la carpeta de ejemplos puedes usar:
		//
		// fc.setCurrentDirectory(new File(System.getProperty("user.dir") +
		// "/resources/examples"));
		fc = new JFileChooser();
		fc.setCurrentDirectory(new File(System.getProperty("user.dir") + "/resources/examples"));
		// TODO Inicializar changeRegionsDialog con instancias del diálogo de cambio
		// de regiones
		changeRegionsDialog = new ChangeRegionsDialog(ctrl);
	}
	// TODO el resto de métodos van aquí…

	private JButton createToolBarButton(String toolTipText, String iconPath, ActionListener al) {
		JButton b = new JButton();
		b.setToolTipText(toolTipText);
		b.setIcon(new ImageIcon(iconPath));
		b.addActionListener(al);
		return b;
	}

	private void setButtonsEnabled(boolean e) {
		openButton.setEnabled(e);
		viewButton.setEnabled(e);
		regionButton.setEnabled(e);
		runButton.setEnabled(e);
	}

	private void runSim(int n, double dt) {
		if (n > 0 && !stopped) {
			try {
				ctrl.advance(dt);
				SwingUtilities.invokeLater(() -> runSim(n - 1, dt));
			} catch (Exception e) {
				ViewUtils.showErrorMsg(e.getMessage());
				setButtonsEnabled(true);
				stopped = true;
			}
		} else {
			setButtonsEnabled(true);
			stopped = true;
		}
	}

	private class ToolBarChildrenListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();
			if (source == openButton) {
				int opt = fc.showOpenDialog(ViewUtils.getWindow(ControlPanel.this));
				if (opt == JFileChooser.APPROVE_OPTION) {
					File selected = fc.getSelectedFile();
					JSONObject jo = new JSONObject(selected.getPath());
					ctrl.reset(jo.getInt("cols"), jo.getInt("rows"), jo.getInt("width"), jo.getInt("height"));
					ctrl.loadData(jo);
				}
			} else if (source == viewButton) {
				SwingUtilities.invokeLater(() -> new MapWindow());
			} else if (source == regionButton) {
				changeRegionsDialog.open(ViewUtils.getWindow(ControlPanel.this));
			} else if (source == runButton) {
				try {
					double dt = Double.parseDouble(dtField.getText());
					int steps = (Integer) stepsSpinner.getValue();
					setButtonsEnabled(false);
					stopped = false;
					runSim(steps, dt);
				} catch (NumberFormatException nfe) {
					ViewUtils.showErrorMsg("Wrong number format");
				}
			} else if (source == stopButton) {
				stopped = true;
			} else if (source == quitButton) {
				ViewUtils.quit(ControlPanel.this);
			}

		}

	}

}
