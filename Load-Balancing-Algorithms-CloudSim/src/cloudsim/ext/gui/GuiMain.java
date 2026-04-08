package cloudsim.ext.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import cloudsim.ext.Constants;
import cloudsim.ext.Simulation;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;
import cloudsim.ext.gui.screens.ConfigureSimulationPanel;
import cloudsim.ext.gui.screens.InternetCharacteristicsScreen;
import cloudsim.ext.gui.screens.ResultsScreen;
import cloudsim.ext.gui.screens.SimulationPanel;

/**
 * The main class of the GUI. Sets up the UI, and controls the screen transitions.
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public class GuiMain extends JFrame implements ActionListener, CloudSimEventListener {

private static final String CMD_ABOUT = "About";
	private static final String CMD_DISPLAY_RESULTS = "display_results";
	private static final String CMD_CANCEL_SIMULATION = "Cancel_simulation";
	private static final String CMD_SHOW_BOUNDARIES = "show_boundaries";
	private static final String INTERNET_CHARACTERISTICS_SCREEN = "Internet characteristics screen";
	private static final String CMD_DEFINE_INTERNET_CHARACTERISTICS = "Define Internet Characteristics";
	private static final String HOME_SCREEN = "home screen";
	private static final String CONFIG_SCREEN = "configScreen";
	private static final String CMD_RUN_SIMULATION = "Run Simulation";
	private static final String CMD_EXIT = "Exit";
	private static final String CMD_CONFIGURE_SIMULATION = "Configure Simulation";	
	private static final Dimension MENU_BUTTON_SIZE = new Dimension(120, 55);
	private static final Dimension FRAME_SIZE = new Dimension(800, 600);
	private static final int MENU_BTN_V_GAP = 10;
	
	private CardLayout screenController;
	private JPanel mainPanel;
	private ConfigureSimulationPanel configScreen;
	private SimulationPanel simulationPanel;
	private JPanel internetBehaviourScreen;
	private ResultsScreen resultsScreen;
	private Simulation simulation;
	private Map<String, JButton> menuButtons;
	private JProgressBar progressBar;
	private JPanel messagePanel;
	private JPanel busyMessagePnl;
	private JToggleButton btnShowBoundaries;
	private boolean simulationStarted = false;
	private boolean simulationFinished = false;
	private JPanel simulationControlPanel;
	private JButton btnCancelSim;
	private JDialog resultsDlg;
	private JButton btnResults;
	private JButton btnExportResults;
	private JDialog abtDlg;
	
	/** No args constructor */
	public GuiMain() throws Exception{	
		simulation = new Simulation(this);
		
		initUI();
		showHomeScreen();
		
	}
	
	private void initUI(){		
		this.setTitle("Green Score Load Balancer");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(FRAME_SIZE);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		this.setJMenuBar(getSimMenuBar());
		
		menuButtons = new HashMap<String, JButton>();
		this.add(getMenuPanel(), BorderLayout.WEST);
		
		mainPanel = new JPanel();
		screenController = new CardLayout();
		mainPanel.setLayout(screenController);
		
		JScrollPane scrollPanel = new JScrollPane(mainPanel);
		this.add(scrollPanel, BorderLayout.CENTER);
	}
	
	private void showHomeScreen(){
		enableMenuPanel();
		
		if (simulationPanel == null){
			JPanel simulationScreen = new JPanel();
			simulationScreen.setLayout(new BorderLayout());
			JPanel pnl = new JPanel();
			simulationPanel = new SimulationPanel();
			pnl.add(simulationPanel);
			simulationScreen.add(pnl, BorderLayout.CENTER);
			
			messagePanel = new JPanel();
			Dimension dimension = new Dimension(500, 60);
			messagePanel.setPreferredSize(dimension);
			messagePanel.setMinimumSize(dimension);
			messagePanel.setMaximumSize(dimension);
			simulationScreen.add(messagePanel, BorderLayout.NORTH);
			
			simulationControlPanel = new JPanel();
			simulationControlPanel.setLayout(new BoxLayout(simulationControlPanel, BoxLayout.X_AXIS));
			btnShowBoundaries = new JToggleButton("Show Region Boundaries");
			btnShowBoundaries.setActionCommand(CMD_SHOW_BOUNDARIES);
			btnShowBoundaries.addActionListener(this);
			simulationControlPanel.add(Box.createHorizontalGlue());
			simulationControlPanel.add(btnShowBoundaries);
			
			simulationControlPanel.setBorder(new EmptyBorder(5, 5, 25, 25));
			simulationScreen.add(simulationControlPanel, BorderLayout.SOUTH);
			
			mainPanel.add(HOME_SCREEN, simulationScreen);
		}
		
		if (simulationStarted){
			
			if (btnCancelSim == null){
				btnCancelSim = new JButton("Cancel Simulation");
				btnCancelSim.setActionCommand(CMD_CANCEL_SIMULATION);
				btnCancelSim.addActionListener(this);
			}
			simulationControlPanel.remove(btnCancelSim);
			simulationControlPanel.add(Box.createHorizontalStrut(10));
			simulationControlPanel.add(btnCancelSim);
		} else if (simulationFinished) {
			simulationControlPanel.remove(btnCancelSim);
			
			if (btnResults == null){
				btnResults = new JButton("Display Detailed Results");
				btnResults.setActionCommand(CMD_DISPLAY_RESULTS);
				btnResults.addActionListener(this);
				simulationControlPanel.add(Box.createHorizontalStrut(10));
				simulationControlPanel.add(btnResults);
			}
			
		}
		
		screenController.show(mainPanel, HOME_SCREEN);
		this.validate();
		this.repaint();
	}
	
	private void showConfigureScreen(){
		
		disableMenuPanel();
		
		if (configScreen == null){
			configScreen = new ConfigureSimulationPanel(simulation, this);
			mainPanel.add(CONFIG_SCREEN, configScreen);
		}
		screenController.show(mainPanel, CONFIG_SCREEN);
		this.validate();
		this.repaint();
	}
	
	private void showInternetBehaviourScreen(){
		disableMenuPanel();
		
		if (internetBehaviourScreen == null){
			internetBehaviourScreen = new InternetCharacteristicsScreen(simulation, this);
			mainPanel.add(INTERNET_CHARACTERISTICS_SCREEN, internetBehaviourScreen);
		}
		screenController.show(mainPanel, INTERNET_CHARACTERISTICS_SCREEN);
		this.validate();
		this.repaint();
	}
	
	private void showResultsScreen(){
		if (resultsDlg == null){
			resultsDlg = new JDialog(this);
			resultsDlg.setLocationRelativeTo(this);
			resultsDlg.setTitle("Green Score - Simulation Results");
			
			resultsScreen = new ResultsScreen(simulation);
			resultsDlg.getContentPane().add(new JScrollPane(resultsScreen));
						
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			resultsDlg.setSize((int) (screenSize.width * 0.8), (int) (screenSize.height * 0.8));
			resultsDlg.setLocation((int) (screenSize.width * 0.1), (int) (screenSize.height * 0.1));
		}
		
		resultsDlg.setVisible(true);
	}
	
	private JPanel getMenuPanel(){
		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
		menuPanel.setBackground(new Color(24, 28, 33));
		menuPanel.setBorder(new CompoundBorder(
			new EmptyBorder(0, 0, 0, 1),
			new EmptyBorder(25, 16, 16, 16)));
		menuPanel.setOpaque(true);

		// App branding at top
		JLabel brand = new JLabel("<html><div style='text-align:center'><b>GREEN<br/>SCORE</b></div></html>");
		brand.setForeground(new Color(46, 160, 67));
		brand.setFont(new Font(brand.getFont().getName(), Font.BOLD, 14));
		brand.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		menuPanel.add(brand);
		menuPanel.add(Box.createVerticalStrut(25));

		addMenuButton(menuPanel, CMD_CONFIGURE_SIMULATION);
		addMenuButton(menuPanel, CMD_DEFINE_INTERNET_CHARACTERISTICS);
		menuPanel.add(Box.createVerticalStrut(20));
		addMenuButton(menuPanel, CMD_RUN_SIMULATION);
		menuPanel.add(Box.createVerticalStrut(20));
		addMenuButton(menuPanel, CMD_EXIT);

		return menuPanel;
	}
	
	private JMenuBar getSimMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		
		JMenu menuHelp = new JMenu("Help");
		menuBar.add(menuHelp);
		
		JMenuItem menuAbout = new JMenuItem(CMD_ABOUT);
		menuAbout.setActionCommand(CMD_ABOUT);
		menuAbout.addActionListener(this);
		menuHelp.add(menuAbout);
		
		return menuBar;
	}

	private void addMenuButton(JPanel pnl, String text) {		
		
		JButton btn = new JButton("<html><p align='center'>" + text + "</p></html>");
		menuButtons.put(text, btn);
		btn.setActionCommand(text);
		btn.setMaximumSize(MENU_BUTTON_SIZE);
		btn.setMinimumSize(MENU_BUTTON_SIZE);
		btn.setPreferredSize(MENU_BUTTON_SIZE);
		
		btn.addActionListener(this);
		
		pnl.add(btn);
		pnl.add(Box.createVerticalStrut(MENU_BTN_V_GAP));
		
	}
	
	private void disableMenuPanel() {
		menuButtons.get(CMD_CONFIGURE_SIMULATION).setEnabled(false);
		menuButtons.get(CMD_DEFINE_INTERNET_CHARACTERISTICS).setEnabled(false);
		menuButtons.get(CMD_RUN_SIMULATION).setEnabled(false);
	}
	
	private void enableMenuPanel() {
		menuButtons.get(CMD_CONFIGURE_SIMULATION).setEnabled(true);
		menuButtons.get(CMD_DEFINE_INTERNET_CHARACTERISTICS).setEnabled(true);
		menuButtons.get(CMD_RUN_SIMULATION).setEnabled(true);
	}
		
	private void showBusyMessage(){
		if (busyMessagePnl == null){
			busyMessagePnl = new JPanel();
			busyMessagePnl.add(new JLabel("<html><h2>Simulation Running...</h2></html>"), BorderLayout.NORTH);
			
			progressBar = new JProgressBar(0, (int) (simulation.getSimulationTime() / 1000));
			progressBar.setStringPainted(true);
			busyMessagePnl.add(progressBar, BorderLayout.CENTER);
		}
		
		messagePanel.add(busyMessagePnl, BorderLayout.NORTH);
		messagePanel.revalidate();
	}
	
	private void showSimulationCompleteMessage(){
		messagePanel.removeAll();
		
		JLabel msg = new JLabel("<html><h2>Simulation Complete</h2></html>");
		messagePanel.add(msg);
		
		messagePanel.revalidate();
		this.repaint();
	}
	
	private void showSimulationCancellingMessage(){
		messagePanel.removeAll();
		
		JLabel msg = new JLabel("<html><h2>Cancelling Simulation...</h2></html>");
		messagePanel.add(msg);
		
		messagePanel.revalidate();
		this.repaint();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(CMD_CONFIGURE_SIMULATION)){			
			showConfigureScreen();
		} else if (e.getActionCommand().equals(CMD_EXIT)){
			System.exit(0);
		} else if (e.getActionCommand().equals(ConfigureSimulationPanel.CMD_DONE_CONFIGURATION)){
			if (configScreen.isValidConfiguration()){
				showHomeScreen();
			}
		} else if (e.getActionCommand().equals(ConfigureSimulationPanel.CMD_CANCEL_CONFIGURATION)){
			showHomeScreen();
		} else if (e.getActionCommand().equals(CMD_RUN_SIMULATION)){
			if (!simulationFinished){
				showBusyMessage();
				
				//Start simulation in a new thread, because this is the Event-dispatch thread
				Thread t = new Thread(){
					public void run(){
						try {
							simulation.runSimulation();
							showSimulationCompleteMessage();
						} catch (Exception e) {
							JOptionPane.showMessageDialog(GuiMain.this, "Encountered an unexpected error" + e.getMessage());
							e.printStackTrace();
						}
					}
				};
				t.start();
							
				simulationStarted = true;
				showHomeScreen();
								
			} else {
				JOptionPane.showMessageDialog(this, "To re-execute the simulation or run another simulation, " +
													"please re-start the simulator." +
													" \nThis is required due to a limitation in the underlying simulation framework.");
			}
			
		} else if (e.getActionCommand().equals(CMD_DEFINE_INTERNET_CHARACTERISTICS)){
			showInternetBehaviourScreen();
		} else if (e.getActionCommand().equals(InternetCharacteristicsScreen.CMD_CANCEL_INTERNET_CONFIG)){
			showHomeScreen();
		} else if (e.getActionCommand().equals(InternetCharacteristicsScreen.CMD_DONE_INTERNET_CONFIG)){
			showHomeScreen();
		} else if (e.getActionCommand().equals(CMD_SHOW_BOUNDARIES)){
			simulationPanel.setShowBoundaries(btnShowBoundaries.isSelected());
		} else if (e.getActionCommand().equals(CMD_CANCEL_SIMULATION)){
			try {
				showSimulationCancellingMessage();
				simulation.cancelSimulation();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage());
			}
		} else if (e.getActionCommand().equals(CMD_DISPLAY_RESULTS)){
			showResultsScreen();
		} else if (e.getActionCommand().equals(CMD_ABOUT)){
			if (abtDlg == null){
				abtDlg = new JDialog();
				abtDlg.setTitle("About Green Score Load Balancer");
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				abtDlg.setLocation((int) screenSize.getWidth()/ 2 - 200, (int) screenSize.getHeight() / 2 - 150);
				abtDlg.setSize(400, 300);

				JLabel text = new JLabel("<html><div align='center'><h2>Green Score Load Balancer</h2>"
											+ "Energy-aware cloud load balancing simulation<br/><br/>"
											+ "Built on CloudAnalyst / CloudSim framework<br/>"
											+ "Original framework by: Bhathiya Wickremasinghe"
											+ "</div></html>");
				abtDlg.getContentPane().add(text);
			}
			
			abtDlg.setVisible(true);
		}
	}


	public void cloudSimEventFired(CloudSimEvent e) {
		if (e.getId() == CloudSimEvents.EVENT_SIMULATION_ENDED){
			simulationFinished = true;
			simulationStarted = false;
			showHomeScreen();
			
			showOnScreenResults();
			showResultsScreen();
		} else if (e.getId() == CloudSimEvents.EVENT_PROGRESS_UPDATE){
			double currSimTime = (Double) e.getParameter(Constants.PARAM_TIME);
			progressBar.setValue((int) (currSimTime / 1000));
		}
	}
	
	private void showOnScreenResults(){
		simulationPanel.setResults(simulation.getResults());
	}
	
	/**
	 * The main method of the application
	 * @param args
	 */
	public static void main(String[] args){
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}

			// Dark green theme overrides
			Color bgDark = new Color(30, 34, 39);
			Color bgMid = new Color(40, 44, 52);
			Color bgLight = new Color(55, 60, 68);
			Color textColor = new Color(220, 225, 230);
			Color accent = new Color(46, 160, 67);
			Color accentDim = new Color(36, 120, 52);
			Color controlBg = new Color(50, 55, 62);

			javax.swing.UIManager.put("control", bgMid);
			javax.swing.UIManager.put("nimbusBase", bgDark);
			javax.swing.UIManager.put("nimbusBlueGrey", bgLight);
			javax.swing.UIManager.put("nimbusFocus", accent);
			javax.swing.UIManager.put("nimbusLightBackground", new Color(45, 50, 58));
			javax.swing.UIManager.put("nimbusSelectionBackground", accentDim);
			javax.swing.UIManager.put("nimbusSelectedText", Color.WHITE);
			javax.swing.UIManager.put("text", textColor);
			javax.swing.UIManager.put("info", bgMid);
			javax.swing.UIManager.put("nimbusInfoBlue", bgLight);
			javax.swing.UIManager.put("menuText", textColor);
			javax.swing.UIManager.put("textForeground", textColor);
			javax.swing.UIManager.put("controlText", textColor);
			javax.swing.UIManager.put("infoText", textColor);

			javax.swing.UIManager.put("Table.background", new Color(45, 50, 58));
			javax.swing.UIManager.put("Table.foreground", textColor);
			javax.swing.UIManager.put("Table.alternateRowColor", new Color(50, 55, 65));
			javax.swing.UIManager.put("Table[Enabled+Selected].textForeground", Color.WHITE);
			javax.swing.UIManager.put("Table[Enabled+Selected].textBackground", accentDim);
			javax.swing.UIManager.put("TableHeader.background", bgLight);
			javax.swing.UIManager.put("TableHeader.foreground", textColor);

			javax.swing.UIManager.put("TextField.background", controlBg);
			javax.swing.UIManager.put("TextField.foreground", textColor);
			javax.swing.UIManager.put("ComboBox.background", controlBg);
			javax.swing.UIManager.put("ComboBox.foreground", textColor);

			javax.swing.UIManager.put("ProgressBar.foreground", accent);
			javax.swing.UIManager.put("nimbusOrange", accent);

			javax.swing.UIManager.put("TabbedPane.background", bgMid);
			javax.swing.UIManager.put("TabbedPane.foreground", textColor);

			javax.swing.UIManager.put("OptionPane.messageForeground", textColor);
			javax.swing.UIManager.put("Panel.background", bgMid);
			javax.swing.UIManager.put("Label.foreground", textColor);

		} catch (Exception ignored) {}

		GuiMain app;
		try {
			app = new GuiMain();
			app.setVisible(true);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, 
										  "An error prevented the application from starting properly!",
										  "Error!",
										  JOptionPane.ERROR_MESSAGE);
			System.out.println("Some error occured in ui");
			e.printStackTrace();
		}
		
		
	}	
}
