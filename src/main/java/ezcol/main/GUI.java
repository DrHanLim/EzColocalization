package ezcol.main;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.Macro;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.plugin.BrowserLauncher;
import ij.plugin.OverlayLabels;
import ij.plugin.frame.Recorder;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.AutoThresholder.Method;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;

import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;

import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import java.awt.GridLayout;

import javax.swing.JTextArea;

import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ActionEvent;

import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JFormattedTextField;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;
import java.beans.PropertyChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import ezcol.align.BackgroundProcessor;
import ezcol.cell.CellFilterDialog;
import ezcol.debug.Debugger;
import ezcol.debug.ExceptionHandler;
import ezcol.files.FilesIO;
import ezcol.metric.BasicCalculator;
import ezcol.metric.StringCompiler;
import ezcol.visual.visual2D.ProgressGlassPane;

import javax.swing.event.ChangeEvent;

/**
 * 
 * @author Huanjie Sheng, Weston Stauffer
 *
 */
public class GUI extends PluginStatic
		implements ActionListener, ChangeListener, ImageListener, WindowListener /*, PropertyChangeListener*/ {

	private static GUI staticGUI;
	private static boolean adaptZoom = true;

	// the main window and its main components of plugin
	private JFrame mainframe;
	private JPanel superPanel;
	private JTabbedPane tabs;
	private JLabel warning;
	private JButton analyzeBtn;

	// used to restore
	private static ImagePlus[] oldImps = new ImagePlus[imps.length];

	// indexes for tabs
	public static final int INPUTTAB = 0, FILTERTAB = 1, VISUALTAB = 2, ANALYSISTAB = 3;
	// all tabs
	private JPanel inputTab;
	private JPanel filterTab;
	private JPanel visualTab;
	private JPanel analysisTab;

	// RightClick UI
	JPopupMenu rightPopMenu = new JPopupMenu();

	// Input UI
	private String directory;
	// image UI
	private Vector<ImageInfo> info = new Vector<ImageInfo>();
	private boolean imgIO = true, imgUpdate = false;
	@SuppressWarnings("unchecked")
	private JComboBox<ImageInfo>[] imgCombbxes = new JComboBox[imps.length];
	private JLabel[] imgTitles = new JLabel[imps.length];
	// alignment UI
	private JCheckBox[] alignedChckbxes = new JCheckBox[align_chckes.length];
	private JComboBox<?>[] alignTholdCombbxes = new JComboBox<?>[alignThold_combs.length];
	private JButton doAlignmentBtn, resetAlignmentBtn, previewTholdBtn;
	private boolean showThold;
	private static final String[] PREVIEW_THOLD = { "Show threshold(s)", "Hide threshold(s)" };

	// cell filters UI
	private JTextField[] filterSizeTexts = new JTextField[SIZE_FILTERS.length];
	private JTextField[] filterRangeTexts = new JTextField[filter_combs.length];
	@SuppressWarnings("rawtypes")
	private JComboBox[] filterCombbxes = new JComboBox[filter_combs.length];
	private JCheckBox waterShedChckbx;
	private JButton tryIDCells, btnMoreFilters;

	// Visualization UI
	// matrix heat maps UI
	private JCheckBox matrixChckbx;
	private JComboBox<String> matrixMetricCombbx;
	private JComboBox<String> matrixStatsCombbx;
	private JSpinner[] matrixFTSpinners = new JSpinner[MAX_NREPORTERS];
	private JPanel[] paneMatrixSpinners = new JPanel[matrixFTSpinners.length];

	// scatterplot UI
	private JCheckBox scatterplotChckbx;
	// heatmaps UI
	private ButtonGroup heatmapTypeRadio = new ButtonGroup();
	private JRadioButton[] heatmapRadiobtns = new JRadioButton[HEATMAPOPTS.length];
	private JCheckBox[] heatmapChckbxes = new JCheckBox[heatmapColor_combs.length];
	@SuppressWarnings("rawtypes")
	private JComboBox[] heatmapColorCombbxes = new JComboBox[heatmapColor_combs.length];
	private JButton previewVisual;

	// Global Calibration

	// indexes for subtabs
	private static final int METRICSUBTAB = 0, MTOSSUBTAB = 2, DISTSUBTAB = 3, OTHERSUBTAB = 1, TOSSUBTAB = 2;
	// four colors are the colors of
	// 1.metricSubTab, 2.mTOSSubTab, 3.distanceSubTab, 4.otherSubTab
	// The order should be the same as the indexes above
	public static final Color[] SUBTABCOLORS = { new Color(230, 230, 250), new Color(250, 235, 215),
			new Color(255, 255, 224), new Color(240, 255, 240) };
	public static final Color[] SUBLABELCOLORS = { new Color(150, 150, 255), Color.ORANGE, Color.YELLOW,
			new Color(100, 255, 100) };
	// Analysis Tab
	private JTabbedPane analysisSubTabs;
	// analysis operator is used to run the analyses
	private AnalysisOperator analysis;
	// outputs UI
	public static final int[] METRICS_2D_ONLY = { PCC, SRCC };
	private JPanel metricSubTab;
	private JRadioButton[][] metricTholdRadiobtns = new JRadioButton[METRICNAMES.length][METRIC_THOLDS.length];
	private ButtonGroup[] metricRadioGroup = new ButtonGroup[METRICNAMES.length];
	private JLabel[] lblMetricTholds = new JLabel[METRIC_THOLDS.length];
	private JSpinner[][] allFTSpinners = new JSpinner[MAX_NREPORTERS][METRICNAMES.length];
	private JLabel[] lblFTunits = new JLabel[allFTSpinners.length];

	private JCheckBox[] metricChckbxes = new JCheckBox[METRICNAMES.length];
	private JCheckBox[] otherChckbxes = new JCheckBox[OTHERNAMES.length];

	// Obsolete
	// Distances to subcellular location UI
	// As a legacy not shown
	private JPanel distSubTab;
	private static final String[] DIST_CHOICES = convertNames(PluginStatic.DIST_CHOICES,
			new String[] { "Particle threshold algorithms:", "Number of selected fractions:" });
	private ButtonGroup distTypeRadios = new ButtonGroup();
	private JRadioButton[] distRadios = new JRadioButton[DIST_CHOICES.length];
	private JSlider[] distFTs = new JSlider[numOfDistFTs.length];
	private JFormattedTextField[] distFTLabels = new JFormattedTextField[numOfDistFTs.length];
	@SuppressWarnings("rawtypes")
	private JComboBox[] distThresholders = new JComboBox<?>[whichDistTholds.length];

	// Custom UI
	public static final int SUCCESS = 0, FAILURE = 1, RUN = 2, SKIP = 3;
	private static final String[] CUSTOM_STATUS = { "Succeeded", "Failed", "Run", "Skip" };
	private static final Color[] CUSTOM_COLORS = { new Color(0, 128, 0), Color.RED, Color.BLACK, Color.BLUE };
	private static final String CUSTOM_URL = "https://docs.oracle.com/javase/7/docs/api/overview-summary.html";
	private JPanel othersubTab;
	private JScrollPane customCodeScrollpnl;
	private JTextArea customCodeTextbx;
	private JButton runCustom, resetCustom, helpCustom;
	private JCheckBox customMetricChckbxes;

	// Obsolete
	// 3DTOS UI
	private JPanel d3TOSSubTab;
	private JComboBox<ImageInfo> imgd3TOS;
	private JSpinner[] d3TOSSpinners;
	private JCheckBox chckbxd3TOS;

	// Output UI
	private JCheckBox[] outputMetricChckbxes = new JCheckBox[OUTPUTMETRICS.length];
	private JCheckBox[] outputOptChckbxes = new JCheckBox[OUTPUTOTHERS.length];

	// about tab
	private static final String ABOUT_TEXT = "Please refer to and cite:\n" + "Stauffer, W., Sheng, H., and Lim, H.N.\n"
			+ "EzColocalization: An ImageJ plugin for visualizing and measuring colocalization \nin cells and organisms (2018)";
	private JPanel aboutPanel;
	private JButton email;
	// please double check that pluginName,if changed, doesn't contain reserved
	// symbols
	private static final String URIEmail = "mailto:" + CONTACT 
										+ "?subject=Question%20about%20your%20" 
										+ pluginName.replaceAll(" ", "%20") 
										+ "%20plugin";

	// progress glasspane
	private ProgressGlassPane progressGlassPane;

	public GUI() {

		if (IJ.isMacro())
			return;
		if (staticGUI != null) {
			WindowManager.toFront(staticGUI.mainframe);
			return;
		}
		imgIO = false;
		loadPreference();
		checkParams();
		gui();
		imgIO = true;
		staticGUI = this;
	}

	/**
	 * This is used for macro to generate GUI
	 * 
	 * @param macro
	 */
	public GUI(boolean macro) {
		// macroGUI();
	}

	/**
	 * This is used for test only
	 * 
	 * @param test
	 */
	public GUI(int test) {
		gui();
		updateTicked(true);
	}

	public static GUI getGUI() {
		return staticGUI;
	}

	/**
	 * Create the frame.
	 */
	private void gui() {

		mainframe = new JFrame(pluginName);
		if (!IJ.isWindows())
			mainframe.setSize(500, 630);
		else
			mainframe.setSize(420, 670);
		mainframe.setLocation(0,
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - mainframe.getHeight() / 2));
		mainframe.setResizable(false);
		if (FilesIO.getResource("coloc.gif") != null)
			mainframe.setIconImage(new ImageIcon(FilesIO.getResource("coloc.gif")).getImage());
		mainframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Rectangle ijBounds = getIJScreenBounds();
		mainframe.setBounds(100 + ijBounds.x, 100 + ijBounds.y, mainframe.getWidth(), mainframe.getHeight());

		// This is the content panel
		superPanel = new JPanel();
		superPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainframe.setContentPane(superPanel);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE };
		superPanel.setLayout(gbl_contentPane);

		// This is the main tabbedpane
		tabs = new JTabbedPane(JTabbedPane.TOP);
		// tabs.setOpaque(true);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.gridheight = 2;
		gbc_tabbedPane.gridwidth = 4;
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		superPanel.add(tabs, gbc_tabbedPane);
		// tabs.getLayout();

		// all functional tabs are initialized in the corresponding methods
		// below
		// These must be done after mainframe is initialized
		inputTab();
		filterTab();
		visualTab();
		analysisTab();
		// outputTab();

		// tab listener is added here to avoid calling it while inserting new
		// tabs
		tabs.addChangeListener(this);
		// These must be done after mainframe is initialized
		aboutPane();
		rightMenu();
		mainMenu();

		progressGlassPane = new ProgressGlassPane();
		// Let GlassPane block further inputs
		progressGlassPane.requestFocusInWindow();
		progressGlassPane.addMouseListener(new MouseAdapter() {
		});
		progressGlassPane.addMouseMotionListener(new MouseMotionAdapter() {
		});
		progressGlassPane.addKeyListener(new KeyAdapter() {
		});
		mainframe.setGlassPane(progressGlassPane);
		mainframe.addWindowListener(this);

		// updateGUI first so nChannels is reflected
		updateGUI();
		// read in images
		updateImgList(null, null);
		// clear the thresholds of selected images
		resetThr();
		// update selections based on selected images
		updateSelection();

		updateTicked();
		if (nbImgs != 0)
			adaptZoom();

		// initialize input images options
		// add none as a chioce to all input combo boxes
		/**
		 * I just don't understand what the ImageJ's authors were thinking Why
		 * is a listener being added in a static way???
		 */
		ImagePlus.addImageListener(this);

		mainframe.pack();
		mainframe.setVisible(true);
	}

	private void mainMenu() {
		JMenuBar menuBar;
		JMenu menu;
		JMenuItem menuItem;
		String menuString;
		String[] menuStrings;

		menuBar = new JMenuBar();
		mainframe.setJMenuBar(menuBar);

		{
			// Build second menu in the menu bar.
			menu = new JMenu("File");
			menu.getAccessibleContext().setAccessibleDescription("This menu has functions related to file");
			menuBar.add(menu);

			menuString = "Open...";
			menuItem = new JMenuItem(menuString);
			menuItem.getAccessibleContext().setAccessibleDescription(menuString);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					IJ.doCommand(((JMenuItem) (e.getSource())).getText());
				}
			});
			menu.add(menuItem);

			menuString = "Test Images";
			menuItem = new JMenuItem(menuString);
			menuItem.getAccessibleContext().setAccessibleDescription(menuString);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					/*
					 * int i = 1; while (true) { try { if (FilesIO.getImagePlus(
					 * "Sample image C" + (i++) + ".tif", true) == null) break;
					 * else IJ.wait(50); } catch (Exception e1) { // TODO
					 * Auto-generated catch block IJ.error(
					 * "Error while loading sample images"); break; } }
					 */
					try {
						FilesIO.openTiffs(true);
					} catch (IOException | URISyntaxException e1) {
						// TODO Auto-generated catch block
						// e1.printStackTrace();
						IJ.error("Error while loading sample images");
					}
				}
			});
			menu.add(menuItem);

			menuString = "Exit";
			menuItem = new JMenuItem(menuString);
			menuItem.getAccessibleContext().setAccessibleDescription(menuString);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					exit();
				}
			});
			menu.add(menuItem);
		}

		{
			menu = new JMenu("Results");
			menu.getAccessibleContext().setAccessibleDescription("This menu has functions related to file");
			menuBar.add(menu);
			menuItem = new JMenuItem("Save All...");
			menuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					String dir = saveAllWindows();
					if (Recorder.record) {
						MacroHandler.saveRecorder(dir);
					}
				}

			});
			menu.add(menuItem);

			menuItem = new JMenuItem("Close All");
			menuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					closeAllWindows();
					if (Recorder.record) {
						MacroHandler.closeRecorder();
					}
				}

			});
			menu.add(menuItem);

			menuItem = new JMenuItem("Select All");
			menuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					chooseAll();
					updateSelection();
				}

			});
			menu.add(menuItem);

			menuItem = new JMenuItem("Reset Params");
			menuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					resetParams();
					updateSelection();
				}

			});
			menu.add(menuItem);
		}

		{
			// Build the stack menu.
			menu = new JMenu("Stack");
			// menu.setMnemonic(KeyEvent.VK_A);
			menu.getAccessibleContext().setAccessibleDescription("Stack related functions incorporated from ImageJ");
			menuBar.add(menu);

			menuStrings = new String[] { "Images to Stack", "Concatenate...", "Import Sequence..." };
			// a group of JMenuItems
			for (String menuString1 : menuStrings) {
				menuItem = new JMenuItem(menuString1);
				menuItem.getAccessibleContext().setAccessibleDescription(menuString1);
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						IJ.doCommand(menuString1);
					}
				});
				menu.add(menuItem);
			}
		}

		{
			// Build the stack menu.
			menu = new JMenu("Settings");
			// menu.setMnemonic(KeyEvent.VK_A);
			menu.getAccessibleContext().setAccessibleDescription("PlugIn Settings");
			menuBar.add(menu);
			ButtonGroup group = new ButtonGroup();

			// Avoid the scope problem by creating a temporary array here
			// bad code
			int[] idxReporters = new int[MAX_NREPORTERS - MIN_NREPORTERS + 1];
			for (int i = MIN_NREPORTERS; i <= MAX_NREPORTERS; i++)
				idxReporters[i - MIN_NREPORTERS] = i;

			for (int i : idxReporters) {
				menuItem = new JRadioButtonMenuItem(i + " Reporter Channels");
				menuItem.setSelected(nReporters == i);
				// menuItem.setMnemonic(KeyEvent.VK_2);
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setNReporters(i);
					}
				});
				group.add(menuItem);
				menu.add(menuItem);
			}

			menu.addSeparator();

			menuItem = new JCheckBoxMenuItem("Auto Layout");
			menuItem.setSelected(adaptZoom);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					adaptZoom = ((JCheckBoxMenuItem) e.getSource()).isSelected();
				}
			});
			menu.add(menuItem);
			
			menuItem = new JMenuItem("Plugin Info...");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					IJ.error(pluginName, FilesIO.getPluginProperties());
				}
			});
			menu.add(menuItem);
		}

	}

	private void rightMenu() {

		// Right Click popMenu UI
		JMenuItem saveMenuItem, closeMenuItem, chooseMenuItem, resetMenuItem;

		saveMenuItem = new JMenuItem("Save All...");
		saveMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String dir = saveAllWindows();
				if (Recorder.record) {
					MacroHandler.saveRecorder(dir);
				}
			}

		});

		closeMenuItem = new JMenuItem("Close All");
		closeMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				closeAllWindows();
				if (Recorder.record) {
					MacroHandler.closeRecorder();
				}
			}

		});

		chooseMenuItem = new JMenuItem("Select All");
		chooseMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				chooseAll();
				updateSelection();
			}

		});

		resetMenuItem = new JMenuItem("Reset Params");
		resetMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				resetParams();
				updateSelection();
			}

		});

		rightPopMenu.add(saveMenuItem);
		rightPopMenu.add(closeMenuItem);
		rightPopMenu.add(chooseMenuItem);
		rightPopMenu.add(resetMenuItem);
		rightPopMenu.updateUI();

		MouseListener ml = new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if (e.getButton() == MouseEvent.BUTTON3)
					rightPopMenu.show(e.getComponent(), e.getX(), e.getY());
				else
					rightPopMenu.setVisible(false);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

		};
		tabs.addMouseListener(ml);
		analysisSubTabs.addMouseListener(ml);
		mainframe.addMouseListener(ml);
	}

	private void inputTab() {
		// inputTab starts here
		inputTab = new JPanel();
		// inputTab.setBorder(null);
		tabs.insertTab("Inputs", null, inputTab, null, INPUTTAB);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0 };
		gbl_panel.columnWeights = new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		inputTab.setLayout(gbl_panel);

		JLabel spacer0 = new JLabel(" ");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 4;
		gbc_label.gridy = 0;
		inputTab.add(spacer0, gbc_label);

		JLabel lblImps2Analyze = new JLabel("Images for analysis");
		lblImps2Analyze.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		GridBagConstraints gbc_lblImps2Analyze = new GridBagConstraints();
		gbc_lblImps2Analyze.insets = new Insets(0, 0, 5, 0);
		gbc_lblImps2Analyze.gridwidth = 11;
		gbc_lblImps2Analyze.gridx = 0;
		gbc_lblImps2Analyze.gridy = 1;
		inputTab.add(lblImps2Analyze, gbc_lblImps2Analyze);

		for (int i = 0; i < imgLabels.length; i++) {
			imgTitles[i] = new JLabel(imgLabels[i]);
			GridBagConstraints gbc_lblChannel = new GridBagConstraints();
			gbc_lblChannel.gridwidth = 5;
			gbc_lblChannel.insets = new Insets(0, 0, 5, 5);
			gbc_lblChannel.gridx = 0;
			gbc_lblChannel.gridy = 2 + i;
			inputTab.add(imgTitles[i], gbc_lblChannel);
		}

		/*
		 * JLabel lblOrRoiManager = new JLabel(" or Mask"); GridBagConstraints
		 * gbc_lblOrRoiManager = new GridBagConstraints();
		 * gbc_lblOrRoiManager.anchor = GridBagConstraints.NORTH;
		 * gbc_lblOrRoiManager.gridwidth = 5; gbc_lblOrRoiManager.insets = new
		 * Insets(0, 0, 5, 5); gbc_lblOrRoiManager.gridx = 0;
		 * gbc_lblOrRoiManager.gridy = 5; inputTab.add(lblOrRoiManager,
		 * gbc_lblOrRoiManager);
		 */

		JLabel lblAlignment = new JLabel(" ");
		GridBagConstraints gbc_lblAlignment = new GridBagConstraints();
		gbc_lblAlignment.insets = new Insets(0, 0, 5, 5);
		gbc_lblAlignment.anchor = GridBagConstraints.SOUTH;
		gbc_lblAlignment.gridwidth = 10;
		gbc_lblAlignment.gridx = 0;
		gbc_lblAlignment.gridy = 6;
		inputTab.add(lblAlignment, gbc_lblAlignment);

		// ComboBoxes for choosing images
		for (int ipic = 0; ipic < imgCombbxes.length; ipic++) {
			imgCombbxes[ipic] = new JComboBox<ImageInfo>();
			imgCombbxes[ipic].setRenderer(ImageInfo.RENDERER);
			imgCombbxes[ipic].putClientProperty("index", ipic);
			imgCombbxes[ipic].addActionListener(this);
			imgCombbxes[ipic].addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					// TODO Auto-generated method stub
					if (!showThold)
						return;
					@SuppressWarnings("unchecked")
					JComboBox<ImageInfo> img = (JComboBox<ImageInfo>) e.getSource();
					ImageInfo item = (ImageInfo) e.getItem();
					if (item.ID == ImageInfo.NONE_ID)
						return;
					int ipic = (int) img.getClientProperty("index");
					if (e.getStateChange() == ItemEvent.SELECTED) {
						// If the same image is selected for a channel with
						// higher priority
						// No need to update threshold
						for (int i = ipic + 1; i < imgCombbxes.length; i++) {
							if (imgCombbxes[i].getItemAt(imgCombbxes[i].getSelectedIndex()).equal(item))
								return;
						}
						updateThr(ipic, item);
					} else if (e.getStateChange() == ItemEvent.DESELECTED) {
						int index = -1;
						// Check to see if the same image is selected for
						// another channel
						for (int i = 0; i < imgCombbxes.length; i++) {
							if (i == ipic)
								continue;
							if (imgCombbxes[i].getItemAt(imgCombbxes[i].getSelectedIndex()).equal(item)) {
								index = i;
							}
						}
						if (index == -1)
							resetThr(item);
						else if (index < ipic)
							updateThr(index, item);
					}
				}

			});
			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.gridwidth = 5;
			gbc_comboBox.insets = new Insets(0, 0, 5, 10);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 5;
			gbc_comboBox.gridy = ipic + 2;
			inputTab.add(imgCombbxes[ipic], gbc_comboBox);
		}

		JLabel lblAlignmentOptions = new JLabel("Alignment and threshold options");
		lblAlignmentOptions.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.gridwidth = 11;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 7;
		inputTab.add(lblAlignmentOptions, gbc_lblNewLabel);

		JLabel lblThresholdAlgorithm = new JLabel("Select threshold algorithm");
		GridBagConstraints gbc_lblThresholdAlgorithmOr = new GridBagConstraints();
		gbc_lblThresholdAlgorithmOr.insets = new Insets(0, 0, 5, 10);
		gbc_lblThresholdAlgorithmOr.gridwidth = 5;
		gbc_lblThresholdAlgorithmOr.gridx = 5;
		gbc_lblThresholdAlgorithmOr.gridy = 8;
		inputTab.add(lblThresholdAlgorithm, gbc_lblThresholdAlgorithmOr);

		// Checkboxes for choosing channels to be aligned
		for (int iAlign = 0; iAlign < align_chckes.length; iAlign++) {
			alignedChckbxes[iAlign] = new JCheckBox("Align " + imgLabels[iAlign]);
			alignedChckbxes[iAlign].setSelected(align_chckes[iAlign]);
			GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
			gbc_chckbxNewCheckBox.gridwidth = 5;
			gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 5, 0);
			gbc_chckbxNewCheckBox.gridx = 0;
			gbc_chckbxNewCheckBox.gridy = 9 + iAlign;
			inputTab.add(alignedChckbxes[iAlign], gbc_chckbxNewCheckBox);
		}

		JLabel lblPhaseThold1 = new JLabel(imgLabels[imgLabels.length - 1]);
		GridBagConstraints gbc_lblPhasecontrast = new GridBagConstraints();
		gbc_lblPhasecontrast.gridwidth = 5;
		gbc_lblPhasecontrast.insets = new Insets(0, 0, 5, 0);
		gbc_lblPhasecontrast.gridx = 0;
		gbc_lblPhasecontrast.gridy = 9 + align_chckes.length;
		inputTab.add(lblPhaseThold1, gbc_lblPhasecontrast);

		JLabel lblPhaseThold2 = new JLabel("	");
		GridBagConstraints gbc_lblalignmentReference = new GridBagConstraints();
		gbc_lblalignmentReference.anchor = GridBagConstraints.NORTH;
		gbc_lblalignmentReference.gridwidth = 5;
		gbc_lblalignmentReference.insets = new Insets(0, 0, 5, 0);
		gbc_lblalignmentReference.gridx = 0;
		gbc_lblalignmentReference.gridy = 10 + align_chckes.length;
		inputTab.add(lblPhaseThold2, gbc_lblalignmentReference);

		// ComboBoxes for choosing desired threshold algorithms
		for (int iThold = 0; iThold < alignThold_combs.length; iThold++) {
			alignTholdCombbxes[iThold] = new JComboBox<String>(ALLTHOLDS);
			alignTholdCombbxes[iThold].addActionListener(this);
			GridBagConstraints gbc_comboBox_align = new GridBagConstraints();
			gbc_comboBox_align.gridwidth = 5;
			gbc_comboBox_align.insets = new Insets(0, 0, 5, 10);
			gbc_comboBox_align.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox_align.gridx = 5;
			gbc_comboBox_align.gridy = 9 + iThold;
			inputTab.add(alignTholdCombbxes[iThold], gbc_comboBox_align);
		}

		// Button to reset alignment
		previewTholdBtn = new JButton();
		previewTholdBtn.setText(PREVIEW_THOLD[showThold ? 1 : 0]);
		previewTholdBtn.addActionListener(this);
		GridBagConstraints gbc_btnPreviewThold = new GridBagConstraints();
		gbc_btnPreviewThold.gridwidth = 12;
		gbc_btnPreviewThold.insets = new Insets(0, 5, 5, 5);
		gbc_btnPreviewThold.gridx = 0;
		gbc_btnPreviewThold.gridy = 14;
		inputTab.add(previewTholdBtn, gbc_btnPreviewThold);

		/*
		 * JLabel label_6 = new JLabel(" "); GridBagConstraints gbc_label_6 =
		 * new GridBagConstraints(); gbc_label_6.insets = new Insets(0, 0, 0,
		 * 0); gbc_label_6.gridx = 4; gbc_label_6.gridy = 15;
		 * inputTab.add(label_6, gbc_label_6);
		 */

		// Button to preview alignment
		doAlignmentBtn = new JButton("Preview");
		doAlignmentBtn.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.gridwidth = 6;
		gbc_btnNewButton.insets = new Insets(0, 5, 5, 5);
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 16;
		inputTab.add(doAlignmentBtn, gbc_btnNewButton);

		// Button to reset alignment
		resetAlignmentBtn = new JButton("Reset");
		resetAlignmentBtn.addActionListener(this);
		GridBagConstraints gbc_btnReset = new GridBagConstraints();
		gbc_btnReset.gridwidth = 6;
		gbc_btnReset.insets = new Insets(0, 5, 5, 5);
		gbc_btnReset.gridx = 6;
		gbc_btnReset.gridy = 16;
		inputTab.add(resetAlignmentBtn, gbc_btnReset);
		// inputTab ends here
	}

	private void filterTab() {
		// filterTab starts here
		filterTab = new JPanel();
		tabs.insertTab("Cell Filters", null, filterTab, null, FILTERTAB);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 1.0,
				Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, Double.MIN_VALUE };
		filterTab.setLayout(gbl_panel_1);

		JLabel spacer9 = new JLabel(" ");
		GridBagConstraints gbc_label_2 = new GridBagConstraints();
		gbc_label_2.insets = new Insets(0, 0, 5, 5);
		gbc_label_2.gridx = 7;
		gbc_label_2.gridy = 1;
		filterTab.add(spacer9, gbc_label_2);

		JLabel lblCellFilters = new JLabel("Pre-watershed filter: ");
		// lblCellFilters.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		GridBagConstraints gbc_lblCellFilters = new GridBagConstraints();
		gbc_lblCellFilters.insets = new Insets(0, 5, 5, 0);
		gbc_lblCellFilters.gridwidth = 5;
		gbc_lblCellFilters.gridx = 1;
		gbc_lblCellFilters.gridy = 2;
		gbc_lblCellFilters.anchor = GridBagConstraints.WEST;
		filterTab.add(lblCellFilters, gbc_lblCellFilters);

		// Labels of size filters and TextFields for size filters
		for (int iFilter = 0; iFilter < SIZE_FILTERS.length; iFilter++) {
			JLabel lblCourseSizepixels = new JLabel(SIZE_FILTERS[iFilter]);
			GridBagConstraints gbc_lblCourseSizepixels = new GridBagConstraints();
			gbc_lblCourseSizepixels.gridwidth = 2;
			gbc_lblCourseSizepixels.insets = new Insets(0, 5, 5, 5);
			gbc_lblCourseSizepixels.gridx = 1;
			gbc_lblCourseSizepixels.gridy = 3 + iFilter * 2;
			filterTab.add(lblCourseSizepixels, gbc_lblCourseSizepixels);

			filterSizeTexts[iFilter] = new JTextField();
			filterSizeTexts[iFilter].setText(getFilterRange(filterMinSize_texts[iFilter], filterMaxSize_texts[iFilter]));
			GridBagConstraints gbc_textField = new GridBagConstraints();
			gbc_textField.insets = new Insets(0, 5, 5, 5);
			gbc_textField.gridwidth = 2;
			gbc_textField.fill = GridBagConstraints.HORIZONTAL;
			gbc_textField.gridx = 3;
			gbc_textField.gridy = 3 + iFilter * 2;
			filterTab.add(filterSizeTexts[iFilter], gbc_textField);
			filterSizeTexts[iFilter].setColumns(10);
		}

		// Checkbox for watershed
		waterShedChckbx = new JCheckBox("Watershed segmentation");
		GridBagConstraints gbc_chckbxWatershedSegmentationto = new GridBagConstraints();
		gbc_chckbxWatershedSegmentationto.anchor = GridBagConstraints.WEST;
		gbc_chckbxWatershedSegmentationto.gridwidth = 3;
		gbc_chckbxWatershedSegmentationto.insets = new Insets(0, 5, 5, 0);
		gbc_chckbxWatershedSegmentationto.gridx = 1;
		gbc_chckbxWatershedSegmentationto.gridy = 4;
		filterTab.add(waterShedChckbx, gbc_chckbxWatershedSegmentationto);

		// other filters
		JLabel lblFluorescenceAndShape = new JLabel("Physical and signal intensity parameter filters: ");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.gridwidth = 5;
		gbc_lblNewLabel_1.insets = new Insets(0, 5, 5, 0);
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 6;
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		filterTab.add(lblFluorescenceAndShape, gbc_lblNewLabel_1);

		// Names of all cell filters
		for (int iFilter = 0; iFilter < filterCombbxes.length; iFilter++) {
			JLabel lblCellFilter1 = new JLabel("Cell filter " + (iFilter + 1));
			GridBagConstraints gbc_lblCellFilter = new GridBagConstraints();
			gbc_lblCellFilter.gridwidth = 1;
			gbc_lblCellFilter.insets = new Insets(0, 5, 5, 5);
			gbc_lblCellFilter.gridx = 1;
			gbc_lblCellFilter.gridy = 7 + iFilter;
			filterTab.add(lblCellFilter1, gbc_lblCellFilter);

			filterCombbxes[iFilter] = new JComboBox<String>(filterStrings);
			filterCombbxes[iFilter].setSelectedIndex(filter_combs[iFilter]);
			GridBagConstraints gbc_comboBox_filters = new GridBagConstraints();
			gbc_comboBox_filters.gridwidth = 2;
			gbc_comboBox_filters.insets = new Insets(0, 5, 5, 5);
			gbc_comboBox_filters.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox_filters.gridx = 2;
			gbc_comboBox_filters.gridy = 7 + iFilter;
			filterTab.add(filterCombbxes[iFilter], gbc_comboBox_filters);

			filterRangeTexts[iFilter] = new JTextField();
			filterRangeTexts[iFilter].setText(getFilterRange(filterMinRange_texts[iFilter], filterMaxRange_texts[iFilter]));
			GridBagConstraints gbc_TextField_filterRanges = new GridBagConstraints();
			gbc_TextField_filterRanges.gridwidth = 2;
			gbc_TextField_filterRanges.insets = new Insets(0, 5, 5, 5);
			gbc_TextField_filterRanges.fill = GridBagConstraints.HORIZONTAL;
			gbc_TextField_filterRanges.gridx = 4;
			gbc_TextField_filterRanges.gridy = 7 + iFilter;
			filterTab.add(filterRangeTexts[iFilter], gbc_TextField_filterRanges);
			filterRangeTexts[iFilter].setColumns(10);
		}

		tryIDCells = new JButton("Preview");
		tryIDCells.addActionListener(this);
		GridBagConstraints gbc_btnPreview = new GridBagConstraints();
		gbc_btnPreview.gridwidth = 1;
		gbc_btnPreview.insets = new Insets(0, 5, 0, 5);
		gbc_btnPreview.gridx = 3;
		gbc_btnPreview.gridy = 15;
		filterTab.add(tryIDCells, gbc_btnPreview);

		btnMoreFilters = new JButton("More Filters...");
		btnMoreFilters.addActionListener(this);
		GridBagConstraints gbc_btnMoreFilters = new GridBagConstraints();
		gbc_btnMoreFilters.gridwidth = 1;
		gbc_btnMoreFilters.insets = new Insets(0, 5, 0, 5);
		gbc_btnMoreFilters.gridx = 5;
		gbc_btnMoreFilters.gridy = 15;
		filterTab.add(btnMoreFilters, gbc_btnMoreFilters);

		// filterTab ends here
	}

	private void visualTab() {

		// visualTab starts here
		visualTab = new JPanel();
		tabs.insertTab("Visualization", null, visualTab, null, VISUALTAB);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_2.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_2.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		visualTab.setLayout(gbl_panel_2);
		{// Heat maps options
			JLabel lblHeatMaps = new JLabel("Heat maps");
			lblHeatMaps.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
			GridBagConstraints gbc_lblHeatMaps = new GridBagConstraints();
			gbc_lblHeatMaps.gridwidth = 6;
			gbc_lblHeatMaps.insets = new Insets(5, 0, 5, 0);
			gbc_lblHeatMaps.gridx = 1;
			gbc_lblHeatMaps.gridy = 1;
			visualTab.add(lblHeatMaps, gbc_lblHeatMaps);

			JLabel spacer7 = new JLabel("Scaling options: ");
			GridBagConstraints gbc_label_4 = new GridBagConstraints();
			gbc_label_4.insets = new Insets(0, 5, 5, 0);
			gbc_label_4.gridwidth = 6;
			gbc_label_4.gridx = 1;
			gbc_label_4.gridy = 2;
			gbc_label_4.anchor = GridBagConstraints.WEST;
			visualTab.add(spacer7, gbc_label_4);

			for (int iHeat = 0; iHeat < heatmapRadiobtns.length; iHeat++) {
				heatmapRadiobtns[iHeat] = new JRadioButton(HEATMAPOPTS[iHeat]);
				heatmapTypeRadio.add(heatmapRadiobtns[iHeat]);
				if (heatmap_radio == iHeat)
					heatmapRadiobtns[iHeat].setSelected(true);
				GridBagConstraints gbc_rdbtnScaledByCell = new GridBagConstraints();
				gbc_rdbtnScaledByCell.anchor = GridBagConstraints.WEST;
				gbc_rdbtnScaledByCell.insets = new Insets(0, 0, 5, 0);
				gbc_rdbtnScaledByCell.gridwidth = 2;
				gbc_rdbtnScaledByCell.gridx = 1 + iHeat * 2;
				gbc_rdbtnScaledByCell.gridy = 3;
				visualTab.add(heatmapRadiobtns[iHeat], gbc_rdbtnScaledByCell);
			}

			JLabel lblColorMaps = new JLabel("Color maps:");
			GridBagConstraints gbc_lblColorMaps = new GridBagConstraints();
			gbc_lblColorMaps.insets = new Insets(0, 5, 5, 0);
			gbc_lblColorMaps.anchor = GridBagConstraints.WEST;
			gbc_lblColorMaps.gridwidth = 6;
			gbc_lblColorMaps.gridx = 1;
			gbc_lblColorMaps.gridy = 4;
			visualTab.add(lblColorMaps, gbc_lblColorMaps);

			// new Checkbox and Comboboxes for heatmaps
			for (int iHeat = 0; iHeat < heatmapColor_combs.length; iHeat++) {
				heatmapChckbxes[iHeat] = new JCheckBox("Channel " + (iHeat + 1));
				GridBagConstraints gbc_checkBox_thold = new GridBagConstraints();
				gbc_checkBox_thold.insets = new Insets(0, 5, 5, 5);
				gbc_checkBox_thold.gridwidth = 2;
				gbc_checkBox_thold.gridx = 1;
				gbc_checkBox_thold.gridy = 5 + iHeat;
				visualTab.add(heatmapChckbxes[iHeat], gbc_checkBox_thold);

				heatmapColorCombbxes[iHeat] = new JComboBox<String>(HEATMAPS);
				heatmapColorCombbxes[iHeat].setSelectedIndex(heatmapColor_combs[iHeat]);
				GridBagConstraints gbc_comboBox_color = new GridBagConstraints();
				gbc_comboBox_color.gridwidth = 4;
				gbc_comboBox_color.insets = new Insets(5, 5, 5, 5);
				gbc_comboBox_color.fill = GridBagConstraints.HORIZONTAL;
				gbc_comboBox_color.gridx = 3;
				gbc_comboBox_color.gridy = 5 + iHeat;
				visualTab.add(heatmapColorCombbxes[iHeat], gbc_comboBox_color);
			}

			JSeparator separator2 = new JSeparator();
			separator2.setForeground(Color.BLACK);
			separator2.setBackground(SUBTABCOLORS[METRICSUBTAB]);
			GridBagConstraints gbc_separator2 = new GridBagConstraints();
			gbc_separator2.insets = new Insets(0, 5, 5, 0);
			gbc_separator2.fill = GridBagConstraints.BOTH;
			gbc_separator2.gridwidth = 6;
			gbc_separator2.gridx = 1;
			gbc_separator2.gridy = 5 + heatmapColor_combs.length;
			visualTab.add(separator2, gbc_separator2);
		}

		{// scatter plots option
			JLabel lblScatterPlots = new JLabel("Scatterplots");
			lblScatterPlots.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
			GridBagConstraints gbc_lblSP = new GridBagConstraints();
			gbc_lblSP.gridwidth = 6;
			gbc_lblSP.insets = new Insets(0, 0, 5, 0);
			gbc_lblSP.gridx = 1;
			gbc_lblSP.gridy = 6 + heatmapColor_combs.length;
			visualTab.add(lblScatterPlots, gbc_lblSP);

			// Checkbox for Scatter Plot
			scatterplotChckbx = new JCheckBox("Cell pixel intensity scatterplots");
			scatterplotChckbx.setSelected(scatter_chck);
			GridBagConstraints gbc_chckbxPISP = new GridBagConstraints();
			gbc_chckbxPISP.gridwidth = 6;
			gbc_chckbxPISP.anchor = GridBagConstraints.SOUTHWEST;
			gbc_chckbxPISP.insets = new Insets(0, 10, 5, 0);
			gbc_chckbxPISP.gridx = 1;
			gbc_chckbxPISP.gridy = 7 + heatmapColor_combs.length;
			;
			visualTab.add(scatterplotChckbx, gbc_chckbxPISP);

			/*JLabel lblScatterNote = new JLabel(" (Use distribution to dictate metric choice)");
			GridBagConstraints gbc_label_3 = new GridBagConstraints();
			gbc_label_3.gridwidth = 6;
			gbc_label_3.insets = new Insets(0, 5, 5, 0);
			gbc_label_3.anchor = GridBagConstraints.NORTHWEST;
			gbc_label_3.gridx = 1;
			gbc_label_3.gridy = 8 + heatmapColor_combs.length;
			;
			visualTab.add(lblScatterNote, gbc_label_3);*/

			JSeparator separator = new JSeparator();
			separator.setForeground(Color.BLACK);
			separator.setBackground(SUBTABCOLORS[METRICSUBTAB]);
			GridBagConstraints gbc_separator = new GridBagConstraints();
			gbc_separator.insets = new Insets(0, 5, 5, 10);
			gbc_separator.fill = GridBagConstraints.BOTH;
			gbc_separator.gridwidth = 6;
			gbc_separator.gridx = 1;
			gbc_separator.gridy = 9 + heatmapColor_combs.length;
			;
			visualTab.add(separator, gbc_separator);
		}

		{// metric matrices options
			JLabel lblMatrices = new JLabel("Metric matrices");
			lblMatrices.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
			GridBagConstraints gbc_lblVM = new GridBagConstraints();
			gbc_lblVM.gridwidth = 6;
			gbc_lblVM.insets = new Insets(0, 0, 5, 0);
			gbc_lblVM.gridx = 1;
			gbc_lblVM.gridy = 10 + heatmapColor_combs.length;
			;
			visualTab.add(lblMatrices, gbc_lblVM);

			// Checkbox for Scatter Plot
			matrixChckbx = new JCheckBox("Matrices of different combinations of fractions");
			matrixChckbx.setSelected(matrix_chck);
			GridBagConstraints gbc_chckbxTM = new GridBagConstraints();
			gbc_chckbxTM.gridwidth = 6;
			gbc_chckbxTM.fill = GridBagConstraints.HORIZONTAL;
			gbc_chckbxTM.insets = new Insets(0, 10, 5, 0);
			gbc_chckbxTM.gridx = 1;
			gbc_chckbxTM.gridy = 11 + heatmapColor_combs.length;
			;
			visualTab.add(matrixChckbx, gbc_chckbxTM);

			for (int iFT = 0; iFT < matrixFTSpinners.length; iFT++) {

				paneMatrixSpinners[iFT] = new JPanel();

				JLabel ftLabel = new JLabel("Ch." + (iFT + 1) + " ");
				ftLabel.setToolTipText("Selected fraction percentage for channel" + (iFT + 1));
				/*
				 * GridBagConstraints gbc_label_ft = new GridBagConstraints();
				 * gbc_label_ft.gridwidth = 1; gbc_label_ft.insets = new
				 * Insets(0, 10, 5, 5); gbc_label_ft.anchor =
				 * GridBagConstraints.EAST; gbc_label_ft.gridx = 1+iFT*2;
				 * gbc_label_ft.gridy = 12 + heatmapChoices.length;;
				 */
				paneMatrixSpinners[iFT].add(ftLabel);

				matrixFTSpinners[iFT] = new JSpinner(new SpinnerNumberModel(matrixFT_spin[iFT], MINFT, MAXFT, STEPFT));
				JLabel unitLabel = new JLabel("%");
				paneMatrixSpinners[iFT].add(matrixFTSpinners[iFT]);
				paneMatrixSpinners[iFT].add(unitLabel);

				JSpinner.NumberEditor editor = new JSpinner.NumberEditor(matrixFTSpinners[iFT], "0");
				matrixFTSpinners[iFT].setEditor(editor);
				JFormattedTextField textField = ((JSpinner.NumberEditor) matrixFTSpinners[iFT].getEditor())
						.getTextField();
				textField.setEditable(true);
				DefaultFormatterFactory factory = (DefaultFormatterFactory) textField.getFormatterFactory();
				NumberFormatter formatter = (NumberFormatter) factory.getDefaultFormatter();
				formatter.setAllowsInvalid(false);

				GridBagConstraints gbc_Mspinner = new GridBagConstraints();
				gbc_Mspinner.gridwidth = 2;
				gbc_Mspinner.insets = new Insets(0, 0, 5, 0);
				gbc_Mspinner.anchor = GridBagConstraints.WEST;
				// gbc_Mspinner.fill = GridBagConstraints.HORIZONTAL;
				gbc_Mspinner.gridx = 1 + iFT * 2;
				gbc_Mspinner.gridy = 12 + heatmapColor_combs.length;
				;
				visualTab.add(paneMatrixSpinners[iFT], gbc_Mspinner);
			}

			{
				JLabel metricLabel = new JLabel("Metric: ");
				GridBagConstraints gbc_label_metric = new GridBagConstraints();
				gbc_label_metric.gridwidth = 1;
				gbc_label_metric.insets = new Insets(0, 10, 5, 5);
				gbc_label_metric.anchor = GridBagConstraints.EAST;
				gbc_label_metric.gridx = 1;
				gbc_label_metric.gridy = 13 + heatmapColor_combs.length;
				visualTab.add(metricLabel, gbc_label_metric);

				matrixMetricCombbx = new JComboBox<String>(matrixMetricList);
				matrixMetricCombbx.setSelectedIndex(matrixMetric_comb);
				GridBagConstraints gbc_comboBox_metric = new GridBagConstraints();
				gbc_comboBox_metric.gridwidth = 1;
				gbc_comboBox_metric.insets = new Insets(0, 0, 5, 0);
				gbc_comboBox_metric.fill = GridBagConstraints.HORIZONTAL;
				gbc_comboBox_metric.gridx = 2;
				gbc_comboBox_metric.gridy = 13 + heatmapColor_combs.length;
				visualTab.add(matrixMetricCombbx, gbc_comboBox_metric);
			}

			{
				JLabel statsLabel = new JLabel("Average: ");
				GridBagConstraints gbc_label_stats = new GridBagConstraints();
				gbc_label_stats.gridwidth = 1;
				gbc_label_stats.insets = new Insets(0, 10, 5, 5);
				gbc_label_stats.anchor = GridBagConstraints.EAST;
				gbc_label_stats.gridx = 3;
				gbc_label_stats.gridy = 13 + heatmapColor_combs.length;
				visualTab.add(statsLabel, gbc_label_stats);

				matrixStatsCombbx = new JComboBox<String>(STATS_METHODS);
				matrixStatsCombbx.setSelectedIndex(matrixStats_comb);
				GridBagConstraints gbc_comboBox_stats = new GridBagConstraints();
				gbc_comboBox_stats.gridwidth = 1;
				gbc_comboBox_stats.insets = new Insets(0, 0, 5, 0);
				gbc_comboBox_stats.fill = GridBagConstraints.HORIZONTAL;
				gbc_comboBox_stats.gridx = 4;
				gbc_comboBox_stats.gridy = 13 + heatmapColor_combs.length;
				visualTab.add(matrixStatsCombbx, gbc_comboBox_stats);
			}
		}

		previewVisual = new JButton("Preview");
		previewVisual.addActionListener(this);
		GridBagConstraints gbc_button_preview = new GridBagConstraints();
		gbc_button_preview.gridwidth = 6;
		gbc_button_preview.insets = new Insets(10, 5, 10, 5);
		gbc_button_preview.gridx = 1;
		gbc_button_preview.gridy = 14 + heatmapColor_combs.length;
		visualTab.add(previewVisual, gbc_button_preview);
		// visualTab ends here
	}

	@SuppressWarnings("serial")
	private void analysisTab() {

		// analysisTab starts here
		analysisTab = new JPanel();
		analysisTab.setLayout(new GridLayout(1, 0, 0, 0));

		// New analysisSubTab
		analysisSubTabs = new JTabbedPane(JTabbedPane.TOP);
		analysisTab.add(analysisSubTabs);

		metricSubTab = new JPanel();
		metricSubTab.setBackground(SUBTABCOLORS[METRICSUBTAB]);

		GridBagLayout gbl_panel_metricSubTab = new GridBagLayout();
		gbl_panel_metricSubTab.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_metricSubTab.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_metricSubTab.columnWeights = new double[] { Double.MIN_VALUE, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_metricSubTab.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, Double.MIN_VALUE };
		metricSubTab.setLayout(gbl_panel_metricSubTab);

		/*
		 * JLabel spacer6 = new JLabel(" "); GridBagConstraints gbc_label_7 =
		 * new GridBagConstraints(); gbc_label_7.insets = new Insets(0, 0, 5,
		 * 5); gbc_label_7.gridx = 3; gbc_label_7.gridy = 0;
		 * metricSubTab.add(spacer6, gbc_label_7);
		 */

		JLabel lblColocalizationMetrics = new JLabel("Colocalization metrics");
		lblColocalizationMetrics.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		GridBagConstraints gbc_lblColocalizationMetrics = new GridBagConstraints();
		gbc_lblColocalizationMetrics.gridwidth = gbl_panel_metricSubTab.columnWidths.length;
		gbc_lblColocalizationMetrics.insets = new Insets(0, 0, 5, 0);
		gbc_lblColocalizationMetrics.gridx = 0;
		gbc_lblColocalizationMetrics.gridy = 1;
		metricSubTab.add(lblColocalizationMetrics, gbc_lblColocalizationMetrics);

		JLabel lblMetricTholdsLabel = new JLabel("Threshold: ");
		lblMetricTholdsLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		GridBagConstraints gbc_lblMetricTholdsLabel = new GridBagConstraints();
		gbc_lblMetricTholdsLabel.insets = new Insets(0, 5, 0, 5);
		gbc_lblMetricTholdsLabel.gridx = 1;
		gbc_lblMetricTholdsLabel.gridy = 2;
		metricSubTab.add(lblMetricTholdsLabel, gbc_lblMetricTholdsLabel);

		for (int iThold = 0; iThold < METRIC_THOLDS.length; iThold++) {

			lblMetricTholds[iThold] = new JLabel(METRIC_THOLDS[iThold]);
			lblMetricTholds[iThold].setFont(new Font("Lucida Grande", Font.PLAIN, 15));
			lblMetricTholds[iThold].setToolTipText(METRIC_THOLDS_TIPS[iThold]);
			GridBagConstraints gbc_lblMetricTholds = new GridBagConstraints();
			gbc_lblMetricTholds.insets = new Insets(0, 5, 0, 5);
			gbc_lblMetricTholds.gridx = 2 + iThold;
			gbc_lblMetricTholds.gridy = 2;
			metricSubTab.add(lblMetricTholds[iThold], gbc_lblMetricTholds);
		}

		for (int ipic = 0; ipic < allFTSpinners.length; ipic++) {

			lblFTunits[ipic] = new JLabel("Ch." + (ipic + 1) + "%");
			lblFTunits[ipic].setFont(new Font("Lucida Grande", Font.PLAIN, 15));
			GridBagConstraints gbc_lblMetricTholdsFT2 = new GridBagConstraints();
			gbc_lblMetricTholdsFT2.insets = new Insets(0, 5, 0, 5);
			gbc_lblMetricTholdsFT2.gridx = 5 + ipic;
			gbc_lblMetricTholdsFT2.gridy = 2;
			metricSubTab.add(lblFTunits[ipic], gbc_lblMetricTholdsFT2);
		}

		for (int iMetric = 0; iMetric < METRICNAMES.length; iMetric++) {

			metricChckbxes[iMetric] = new JCheckBox(METRICNAMES[iMetric]);
			metricChckbxes[iMetric].setBackground(SUBTABCOLORS[METRICSUBTAB]);
			GridBagConstraints gbc_chckbxNewCheckBox_3 = new GridBagConstraints();
			gbc_chckbxNewCheckBox_3.anchor = GridBagConstraints.WEST;
			gbc_chckbxNewCheckBox_3.insets = new Insets(5, 5, 5, 5);
			gbc_chckbxNewCheckBox_3.gridx = 1;
			gbc_chckbxNewCheckBox_3.gridy = 3 + iMetric;
			metricSubTab.add(metricChckbxes[iMetric], gbc_chckbxNewCheckBox_3);

			metricRadioGroup[iMetric] = new ButtonGroup();

			for (int iThold = 0; iThold < METRIC_THOLDS.length; iThold++) {

				if (NO_THOLD_ALL.indexOf(iMetric) >= 0 && iThold == IDX_THOLD_ALL)
					continue;
				metricTholdRadiobtns[iMetric][iThold] = new JRadioButton();
				metricRadioGroup[iMetric].add(metricTholdRadiobtns[iMetric][iThold]);
				if (iThold == metricThold_radios[iMetric])
					metricTholdRadiobtns[iMetric][iThold].setSelected(true);
				metricTholdRadiobtns[iMetric][iThold].setBackground(SUBTABCOLORS[METRICSUBTAB]);
				metricTholdRadiobtns[iMetric][iThold].putClientProperty("iMetric", iMetric);

				if (iThold == IDX_THOLD_FT) {
					metricTholdRadiobtns[iMetric][iThold].addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							// TODO Auto-generated method stub
							Object origin = e.getSource();
							int iMetric = (Integer) ((JRadioButton) origin).getClientProperty("iMetric");
							if (e.getStateChange() == ItemEvent.SELECTED)
								for (int i = 0; i < allFTSpinners.length; i++)
									allFTSpinners[i][iMetric].setEnabled(true);
							else if (e.getStateChange() == ItemEvent.DESELECTED)
								for (int i = 0; i < allFTSpinners.length; i++)
									allFTSpinners[i][iMetric].setEnabled(false);
						}

					});
				}

				GridBagConstraints gbc_rdbtnLinear = new GridBagConstraints();
				gbc_rdbtnLinear.anchor = GridBagConstraints.CENTER;
				gbc_rdbtnLinear.insets = new Insets(5, 5, 5, 5);
				gbc_rdbtnLinear.gridx = 2 + iThold;
				gbc_rdbtnLinear.gridy = 3 + iMetric;
				metricSubTab.add(metricTholdRadiobtns[iMetric][iThold], gbc_rdbtnLinear);
			}

			if (metricRadioGroup[iMetric].getSelection() == null) {
				int iThold = 0;
				while (metricTholdRadiobtns[iMetric][iThold] == null)
					iThold++;
				metricTholdRadiobtns[iMetric][iThold].setSelected(true);
			}

			for (int ipic = 0; ipic < allFTSpinners.length; ipic++) {

				allFTSpinners[ipic][iMetric] = new JSpinner(
						new SpinnerNumberModel(allFT_spins[ipic][iMetric], MINFT, MAXFT, STEPFT));
				GridBagConstraints gbc_spinner = new GridBagConstraints();
				gbc_spinner.insets = new Insets(0, 5, 0, 5);
				gbc_spinner.gridx = 5 + ipic;
				gbc_spinner.gridy = 3 + iMetric;
				metricSubTab.add(allFTSpinners[ipic][iMetric], gbc_spinner);

				JSpinner.NumberEditor editor = new JSpinner.NumberEditor(allFTSpinners[ipic][iMetric], "0");
				allFTSpinners[ipic][iMetric].setEditor(editor);
				JFormattedTextField textField = ((JSpinner.NumberEditor) allFTSpinners[ipic][iMetric].getEditor())
						.getTextField();
				textField.setEditable(true);
				DefaultFormatterFactory factory = (DefaultFormatterFactory) textField.getFormatterFactory();
				NumberFormatter formatter = (NumberFormatter) factory.getDefaultFormatter();
				formatter.setAllowsInvalid(false);

				if (metricThold_radios[iMetric] != IDX_THOLD_FT) {
					allFTSpinners[ipic][iMetric].setEnabled(false);
				}
			}

		}

		JLabel spacer5 = new JLabel(" ");
		GridBagConstraints gbc_label_8 = new GridBagConstraints();
		gbc_label_8.insets = new Insets(5, 5, 5, 5);
		gbc_label_8.gridx = 3;
		gbc_label_8.gridy = 7;
		metricSubTab.add(spacer5, gbc_label_8);

		JLabel lblOtherMetrics = new JLabel("Other metrics");
		lblOtherMetrics.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		GridBagConstraints gbc_lblOtherMetrics = new GridBagConstraints();
		gbc_lblOtherMetrics.insets = new Insets(0, 0, 5, 0);
		gbc_lblOtherMetrics.gridwidth = gbl_panel_metricSubTab.columnWidths.length;
		gbc_lblOtherMetrics.gridx = 0;
		gbc_lblOtherMetrics.gridy = 8;
		metricSubTab.add(lblOtherMetrics, gbc_lblOtherMetrics);

		int NUMOFCOLUMNS = 2;
		for (int i = 0; i < otherChckbxes.length; i++) {
			otherChckbxes[i] = new JCheckBox(OTHERNAMES[i]);
			otherChckbxes[i].setBackground(SUBTABCOLORS[METRICSUBTAB]);
			GridBagConstraints gbc_chckbxNewCheckBox_11 = new GridBagConstraints();
			gbc_chckbxNewCheckBox_11.gridwidth = 2;
			gbc_chckbxNewCheckBox_11.insets = new Insets(5, 5, 5, 5);
			gbc_chckbxNewCheckBox_11.gridx = 1 + i % NUMOFCOLUMNS * 4;
			gbc_chckbxNewCheckBox_11.gridy = 9 + i / NUMOFCOLUMNS;
			metricSubTab.add(otherChckbxes[i], gbc_chckbxNewCheckBox_11);
		}
		otherChckbxes[otherChckbxes.length - 1].addActionListener(this);

		JSeparator separator = new JSeparator();
		separator.setForeground(Color.BLACK);
		separator.setBackground(SUBTABCOLORS[METRICSUBTAB]);
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.insets = new Insets(0, 0, 5, 0);
		gbc_separator.fill = GridBagConstraints.BOTH;
		gbc_separator.gridwidth = gbl_panel_metricSubTab.columnWidths.length - 2;
		gbc_separator.gridx = 1;
		gbc_separator.gridy = 10;
		metricSubTab.add(separator, gbc_separator);

		/*
		 * JLabel lblResults = new JLabel("Results"); lblResults.setFont(new
		 * Font("Lucida Grande", Font.PLAIN, 15)); GridBagConstraints
		 * gbc_lblResults = new GridBagConstraints(); gbc_lblResults.insets =
		 * new Insets(0, 0, 5, 0); gbc_lblResults.gridwidth = 8;
		 * gbc_lblResults.gridx = 0; gbc_lblResults.gridy = 11;
		 * metricSubTab.add(lblResults, gbc_lblResults);
		 */

		JLabel lblForSelectedMetrics = new JLabel("For values of selected metric(s):");
		lblForSelectedMetrics.setFont(new Font("Dialog", Font.PLAIN, 13));
		GridBagConstraints gbc_lblForSelectedMetrics = new GridBagConstraints();
		gbc_lblForSelectedMetrics.anchor = GridBagConstraints.CENTER;
		gbc_lblForSelectedMetrics.gridwidth = gbl_panel_metricSubTab.columnWidths.length;
		gbc_lblForSelectedMetrics.insets = new Insets(0, 10, 5, 5);
		gbc_lblForSelectedMetrics.gridx = 0;
		gbc_lblForSelectedMetrics.gridy = 11;
		metricSubTab.add(lblForSelectedMetrics, gbc_lblForSelectedMetrics);

		NUMOFCOLUMNS = 2;
		for (int i = 0; i < outputMetricChckbxes.length; i++) {
			outputMetricChckbxes[i] = new JCheckBox(OUTPUTMETRICS[i]);
			outputMetricChckbxes[i].setBackground(SUBTABCOLORS[METRICSUBTAB]);
			GridBagConstraints gbc_chckbxNewCheckBox_14 = new GridBagConstraints();
			gbc_chckbxNewCheckBox_14.anchor = GridBagConstraints.WEST;
			gbc_chckbxNewCheckBox_14.insets = new Insets(10, 10, 10, 10);
			gbc_chckbxNewCheckBox_14.gridwidth = 2;
			gbc_chckbxNewCheckBox_14.gridx = 1 + i % NUMOFCOLUMNS * 4;
			gbc_chckbxNewCheckBox_14.gridy = 12 + i / NUMOFCOLUMNS;
			metricSubTab.add(outputMetricChckbxes[i], gbc_chckbxNewCheckBox_14);
		}

		/*
		 * JLabel label_19 = new JLabel(" "); GridBagConstraints gbc_label_19 =
		 * new GridBagConstraints(); gbc_label_19.insets = new Insets(0, 0, 5,
		 * 5); gbc_label_19.gridx = 4; gbc_label_19.gridy = 7;
		 * outputTab.add(label_19, gbc_label_19);
		 */

		JLabel lblForCellIdentification = new JLabel("For cell identification function:");
		lblForCellIdentification.setFont(new Font("Dialog", Font.PLAIN, 13));
		GridBagConstraints gbc_lblForCellIdentification = new GridBagConstraints();
		gbc_lblForCellIdentification.anchor = GridBagConstraints.CENTER;
		gbc_lblForCellIdentification.gridwidth = gbl_panel_metricSubTab.columnWidths.length;
		gbc_lblForCellIdentification.insets = new Insets(5, 10, 5, 5);
		gbc_lblForCellIdentification.gridx = 0;
		gbc_lblForCellIdentification.gridy = 13;
		metricSubTab.add(lblForCellIdentification, gbc_lblForCellIdentification);

		NUMOFCOLUMNS = 2;
		for (int i = 0; i < outputOptChckbxes.length; i++) {
			outputOptChckbxes[i] = new JCheckBox(OUTPUTOTHERS[i]);
			outputOptChckbxes[i].setBackground(SUBTABCOLORS[METRICSUBTAB]);
			GridBagConstraints gbc_chckbxRegionsOfInterest = new GridBagConstraints();
			gbc_chckbxRegionsOfInterest.anchor = GridBagConstraints.WEST;
			gbc_chckbxRegionsOfInterest.insets = new Insets(10, 10, 10, 10);
			gbc_chckbxRegionsOfInterest.gridwidth = 2;
			gbc_chckbxRegionsOfInterest.gridx = 1 + i % NUMOFCOLUMNS * 4;
			gbc_chckbxRegionsOfInterest.gridy = 14 + i / NUMOFCOLUMNS;
			metricSubTab.add(outputOptChckbxes[i], gbc_chckbxRegionsOfInterest);
		}

		// Custom function
		othersubTab = new JPanel();
		othersubTab.setBackground(SUBTABCOLORS[OTHERSUBTAB]);

		GridBagLayout gbl_panel_9 = new GridBagLayout();
		gbl_panel_9.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_9.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_9.columnWeights = new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };
		gbl_panel_9.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		othersubTab.setLayout(gbl_panel_9);

		JLabel lblWriteYourOwn = new JLabel("Write your own function in Java below");
		lblWriteYourOwn.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		GridBagConstraints gbc_lblWriteYourOwn = new GridBagConstraints();
		gbc_lblWriteYourOwn.gridwidth = 12;
		gbc_lblWriteYourOwn.insets = new Insets(0, 0, 5, 0);
		gbc_lblWriteYourOwn.gridx = 0;
		gbc_lblWriteYourOwn.gridy = 1;
		othersubTab.add(lblWriteYourOwn, gbc_lblWriteYourOwn);

		customMetricChckbxes = new JCheckBox("", custom_chck);
		customMetricChckbxes.setHorizontalAlignment(JCheckBox.CENTER);
		customMetricChckbxes.setBackground(SUBTABCOLORS[OTHERSUBTAB]);
		if (custom_chck)
			setCustomStatus(RUN);
		else
			setCustomStatus(SKIP);
		customMetricChckbxes.addActionListener(this);
		GridBagConstraints gbc_lblchckbxCustom = new GridBagConstraints();
		gbc_lblchckbxCustom.gridwidth = 12;
		gbc_lblchckbxCustom.insets = new Insets(0, 0, 5, 5);
		// gbc_lblchckbxCustom.anchor = GridBagConstraints.WEST;
		gbc_lblchckbxCustom.gridx = 0;
		gbc_lblchckbxCustom.gridy = 2;
		othersubTab.add(customMetricChckbxes, gbc_lblchckbxCustom);

		customCodeTextbx = new JTextArea();
		// customCode.setSize(tabWidth1,subTabHeight);
		// if (!IJ.isWindows())customCode.setSize(tabWidth2, subTabHeight);
		customCode_text = StringCompiler.getDefaultCode();
		customCodeTextbx.setText(customCode_text);
		customCodeTextbx.setEditable(true);
		customCodeTextbx.setLineWrap(false);
		customCodeTextbx.setTabSize(StringCompiler.tabSize);
		// customCode.setPreferredSize(new Dimension(356, 112));

		customCodeScrollpnl = new JScrollPane(customCodeTextbx, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		customCodeScrollpnl.setPreferredSize(new Dimension(356, 112));

		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.gridheight = 8;
		gbc_textArea.gridwidth = 12;
		gbc_textArea.insets = new Insets(5, 10, 5, 10);
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 0;
		gbc_textArea.gridy = 3;
		othersubTab.add(customCodeScrollpnl, gbc_textArea);

		// The following block of code is retrieved from
		// https://web.archive.org/web/20100114122417/http://exampledepot.com/egs/javax.swing.undo/UndoText.html
		{
			final UndoManager undoManager = new UndoManager();
			// Listen for undo and redo events
			customCodeTextbx.getDocument().addUndoableEditListener(new UndoableEditListener() {
				public void undoableEditHappened(UndoableEditEvent evt) {
					undoManager.addEdit(evt.getEdit());
				}
			});

			// Create an undo action and add it to the text component
			customCodeTextbx.getActionMap().put("Undo", new AbstractAction("Undo") {
				public void actionPerformed(ActionEvent evt) {
					try {
						if (undoManager.canUndo()) {
							undoManager.undo();
						}
					} catch (CannotUndoException e) {
					}
				}
			});

			// Bind the undo action to ctrl-Z
			customCodeTextbx.getInputMap().put(KeyStroke.getKeyStroke("ctrl Z"), "Undo");

			// Create a redo action and add it to the text component
			customCodeTextbx.getActionMap().put("Redo", new AbstractAction("Redo") {
				public void actionPerformed(ActionEvent evt) {
					try {
						if (undoManager.canRedo()) {
							undoManager.redo();
						}
					} catch (CannotRedoException e) {
					}
				}
			});

			// Bind the redo action to ctrl-Y
			customCodeTextbx.getInputMap().put(KeyStroke.getKeyStroke("ctrl Y"), "Redo");
		}

		runCustom = new JButton("Compile");
		runCustom.addActionListener(this);
		GridBagConstraints gbc_btn_runCustom = new GridBagConstraints();
		gbc_btn_runCustom.gridwidth = 2;
		gbc_btn_runCustom.insets = new Insets(10, 10, 10, 10);
		gbc_btn_runCustom.gridx = 0;
		gbc_btn_runCustom.gridy = 12;
		othersubTab.add(runCustom, gbc_btn_runCustom);

		resetCustom = new JButton("Reset");
		resetCustom.addActionListener(this);
		GridBagConstraints gbc_btn_resetCustom = new GridBagConstraints();
		gbc_btn_resetCustom.gridwidth = 2;
		gbc_btn_resetCustom.insets = new Insets(10, 10, 10, 10);
		gbc_btn_resetCustom.gridx = 3;
		gbc_btn_resetCustom.gridy = 12;
		othersubTab.add(resetCustom, gbc_btn_resetCustom);

		helpCustom = new JButton("Help");
		helpCustom.addActionListener(this);
		GridBagConstraints gbc_btn_helpCustom = new GridBagConstraints();
		gbc_btn_helpCustom.gridwidth = 2;
		gbc_btn_helpCustom.insets = new Insets(10, 10, 10, 10);
		gbc_btn_helpCustom.gridx = 6;
		gbc_btn_helpCustom.gridy = 12;
		othersubTab.add(helpCustom, gbc_btn_helpCustom);

		// distSubTab as a legacy not shown
		/*
		{
			DecimalFormat noDigit = (DecimalFormat) (DecimalFormat.getInstance(Locale.UK));
			noDigit.setGroupingSize(0);
			noDigit.setMaximumFractionDigits(0);
			
			distSubTab = new JPanel();
			distSubTab.setBackground(SUBTABCOLORS[DISTSUBTAB]);

			GridBagLayout gbl_panel_dist = new GridBagLayout();
			gbl_panel_dist.columnWidths = new int[] { 0, 0, 0, 0 };
			gbl_panel_dist.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			gbl_panel_dist.columnWeights = new double[] { 0.0, 1.0, 1.0, Double.MIN_VALUE };
			gbl_panel_dist.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
					Double.MIN_VALUE };
			distSubTab.setLayout(gbl_panel_dist);

			JLabel spacer_11 = new JLabel(" ");
			GridBagConstraints gbc_label_11 = new GridBagConstraints();
			gbc_label_11.insets = new Insets(0, 0, 5, 5);
			gbc_label_11.gridx = 0;
			gbc_label_11.gridy = 0;
			distSubTab.add(spacer_11, gbc_label_11);

			JLabel lblDist2Pole = new JLabel("Distances");
			lblDist2Pole.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
			GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
			gbc_lblNewLabel_3.gridwidth = 3;
			gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 0);
			gbc_lblNewLabel_3.gridx = 0;
			gbc_lblNewLabel_3.gridy = 1;
			distSubTab.add(lblDist2Pole, gbc_lblNewLabel_3);

			JLabel lblDist2PoleNote = new JLabel(" Distance of all contiguous particles above threshold to");
			GridBagConstraints gbc_lblDistanceOfAll = new GridBagConstraints();
			gbc_lblDistanceOfAll.anchor = GridBagConstraints.WEST;
			gbc_lblDistanceOfAll.gridwidth = 3;
			gbc_lblDistanceOfAll.insets = new Insets(0, 0, 5, 0);
			gbc_lblDistanceOfAll.gridx = 0;
			gbc_lblDistanceOfAll.gridy = 2;
			distSubTab.add(lblDist2PoleNote, gbc_lblDistanceOfAll);

			JLabel lblDist2PoleNote2 = new JLabel(" nearest pole (bacilli only)");
			GridBagConstraints gbc_lblNearestPole = new GridBagConstraints();
			gbc_lblNearestPole.gridwidth = 3;
			gbc_lblNearestPole.anchor = GridBagConstraints.WEST;
			gbc_lblNearestPole.insets = new Insets(0, 0, 5, 0);
			gbc_lblNearestPole.gridx = 0;
			gbc_lblNearestPole.gridy = 3;
			distSubTab.add(lblDist2PoleNote2, gbc_lblNearestPole);

			JLabel spacer3 = new JLabel(" ");
			GridBagConstraints gbc_label_12 = new GridBagConstraints();
			gbc_label_12.insets = new Insets(0, 0, 5, 5);
			gbc_label_12.gridx = 0;
			gbc_label_12.gridy = 4;
			distSubTab.add(spacer3, gbc_label_12);

			for (int iDist = 0; iDist < distRadios.length; iDist++) {
				distRadios[iDist] = new JRadioButton(DIST_CHOICES[iDist]);
				distRadios[iDist].setBackground(SUBTABCOLORS[DISTSUBTAB]);
				if (whichDist == iDist)
					distRadios[iDist].setSelected(true);
				distTypeRadios.add(distRadios[iDist]);
				GridBagConstraints gbc_lblThresholdAlgorithms = new GridBagConstraints();
				gbc_lblThresholdAlgorithms.gridwidth = 3;
				gbc_lblThresholdAlgorithms.insets = new Insets(0, 0, 5, 5);
				gbc_lblThresholdAlgorithms.anchor = GridBagConstraints.WEST;
				gbc_lblThresholdAlgorithms.gridx = 0;
				gbc_lblThresholdAlgorithms.gridy = 5 + iDist * 4;
				distSubTab.add(distRadios[iDist], gbc_lblThresholdAlgorithms);
			}

			distRadios[1] = new JRadioButton();
			distRadios[1].setBackground(SUBTABCOLORS[DISTSUBTAB]);
			distRadios[1].setSelected(false);
			distTypeRadios.add(distRadios[1]);
			GridBagConstraints gbc_label_radioFT = new GridBagConstraints();
			gbc_label_radioFT.gridwidth = 3;
			gbc_label_radioFT.insets = new Insets(0, 0, 5, 5);
			gbc_label_radioFT.anchor = GridBagConstraints.WEST;
			gbc_label_radioFT.gridx = 0;
			gbc_label_radioFT.gridy = 9;
			distSubTab.add(distRadios[1], gbc_label_radioFT);

			for (int iDist = 0; iDist < distThresholders.length; iDist++) {
				JLabel lblChannel1Threshold = new JLabel("Channel " + (iDist + 1) + " :");
				GridBagConstraints gbc_lblChannel_4 = new GridBagConstraints();
				gbc_lblChannel_4.insets = new Insets(0, 10, 5, 10);
				gbc_lblChannel_4.gridx = 0;
				gbc_lblChannel_4.gridy = 6 + iDist;
				distSubTab.add(lblChannel1Threshold, gbc_lblChannel_4);

				distThresholders[iDist] = new JComboBox<String>(ALLDISTTHOLDS);
				GridBagConstraints gbc_comboBox_16 = new GridBagConstraints();
				gbc_comboBox_16.gridwidth = 1;
				gbc_comboBox_16.insets = new Insets(0, 10, 5, 10);
				gbc_comboBox_16.fill = GridBagConstraints.HORIZONTAL;
				gbc_comboBox_16.gridx = 1;
				gbc_comboBox_16.gridy = 6 + iDist;
				distSubTab.add(distThresholders[iDist], gbc_comboBox_16);
			}

			JLabel spacer3_2 = new JLabel(" ");
			GridBagConstraints gbc_label_32 = new GridBagConstraints();
			gbc_label_32.insets = new Insets(0, 0, 5, 5);
			gbc_label_32.gridx = 0;
			gbc_label_32.gridy = 8;
			distSubTab.add(spacer3_2, gbc_label_32);

			for (int iDist = 0; iDist < numOfDistFTs.length; iDist++) {
				JLabel lblNumChan1SF = new JLabel("Channel " + (iDist + 1) + " :");
				// lblNumChan1SF.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				GridBagConstraints gbc_label_15 = new GridBagConstraints();
				gbc_label_15.insets = new Insets(0, 10, 5, 10);
				gbc_label_15.anchor = GridBagConstraints.WEST;
				gbc_label_15.gridx = 0;
				gbc_label_15.gridy = 10 + iDist * 2;
				distSubTab.add(lblNumChan1SF, gbc_label_15);

				distFTs[iDist] = new JSlider();
				distFTs[iDist].setBackground(SUBTABCOLORS[DISTSUBTAB]);
				distFTs[iDist].setMinimum(MINDISTFT);
				distFTs[iDist].setMaximum(MAXDISTFT);
				distFTs[iDist].setValue(numOfDistFTs[iDist]);
				distFTs[iDist].addChangeListener(this);
				GridBagConstraints gbc_slider_2 = new GridBagConstraints();
				gbc_slider_2.insets = new Insets(0, 10, 5, 10);
				gbc_slider_2.gridwidth = 1;
				gbc_slider_2.fill = GridBagConstraints.HORIZONTAL;
				gbc_slider_2.gridx = 1;
				gbc_slider_2.gridy = 10 + iDist * 2;
				distSubTab.add(distFTs[iDist], gbc_slider_2);

				distFTLabels[iDist] = new JFormattedTextField(noDigit);
				distFTLabels[iDist].setValue(numOfDistFTs[iDist]);
				distFTLabels[iDist].setColumns(10);
				GridBagConstraints gbc_textField_12 = new GridBagConstraints();
				gbc_textField_12.insets = new Insets(0, 0, 5, 10);
				gbc_textField_12.fill = GridBagConstraints.HORIZONTAL;
				gbc_textField_12.gridx = 2;
				gbc_textField_12.gridy = 10 + iDist * 2;
				distSubTab.add(distFTLabels[iDist], gbc_textField_12);
			}
		}*/

		d3TOSSubTab = new JPanel();
		d3TOSSubTab.setBackground(SUBTABCOLORS[OTHERSUBTAB]);

		GridBagLayout gbl_panel_TOS = new GridBagLayout();
		gbl_panel_TOS.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_TOS.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, mainframe.getHeight() / 2, 0 };
		gbl_panel_TOS.columnWeights = new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };
		gbl_panel_TOS.rowWeights = new double[] { 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0,
				Double.MIN_VALUE };
		d3TOSSubTab.setLayout(gbl_panel_TOS);

		chckbxd3TOS = new JCheckBox("3D TOS", doD3TOS);
		chckbxd3TOS.setBackground(SUBTABCOLORS[OTHERSUBTAB]);
		chckbxd3TOS.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		chckbxd3TOS.addActionListener(this);
		GridBagConstraints gbc_comboBoxTitle = new GridBagConstraints();
		gbc_comboBoxTitle.gridwidth = 14;
		gbc_comboBoxTitle.insets = new Insets(0, 0, 5, 10);
		gbc_comboBoxTitle.gridx = 1;
		gbc_comboBoxTitle.gridy = 0;
		d3TOSSubTab.add(chckbxd3TOS, gbc_comboBoxTitle);

		JLabel TOSName = new JLabel("Flourescence Channel 3: ");
		chckbxd3TOS.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		GridBagConstraints gbc_comboBoxName = new GridBagConstraints();
		gbc_comboBoxName.gridwidth = 4;
		gbc_comboBoxName.insets = new Insets(0, 0, 5, 10);
		gbc_comboBoxName.gridx = 0;
		gbc_comboBoxName.gridy = 1;
		d3TOSSubTab.add(TOSName, gbc_comboBoxName);

		// ComboBoxes for choosing images
		imgd3TOS = new JComboBox<ImageInfo>();
		imgd3TOS.addActionListener(this);
		// imgd3TOS.addItemListener(this);
		imgd3TOS.setToolTipText(IMGTIPS3DTOS);
		imgd3TOS.setRenderer(ImageInfo.RENDERER);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.gridwidth = 5;
		gbc_comboBox.insets = new Insets(0, 0, 5, 10);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 5;
		gbc_comboBox.gridy = 1;
		d3TOSSubTab.add(imgd3TOS, gbc_comboBox);

		JLabel TOSFTs = new JLabel("Selected Fractions: ");
		chckbxd3TOS.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		GridBagConstraints gbc_comboBoxFTs = new GridBagConstraints();
		gbc_comboBoxFTs.gridwidth = 4;
		gbc_comboBoxFTs.insets = new Insets(0, 0, 5, 10);
		gbc_comboBoxFTs.gridx = 0;
		gbc_comboBoxFTs.gridy = 3;
		d3TOSSubTab.add(TOSFTs, gbc_comboBoxFTs);

		d3TOSSpinners = new JSpinner[tosFTs.length];
		for (int iChannel = 0; iChannel < tosFTs.length; iChannel++) {

			JLabel lblTOSFT2 = new JLabel("S" + (iChannel + 1) + "(%)");
			lblTOSFT2.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
			GridBagConstraints gbc_lblTOSFT2 = new GridBagConstraints();
			gbc_lblTOSFT2.insets = new Insets(0, 5, 0, 5);
			gbc_lblTOSFT2.gridx = 5 + iChannel;
			gbc_lblTOSFT2.gridy = 2;
			d3TOSSubTab.add(lblTOSFT2, gbc_lblTOSFT2);

			d3TOSSpinners[iChannel] = new JSpinner(new SpinnerNumberModel(tosFTs[iChannel], MINFT, MAXFT, STEPFT));
			GridBagConstraints gbc_spinner = new GridBagConstraints();
			gbc_spinner.insets = new Insets(0, 5, 0, 5);
			gbc_spinner.gridx = 5 + iChannel;
			gbc_spinner.gridy = 3;
			d3TOSSubTab.add(d3TOSSpinners[iChannel], gbc_spinner);

			JSpinner.NumberEditor editor = new JSpinner.NumberEditor(d3TOSSpinners[iChannel], "0");
			d3TOSSpinners[iChannel].setEditor(editor);
			JFormattedTextField textField = ((JSpinner.NumberEditor) d3TOSSpinners[iChannel].getEditor())
					.getTextField();
			textField.setEditable(true);
			DefaultFormatterFactory factory = (DefaultFormatterFactory) textField.getFormatterFactory();
			NumberFormatter formatter = (NumberFormatter) factory.getDefaultFormatter();
			formatter.setAllowsInvalid(false);
		}

		// addTabs
		tabs.insertTab("Analysis", null, analysisTab, null, ANALYSISTAB);
		analysisSubTabs.insertTab("Analysis metrics", null, metricSubTab, null, METRICSUBTAB);
		analysisSubTabs.setBackgroundAt(METRICSUBTAB, SUBLABELCOLORS[METRICSUBTAB]);
		// hide mTOSSubTab
		// analysisSubTabs.insertTab("mTOS", null, mTOSSubTab, null,MTOSSUBTAB);
		// analysisSubTabs.setBackgroundAt(MTOSSUBTAB,
		// SUBLABELCOLORS[MTOSSUBTAB]);
		// hide distSubTab
		// analysisSubTabs.insertTab("Distances", null, distSubTab,
		// null,DISTSUBTAB);
		// analysisSubTabs.setBackgroundAt(DISTSUBTAB, Color.YELLOW);

		analysisSubTabs.insertTab("Custom", null, othersubTab, null, OTHERSUBTAB);
		analysisSubTabs.setBackgroundAt(OTHERSUBTAB, SUBLABELCOLORS[OTHERSUBTAB]);

		// analysisSubTabs.insertTab("3D TOS", null, d3TOSSubTab, null,
		// TOSSUBTAB);
		// analysisSubTabs.setBackgroundAt(TOSSUBTAB,
		// SUBLABELCOLORS[TOSSUBTAB]);

		// analysisTab ends here
	}

	private void aboutPane() {
		// aboutPanel starts here
		aboutPanel = new JPanel();
		GridBagConstraints gbc_panel_about = new GridBagConstraints();
		gbc_panel_about.gridwidth = 4;
		gbc_panel_about.insets = new Insets(0, 0, 5, 0);
		gbc_panel_about.fill = GridBagConstraints.BOTH;
		gbc_panel_about.gridx = 0;
		gbc_panel_about.gridy = 2;
		superPanel.add(aboutPanel, gbc_panel_about);

		JTextArea txtrReference = new JTextArea();
		txtrReference.setBackground(SystemColor.window);
		txtrReference.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		txtrReference.setText(ABOUT_TEXT);
		txtrReference.setEditable(false);
		aboutPanel.add(txtrReference);

		warning = new JLabel("------------------------------------------");
		warning.setForeground(Color.RED);
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.insets = new Insets(0, 0, 0, 5);
		gbc_label_1.gridx = 1;
		gbc_label_1.gridy = 3;
		superPanel.add(warning, gbc_label_1);

		// Master button here
		analyzeBtn = new JButton("Analyze");
		analyzeBtn.addActionListener(this);
		GridBagConstraints gbc_btnAnalyze = new GridBagConstraints();
		gbc_btnAnalyze.insets = new Insets(0, 0, 0, 5);
		gbc_btnAnalyze.gridx = 2;
		gbc_btnAnalyze.gridy = 3;
		superPanel.add(analyzeBtn, gbc_btnAnalyze);

		// Email button here
		email = new JButton("Help");
		email.setToolTipText("Email the authors for any question");
		email.addActionListener(this);
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.anchor = GridBagConstraints.EAST;
		gbc_btnNewButton_1.gridx = 3;
		gbc_btnNewButton_1.gridy = 3;
		superPanel.add(email, gbc_btnNewButton_1);
	}

	public void updateImgList(ImagePlus addImp, ImagePlus deleteImp) {
		final byte NO_IMG = 4, NULL_IMG = 0, ADD_IMG = 1, DEL_IMG = 2, REPLACE_IMG = ADD_IMG + DEL_IMG;
		// use as offset when new image labels are added to JCombobox
		final int[] offset = new int[imgCombbxes.length];

		boolean isOpened = false;
		int idxClose = -1;
		boolean updateListAll = addImp == null && deleteImp == null;

		if (imgCombbxes == null)
			return;
		int[] selected = new int[imgCombbxes.length];
		for (int ipic = 0; ipic < selected.length; ipic++) {
			if (imgCombbxes[ipic] != null)
				selected[ipic] = imgCombbxes[ipic].getSelectedIndex();
			// general offset is set here, must be consistent to the loop below
			offset[ipic] = 1;
		}
		// conditional offset is set here, must be consistent to the one
		// outsidee the loop below
		offset[offset.length - 1] = 2;

		// img3dTOS
		// final int d3TOSoffset = 1;
		// int d3TOSselected = imgd3TOS.getSelectedIndex();

		// This part
		if (updateListAll) {
			resetInfoArray();
			for (int ipic = 0; ipic < imgCombbxes.length; ipic++) {
				imgCombbxes[ipic].setEnabled(false);
				imgCombbxes[ipic].removeAllItems();
				imgCombbxes[ipic].addItem(NOIMAGE);
			}
			// imgs[imgs.length-1] has one additional element, please be
			// cautious
			// Because of this, the offset has been set as above;
			imgCombbxes[imgCombbxes.length - 1].addItem(ROIMANAGER_IMAGE);
			// img3dTOS
			/*
			 * if(imgd3TOS!=null){ imgd3TOS.setEnabled(false);
			 * imgd3TOS.removeAllItems(); imgd3TOS.addItem(NOIMAGE); }
			 */
		}

		// non-grayscaled images are not taken into account
		if (WindowManager.getImageCount() != 0) {
			nbImgs = 0;
			int[] IDList = WindowManager.getIDList();
			for (int i = 0; i < IDList.length; i++) {
				ImagePlus currImg = WindowManager.getImage(IDList[i]);
				if (currImg.createLut().isGrayscale()) {
					nbImgs++;
					if (updateListAll) {
						ImageInfo item = new ImageInfo(currImg);
						info.insertElementAt(item, info.size() - 1);
						for (int ipic = 0; ipic < imgCombbxes.length; ipic++)
							imgCombbxes[ipic].insertItemAt(item, imgCombbxes[ipic].getItemCount() - offset[ipic]);
						// img3dTOS
						/*
						 * if(imgd3TOS!=null) imgd3TOS.insertItemAt(item,
						 * imgd3TOS.getItemCount()-d3TOSoffset);
						 */
					}
					if (addImp != null && currImg == addImp)
						isOpened = true;

				}
			}
		}

		if (deleteImp != null) {
			for (int i = 0; i < info.size(); i++) {
				if (info.elementAt(i).equalID(deleteImp)) {
					idxClose = i;
					break;
				}
			}
		}

		if (!isOpened)
			addImp = null;

		if (idxClose < 0)
			deleteImp = null;

		byte updateTypeAfter = 0;
		if (updateListAll)
			updateTypeAfter = NO_IMG;
		else
			updateTypeAfter = (byte) ((addImp == null ? NULL_IMG : ADD_IMG) + (deleteImp == null ? NULL_IMG : DEL_IMG));

		if (nbImgs <= 0) {
			tabs.setSelectedIndex(0);
			tabs.setEnabled(false);
			analyzeBtn.setEnabled(false);
			warning.setText("Image(s) should be opened to run analysis");
		} else {
			tabs.setEnabled(true);
			analyzeBtn.setEnabled(true);
			warning.setText("Please make sure everything is all set\n");
		}

		// insert new Image before [No Image]
		switch (updateTypeAfter) {
		case NULL_IMG:
			return;
		case NO_IMG:
			// if (updateType == 0)
			int i = 0;
			for (int ipic = 0; ipic < selected.length; ipic++)
				if (ipic < info.size() && imgCombbxes[ipic].isVisible())
					selected[ipic] = i++;
				else
					selected[ipic] = imgCombbxes[ipic].getItemCount() - offset[ipic];
			for (int ipic = 0; ipic < imgCombbxes.length; ipic++)
				imgCombbxes[ipic].setEnabled(false);
			// d3TOSselected = selected.length;
			break;
		case ADD_IMG: {
			// Avoid adding one image twice
			// if (updateType == (ADD_IMG + DEL_IMG) && addImp.getID() ==
			// deleteImp.getID())
			// return;
			boolean noimg = true;
			for (int ipic = 0; ipic < selected.length; ipic++) {
				// If the selection is beyond known, it should be maintained
				// If the JCombobox is not visible, its selection should also be
				// maintained
				// The way to maintain current selection is increasing the index
				// by one
				// Here the offset doesn't apply because ADD_IMG add one image
				// at a time
				// It is independent of the number of elements on the list
				if (selected[ipic] >= info.size() || !imgCombbxes[ipic].isVisible())
					selected[ipic]++;
				else if (info.elementAt(selected[ipic]).equal(NOIMAGE)) {
					if (noimg)
						noimg = false;
					else
						selected[ipic]++;
				}
			}
			ImageInfo item = new ImageInfo(addImp);
			for (int ipic = 0; ipic < imgCombbxes.length; ipic++) {
				imgCombbxes[ipic].setEnabled(false);
				imgCombbxes[ipic].insertItemAt(item, imgCombbxes[ipic].getItemCount() - offset[ipic]);
			}

			// img3dTOS
			/*
			 * if(imgd3TOS!=null){
			 * 
			 * if(d3TOSselected>=info.size()) d3TOSselected++; else
			 * if(info.elementAt(d3TOSselected).equal(NOIMAGE)) if(noimg)
			 * noimg=false; else d3TOSselected++;
			 * 
			 * imgd3TOS.setEnabled(false); imgd3TOS.insertItemAt(item,
			 * imgd3TOS.getItemCount()-d3TOSoffset); }
			 */
			// It is very important that this comes the last
			info.insertElementAt(item, info.size() - 1);
			break;
		}
		case DEL_IMG: {
			info.remove(idxClose);
			for (int ipic = 0; ipic < selected.length; ipic++) {
				// Here the offset does apply because we always want to reset to
				// done
				// not additional elements.
				if (selected[ipic] == idxClose)
					selected[ipic] = info.size() - 1;
				else if (selected[ipic] > idxClose)
					selected[ipic]--;
			}
			for (int ipic = 0; ipic < imgCombbxes.length; ipic++) {
				imgCombbxes[ipic].setEnabled(false);
				imgCombbxes[ipic].removeItemAt(idxClose);
			}

			// img3dTOS
			/*
			 * if(imgd3TOS!=null){ if(d3TOSselected==idxClose)
			 * d3TOSselected=info.size()-1; else if(d3TOSselected>idxClose)
			 * d3TOSselected--; imgd3TOS.setEnabled(false);
			 * imgd3TOS.removeItemAt(idxClose); }
			 */
			break;
		}
		case REPLACE_IMG: {
			// String item=addImp.getTitle();
			info.remove(idxClose);
			ImageInfo item = new ImageInfo(addImp);
			info.add(idxClose, item);
			for (int ipic = 0; ipic < imgCombbxes.length; ipic++) {
				imgCombbxes[ipic].setEnabled(false);
				imgCombbxes[ipic].removeItemAt(idxClose);
				imgCombbxes[ipic].insertItemAt(item, idxClose);
			}
			// img3dTOS
			/*
			 * if(imgd3TOS!=null){ imgd3TOS.removeItemAt(idxClose);
			 * imgd3TOS.insertItemAt(item, idxClose); }
			 */

			break;
		}
		default:
			break;
		}

		for (int ipic = 0; ipic < selected.length; ipic++) {
			if (imgCombbxes[ipic] != null) {
				if (selected[ipic] < 0)
					selected[ipic] = 0;
				if (selected[ipic] >= imgCombbxes[ipic].getItemCount())
					selected[ipic] = imgCombbxes[ipic].getItemCount() - offset[ipic];
				imgCombbxes[ipic].setSelectedIndex(selected[ipic]);
				imgCombbxes[ipic].setEnabled(true);
			}
		}

		/*
		 * //img3dTOS if(d3TOSselected < 0) d3TOSselected = 0; if(d3TOSselected
		 * >= imgd3TOS.getItemCount()) d3TOSselected = imgd3TOS.getItemCount() -
		 * d3TOSoffset;
		 * 
		 * 
		 * if(imgd3TOS.getSelectedIndex() != d3TOSselected)
		 * imgd3TOS.setSelectedIndex(d3TOSselected); imgd3TOS.setEnabled(true);
		 */
	}

	public void resetInfoArray() {
		info.clear();
		info.add(NOIMAGE);
	}

	/**
	 * This only works for all three images were opened before the plugin is
	 * opened
	 */
	private void adaptZoom() {
		/*
		 * System.out.println("------------------");
		 * Debugger.printStackTrace(8);
		 * System.out.println("------------------");
		 */
		if (!adaptZoom || imgCombbxes == null || imgCombbxes.length < 3)
			return;
		Rectangle thisScreen = getGUIScreenBounds();
		int screenHeight = thisScreen.height;

		int[] strImgs = new int[nChannels];
		Window[] iwImgs = new Window[nChannels];
		Window iwOpened = null;

		for (int i = 0; i < nReporters; i++) {
			if (imgCombbxes[i].getSelectedIndex() != -1) {
				strImgs[i] = imgCombbxes[i].getItemAt(imgCombbxes[i].getSelectedIndex()).ID;
			}
			if (strImgs[i] == ImageInfo.NONE_ID)
				iwImgs[i] = null;
			else if (strImgs[i] == ImageInfo.ROI_MANAGER_ID)
				iwImgs[i] = RoiManager.getInstance();
			else
				iwImgs[i] = WindowManager.getImage(strImgs[i]) == null ? null
						: WindowManager.getImage(strImgs[i]).getWindow();
			if (iwImgs[i] != null)
				iwOpened = iwImgs[i];
		}

		if (imgCombbxes[imgCombbxes.length - 1].getSelectedIndex() != -1) {
			strImgs[nReporters] = imgCombbxes[imgCombbxes.length - 1].getItemAt(imgCombbxes[imgCombbxes.length - 1].getSelectedIndex()).ID;
		}
		if (strImgs[nReporters] == ImageInfo.NONE_ID)
			iwImgs[nReporters] = null;
		else if (strImgs[nReporters] == ImageInfo.ROI_MANAGER_ID)
			iwImgs[nReporters] = RoiManager.getInstance();
		else
			iwImgs[nReporters] = WindowManager.getImage(strImgs[nReporters]) == null ? null
					: WindowManager.getImage(strImgs[nReporters]).getWindow();
		if (iwImgs[nReporters] != null)
			iwOpened = iwImgs[nReporters];

		if (iwOpened == null)
			return;

		int nRows = nChannels / 2, nColumns = nChannels / nRows;
		int height = 0;
		int width = (int) (thisScreen.getX() + thisScreen.getWidth() - mainframe.getWidth() - mainframe.getX())
				/ nColumns;
		int maxHeight = screenHeight / nRows;

		for (int i = 0; i < iwImgs.length; i++) {
			if (iwImgs[i] == null)
				continue;
			if (iwImgs[i].getHeight() * width / iwImgs[i].getWidth() > height)
				height = iwImgs[i].getHeight() * width / iwImgs[i].getWidth();
			if (height > maxHeight) {
				height = maxHeight;
				width = iwImgs[i].getWidth() * maxHeight / iwImgs[i].getHeight();
			}
		}

		int yOffset = (screenHeight - height * nRows) / 2;

		for (int i = 0; i < iwImgs.length; i++) {
			if (iwImgs[i] == null)
				continue;

			if (iwImgs[i] instanceof ImageWindow)
				((ImageWindow) iwImgs[i]).setLocationAndSize(
						mainframe.getWidth() + mainframe.getX() + width * (i % nColumns),
						yOffset + thisScreen.y + height * (i / nColumns), width, height);
			else {
				iwImgs[i].setBounds(mainframe.getWidth() + mainframe.getX() + width * (i % nColumns),
						yOffset + thisScreen.y + height * (i / nColumns), width, height);
				iwImgs[i].pack();
			}
			// iwImgs[i].toFront();
		}
		// adaptedZoom=true;
	}

	private void updateTicked(boolean test) {
		// handle alignment module
		alignedChckbxes[0].setEnabled(true);
		alignedChckbxes[1].setEnabled(true);
		alignTholdCombbxes[0].setEnabled(true);
		alignTholdCombbxes[1].setEnabled(true);
		alignTholdCombbxes[2].setEnabled(true);
		doAlignmentBtn.setEnabled(true);
		resetAlignmentBtn.setEnabled(true);

		// handle heatmap module
		heatmapRadiobtns[0].setSelected(true);
		heatmapRadiobtns[0].setEnabled(true);
		heatmapColorCombbxes[0].setEnabled(true);
		heatmapColorCombbxes[1].setEnabled(true);

		// handle output choices
		for (int i = 0; i < metricChckbxes.length; i++)
			metricChckbxes[i].setEnabled(true);

		for (int i = 0; i < otherChckbxes.length; i++)
			otherChckbxes[i].setEnabled(true);

		// handle tabs
		tabs.setEnabled(true);
		tabs.setEnabledAt(INPUTTAB, true);
		tabs.setEnabledAt(FILTERTAB, true);
		tabs.setEnabledAt(VISUALTAB, true);
		tabs.setEnabledAt(ANALYSISTAB, true);
		analysisSubTabs.setEnabled(true);
		analysisSubTabs.setEnabledAt(METRICSUBTAB, true);
		// analysisSubTabs.setEnabledAt(MTOSSUBTAB,true);
		// analysisSubTabs.setEnabledAt(DISTSUBTAB,true);
		analysisSubTabs.setEnabledAt(OTHERSUBTAB, true);
		analyzeBtn.setEnabled(true);
	}

	public void updateTicked() {
		if (imgCombbxes == null)
			return;

		boolean anyReporter = false, allReporters = true;

		// Reporter Channels
		// Assumption that the first nReporters are enabled
		for (int ipic = 0; ipic < nReporters; ipic++) {
			boolean isPresent = imgCombbxes[ipic].isVisible() && imgCombbxes[ipic].getSelectedIndex() != -1
					&& !imgCombbxes[ipic].getItemAt(imgCombbxes[ipic].getSelectedIndex()).equal(NOIMAGE);
			
			alignedChckbxes[ipic].setEnabled(isPresent);
			alignTholdCombbxes[ipic].setEnabled(isPresent);
			heatmapColorCombbxes[ipic].setEnabled(isPresent);
			heatmapChckbxes[ipic].setEnabled(isPresent);
			
			if(!isPresent){
				alignedChckbxes[ipic].setSelected(false);
				align_chckes[ipic]=false;
				heatmapChckbxes[ipic].setSelected(false);
				heatmap_chckes[ipic]=false;
			}

			anyReporter |= isPresent;
			allReporters &= isPresent;
		}

		for (JCheckBox chckBx : otherChckbxes)
			chckBx.setEnabled(anyReporter);
		
		if(!anyReporter){
			for (int i =0;i< otherChckbxes.length;i++){
				otherChckbxes[i].setSelected(false);
				other_chckes[i]=false;
			}
		}

		scatterplotChckbx.setEnabled(allReporters);
		matrixChckbx.setEnabled(allReporters);
		matrixMetricCombbx.setEnabled(allReporters);
		matrixStatsCombbx.setEnabled(allReporters);
		for (JSpinner jsp : matrixFTSpinners)
			jsp.setEnabled(allReporters);

		for (JCheckBox chckBx : metricChckbxes)
			chckBx.setEnabled(allReporters);

		for (JCheckBox chckBx : outputMetricChckbxes)
			chckBx.setEnabled(allReporters);

		if(!allReporters){
			scatterplotChckbx.setSelected(false);
			matrixChckbx.setSelected(false);
			for(int i=0;i<metricChckbxes.length;i++){
				metricChckbxes[i].setSelected(false);
				metric_chckes[i]=false;
			}
			for(int i=0;i<outputMetricChckbxes.length;i++){
				outputMetricChckbxes[i].setSelected(false);
				outputMetric_chckes[i]=false;
			}	
		}
		
		
		tabs.setEnabledAt(VISUALTAB, anyReporter);

		analysisSubTabs.setEnabledAt(OTHERSUBTAB, anyReporter);

		// Cell ID Channel although there is only one
		for (int ipic = MAX_NCHANNELS - 1; ipic >= MAX_NREPORTERS; ipic--) {
			boolean isPresent = imgCombbxes[ipic].isVisible() && imgCombbxes[ipic].getSelectedIndex() != -1
					&& !imgCombbxes[ipic].getItemAt(imgCombbxes[ipic].getSelectedIndex()).equal(NOIMAGE);

			if (!isPresent) {
				for (int i = 0; i < nReporters; i++){
					alignedChckbxes[i].setEnabled(false);
					align_chckes[i]=false;
				}
			}
			alignTholdCombbxes[ipic].setEnabled(isPresent);
			heatmapRadiobtns[0].setEnabled(isPresent);
			if (heatmapRadiobtns[0].isSelected() != isPresent) {
				heatmapRadiobtns[0].setSelected(isPresent);
				heatmapRadiobtns[1].setSelected(!isPresent);
			}
			
			for (JCheckBox chckBx : outputOptChckbxes)
				chckBx.setEnabled(isPresent);
			
			if(!isPresent){
				for(int i=0;i<outputOptChckbxes.length;i++){
					outputOptChckbxes[i].setSelected(false);
					outputOpt_chckes[i]=false;
				}
			}
			
			tabs.setEnabledAt(FILTERTAB, isPresent);

			previewTholdBtn.setEnabled(isPresent);
			doAlignmentBtn.setEnabled(anyReporter && isPresent);
			resetAlignmentBtn.setEnabled(anyReporter && isPresent);

			tabs.setEnabledAt(ANALYSISTAB, anyReporter || isPresent);
			analysisSubTabs.setEnabledAt(METRICSUBTAB, anyReporter || isPresent);
			analyzeBtn.setEnabled(anyReporter || isPresent);

		}

		if (!tabs.isEnabledAt(tabs.getSelectedIndex()))
			tabs.setSelectedIndex(INPUTTAB);
		if (!analysisSubTabs.isEnabledAt(analysisSubTabs.getSelectedIndex()))
			analysisSubTabs.setSelectedIndex(METRICSUBTAB);

	}

	private void loadPreference() {
		// nChannel
		nChannels = (int) Prefs.get(pluginName + "nChannels", nChannels);

		// input tab
		for (int iAlign = 0; iAlign < align_chckes.length; iAlign++)
			align_chckes[iAlign] = Prefs.get(pluginName + "whichAlign" + iAlign, align_chckes[iAlign]);

		for (int iThold = 0; iThold < alignThold_combs.length; iThold++)
			alignThold_combs[iThold] = (int) Prefs.get(pluginName + "whichThold" + iThold, alignThold_combs[iThold]);

		// cell filter tab
		waterShed_chck = Prefs.get(pluginName + "doWaterShed", waterShed_chck);

		for (int iSize = 0; iSize < filterMinSize_texts.length; iSize++) {
			filterMinSize_texts[iSize] = Prefs.get(pluginName + "minSize" + iSize, filterMinSize_texts[iSize]);
			filterMaxSize_texts[iSize] = Prefs.get(pluginName + "maxSize" + iSize, filterMaxSize_texts[iSize]);
		}

		for (int iFilter = 0; iFilter < filter_combs.length; iFilter++) {
			filter_combs[iFilter] = (int) Prefs.get(pluginName + "filterChoice" + iFilter, filter_combs[iFilter]);
			filterBackRatio_texts[iFilter] = Prefs.get(pluginName + "backRatio" + iFilter, filterBackRatio_texts[iFilter]);
			filterMinRange_texts[iFilter] = Prefs.get(pluginName + "minRange" + iFilter, filterMinRange_texts[iFilter]);
			filterMinRange_texts[iFilter] = Prefs.get(pluginName + "minRange" + iFilter, filterMinRange_texts[iFilter]);
		}

		int adFilterNum = (int) Prefs.get(pluginName + "adFilterNum", adFilterChoices.size());

		for (int iFilter = 0; iFilter < adFilterNum; iFilter++) {
			adFilterChoices.add((int) Prefs.get(pluginName + "adFilterChoice" + iFilter, 0));
			adBackRatios.add(Prefs.get(pluginName + "adBackRatio" + iFilter, false));
			adMinRanges.add(Prefs.get(pluginName + "adMinRange" + iFilter, DEFAULT_MIN));
			adMaxRanges.add(Prefs.get(pluginName + "adMaxRange" + iFilter, DEFAULT_MAX));
		}

		// visual tab
		matrix_chck = Prefs.get(pluginName + "doMatrix", matrix_chck);

		matrixMetric_comb = (int) Prefs.get(pluginName + "metricChoice", matrixMetric_comb);

		matrixStats_comb = (int) Prefs.get(pluginName + "statsChoice", matrixStats_comb);

		for (int iTOS = 0; iTOS < matrixFT_spin.length; iTOS++)
			matrixFT_spin[iTOS] = (int) Prefs.get(pluginName + "numOfFT" + iTOS, matrixFT_spin[iTOS]);

		scatter_chck = Prefs.get(pluginName + "doScatter", scatter_chck);

		for (int iHeat = 0; iHeat < heatmap_chckes.length; iHeat++)
			heatmap_chckes[iHeat] = Prefs.get(pluginName + "whichHeatmap" + iHeat, heatmap_chckes[iHeat]);

		heatmap_radio = (int) Prefs.get(pluginName + "whichHeatmapOpt", heatmap_radio);

		for (int iHeat = 0; iHeat < heatmapColor_combs.length; iHeat++)
			heatmapColor_combs[iHeat] = (int) Prefs.get(pluginName + "heatmapChoice" + iHeat, heatmapColor_combs[iHeat]);

		// analysis tab
		for (int iMetric = 0; iMetric < metric_chckes.length; iMetric++)
			metric_chckes[iMetric] = Prefs.get(pluginName + "metricOpts" + iMetric, metric_chckes[iMetric]);

		for (int iOpt = 0; iOpt < other_chckes.length; iOpt++)
			other_chckes[iOpt] = Prefs.get(pluginName + "otherOpts" + iOpt, other_chckes[iOpt]);

		mTOSscale = (int) Prefs.get(pluginName + "mTOSscale", mTOSscale);

		whichDist = (int) Prefs.get(pluginName + "whichDist", whichDist);

		for (int iDist = 0; iDist < numOfDistFTs.length; iDist++)
			numOfDistFTs[iDist] = (int) Prefs.get(pluginName + "numOfDistFT" + iDist, numOfDistFTs[iDist]);

		for (int iDist = 0; iDist < whichDistTholds.length; iDist++)
			whichDistTholds[iDist] = (int) Prefs.get(pluginName + "whichDistThold" + iDist, whichDistTholds[iDist]);

		custom_chck = Prefs.get(pluginName + "doCustom", custom_chck);

		for (int iThold = 0; iThold < metricThold_radios.length; iThold++)
			metricThold_radios[iThold] = (int) Prefs.get(pluginName + "metricThold" + iThold, metricThold_radios[iThold]);

		for (int iChannel = 0; iChannel < allFT_spins.length; iChannel++)
			for (int iMetric = 0; iMetric < allFT_spins[iChannel].length; iMetric++)
				allFT_spins[iChannel][iMetric] = (int) Prefs.get(pluginName + "allFT-c" + iChannel + "-" + iMetric,
						allFT_spins[iChannel][iMetric]);

		// output tab
		for (int iMetric = 0; iMetric < outputMetric_chckes.length; iMetric++)
			outputMetric_chckes[iMetric] = Prefs.get(pluginName + "outputMetric" + iMetric, outputMetric_chckes[iMetric]);

		for (int iOpt = 0; iOpt < outputOpt_chckes.length; iOpt++)
			outputOpt_chckes[iOpt] = Prefs.get(pluginName + "outputOther" + iOpt, outputOpt_chckes[iOpt]);

		// 3d TOS

		doD3TOS = Prefs.get(pluginName + "d3TOSOpt", doD3TOS);

		for (int iTOS = 0; iTOS < tosFTs.length; iTOS++)
			tosFTs[iTOS] = (int) Prefs.get(pluginName + "tosFT-c" + iTOS, tosFTs[iTOS]);
	}

	private Overlay newOverlay() {
		Overlay overlay = OverlayLabels.createOverlay();
		overlay.drawLabels(false);
		if (overlay.getLabelFont() == null && overlay.getLabelColor() == null) {
			overlay.setLabelColor(Color.white);
			overlay.drawBackgrounds(true);
		}
		overlay.drawNames(Prefs.useNamesAsLabels);
		return overlay;
	}

	/*
	 * private void setOverlay(ImagePlus imp, Overlay overlay) { if (imp ==
	 * null) return; ImageCanvas ic = imp.getCanvas(); if (ic == null) {
	 * imp.setOverlay(overlay); return; } ic.setShowAllList(overlay);
	 * imp.setOverlay(overlay); imp.draw(); }
	 */

	private void resetRoi() {
		for (int ipic = 0; ipic < imgCombbxes.length; ipic++) {
			if (imgCombbxes[ipic] == null)
				continue;
			imps[ipic] = WindowManager.getImage(imgCombbxes[ipic].getItemAt(imgCombbxes[ipic].getSelectedIndex()).ID);
			if (imps[ipic] == null)
				continue;
			imps[ipic].setOverlay(null);
			imps[ipic].draw();
		}
	}

	private void redoAlignment() {
		if (imps == null)
			return;
		for (int ipic = 0; ipic < imgCombbxes.length; ipic++) {
			if (imps[ipic] != null && oldImps[ipic] != null) {
				ImagePlus tempImg = (ImagePlus) imps[ipic].clone();
				imps[ipic].setImage(oldImps[ipic]);
				imps[ipic].setTitle(oldImps[ipic].getTitle());
				updateImgList(imps[ipic], tempImg);
			}
		}
	}

	private void previewIDCells() {
		Roi[] rois = null;
		if (roiCells != null)
			rois = roiCells.getRoisAsArray();
		Overlay overlay = null;
		if (rois != null) {
			overlay = newOverlay();
			for (int i = 0; i < rois.length; i++)
				overlay.add(rois[i]);
		}
		for (int ipic = 0; ipic < imps.length; ipic++) {
			if (imps[ipic] != null) {
				imps[ipic].setOverlay(overlay);
				imps[ipic].draw();
			}
		}
	}

	public void retrieveParams() {

		ImagePlus temp = null;
		for (int ipic = 0; ipic < imgCombbxes.length; ipic++) {
			if (!imgCombbxes[ipic].isEnabled())
				imps[ipic] = null;
			else if (imgCombbxes[ipic].getItemAt(imgCombbxes[ipic].getSelectedIndex()).equal(ROIMANAGER_IMAGE))
				imps[ipic] = roiManager2Mask(temp);
			else if (imgCombbxes[ipic].getItemAt(imgCombbxes[ipic].getSelectedIndex()).equal(NOIMAGE))
				imps[ipic] = null;
			else {
				imps[ipic] = WindowManager.getImage(imgCombbxes[ipic].getItemAt(imgCombbxes[ipic].getSelectedIndex()).ID);
				temp = imps[ipic];
			}
		}

		for (int iAlign = 0; iAlign < align_chckes.length; iAlign++) {
			align_chckes[iAlign] = alignedChckbxes[iAlign].isSelected();
			Prefs.set(pluginName + "whichAlign" + iAlign + "." + getVarName(align_chckes[iAlign]), align_chckes[iAlign]);
			align_chckes[iAlign] &= alignedChckbxes[iAlign].isEnabled();
		}

		for (int iThold = 0; iThold < alignTholdCombbxes.length; iThold++) {
			alignThold_combs[iThold] = alignTholdCombbxes[iThold].getSelectedIndex();
			Prefs.set(pluginName + "whichThold" + iThold + "." + getVarName(alignThold_combs[iThold]), alignThold_combs[iThold]);
		}

		waterShed_chck = waterShedChckbx.isSelected();
		Prefs.set(pluginName + "doWaterShed" + "." + getVarName(waterShed_chck), waterShed_chck);
		waterShed_chck &= waterShedChckbx.isEnabled();

		for (int iSize = 0; iSize < filterMinSize_texts.length; iSize++) {
			double[] range = str2doubles(filterSizeTexts[iSize].getText());
			filterMinSize_texts[iSize] = range[0];
			filterMaxSize_texts[iSize] = range[1];
			Prefs.set(pluginName + "minSize" + iSize + "." + getVarName(filterMinSize_texts[iSize]), filterMinSize_texts[iSize]);
			Prefs.set(pluginName + "maxSize" + iSize + "." + getVarName(filterMaxSize_texts[iSize]), filterMaxSize_texts[iSize]);
		}

		for (int iFilter = 0; iFilter < filter_combs.length; iFilter++) {
			filter_combs[iFilter] = filterCombbxes[iFilter].getSelectedIndex();
			double[] range = str2doubles(filterRangeTexts[iFilter].getText());
			// This need to be changed
			// if(filterRange.contains("X")||filterRange.contains("x"))
			// backRatios[iFilter]=true;
			filterMinRange_texts[iFilter] = range[0];
			filterMaxRange_texts[iFilter] = range[1];

			Prefs.set(pluginName + "filterChoice" + iFilter + "." + getVarName(filter_combs[iFilter]),
					filter_combs[iFilter]);
			Prefs.set(pluginName + "backRatio" + iFilter + "." + getVarName(filterBackRatio_texts[iFilter]), filterBackRatio_texts[iFilter]);
			Prefs.set(pluginName + "minRange" + iFilter + "." + getVarName(filterMinRange_texts[iFilter]), filterMinRange_texts[iFilter]);
			Prefs.set(pluginName + "maxRange" + iFilter + "." + getVarName(filterMaxRange_texts[iFilter]), filterMaxRange_texts[iFilter]);
		}

		for (int iFilter = 0; iFilter < adFilterChoices.size(); iFilter++) {

			Prefs.set(pluginName + "adFilterNum" + "." + getVarName(adFilterChoices.size()), adFilterChoices.size());
			Prefs.set(pluginName + "adFilterChoice" + iFilter + "." + getVarName(adFilterChoices.get(iFilter)),
					adFilterChoices.get(iFilter));
			Prefs.set(pluginName + "adBackRatio" + iFilter + "." + getVarName(adBackRatios.get(iFilter)),
					adBackRatios.get(iFilter));
			Prefs.set(pluginName + "adMinRange" + iFilter + "." + getVarName(adMinRanges.get(iFilter)),
					adMinRanges.get(iFilter));
			Prefs.set(pluginName + "adMaxRange" + iFilter + "." + getVarName(adMaxRanges.get(iFilter)),
					adMaxRanges.get(iFilter));
		}

		{// visualTab options

			scatter_chck = scatterplotChckbx.isSelected();
			Prefs.set(pluginName + "doScatter" + "." + getVarName(scatter_chck), scatter_chck);
			scatter_chck &= scatterplotChckbx.isEnabled();

			// heat map options
			for (int iHeat = 0; iHeat < heatmapChckbxes.length; iHeat++) {
				heatmap_chckes[iHeat] = heatmapChckbxes[iHeat].isSelected();
				Prefs.set(pluginName + "whichHeatmap" + iHeat + "." + getVarName(heatmap_chckes[iHeat]), heatmap_radio);
				heatmap_chckes[iHeat] &= heatmapChckbxes[iHeat].isEnabled();
			}

			for (int iHeat = 0; iHeat < heatmapRadiobtns.length; iHeat++) {
				if (heatmapRadiobtns[iHeat].isSelected()) {
					heatmap_radio = iHeat;
					break;
				}
			}
			Prefs.set(pluginName + "whichHeatmapOpt" + "." + getVarName(heatmap_radio), heatmap_radio);

			for (int iHeat = 0; iHeat < heatmapColor_combs.length; iHeat++) {
				heatmapColor_combs[iHeat] = heatmapColorCombbxes[iHeat].getSelectedIndex();
				Prefs.set(pluginName + "heatmapChoice" + iHeat + "." + getVarName(heatmapColor_combs[iHeat]),
						heatmapColor_combs[iHeat]);
			}

			// metric matrices
			matrix_chck = matrixChckbx.isSelected();
			Prefs.set(pluginName + "doMatrix" + "." + getVarName(matrix_chck), matrix_chck);
			matrix_chck &= matrixChckbx.isEnabled();

			matrixMetric_comb = matrixMetricCombbx.getSelectedIndex();
			Prefs.set(pluginName + "metricChoice" + "." + getVarName(matrixMetric_comb), matrixMetric_comb);

			matrixStats_comb = matrixStatsCombbx.getSelectedIndex();
			Prefs.set(pluginName + "statsChoice" + "." + getVarName(matrixStats_comb), matrixStats_comb);

			for (int iTOS = 0; iTOS < matrixFTSpinners.length; iTOS++) {
				matrixFT_spin[iTOS] = (Integer) matrixFTSpinners[iTOS].getValue();
				Prefs.set(pluginName + "numOfFT" + iTOS + "." + getVarName(matrixFT_spin[iTOS]), matrixFT_spin[iTOS]);
			}

			for (int iDist = 0; iDist < distRadios.length; iDist++) {
				if (distRadios[iDist].isSelected()) {
					whichDist = iDist;
					break;
				}
			}
			Prefs.set(pluginName + "whichDist" + "." + getVarName(whichDist), whichDist);

			for (int iDist = 0; iDist < numOfDistFTs.length; iDist++) {
				numOfDistFTs[iDist] = Integer.parseInt(distFTLabels[iDist].getText());
				Prefs.set(pluginName + "numOfDistFT" + iDist + "." + getVarName(numOfDistFTs[iDist]),
						numOfDistFTs[iDist]);
			}

			for (int iDist = 0; iDist < whichDistTholds.length; iDist++) {
				whichDistTholds[iDist] = distThresholders[iDist].getSelectedIndex();
				Prefs.set(pluginName + "whichDistThold" + iDist + "." + getVarName(whichDistTholds[iDist]),
						whichDistTholds[iDist]);
			}

		}

		for (int iColumn = 0; iColumn < metric_chckes.length; iColumn++) {
			metric_chckes[iColumn] = metricChckbxes[iColumn].isSelected();
			Prefs.set(pluginName + "metricOpts" + iColumn + "." + getVarName(metric_chckes[iColumn]), metric_chckes[iColumn]);
			metric_chckes[iColumn] &= metricChckbxes[iColumn].isEnabled();
		}

		for (int iMetric = 0; iMetric < metricThold_radios.length; iMetric++) {
			for (int iThold = 0; iThold < metricTholdRadiobtns[iMetric].length; iThold++) {
				if (metricTholdRadiobtns[iMetric][iThold] != null && metricTholdRadiobtns[iMetric][iThold].isSelected())
					metricThold_radios[iMetric] = iThold;
			}
			Prefs.set(pluginName + "metricThold" + iMetric + "." + getVarName(metricThold_radios[iMetric]),
					metricThold_radios[iMetric]);
		}

		int iFTs = metricThold_radios.length;

		for (int iMetric = 0; iMetric < metricThold_radios.length; iMetric++) {
			for (int iThold = 0; iThold < metricTholdRadiobtns[iMetric].length; iThold++) {
				if (metricTholdRadiobtns[iMetric][iThold] != null && metricTholdRadiobtns[iMetric][iThold].isSelected())
					metricThold_radios[iMetric] = iThold;
				if (metricThold_radios[iMetric] == IDX_THOLD_FT)
					iFTs += allFT_spins.length;
			}
			Prefs.set(pluginName + "metricThold" + iMetric + "." + getVarName(metricThold_radios[iMetric]),
					metricThold_radios[iMetric]);
		}

		for (int iChannel = 0; iChannel < allFT_spins.length; iChannel++)
			for (int iMetric = 0; iMetric < allFT_spins[iChannel].length; iMetric++) {
				try {
					allFTSpinners[iChannel][iMetric].commitEdit();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
				allFT_spins[iChannel][iMetric] = (Integer) allFTSpinners[iChannel][iMetric].getValue();
				Prefs.set(
						pluginName + "allFT-c" + iChannel + "-" + iMetric + "." + getVarName(allFT_spins[iChannel][iMetric]),
						allFT_spins[iChannel][iMetric]);
			}

		
		boolean anyOtherMetric = false;
		for (int iColumn = 0; iColumn < other_chckes.length; iColumn++) {
			other_chckes[iColumn] = otherChckbxes[iColumn].isSelected();
			Prefs.set(pluginName + "otherOpts" + iColumn + "." + getVarName(other_chckes[iColumn]), other_chckes[iColumn]);
			other_chckes[iColumn] &= otherChckbxes[iColumn].isEnabled();
			anyOtherMetric |= other_chckes[iColumn];
		}
		
		// DIYcode will be not recored because of it's complication
		customCode_text = customCodeTextbx.getText();

		// doCustom, however, is recored
		custom_chck = customMetricChckbxes.isSelected();
		Prefs.set(pluginName + "doCustom" + "." + getVarName(custom_chck), custom_chck);
		custom_chck &= customMetricChckbxes.isEnabled();
		
		
		for (int iColumn = 0; iColumn < outputMetric_chckes.length; iColumn++) {
			outputMetric_chckes[iColumn] = outputMetricChckbxes[iColumn].isSelected();
			Prefs.set(pluginName + "outputMetric" + iColumn + "." + getVarName(outputMetric_chckes[iColumn]),
					outputMetric_chckes[iColumn]);
			outputMetric_chckes[iColumn] &= outputMetricChckbxes[iColumn].isEnabled();
		}
		
		/**
		 * retrieve metricTholds and allFTs to allTholds but not record it
		 * Add one more term for average intensity and custom metric
		 */
		allTholds = new int[iFTs+1];
		iFTs = 0;
		for (int iMetric = 0; iMetric < metricThold_radios.length; iMetric++) {
			if (metric_chckes[iMetric]) {
				allTholds[iFTs++] = BasicCalculator.getThold(metricThold_radios[iMetric]);
				if (metricThold_radios[iMetric] == IDX_THOLD_FT) {
					for (int iChannel = 0; iChannel < allFT_spins.length; iChannel++)
						allTholds[iFTs++] = allFT_spins[iChannel][iMetric];
				}
			} else {
				allTholds[iFTs++] = BasicCalculator.THOLD_NONE;
			}
		}
		if (anyOtherMetric)
			allTholds[allTholds.length-1] = BasicCalculator.THOLD_ALL;
		else
			allTholds[allTholds.length-1] = BasicCalculator.THOLD_NONE;

		for (int iColumn = 0; iColumn < outputOpt_chckes.length; iColumn++) {
			outputOpt_chckes[iColumn] = outputOptChckbxes[iColumn].isSelected();
			Prefs.set(pluginName + "outputOther" + iColumn + "." + getVarName(outputOpt_chckes[iColumn]),
					outputOpt_chckes[iColumn]);
			outputOpt_chckes[iColumn] &= outputOptChckbxes[iColumn].isEnabled();
		}

		// 3D TOS Opts
		/*
		 * for (int ipic=0;ipic<imps.length;ipic++) impd3TOS[ipic] = imps[ipic];
		 * 
		 * if(imgd3TOS.getItemAt(imgd3TOS.getSelectedIndex()).equal(NOIMAGE))
		 * impd3TOS[impd3TOS.length-1] = null; else impd3TOS[impd3TOS.length-1]
		 * =
		 * WindowManager.getImage(imgd3TOS.getItemAt(imgd3TOS.getSelectedIndex()
		 * ).ID);
		 */

		/*
		 * if(chckbxd3TOS!=null && chckbxd3TOS.isEnabled()) doD3TOS =
		 * chckbxd3TOS.isSelected(); else doD3TOS = false;
		 * Prefs.set(PlugInName+"d3TOSOpt"+"."+getVarName(doD3TOS), doD3TOS);
		 * 
		 * for(int iChannel=0;iChannel<tosFTs.length;iChannel++){ try {
		 * d3TOSSpinners[iChannel].commitEdit(); } catch (ParseException e) { //
		 * TODO Auto-generated catch block //e.printStackTrace(); }
		 * tosFTs[iChannel] = (Integer)d3TOSSpinners[iChannel].getValue();
		 * Prefs.set(PlugInName+"tosFT-c"+iChannel+"."+getVarName(tosFTs[
		 * iChannel]), tosFTs[iChannel]); }
		 */

		// 3d tos tholds
		tosTholds = new int[tosFTs.length + 1];
		tosTholds[0] = BasicCalculator.THOLD_FT;
		for (int iMetric = 0; iMetric < tosFTs.length; iMetric++) {
			tosTholds[iMetric + 1] = tosFTs[iMetric];
		}

		if (Recorder.record) {
			MacroHandler.macroRecorder(true);
			// Recorder.record("//The action can only be recorded with Macro
			// language");
		}

		retrieveOptions();

	}

	public void updateThr() {
		for (int ipic = 0; ipic < imgCombbxes.length; ipic++) {
			updateThr(ipic);
		}
	}

	private void updateThr(int ipic) {
		if (ipic < 0 || ipic >= imgCombbxes.length || imgCombbxes[ipic] == null || imgCombbxes[ipic].getSelectedIndex() == -1)
			return;
		updateThr(ipic, imgCombbxes[ipic].getItemAt(imgCombbxes[ipic].getSelectedIndex()));
	}

	private void updateThr(int ipic, ImageInfo info) {
		imgUpdate = false;
		ImagePlus imp = WindowManager.getImage(info.ID);
		if (imp != null) {
			if (ipic >= 0 && ipic < alignThold_combs.length)
				imp.getProcessor().setAutoThreshold(Method.valueOf(ALLTHOLDS[alignThold_combs[ipic]]),
						!BackgroundProcessor.detectBackground(imp), ImageProcessor.RED_LUT);
			else
				imp.getProcessor().resetThreshold();
			imp.updateAndDraw();
		}
		imgUpdate = true;
	}

	public void resetThr() {
		for (int ipic = 0; ipic < imgCombbxes.length; ipic++) {
			resetThr(ipic);
		}
	}

	private void resetThr(int ipic) {
		if (ipic < 0 || ipic >= imgCombbxes.length || imgCombbxes[ipic] == null || imgCombbxes[ipic].getSelectedIndex() == -1)
			return;
		updateThr(-1, imgCombbxes[ipic].getItemAt(imgCombbxes[ipic].getSelectedIndex()));
	}

	private void resetThr(ImageInfo info) {
		updateThr(-1, info);
	}

	public void actionPerformed(ActionEvent e) {

		Object origin = e.getSource();
		imgUpdate = false;
		for (int i = 0; i < imgCombbxes.length; i++) {
			if (origin == imgCombbxes[i] && imgCombbxes[i].isEnabled()) {
				adaptZoom();
				updateTicked();
				ImagePlus imp = WindowManager.getImage(imgCombbxes[i].getItemAt(imgCombbxes[i].getSelectedIndex()).ID);
				if (imp != null)
					imp.getWindow().toFront();
				imgUpdate = true;
				return;
			}
		}
		// Modifiers: none=16, shift=17, ctrl=18, alt=24

		for (int iThold = 0; iThold < alignTholdCombbxes.length; iThold++) {
			if (origin == alignTholdCombbxes[iThold]) {
				alignThold_combs[iThold] = alignTholdCombbxes[iThold].getSelectedIndex();
				if (showThold)
					updateThr(iThold);
				imgUpdate = true;
				return;
			}
		}

		// prepare an email instead of opening the website
		if (origin == email) {
			Desktop desktop;
			if (Desktop.isDesktopSupported() && (desktop = Desktop.getDesktop()).isSupported(Desktop.Action.MAIL)) {
				try {
					desktop.mail(new URI(URIEmail));
				} catch (IOException | URISyntaxException e1) {
					// TODO Auto-generated catch block
					throw new RuntimeException("Desktop doesn't support mailto; mail is dead anyway ;)");
				}
			} else {
				// TODO fallback to some Runtime.exec(..) voodoo?
				throw new RuntimeException("Desktop doesn't support mailto; mail is dead anyway ;)");
			}
		} else if (origin == analyzeBtn) {
			retrieveParams();
			analysis = new AnalysisOperator(this);
			analysis.prepAll();
			analysis.execute(((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) ? false : true);

		} else if (origin == previewVisual) {
			retrieveParams();
			analysis = new AnalysisOperator(this);
			analysis.prepVisual();
			analysis.execute(((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) ? false : true);

		} else if (origin == previewTholdBtn) {
			showThold = !showThold;
			previewTholdBtn.setText(PREVIEW_THOLD[showThold ? 1 : 0]);
			if (showThold)
				updateThr();
			else
				resetThr();

		} else if (origin == doAlignmentBtn) {
			retrieveParams();
			analysis = new AnalysisOperator(this);
			analysis.prepAlignment();
			analysis.execute(((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) ? false : true);

		} else if (origin == resetAlignmentBtn) {
			redoAlignment();

		} else if (origin == tryIDCells) {
			retrieveParams();
			if (imps[imps.length - 1] != null) {
				options = DO_ROIS;
				analysis = new AnalysisOperator(this);
				analysis.prepIDCells();
				analysis.execute(false);
				previewIDCells();
			} else {
				IJ.error(pluginName + " error",
						"Missing Input in " + imgLabels[imgLabels.length - 1] + " for selected operation");
			}
		} else if (origin == btnMoreFilters) {
			CellFilterDialog gdCellFilters = CellFilterDialog.getCellFilterDialog("More filters", mainframe);
			gdCellFilters.showDialog();
			if (gdCellFilters.wasOKed()) {
				gdCellFilters.retrieveFilters(adFilterChoices, adBackRatios);
				gdCellFilters.retrieveRanges(adMinRanges, adMaxRanges);
			}
		} else if (origin == runCustom) {
			// retrieveParams();
			// doCustom=checkCustom.isSelected();
			if (customMetricChckbxes.isSelected()) {
				customCode_text = customCodeTextbx.getText();
				StringCompiler testCode = new StringCompiler();
				try {

					if (testCode.compile(customCode_text))
						setCustomStatus(SUCCESS);
					else
						setCustomStatus(FAILURE);

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					setCustomStatus(FAILURE);
					e1.printStackTrace();
					ExceptionHandler.handleException(e1);
				}
			}

		} else if (origin == resetCustom) {
			customCodeTextbx.setText(StringCompiler.getDefaultCode());
			setCustomStatus(SKIP);
			customMetricChckbxes.setSelected(false);
			otherChckbxes[otherChckbxes.length - 1].setSelected(false);
			// need to queue the setValue function otherwise not working
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					customCodeScrollpnl.getVerticalScrollBar().setValue(0);
				}
			});

		} else if (origin == helpCustom) {
			try {
				BrowserLauncher.openURL(CUSTOM_URL);
			} catch (IOException ie) {
				IJ.error(CUSTOM_URL + " cannot be opened");
			}

		} else if (origin == customMetricChckbxes) {
			if (customMetricChckbxes.isSelected())
				setCustomStatus(RUN);
			else
				setCustomStatus(SKIP);
			otherChckbxes[otherChckbxes.length - 1].setSelected(customMetricChckbxes.isSelected());

		} else if (origin == otherChckbxes[otherChckbxes.length - 1]) {
			if (otherChckbxes[otherChckbxes.length - 1].isSelected())
				setCustomStatus(RUN);
			else
				setCustomStatus(SKIP);
			customMetricChckbxes.setSelected(otherChckbxes[otherChckbxes.length - 1].isSelected());
		} /*
			 * else if(origin==chckbxd3TOS){
			 * imgd3TOS.setEnabled(chckbxd3TOS.isSelected()); for (JSpinner
			 * spinner: d3TOSSpinners)
			 * spinner.setEnabled(chckbxd3TOS.isSelected()); }
			 */

		imgUpdate = true;

	}

	/*
	 * public void itemStateChanged(ItemEvent e) { if (e.getStateChange() ==
	 * ItemEvent.SELECTED) { updateTicked(); if (tabs.isEnabled()) { int
	 * tabSelect = tabs.getSelectedIndex(); tabs.setSelectedIndex(tabSelect); }
	 * }
	 * 
	 * }
	 */
	public void setImageListener(Boolean imgIO, Boolean imgUpdate) {
		if (imgIO != null)
			this.imgIO = imgIO;
		if (imgUpdate != null)
			this.imgUpdate = imgUpdate;
	}

	public void imageOpened(ImagePlus imp) {
		if (imgIO) {
			updateImgList(imp, null);
			adaptZoom();
			updateTicked();
		}
	}

	public void imageClosed(ImagePlus imp) {
		if (imgIO) {
			updateImgList(null, imp);
			adaptZoom();
			updateTicked();
		}
	}

	public void imageUpdated(ImagePlus imp) {
		if (imgUpdate && imp.getID() != 0) {
			boolean listed = false;
			for (ImageInfo imgInfo : info) {
				if (imgInfo.equalID(imp)) {
					listed = true;
					break;
				}
			}
			if (listed) {
				updateImgList(imp, imp);
			}
		}
	}

	public void stateChanged(ChangeEvent e) {
		Object origin = e.getSource();

		if (origin == tabs) {
			switch (tabs.getSelectedIndex()) {
			case FILTERTAB:
				resetThr();
				updateThr(imgCombbxes.length - 1);
				break;
			case INPUTTAB:
				if (showThold)
					updateThr();
				else
					resetThr();
				break;
			default:
				resetThr();
			}
		}

		if (origin == tabs && tabs.getSelectedIndex() != FILTERTAB)
			resetRoi();

		/*
		for (int iFT = 0; iFT < distFTs.length; iFT++) {
			if (origin == distFTs[iFT]) {
				distFTLabels[iFT].setText("" + distFTs[iFT].getValue());
				updateFT(iFT);
			}
		}*/
	}
	/*
	public void propertyChange(PropertyChangeEvent e) {
		Object origin = e.getSource();
		// if (origin==xyCalibTxt || origin==zCalibTxt) updateCostesRandParam();

		for (int iFT = 0; iFT < distFTLabels.length; iFT++) {
			if (origin == distFTLabels[iFT]) {
				distFTs[iFT].setValue((int) parseDouble(distFTLabels[iFT].getText()));
				updateFT(iFT);
			}
		}

	}
	 */
	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		if (analysis != null)
			analysis.cancel();
		ImagePlus.removeImageListener(this);
		resetThr();
		mainframe.dispose();
		WindowManager.removeWindow(mainframe);
		staticGUI = null;
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
		WindowManager.addWindow(mainframe);
	}

	@Deprecated
	public void updateFT(int i) {
	}

	@Deprecated
	public void updateSlice() {
	}

	static void retrieveOptions() {
		PluginStatic.retrieveOptions();

		// store the old images for reset alignment
		for (int ipic = 0; ipic < imps.length; ipic++) {
			if (imps[ipic] != null) {
				oldImps[ipic] = imps[ipic].duplicate();
				oldImps[ipic].setTitle(imps[ipic].getTitle());
			} else
				oldImps[ipic] = null;
		}
	}

	void setCustomStatus(int status) {
		if (customMetricChckbxes == null)
			return;
		switch (status) {

		case SUCCESS:
			customMetricChckbxes.setText(CUSTOM_STATUS[SUCCESS]);
			customMetricChckbxes.setForeground(CUSTOM_COLORS[SUCCESS]);
			break;
		case FAILURE:
			customMetricChckbxes.setText(CUSTOM_STATUS[FAILURE]);
			customMetricChckbxes.setForeground(CUSTOM_COLORS[FAILURE]);
			break;
		case RUN:
			customMetricChckbxes.setText(CUSTOM_STATUS[RUN]);
			customMetricChckbxes.setForeground(CUSTOM_COLORS[RUN]);
			break;
		case SKIP:
			customMetricChckbxes.setText(CUSTOM_STATUS[SKIP]);
			customMetricChckbxes.setForeground(CUSTOM_COLORS[SKIP]);
			break;
		default:
			break;

		}

	}

	ProgressGlassPane getProgressGlassPane() {
		return progressGlassPane;
	}

	public static String getVarName(float a) {
		return "float";
	}

	public static String getVarName(int a) {
		return "int";
	}

	public static String getVarName(boolean a) {
		return "boolean";
	}

	public static String getVarName(byte a) {
		return "byte";
	}

	public static String getVarName(char a) {
		return "char";
	}

	public static String getVarName(long a) {
		return "long";
	}

	public static String getVarName(short a) {
		return "short";
	}

	public static String getVarName(double a) {
		return "double";
	}

	/**
	 * This method make sure <code>ref</code> and <code>target</code> have the
	 * same length if <code>ref</code> is longer than <code>target</code>,
	 * <code>target</code> will be extended and the extra elements will be
	 * filled as in <code>ref</code>; if <code>target</code> is longer than
	 * <code>ref</code>, <code>target</code> will be truncated to the length of
	 * <code>ref</code>.
	 * 
	 * @param ref
	 *            reference array
	 * @param target
	 *            desired array
	 * @return an array which is the same length as <code>ref</code> with the
	 *         elements in <code>target</code>
	 */
	public static String[] convertNames(String[] ref, String[] target) {

		String[] temp = target;
		if (target.length > ref.length) {
			target = new String[ref.length];
			System.arraycopy(temp, 0, target, 0, ref.length);
		} else if (target.length < ref.length) {
			target = new String[ref.length];
			System.arraycopy(temp, 0, target, 0, temp.length);
			System.arraycopy(ref, temp.length, target, temp.length, ref.length - temp.length);
		}

		return target;

	}

	public String saveAllWindows() {
		// I don't understand why ImageJ change the LookAndFeel while opening a
		// simple directory dialog window
		// However, I have to do this here to keep my looking
		/*
		 * LookAndFeel laf = UIManager.getLookAndFeel(); String dir =
		 * IJ.getDirectory("Choose the directory to save the results...");
		 * if(IJ.isWindows()){ try { UIManager.setLookAndFeel(laf); } catch
		 * (UnsupportedLookAndFeelException e) { IJ.error(
		 * "Errors in saving windows"); } }
		 */
		String dir = getDirectory("Choose the directory to save the results...");
		if (dir == null)
			return null;
		saveAllWindows(dir);
		return dir;
	}

	/**
	 * This method is copied from
	 * <code>ij.io.DirectoryChooser.getDirectoryUsingJFileChooserOnThisThread</code>
	 * This is because DirectoryChooser reset the looking using
	 * <code>Java2.setSystemLookAndFeel()</code>}; This will mess up the looking
	 * of plugin window.
	 * 
	 * @param title
	 * @return
	 */
	private String getDirectory(final String title) {
		try {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle(title);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			String defaultDir = OpenDialog.getDefaultDirectory();
			if (defaultDir != null) {
				File f = new File(defaultDir);
				if (IJ.debugMode)
					IJ.log("DirectoryChooser,setSelectedFile: " + f);
				chooser.setSelectedFile(f);
			}
			chooser.setApproveButtonText("Select");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				directory = file.getAbsolutePath();
				if (!directory.endsWith(File.separator))
					directory += File.separator;
				OpenDialog.setDefaultDirectory(directory);
			}
		} catch (Exception e) {
		}

		return directory;
	}


	public void updateSelection() {
		// image UI
		// DO NOT update Image list

		// alignment UI
		for (int i = 0; i < alignedChckbxes.length; i++) {
			if (!alignedChckbxes[i].isEnabled())
				align_chckes[i] = false;
			alignedChckbxes[i].setSelected(align_chckes[i]);
		}

		for (int i = 0; i < alignTholdCombbxes.length; i++)
			if (alignTholdCombbxes[i].isEnabled())
				alignTholdCombbxes[i].setSelectedIndex(alignThold_combs[i]);
			else
				alignThold_combs[i] = alignTholdCombbxes[i].getSelectedIndex();

		/**
		 * filters are never disabled, besides the code to retrieve the values
		 * is complicated no need to bother here
		 */
		for (int i = 0; i < filterSizeTexts.length; i++)
			filterSizeTexts[i].setText(getFilterRange(filterMinSize_texts[i], filterMaxSize_texts[i]));

		// cell filters UI
		for (int i = 0; i < filterCombbxes.length; i++) {
			filterCombbxes[i].setSelectedIndex(filter_combs[i]);
			filterRangeTexts[i].setText(getFilterRange(filterMinRange_texts[i], filterMaxRange_texts[i], filterBackRatio_texts[i]));
		}
		CellFilterDialog.reset();

		if (!waterShedChckbx.isEnabled())
			waterShed_chck = false;
		waterShedChckbx.setSelected(waterShed_chck);

		// Visualization UI
		// matrix heat map UI
		if (!matrixChckbx.isEnabled())
			matrix_chck = false;
		matrixChckbx.setSelected(matrix_chck);

		if (matrixMetricCombbx.isEnabled())
			matrixMetricCombbx.setSelectedIndex(matrixMetric_comb);
		else
			matrixMetric_comb = matrixMetricCombbx.getSelectedIndex();

		if (matrixStatsCombbx.isEnabled())
			matrixStatsCombbx.setSelectedIndex(matrixStats_comb);
		else
			matrixStats_comb = matrixStatsCombbx.getSelectedIndex();

		for (int i = 0; i < matrixFTSpinners.length; i++) {
			if (matrixFTSpinners[i].isEnabled())
				matrixFTSpinners[i].setValue(matrixFT_spin[i]);
			else
				matrixFT_spin[i] = (Integer) matrixFTSpinners[i].getValue();
		}

		// scatterplot UI
		if (!scatterplotChckbx.isEnabled())
			scatter_chck = false;
		scatterplotChckbx.setSelected(scatter_chck);

		// heatmaps UI
		for (int i = 0; i < heatmapRadiobtns.length; i++) {
			if (i == heatmap_radio && heatmapRadiobtns[heatmap_radio].isEnabled())
				heatmapRadiobtns[heatmap_radio].setSelected(true);
			else
				heatmap_radio++;
		}

		for (int i = 0; i < heatmapChckbxes.length; i++) {
			if (!heatmapChckbxes[i].isEnabled())
				heatmap_chckes[i] = false;
			heatmapChckbxes[i].setSelected(heatmap_chckes[i]);
			if (heatmapColorCombbxes[i].isEnabled())
				heatmapColorCombbxes[i].setSelectedIndex(heatmapColor_combs[i]);
			else
				heatmapColor_combs[i] = heatmapColorCombbxes[i].getSelectedIndex();
		}

		// Analysis Tab
		// outputs UI
		for (int i = 0; i < metricChckbxes.length; i++) {
			if (!metricChckbxes[i].isEnabled())
				metric_chckes[i] = false;
			metricChckbxes[i].setSelected(metric_chckes[i]);
		}

		for (int i = 0; i < otherChckbxes.length; i++) {
			if (!otherChckbxes[i].isEnabled())
				other_chckes[i] = false;
			otherChckbxes[i].setSelected(other_chckes[i]);
		}

		// analysis subtab
		for (int i = 0; i < metricTholdRadiobtns.length; i++) {
			for (int j = 0; j < metricTholdRadiobtns[i].length; j++) {
				if (j == metricThold_radios[i] && metricTholdRadiobtns[i][j] != null && metricTholdRadiobtns[i][j].isEnabled())
					metricTholdRadiobtns[i][j].setSelected(true);
				else
					metricThold_radios[i]++;

			}
			/*
			 * int count = 0; for (int j = 0; j < metricRadios[i].length; j++) {
			 * while (metricRadios[i][j] == null ||
			 * !metricRadios[i][j].isEnabled()) { j++; count++; } if
			 * (metricTholds[i] == j - count)
			 * metricRadios[i][j].setSelected(true); }
			 */
		}

		for (int i = 0; i < allFTSpinners.length; i++) {
			for (int j = 0; j < allFTSpinners[i].length; j++) {
				boolean isEnabled = allFTSpinners[i][j].isEnabled();
				if (isEnabled) {
					try {
						allFTSpinners[i][j].commitEdit();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					}
					allFTSpinners[i][j].setValue(allFT_spins[i][j]);
					allFTSpinners[i][j].setEnabled(isEnabled);
				} else {
					allFT_spins[i][j] = (Integer) allFTSpinners[i][j].getValue();
				}
			}
		}
		// mTOS UI
		/*
		 * for(int i=0;i<mTOSFTs.length;i++){ mTOSFTs[i].setValue(numOfFTs[i]);
		 * mTOSFTLabels[i].setValue(numOfFTs[i]); }
		 * mTOSRadios[mTOSscale].setSelected(true);
		 * 
		 * //Distances to subcellular location UI
		 * distRadios[whichDist].setSelected(true);
		 * 
		 * for(int iDist=0;iDist<distFTs.length;iDist++){
		 * distFTs[iDist].setValue(numOfDistFTs[iDist]);
		 * distFTLabels[iDist].setValue(numOfDistFTs[iDist]); }
		 * 
		 * for(int i=0;i<distThresholders.length;i++)
		 * distThresholders[i].setSelectedIndex(whichDistTholds[i]);
		 */

		// Custom UI
		if (!customMetricChckbxes.isEnabled())
			custom_chck = false;
		customMetricChckbxes.setSelected(custom_chck);
		customCodeTextbx.setText(customCode_text);
		if (custom_chck)
			setCustomStatus(RUN);
		else
			setCustomStatus(SKIP);
		// need to queue the setValue function otherwise not working
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				customCodeScrollpnl.getVerticalScrollBar().setValue(0);
			}
		});

		// Output UI
		for (int i = 0; i < outputMetricChckbxes.length; i++){
			if(!outputMetricChckbxes[i].isEnabled())
				outputMetric_chckes[i]=false;
			outputMetricChckbxes[i].setSelected(outputMetric_chckes[i]);
		}
		for (int i = 0; i < outputOptChckbxes.length; i++){
			if(!outputOptChckbxes[i].isEnabled())
				outputOpt_chckes[i]=false;
			outputOptChckbxes[i].setSelected(outputOpt_chckes[i]);
		}

	}

	private Rectangle getGUIScreenBounds() {
		if (mainframe == null || !mainframe.isVisible())
			return getIJScreenBounds();
		else
			return getScreenBounds(mainframe.getGraphicsConfiguration().getDevice());
	}

	private Rectangle getIJScreenBounds() {
		GraphicsDevice ijScreen = IJ.getInstance().getGraphicsConfiguration().getDevice();
		return getScreenBounds(ijScreen);
	}

	private Rectangle getScreenBounds(GraphicsDevice screen) {
		GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

		int ijScreenIndex = -1;
		for (int i = 0; i < gds.length; i++) {
			if (gds[i].equals(screen)) {
				ijScreenIndex = i;
				break;
			}
		}
		if (ijScreenIndex == -1)
			return new Rectangle(0, 0, 0, 0);
		else {
			GraphicsConfiguration gc = gds[ijScreenIndex].getDefaultConfiguration();
			Rectangle bounds = gc.getBounds();
			Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
			Rectangle effectiveScreenArea = new Rectangle();
			effectiveScreenArea.x = bounds.x + screenInsets.left;
			effectiveScreenArea.y = bounds.y + screenInsets.top;
			effectiveScreenArea.height = bounds.height - screenInsets.top - screenInsets.bottom;
			effectiveScreenArea.width = bounds.width - screenInsets.left - screenInsets.right;
			return effectiveScreenArea;
		}
	}

	public void exit() {
		mainframe.dispatchEvent(new WindowEvent(mainframe, WindowEvent.WINDOW_CLOSING));
	}

	public void setNReporters(int nReporter) {
		nReporters = nReporter;
		nChannels = nReporters + (MAX_NCHANNELS - MAX_NREPORTERS);
		Prefs.set(pluginName + "nChannels" + "." + getVarName(nChannels), nChannels);
		filterStrings = makeAllFilters(nReporters);

		// imps = new ImagePlus[nChannels];

		// whichAligns = newArray(DEFAULT_BOOLEAN,nReporter);
		// whichTholds = newArray(DEFAULT_CHOICE,nChannels);

		// whichHeatmap = newArray(DEFAULT_BOOLEAN,nReporter);
		// numOfFTs = newArray(DEFAULT_CHOICE,nReporter);
		// heatmapChoices = newArray(nReporter);

		// allFTs = newArray(DEFAULT_FT, nReporter, METRICNAMES.length);

		// numOfDistFTs = newArray(DEFAULT_FT,nReporter);
		// whichDistTholds = newArray(DEFAULT_CHOICE,nReporter);

		updateGUI();
		updateTicked();
		adaptZoom();
	}

	@SuppressWarnings("unchecked")
	public void updateGUI() {
		// nReporters:
		for (int i = MIN_NREPORTERS; i < nReporters; i++) {

			imgTitles[i].setVisible(true);
			imgCombbxes[i].setVisible(true);
			imgCombbxes[i].setEnabled(true);
			alignTholdCombbxes[i].setVisible(true);
			alignTholdCombbxes[i].setEnabled(true);

			alignedChckbxes[i].setVisible(true);
			alignedChckbxes[i].setEnabled(true);
			paneMatrixSpinners[i].setVisible(true);
			paneMatrixSpinners[i].setEnabled(true);
			// matrixSpinners[i].setVisible(true);
			heatmapChckbxes[i].setVisible(true);
			heatmapChckbxes[i].setEnabled(true);
			heatmapColorCombbxes[i].setVisible(true);
			heatmapColorCombbxes[i].setEnabled(true);
			for (int j = 0; j < METRICNAMES.length; j++) {
				allFTSpinners[i][j].setVisible(true);
				allFTSpinners[i][j].setEnabled(
						metricTholdRadiobtns[j][IDX_THOLD_FT].isSelected()
						);
			}
			lblFTunits[i].setVisible(true);

			for (int iFilter = 0; iFilter < filterCombbxes.length; iFilter++) {
				DefaultComboBoxModel<String> cbm = (DefaultComboBoxModel<String>) filterCombbxes[iFilter].getModel();
				for (int iString = 0; iString < INTEN_FILTERS.length; iString++) {
					String temp = makeIntFilterString(INTEN_FILTERS[iString], i);
					if (cbm.getIndexOf(temp) < 0)
						cbm.addElement(temp);
				}
			}

			//DefaultComboBoxModel<String> cbm = (DefaultComboBoxModel<String>) matrixMetricCombbx.getModel();
			//if (cbm.getIndexOf("M" + (i + 1)) < 0)
			//	cbm.addElement("M" + (i + 1));

			// some channels might not have input, adjust for that
			// we need to call updateTicked() after updateGUI()
			/*
			 * boolean isPresent = imgs[i].getSelectedIndex() != -1 &&
			 * !imgs[i].getItemAt(imgs[i].getSelectedIndex()).equal(NOIMAGE);
			 * alignedChecks[i].setEnabled(isPresent);
			 * thresholders[i].setEnabled(isPresent);
			 * heatmapColors[i].setEnabled(isPresent);
			 * heatmapChckbx[i].setEnabled(isPresent);
			 */

		}

		for (int i = nReporters; i < MAX_NREPORTERS; i++) {

			imgTitles[i].setVisible(false);
			imgCombbxes[i].setVisible(false);
			imgCombbxes[i].setEnabled(false);
			alignTholdCombbxes[i].setVisible(false);
			alignTholdCombbxes[i].setEnabled(false);

			alignedChckbxes[i].setVisible(false);
			alignedChckbxes[i].setEnabled(false);
			paneMatrixSpinners[i].setVisible(false);
			paneMatrixSpinners[i].setEnabled(false);
			// matrixSpinners[i].setVisible(false);
			heatmapChckbxes[i].setVisible(false);
			heatmapChckbxes[i].setEnabled(false);
			heatmapColorCombbxes[i].setVisible(false);
			heatmapColorCombbxes[i].setEnabled(false);
			for (int j = 0; j < METRICNAMES.length; j++) {
				allFTSpinners[i][j].setVisible(false);
				allFTSpinners[i][j].setEnabled(false);
			}
			lblFTunits[i].setVisible(false);

			for (int iFilter = 0; iFilter < filterCombbxes.length; iFilter++) {
				DefaultComboBoxModel<String> cbm = (DefaultComboBoxModel<String>) filterCombbxes[iFilter].getModel();
				for (int iString = 0; iString < INTEN_FILTERS.length; iString++)
					cbm.removeElement(makeIntFilterString(INTEN_FILTERS[iString], i));
			}

			//DefaultComboBoxModel<String> cbm = (DefaultComboBoxModel<String>) matrixMetricCombbx.getModel();
			//cbm.removeElement("M" + (i + 1));

		}

		if (nReporters == 2) {

			for (int i = 0; i < metricTholdRadiobtns.length; i++) {
				metricTholdRadiobtns[i][IDX_THOLD_COSTES].setVisible(true);
				metricTholdRadiobtns[i][IDX_THOLD_COSTES].setEnabled(true);
			}
			//lblMetricTholds[IDX_THOLD_ALL].setVisible(true);
			lblMetricTholds[IDX_THOLD_COSTES].setVisible(true);

			for (int iMetric : METRICS_2D_ONLY) {
				for (JRadioButton jrb : metricTholdRadiobtns[iMetric]) {
					if (jrb != null) {
						jrb.setVisible(true);
						jrb.setEnabled(true);
					}
				}
				for (int i = 0; i < nReporters; i++) {
					allFTSpinners[i][iMetric].setVisible(true);
					allFTSpinners[i][iMetric].setEnabled(
							metricTholdRadiobtns[iMetric][IDX_THOLD_FT].isSelected()
					);
				}
				metricChckbxes[iMetric].setVisible(true);
				metricChckbxes[iMetric].setEnabled(true);
			}

		} else if(nReporters == 3){
			for (int i = 0; i < metricTholdRadiobtns.length; i++) {
				metricTholdRadiobtns[i][IDX_THOLD_COSTES].setVisible(false);
				metricTholdRadiobtns[i][IDX_THOLD_COSTES].setEnabled(false);
				if (metricTholdRadiobtns[i][IDX_THOLD_COSTES].isSelected())
					if (metricTholdRadiobtns[i][IDX_THOLD_ALL] == null)
						metricTholdRadiobtns[i][IDX_THOLD_FT].setSelected(true);
					else
						metricTholdRadiobtns[i][IDX_THOLD_ALL].setSelected(true);
			}
			//lblMetricTholds[IDX_THOLD_ALL].setVisible(false);
			lblMetricTholds[IDX_THOLD_COSTES].setVisible(false);

			for (int iMetric : METRICS_2D_ONLY) {
				for (JRadioButton jrb : metricTholdRadiobtns[iMetric]) {
					if (jrb != null) {
						jrb.setVisible(false);
						jrb.setEnabled(false);
					}
				}
				for (int i = 0; i < allFTSpinners.length; i++) {
					allFTSpinners[i][iMetric].setVisible(false);
					allFTSpinners[i][iMetric].setEnabled(
							metricTholdRadiobtns[iMetric][IDX_THOLD_FT].isSelected()
					);
				}
				metricChckbxes[iMetric].setVisible(false);
				metricChckbxes[iMetric].setEnabled(false);
			}
		}

		customCode_text = StringCompiler.makeDefaultCode(nReporters);
		customCodeTextbx.setText(customCode_text);

		matrixMetricList = getMatrixMetrics(nReporters);
		matrixMetricCombbx.setModel(new DefaultComboBoxModel<String>(matrixMetricList));

		CellFilterDialog.syncFilter();
		
		mainframe.getContentPane().revalidate();
		mainframe.getContentPane().repaint();
	}
}
