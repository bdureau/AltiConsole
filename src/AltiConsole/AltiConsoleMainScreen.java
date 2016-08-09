package AltiConsole;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
//import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;


import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import l10n.DebugTranslator;
import l10n.ResourceBundleTranslator;
import l10n.Translator;

import javax.swing.*;

import config.Splash;
import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;
import org.jdesktop.swingx.JXList;

import config.LicenseDialog;
import config.UserPref;
import config.Utils;
import AltiConfig.AltiConfigData;
import AltiConfig.AltiConfigDlg;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * A demo showing the addition and removal of multiple datasets / renderers.
 */

@SuppressWarnings("serial")
public class AltiConsoleMainScreen extends JFrame implements ActionListener {

	/** The plot. */
	public XYPlot plot;

	/** The index of the last dataset added. */
	//private int datasetIndex = 0;

	// Menu
	private JMenuBar jJMenuBar = null;
	private JMenu fileMenu = null;
	private JMenu optionMenu = null;
	private JMenu altiConfigMenu = null;
	private JMenu helpMenu = null;
	private JMenuItem loadDataMenuItem = null;
	private JMenuItem eraseAllDataMenuItem = null;
	private JMenuItem saveASMenuItem = null;
	private JMenuItem exitMenuItem = null;
	private JMenuItem preferencesMenuItem = null;
	private JMenuItem retrieveAltiConfigMenuItem = null;
	private JMenuItem licenseMenuItem = null;
	private JMenuItem aboutMenuItem = null;
	private JMenuItem onLineHelpMenuItem = null;
	private JMenuItem uploadFirmwareMenuItem = null;

	// List
	private JXList flightList = null;
	public JComboBox comPorts;
	public JComboBox serialRates;
	private JLabel comPortsLabel;
	private JLabel serialRatesLabel;
	public JTextArea txtLog;
	private JScrollPane scrollPane;
	private JButton retrieveFlights;
	private DefaultListModel listData;
	public AltimeterSerial Serial = null;
	final Translator trans;
	public JFreeChart chart;
	public JLabel apogeeAltitudeLabel;
	public JLabel flightNbrLabel;
	public JLabel nbrPointLabel;
	public JLabel mainAltitudeLabel;

	/**
	 * Constructs main screen of the application.
	 * 
	 * @param title
	 *            the frame title.
	 */
	public AltiConsoleMainScreen(final String title) {

		super(title);
		trans = Application.getTranslator();

		// //////// Menu code starts her //////////////
		// File
		fileMenu = new JMenu();
		fileMenu.setText(trans.get("AltiConsoleMainScreen.File"));

		// Load data
		loadDataMenuItem = new JMenuItem();
		loadDataMenuItem.setText(trans
				.get("AltiConsoleMainScreen.RetrieveFlightData"));
		loadDataMenuItem.setActionCommand("RETRIEVE_FLIGHT");
		loadDataMenuItem.addActionListener(this);

		eraseAllDataMenuItem = new JMenuItem();
		eraseAllDataMenuItem.setText(trans
				.get("AltiConsoleMainScreen.EraseFlightData"));
		eraseAllDataMenuItem.setActionCommand("ERASE_FLIGHT");
		eraseAllDataMenuItem.addActionListener(this);

		// Save as
		saveASMenuItem = new JMenuItem();
		saveASMenuItem.setText(trans
				.get("AltiConsoleMainScreen.saveFlightData"));
		saveASMenuItem.setActionCommand("SAVE_FLIGHT");
		saveASMenuItem.addActionListener(this);

		// Exit
		exitMenuItem = new JMenuItem();
		exitMenuItem.setText(trans.get("AltiConsoleMainScreen.Exit"));
		exitMenuItem.setActionCommand("EXIT");
		exitMenuItem.addActionListener(this);

		fileMenu.add(saveASMenuItem);

		fileMenu.add(loadDataMenuItem);
		fileMenu.add(eraseAllDataMenuItem);
		fileMenu.add(exitMenuItem);

		// Option menu
		optionMenu = new JMenu(trans.get("AltiConsoleMainScreen.Option"));
		preferencesMenuItem = new JMenuItem(
				trans.get("AltiConsoleMainScreen.Preferences"));
		preferencesMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("pref\n");
				Preferences.showPreferences(AltiConsoleMainScreen.this);
				//LicenseDialog.showPreferences(AltiConsoleMainScreen.this);
				System.out.println("change units\n");
				String Units;
				System.out.println(UserPref.getAppUnits());
				if (Utils.equals(UserPref.getAppUnits(), "Unit.Metrics"))
					Units = trans.get("Unit.Metrics");
				else
					Units = trans.get("Unit.Imperial");

				chart.getXYPlot()
						.getRangeAxis()
						.setLabel(
								trans.get("AltiConsoleMainScreen.altitude")
										+ " (" + Units + ")");
				if (Serial.getConnected()) {
					RetrievingFlight();
				}

			}
		});

		optionMenu.add(preferencesMenuItem);

		// Configuration menu
		altiConfigMenu = new JMenu(
				trans.get("AltiConsoleMainScreen.ConfigAltimeter"));

		retrieveAltiConfigMenuItem = new JMenuItem(
				trans.get("AltiConsoleMainScreen.RetrieveAltiConfig"));
		retrieveAltiConfigMenuItem.setActionCommand("RETRIEVE_ALTI_CFG");
		retrieveAltiConfigMenuItem.addActionListener(this);
		altiConfigMenu.add(retrieveAltiConfigMenuItem);

		uploadFirmwareMenuItem = new JMenuItem(
				trans.get("AltiConsoleMainScreen.UploadFirmware"));

		uploadFirmwareMenuItem.setActionCommand("UPLOAD_FIRMWARE");
		uploadFirmwareMenuItem.addActionListener(this);
		altiConfigMenu.add(uploadFirmwareMenuItem);

		// Help
		helpMenu = new JMenu(trans.get("AltiConsoleMainScreen.Help"));
		jJMenuBar = new JMenuBar();

		// Manual
		onLineHelpMenuItem = new JMenuItem(
				trans.get("AltiConsoleMainScreen.onLineHelp"));
		onLineHelpMenuItem.setActionCommand("ON_LINE_HELP");
		onLineHelpMenuItem.addActionListener(this);

		// license
		licenseMenuItem = new JMenuItem(
				trans.get("AltiConsoleMainScreen.license"));
		licenseMenuItem.setActionCommand("LICENSE");
		licenseMenuItem.addActionListener(this);

		// AboutScreen
		aboutMenuItem = new JMenuItem();
		aboutMenuItem.setText(trans.get("AltiConsoleMainScreen.About"));
		aboutMenuItem.setActionCommand("ABOUT");
		aboutMenuItem.addActionListener(this);

		helpMenu.add(onLineHelpMenuItem);
		helpMenu.add(licenseMenuItem);
		helpMenu.add(aboutMenuItem);

		jJMenuBar.add(fileMenu);
		jJMenuBar.add(optionMenu);
		jJMenuBar.add(altiConfigMenu);
		jJMenuBar.add(helpMenu);
		this.setJMenuBar(jJMenuBar);

		// ///// end of Menu code

		// Button
		retrieveFlights = new JButton();
		retrieveFlights.setText(trans
				.get("AltiConsoleMainScreen.RetrieveFlights"));
		retrieveFlights.setActionCommand("RETRIEVE_FLIGHT");
		retrieveFlights.addActionListener(this);
		retrieveFlights.setToolTipText(trans
				.get("AltiConsoleMainScreen.ttipRetrieveFlight"));

		// combo serial rate
		String[] serialRateStrings = { "300", "1200", "2400", "4800", "9600",
				"14400", "19200", "28800", "38400", "57600", "115200" };

		serialRatesLabel = new JLabel(
				trans.get("AltiConsoleMainScreen.comPortSpeed") + " ");
		serialRates = new JComboBox();
		System.out.println(UserPref.getDefComSpeed() + "\n");
		for (int i = 0; i < serialRateStrings.length; i++) {
			serialRates.addItem(serialRateStrings[i]);

			if (Utils.equals(UserPref.getDefComSpeed(), serialRateStrings[i])) {
				serialRates.setSelectedIndex(i);
			}
		}
		serialRates.setToolTipText(trans
				.get("AltiConsoleMainScreen.ttipChoosePortSpeed"));

		comPortsLabel = new JLabel(trans.get("AltiConsoleMainScreen.port")
				+ " ");
		comPorts = new JComboBox();

		comPorts.setActionCommand("comPorts");
		comPorts.addActionListener(this);
		comPorts.setToolTipText(trans
				.get("AltiConsoleMainScreen.ttipChooseAltiport"));
		listData = new DefaultListModel();

		flightList = new JXList(listData);
		flightList.addListSelectionListener(new ValueReporter());

		JPanel TopPanelLeft = new JPanel();
		TopPanelLeft.setLayout(new BorderLayout());
		TopPanelLeft.add(comPortsLabel, BorderLayout.WEST);
		TopPanelLeft.add(comPorts, BorderLayout.EAST);

		JPanel TopPanelMiddle = new JPanel();
		TopPanelMiddle.setLayout(new BorderLayout());
		TopPanelMiddle.add(retrieveFlights, BorderLayout.WEST);

		JPanel TopPanelRight = new JPanel();
		TopPanelRight.setLayout(new BorderLayout());
		TopPanelRight.add(serialRatesLabel, BorderLayout.WEST);
		TopPanelRight.add(serialRates, BorderLayout.EAST);

		JPanel TopPanel = new JPanel();
		TopPanel.setLayout(new BorderLayout());

		TopPanel.add(TopPanelRight, BorderLayout.EAST);
		TopPanel.add(TopPanelMiddle, BorderLayout.CENTER);
		TopPanel.add(TopPanelLeft, BorderLayout.WEST);
		JPanel MiddlePanel = new JPanel();
		MiddlePanel.setLayout(new BorderLayout());

		MiddlePanel.add(TopPanel, BorderLayout.NORTH);
		MiddlePanel.add(flightList, BorderLayout.WEST);

		String Units;
		if (Utils.equals(UserPref.getAppUnits(), "Unit.Metrics"))
			Units = trans.get("Unit.Metrics");
		else
			Units = trans.get("Unit.Imperial");

		chart = ChartFactory.createXYLineChart(
				trans.get("AltiConsoleMainScreen.Title"),
				trans.get("AltiConsoleMainScreen.time"),
				trans.get("AltiConsoleMainScreen.altitude") + " (" + Units
						+ ")", null);

		chart.setBackgroundPaint(Color.white);
		System.out.println(chart.getSubtitle(0));

		this.plot = chart.getXYPlot();
		this.plot.setBackgroundPaint(Color.lightGray);
		this.plot.setDomainGridlinePaint(Color.white);
		this.plot.setRangeGridlinePaint(Color.white);

		final ValueAxis axis = this.plot.getDomainAxis();
		axis.setAutoRange(true);

		final NumberAxis rangeAxis2 = new NumberAxis("Range Axis 2");
		rangeAxis2.setAutoRangeIncludesZero(false);

		final ChartPanel chartPanel = new ChartPanel(chart);

		MiddlePanel.add(chartPanel, BorderLayout.CENTER);

		JPanel InfoPanel = new JPanel(new MigLayout("fill"));
		InfoPanel.add(
				new JLabel(trans.get("AltiConsoleMainScreen.ApogeeAltitude")),
				"gapright para");
		apogeeAltitudeLabel = new JLabel();
		InfoPanel.add(apogeeAltitudeLabel, "growx");
		InfoPanel.add(
				new JLabel(trans.get("AltiConsoleMainScreen.MainAltitude")),
				"gapright para");
		mainAltitudeLabel = new JLabel();
		InfoPanel.add(mainAltitudeLabel, "growx");
		flightNbrLabel = new JLabel();
		
		InfoPanel.add(
				new JLabel(trans.get("AltiConsoleMainScreen.NbrOfPoint")),
				"growx");
		nbrPointLabel = new JLabel();
		InfoPanel.add(nbrPointLabel, "wrap rel,growx");
		

		txtLog = new JTextArea(5, 70);
		txtLog.setEditable(false);
		txtLog.setAutoscrolls(true);

		scrollPane = new JScrollPane(txtLog);
		scrollPane.setAutoscrolls(true);
		// BottomPanel.add(scrollPane, BorderLayout.WEST);
		InfoPanel.add(scrollPane, "span");
		// MiddlePanel.add(BottomPanel, BorderLayout.SOUTH);
		MiddlePanel.add(InfoPanel, BorderLayout.SOUTH);
		setContentPane(MiddlePanel);
		try {
			try {
				Serial = new AltimeterSerial(this);

				Serial.searchForPorts();
			} catch (UnsatisfiedLinkError e) {
				System.out.println("USB Library rxtxSerial.dll Not Found");
				System.out.println("Exception:" + e.toString() + ":"
						+ e.getMessage());
				System.out.println(e.toString());
				JOptionPane
						.showMessageDialog(
								null,
								"You must copy the appropriate rxtxSerial.dll \n "
										+ "to your local 32 bit or 64 bitjava JRE installation\n\n"
										+ " .../arduino-1.0.1-windows/arduino-1.0.1/rxtxSerial.dll\n"
										+ "to\n C:/Program Files (x86)/Java/jre7/bin/rxtxSerial.dll\n"
										+ "also right click Properties->Unblock",
								trans.get("AltiConsoleMainScreen.InstallationProblem"),
								JOptionPane.WARNING_MESSAGE);
			}
		} catch (NoClassDefFoundError e) {
			System.out.println("Missing RXTXcomm.jar in java installation");
			System.out.println("Exception:" + e.toString() + ":"
					+ e.getMessage());
			System.out.println(e.toString());
			JOptionPane
					.showMessageDialog(
							null,
							"You must copy RXTXcomm.jar from the Arduino software\n "
									+ "to your local 32 bit java JRE installation\n\n"
									+ " .../arduino-1.0.1-windows/arduino-1.0.1/lib/RXTXcomm.jar\n"
									+ "to\n C:/Program Files (x86)/Java/jre7/lib/ext/RXTXcomm.jar\n"
									+ "also right click Properties->Unblock",
							trans.get("AltiConsoleMainScreen.InstallationProblem"),
							JOptionPane.WARNING_MESSAGE);
		}
	}

	public AltiConfigData retrieveAltiConfig() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		AltiConfigData Alticonfig = null;
		Alticonfig = new AltiConfigData();

		if (Serial.getConnected() == false) {
			boolean ret = false;
			ret = ConnectToAlti();
			if (!ret) {
				System.out.println("retrieveAltiConfig - Data retrieval timed out1\n");
				this.setCursor(Cursor.getDefaultCursor());
				return null;
			}
		}
		Serial.clearInput();
		//Serial.DataReady = false;
		Serial.setDataReady(false);
		// send command to switch off the continuity test
		Serial.writeData("b;\n");
		System.out.println("b;\n");
		long timeOut = 10000;
		if (UserPref.getRetrievalTimeout()!=null && UserPref.getRetrievalTimeout()!="")
			timeOut = Long.decode(UserPref.getRetrievalTimeout());
		long startTime = System.currentTimeMillis();
		//while (!Serial.getDataReady()) {
		while (true) {
			long currentTime = System.currentTimeMillis();
			if(Serial.getDataReady())
				break;
			if ((currentTime - startTime) > timeOut) {
				// This is some sort of data retrieval timeout
				System.out.println("retrieveAltiConfig - Data retrieval timed out2\n");
				if (Serial.getDataReady())
					System.out.println("Data is true\n");
				else
					System.out.println("Data is false\n");
				JOptionPane.showMessageDialog(null,
						trans.get("AltiConsoleMainScreen.dataTimeOut")+"0",
						trans.get("AltiConsoleMainScreen.ConnectionError"),
						JOptionPane.ERROR_MESSAGE);
				this.setCursor(Cursor.getDefaultCursor());
				
				return null;
			}
		}
		if (Serial.AltiCfg != null) {
			System.out.println("Reading altimeter config\n");
			System.out.println(Serial.AltiCfg.getUnits() + "\n");
			Alticonfig = Serial.AltiCfg;

		}
		this.setCursor(Cursor.getDefaultCursor());
		return Alticonfig;
	}

	public boolean ConnectToAlti() {
		long timeOut = 10000;
		if (UserPref.getRetrievalTimeout()!=null && UserPref.getRetrievalTimeout()!="")
			timeOut = Long.decode(UserPref.getRetrievalTimeout());
		System.out.println("timeOut "+timeOut);
		if (Serial.getConnected() == true) {
			System.out.println("I am already connected... let's disconnect\n");
			Serial.disconnect();
			//Serial.DataReady = false;
			Serial.setDataReady(false);
		}

		/* Connect */
		Serial.connect();
		//while (Serial.getConnected() != true);
		if (Serial.getConnected() == true) {
			if (Serial.initIOStream() == true) {
				if (Serial.initListener() == true) {
					// send command to switch off the continuity test
					//Serial.DataReady = false;
					Serial.clearInput();
					Serial.setDataReady(false);
					Serial.writeData("f;\n");
					System.out.println("f;\n");
					long startTime; // 
					Serial.lastReceived= System.currentTimeMillis();
					while (Serial.getDataReady()!=true) {
						long currentTime = System.currentTimeMillis();
						startTime = Serial.lastReceived;
						if ((currentTime - startTime) > timeOut) {
							// This is some sort of data retrieval timeout
							System.out.println("ConnectToAlti - Data retrieval timed out\n");
							this.setCursor(Cursor.getDefaultCursor());
							JOptionPane
									.showMessageDialog(
											null,
											trans.get("AltiConsoleMainScreen.dataTimeOut"),
											trans.get("AltiConsoleMainScreen.ConnectionError"),
											JOptionPane.ERROR_MESSAGE);
							return false;
						}
					}
					System.out.println("ConnectToAlti - "+Serial.commandRet);
					//System.out.println("Data retrieval timed out\n");
					//Serial.DataReady = false;
					Serial.clearInput();
					Serial.setDataReady(false);
					Serial.writeData("f;\n");
					//startTime = System.currentTimeMillis();
					Serial.lastReceived= System.currentTimeMillis();
					System.out.println("ConnectToAlti - 2");
					if (Serial.getDataReady()) 
						System.out.println("ConnectToAlti - 2 true");
					while (Serial.getDataReady()!=true) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						long currentTime = System.currentTimeMillis();
						startTime = Serial.lastReceived;
						if ((currentTime - startTime) > timeOut) {
							// This is some sort of data retrieval timeout
							System.out.println("ConnectToAlti - Data retrieval timed out1\n");
							this.setCursor(Cursor.getDefaultCursor());
							JOptionPane
									.showMessageDialog(
											null,
											trans.get("AltiConsoleMainScreen.dataTimeOut"),
											trans.get("AltiConsoleMainScreen.ConnectionError"),
											JOptionPane.ERROR_MESSAGE);
							
							return false;
						}
					}
					System.out.println("ConnectToAlti - 2 I am out");
					//Serial.DataReady = false;
					Serial.setDataReady(false);

				}
			}
		}
		return true;
	}

	public void DisconnectFromAlti() {
		if (Serial.getConnected() == true) {
			// turn continuity on
			Serial.writeData("g;\n");
			Serial.disconnect();
			//Serial.DataReady = false;
			Serial.setDataReady(false);
		}
	}

	public boolean ErasingFlight() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (Serial.getConnected() == false) {
			boolean ret = false;
			ret = ConnectToAlti();
			if (!ret) {
				System.out.println("Data retrieval timed out\n");
				this.setCursor(Cursor.getDefaultCursor());
				return false;
			}
		}
		Serial.writeData("e;\n");
		this.setCursor(Cursor.getDefaultCursor());
		return true;
	}

	public boolean RetrievingFlight() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		listData = null;

		listData = new DefaultListModel();

		if (Serial.getConnected() == false) {
			boolean ret = false;
			ret = ConnectToAlti();
			if (!ret) {
				System.out.println("Data retrieval timed out1\n");
				JOptionPane.showMessageDialog(null,
						trans.get("AltiConsoleMainScreen.dataTimeOut"),
						trans.get("AltiConsoleMainScreen.ConnectionError"),
						JOptionPane.ERROR_MESSAGE);
				this.setCursor(Cursor.getDefaultCursor());
				return false;
			}
		}
		// check the connection
		//Serial.DataReady = false;
		Serial.setDataReady(false);
		Serial.writeData("h;\n");
		long startTime; // = System.currentTimeMillis();
		Serial.lastReceived = System.currentTimeMillis();
		while (!Serial.getDataReady()) {
			long currentTime = System.currentTimeMillis();
			startTime = Serial.lastReceived;
			if ((currentTime - startTime) > 130000) {
				// This is some sort of data retrieval timeout
				System.out.println("Data retrieval timed out2 flight\n");
				JOptionPane.showMessageDialog(null,
						trans.get("AltiConsoleMainScreen.dataTimeOut"),
						trans.get("AltiConsoleMainScreen.ConnectionError"),
						JOptionPane.ERROR_MESSAGE);
				this.setCursor(Cursor.getDefaultCursor());
				return false;
			}
		}
				
		Serial.initFlightData(UserPref.getAppUnits());

		// get the flight data
		//Serial.DataReady = false;
		Serial.setDataReady(false);
		Serial.writeData("a;\n");

		Serial.lastReceived = System.currentTimeMillis();
		while (!Serial.getDataReady()) {
			long currentTime = System.currentTimeMillis();
			startTime = Serial.lastReceived;
			if ((currentTime - startTime) > 130000) {
				// This is some sort of data retrieval timeout
				System.out.println("Data retrieval timed out3\n");
				JOptionPane.showMessageDialog(null,
						trans.get("AltiConsoleMainScreen.dataTimeOut"),
						trans.get("AltiConsoleMainScreen.ConnectionError"),
						JOptionPane.ERROR_MESSAGE);
				this.setCursor(Cursor.getDefaultCursor());
				return false;
			}
		}
		System.out.println("done retrieving flight\n");
		List<String> AllFlightNames2;

		AllFlightNames2 = Serial.MyFlight.getAllFlightNames2();

		for (String z : AllFlightNames2) {

			listData.addElement(z);
		}

		flightList.setModel(listData);
		flightList.setAutoCreateRowSorter(true);
		flightList.toggleSortOrder();
		flightList.setSortOrder(SortOrder.ASCENDING);

		flightList.clearSelection();

		flightList.setSelectedIndex(0);
		if (Serial.MyFlight.FlightExist("Flight 1")) {
			plot.setDataset(0, Serial.MyFlight.GetFlightData("Flight 1"));
		}
		this.setCursor(Cursor.getDefaultCursor());
		return true;

	}

	public boolean SavingFlight() {
		JFileChooser saveFile = new JFileChooser();

		String currentFligtName = null;
		if (Serial.MyFlight == null) {
			System.out.println("Serial.MyFlight is null ");
			return false;
		}
		if (flightList == null) {
			System.out.println("flightList is null ");
			return false;
		}
		if (!flightList.isValid()) {
			System.out.println("flightList is invalid ");
			return false;
		}
		if (flightList.getComponentCount() > 0) {
			System.out.println("nbr of flight "
					+ flightList.getComponentCount() + "\n");
			System.out.println("flightname "
					+ flightList.getSelectedValue().toString() + "\n");
			currentFligtName = flightList.getSelectedValue().toString();
		} else
			return false;

		saveFile.setSelectedFile(new File(flightList.getSelectedValue()
				.toString() + ".csv"));
		if (saveFile.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = saveFile.getSelectedFile();

			try {
				// get flight data
				XYSeriesCollection flightData = null;
				flightData = Serial.MyFlight.GetFlightData(flightList
						.getSelectedValue().toString());
				int numberOfPoints;
				numberOfPoints = flightData.getSeries(0).getItemCount();

				// Create file
				FileWriter fstream = new FileWriter(file.getName());
				BufferedWriter out = new BufferedWriter(fstream);
				out.write("time, altitude\n");
				int i;
				for (i = 0; i < numberOfPoints; i++) {
					long X, Y;
					X = flightData.getSeries(0).getX(i).longValue();
					Y = flightData.getSeries(0).getY(i).longValue();
					System.out.println(X + "," + Y + "\n");
					out.write(X + "," + Y + "\n");
				}
				// Close the output stream
				out.close();
			} catch (Exception e) {// Catch exception if any
				System.err.println("Error: " + e.getMessage());
			}
		}
		return true;
	}

	/**
	 * Handles all the actions.
	 * 
	 * @param e
	 *            the action event.
	 */
	public void actionPerformed(final ActionEvent e) {
		final Translator trans = Application.getTranslator();

		if (e.getActionCommand().equals("EXIT")) {
			System.out.println("exit and disconnecting\n");
			if (JOptionPane.showConfirmDialog(this,
					trans.get("AltiConsoleMainScreen.ClosingWindow"),
					trans.get("AltiConsoleMainScreen.ReallyClosing"),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
				DisconnectFromAlti();
				System.exit(0);
			}
		} else if (e.getActionCommand().equals("ABOUT")) {
			AboutDialog.showPreferences(AltiConsoleMainScreen.this);
		}
		// ERASE_FLIGHT
		else if (e.getActionCommand().equals("ERASE_FLIGHT")) {

			if (JOptionPane.showConfirmDialog(this,
					trans.get("AltiConsoleMainScreen.eraseAllflightData"),
					trans.get("AltiConsoleMainScreen.eraseAllflightDataTitle"),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
				System.out.println("erasing flight\n");
				ErasingFlight();
			}

		} else if (e.getActionCommand().equals("RETRIEVE_FLIGHT")) {

			System.out.println("retrieving flight\n");
			RetrievingFlight();

		} else
		// SAVE_FLIGHT
		if (e.getActionCommand().equals("SAVE_FLIGHT")) {
			System.out.println("Saving current flight\n");
			SavingFlight();
		} else
		// RETRIEVE_ALTI_CFG
		if (e.getActionCommand().equals("RETRIEVE_ALTI_CFG")) {
			System.out.println("retrieving alti config\n");
			AltiConfigData pAlticonfig = retrieveAltiConfig();
			if (pAlticonfig != null)
				AltiConfigDlg.showPreferences(AltiConsoleMainScreen.this,
						pAlticonfig, Serial);

		}
		// LICENSE
		else if (e.getActionCommand().equals("LICENSE")) {

			LicenseDialog.showPreferences(AltiConsoleMainScreen.this);
		}
		// comPorts
		else if (e.getActionCommand().equals("comPorts")) {
			DisconnectFromAlti();
			String currentPort;
			//e.
			currentPort = (String) comPorts.getSelectedItem();
			if(Serial!=null)
			Serial.searchForPorts();

			System.out.println("We have a new selected value for comport\n");
			comPorts.setSelectedItem(currentPort);
		}
		// UPLOAD_FIRMWARE
		else if (e.getActionCommand().equals("UPLOAD_FIRMWARE")) {
			System.out.println("upload firmware\n");
			//make sure to disconnect first
			DisconnectFromAlti();
			JFileChooser fc = new JFileChooser(); 
			String hexfile = null;
			File startFile = new File(System.getProperty("user.dir"));
			//FileNameExtensionFilter filter;
			
			fc.setDialogTitle( "Select firmware");
			//fc.set
			fc.setCurrentDirectory(startFile);
			//fc.addChoosableFileFilter(new FileNameExtensionFilter("*.HEX", "hex"));
			fc.setFileFilter(new FileNameExtensionFilter("*.hex", "hex"));
			//fc.fil
			int action = fc.showOpenDialog(SwingUtilities
					.windowForComponent(this));
			if (action == JFileChooser.APPROVE_OPTION) {
				hexfile = fc.getSelectedFile().getAbsolutePath();
			}
			if (hexfile != null) {

				String exefile = UserPref.getAvrdudePath();

				String conffile = UserPref.getAvrdudeConfigPath();

				String opts = " -v -v -v -v -patmega328p -carduino -P\\\\.\\"
						+ (String) this.comPorts.getSelectedItem()
						+ " -b115200 -D -V ";

				String cmd = exefile + " -C" + conffile + opts + " -Uflash:w:"
						+ hexfile + ":i";
				System.out.println(cmd);

				try {
					Process p = Runtime.getRuntime().exec(cmd);
					AfficheurFlux fluxSortie = new AfficheurFlux(
							p.getInputStream(), this);
					AfficheurFlux fluxErreur = new AfficheurFlux(
							p.getErrorStream(), this);

					new Thread(fluxSortie).start();
					new Thread(fluxErreur).start();

					p.waitFor();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
		}
		// ON_LINE_HELP
		else if (e.getActionCommand().equals("ON_LINE_HELP")) {
			Desktop d = Desktop.getDesktop();
			System.out.println("Online help \n");
			try {
				d.browse(new URI(trans.get("help.url")));
			} catch (URISyntaxException e1) {

				System.out.println("Illegal URL:  " + trans.get("help.url")
						+ " " + e1.getMessage());
			} catch (IOException e1) {
				System.out.println("Unable to launch browser: "
						+ e1.getMessage());
			}

		}
	}

	private class ValueReporter implements ListSelectionListener {
		/**
		 * You get three events in many cases -- one for the deselection of the
		 * originally selected entry, one indicating the selection is moving,
		 * and one for the selection of the new entry. In the first two cases,
		 * getValueIsAdjusting returns true, thus the test below when only the
		 * third case is of interest.
		 */
		public void valueChanged(ListSelectionEvent event) {
			System.out.println(flightList.toString());
			if (!event.getValueIsAdjusting())
				if (Serial.MyFlight != null)
					if (flightList != null)
						if (flightList.isValid()) {
							System.out.println("Selected flight: "
									+ flightList.getSelectedValue().toString());

							plot.setDataset(0, Serial.MyFlight
									.GetFlightData(flightList
											.getSelectedValue().toString()));
						}
		}
	}

	/**
	 * Initializes the localization system.
	 */
	private static void initializeL10n() {

		Translator t;
		Locale userLocale = UserPref.getApplicationLocale();

		if (userLocale != null) {
			Locale.setDefault(userLocale);
		}
		try {

			t = new ResourceBundleTranslator("l10n.messages");
		} catch (MissingResourceException e) {
			t = new ResourceBundleTranslator("messages");
		}

		if (Locale.getDefault().getLanguage().equals("xx")) {
			t = new DebugTranslator(t);
		}

		Application.setBaseTranslator(t);
	}

	/**
	 * Starting point for the altimeter console application.
	 * 
	 * @param args
	 *            ignored.
	 */
	public static void main(final String[] args) {

		initializeL10n();
		final Translator trans = Application.getTranslator();
		Splash.init();

		// "Altimeter console"
		final AltiConsoleMainScreen alticonsole = new AltiConsoleMainScreen(
				trans.get("AltiConsoleMainScreen.Title"));

		Image icone;

		URL myURL;
		myURL = AltiConsoleMainScreen.class
				.getResource("/pix/bear_altimeters-small.png");
		if (myURL == null)
			myURL = AltiConsoleMainScreen.class
					.getResource("/bear_altimeters-small.png");
		icone = Toolkit.getDefaultToolkit().getImage(myURL);

		if (icone == null) {
			icone = Toolkit.getDefaultToolkit().getImage(
					AltiConsoleMainScreen.class
							.getResource("/bear_altimeters-small.png"));

		}
		alticonsole.pack();
		alticonsole.setIconImage(icone);
		alticonsole.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		alticonsole.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {

				System.out.println("exit and disconnecting\n");
				if (JOptionPane.showConfirmDialog(alticonsole,
						trans.get("AltiConsoleMainScreen.ClosingWindow"),
						trans.get("AltiConsoleMainScreen.ReallyClosing"),
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					alticonsole.DisconnectFromAlti();
					System.exit(0);
				}
			}
		});
		RefineryUtilities.centerFrameOnScreen(alticonsole);
		alticonsole.setVisible(true);

	}

}

class AfficheurFlux implements Runnable {

	private final InputStream inputStream;
	private AltiConsoleMainScreen window;

	AfficheurFlux(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	AfficheurFlux(InputStream inputStream, AltiConsoleMainScreen window1) {
		this.window = window1;
		this.inputStream = inputStream;
	}

	private BufferedReader getBufferedReader(InputStream is) {
		return new BufferedReader(new InputStreamReader(is));
	}

	@Override
	public void run() {
		BufferedReader br = getBufferedReader(inputStream);
		String ligne = "";
		try {
			while ((ligne = br.readLine()) != null) {
				System.out.println(ligne);
				window.txtLog.append(ligne + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
