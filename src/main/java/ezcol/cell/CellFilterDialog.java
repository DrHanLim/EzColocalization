package ezcol.cell;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import ezcol.files.FilesIO;
import ezcol.main.PluginConstants;
import ezcol.main.PluginStatic;
import ij.IJ;
import ij.WindowManager;
import ij.plugin.ScreenGrabber;

@SuppressWarnings("serial")
public class CellFilterDialog extends JDialog implements ActionListener, KeyListener{

	private static CellFilterDialog staticCellFilterDialog;
	
	public static String[] filterStrings = PluginStatic.getFilterStrings();
	
	private String filterLabel = "More filter ";
	private int count = 0;
	private static final int FILTER_HEIGHT = 32;
	private static final int SCREEN_HEIGHT = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
	private boolean wasOKed, wasCanceled;
	
	private final JPanel contentPanel = new JPanel();
	private JPanel panel;
	private GridBagLayout gbl_panel;
	private JButton btnAddFilter;
	private JButton btnOK, btnCancel;
	
	private ArrayList<JLabel> filterNames = new ArrayList<JLabel>();
	private ArrayList<JTextField> filterRanges = new ArrayList<JTextField>();
	private ArrayList<JComboBox<String>> filterChoices = new ArrayList<JComboBox<String>>();
	private ArrayList<JButton> btnsRemoveFilter = new ArrayList<JButton>();
	
	public CellFilterDialog(){this("");}
	
	public CellFilterDialog(String title){
		this(title, WindowManager.getCurrentImage()!=null?
				(Frame)WindowManager.getCurrentImage().getWindow():IJ.getInstance()!=null?IJ.getInstance():new Frame());
	}
	
	public CellFilterDialog(String title, Frame parent) {
		super(parent==null?new Frame():parent, title, true);
	}
	
	public static CellFilterDialog getCellFilterDialog(){
		if(staticCellFilterDialog==null){
			staticCellFilterDialog = new CellFilterDialog();
		}
		return staticCellFilterDialog;
	}
	
	public static CellFilterDialog getCellFilterDialog(String title){
		if(staticCellFilterDialog==null){
			staticCellFilterDialog = new CellFilterDialog(title);
		}
		return staticCellFilterDialog;
	}
	
	public static CellFilterDialog getCellFilterDialog(String title, Frame parent){
		if(staticCellFilterDialog==null){
			staticCellFilterDialog = new CellFilterDialog(title,parent);
		}
		return staticCellFilterDialog;
	}
	
	public void retrieveFilters(ArrayList<Integer> filterChoices,ArrayList<Boolean> backRatios){
		if(filterChoices == null || backRatios == null)
			return;
		filterChoices.clear();
		backRatios.clear();
		for(JComboBox<String> filters: this.filterChoices){
			filterChoices.add(filters.getSelectedIndex());
			backRatios.add(false);
		}
	}
	
	public void retrieveRanges(ArrayList<Double> minRanges,ArrayList<Double> maxRanges){
		if(minRanges == null || maxRanges == null){
			return;
		}
		minRanges.clear();
		maxRanges.clear();
		for(JTextField ranges: filterRanges){
			double[] range = PluginStatic.str2doubles(ranges.getText());
			minRanges.add(range[0]);
			maxRanges.add(range[1]);
		}
	}
	
	public void showDialog(){
		if(panel==null)
			setUI();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	private void setUI(){
		setTitle("More filters");
		setBounds(100, 100, 502, 234);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{336, 0};
		gridBagLayout.rowHeights = new int[]{20, 41, 15, 22, 20, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagConstraints gbc_contentPanel = new GridBagConstraints();
		gbc_contentPanel.fill = GridBagConstraints.BOTH;
		gbc_contentPanel.insets = new Insets(0, 0, 5, 0);
		gbc_contentPanel.gridx = 0;
		gbc_contentPanel.gridy = 1;
		getContentPane().add(contentPanel, gbc_contentPanel);
		contentPanel.setLayout(new GridLayout(1, 0, 0, 0));
		{
			panel = new JPanel();
			contentPanel.add(panel);
			gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{19, 75, 17, 96, 31, 132, 0, 18, 0};
			gbl_panel.rowHeights = new int[]{FILTER_HEIGHT, 0};
			gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);
			
			{
				count++;
				JLabel lblNewLabel = new JLabel(filterLabel+count);
				lblNewLabel.setFont(new Font("Arial", Font.PLAIN, 16));
				GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
				gbc_lblNewLabel.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblNewLabel.insets = new Insets(2, 0, 2, 5);
				gbc_lblNewLabel.gridx = 1;
				gbc_lblNewLabel.gridy = 0;
				panel.add(lblNewLabel, gbc_lblNewLabel);
				filterNames.add(lblNewLabel);
			}
			{
				JComboBox<String> comboBox = new JComboBox<String>(filterStrings);
				GridBagConstraints gbc_comboBox = new GridBagConstraints();
				gbc_comboBox.insets = new Insets(0, 0, 0, 5);
				gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
				gbc_comboBox.gridx = 3;
				gbc_comboBox.gridy = 0;
				panel.add(comboBox, gbc_comboBox);
				filterChoices.add(comboBox);
			}
			{
				JTextField textField = new JTextField();
				textField.setText(PluginStatic.getFilterRange(PluginConstants.DEFAULT_MIN,PluginConstants.DEFAULT_MAX));
				GridBagConstraints gbc_TextField = new GridBagConstraints();
				gbc_TextField.insets = new Insets(0, 0, 0, 5);
				gbc_TextField.fill = GridBagConstraints.HORIZONTAL;
				gbc_TextField.gridx = 5;
				gbc_TextField.gridy = 0;
				panel.add(textField, gbc_TextField);
				filterRanges.add(textField);
			}
			{
				JButton button = new JButton();
				button.setBorder(BorderFactory.createEmptyBorder());
				button.setContentAreaFilled(false);
				if(FilesIO.getResource("deleteIcon16.gif")!=null)
					button.setIcon(new ImageIcon(FilesIO.getResource("deleteIcon16.gif")));
				else
					button.setText("X");
				button.addActionListener(this);
				GridBagConstraints gbc_button = new GridBagConstraints();
				gbc_button.insets = new Insets(0, 5, 0, 5);
				gbc_button.gridx = 6;
				gbc_button.gridy = 0;
				panel.add(button, gbc_button);
				btnsRemoveFilter.add(button);
			}
		}
		{
			btnAddFilter = new JButton("Add Filter");
			btnAddFilter.addActionListener(this);
			GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
			gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
			gbc_btnNewButton.gridx = 0;
			gbc_btnNewButton.gridy = 3;
			getContentPane().add(btnAddFilter, gbc_btnNewButton);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			GridBagConstraints gbc_buttonPane = new GridBagConstraints();
			gbc_buttonPane.anchor = GridBagConstraints.NORTH;
			gbc_buttonPane.fill = GridBagConstraints.HORIZONTAL;
			gbc_buttonPane.gridx = 0;
			gbc_buttonPane.gridy = 4;
			getContentPane().add(buttonPane, gbc_buttonPane);
			{
				btnOK = new JButton("OK");
				btnOK.setActionCommand("OK");
				btnOK.addActionListener(this);
				btnOK.addKeyListener(this);
				buttonPane.add(btnOK);
				getRootPane().setDefaultButton(btnOK);
			}
			{
				btnCancel = new JButton("Cancel");
				btnCancel.addActionListener(this);
				btnCancel.addKeyListener(this);
				buttonPane.add(btnCancel);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		Object origin=e.getSource();
		if(origin == btnAddFilter){
			Rectangle rec =getBounds();
			rec.height += FILTER_HEIGHT;
			if(rec.height > SCREEN_HEIGHT)
				return;
				
			setBounds(rec);
			
			int[] rowHeights = new int[gbl_panel.rowHeights.length+1];
			double[] rowWeights = new double[gbl_panel.rowWeights.length+1];
			
			rowHeights[0] = gbl_panel.rowHeights[0];
			System.arraycopy(gbl_panel.rowHeights, 0, rowHeights, 1, gbl_panel.rowHeights.length);
			
			rowWeights[0] = gbl_panel.rowWeights[0];
			System.arraycopy(gbl_panel.rowWeights, 0, rowWeights, 1, gbl_panel.rowWeights.length);
		
			gbl_panel.rowHeights = rowHeights;
			gbl_panel.rowWeights = rowWeights;
			
			// TODO Auto-generated method stub
		
			count++;
			JLabel lblNewLabel = new JLabel(filterLabel+count);
			lblNewLabel.setFont(new Font("Arial", Font.PLAIN, 17));
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
			gbc_lblNewLabel.gridx = 1;
			gbc_lblNewLabel.gridy = count-1;
			panel.add(lblNewLabel, gbc_lblNewLabel);
			filterNames.add(lblNewLabel);
			
			JComboBox<String> comboBox = new JComboBox<String>(filterStrings);
			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.insets = new Insets(0, 0, 0, 5);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 3;
			gbc_comboBox.gridy = count-1;
			panel.add(comboBox, gbc_comboBox);
			filterChoices.add(comboBox);
			
			
			JTextField textField = new JTextField();
			textField.setText(PluginStatic.getFilterRange(PluginConstants.DEFAULT_MIN,PluginConstants.DEFAULT_MAX));
			GridBagConstraints gbc_TextField = new GridBagConstraints();
			gbc_TextField.insets = new Insets(0, 0, 0, 5);
			gbc_TextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_TextField.gridx = 5;
			gbc_TextField.gridy = count-1;
			panel.add(textField, gbc_TextField);
			filterRanges.add(textField);
			
		
			JButton button = new JButton();
			button.setBorder(BorderFactory.createEmptyBorder());
			button.setContentAreaFilled(false);
			if(FilesIO.getResource("deleteIcon16.gif")!=null)
				button.setIcon(new ImageIcon(FilesIO.getResource("deleteIcon16.gif")));
			else
				button.setText("X");
			button.addActionListener(this);
			GridBagConstraints gbc_button = new GridBagConstraints();
			gbc_button.insets = new Insets(0, 5, 0, 5);
			gbc_button.gridx = 6;
			gbc_button.gridy = count-1;
			panel.add(button, gbc_button);
			btnsRemoveFilter.add(button);
			revalidate();
			repaint();
		}
		
		int idx = btnsRemoveFilter.indexOf(origin);
		if(idx != -1){
			if(count == 1)
				return;
			Rectangle rec =getBounds();
			rec.height -= FILTER_HEIGHT;
			setBounds(rec);
			
			int[] rowHeights = new int[gbl_panel.rowHeights.length-1];
			double[] rowWeights = new double[gbl_panel.rowWeights.length-1];
			
			System.arraycopy(gbl_panel.rowHeights, 1, rowHeights, 0, rowHeights.length);
			System.arraycopy(gbl_panel.rowWeights, 1, rowWeights, 0, rowWeights.length);
		
			gbl_panel.rowHeights = rowHeights;
			gbl_panel.rowWeights = rowWeights;
			
			panel.remove(filterNames.get(idx));
			panel.remove(filterChoices.get(idx));
			panel.remove(filterRanges.get(idx));
			panel.remove(btnsRemoveFilter.get(idx));
			
			filterNames.remove(idx);
			filterChoices.remove(idx);
			filterRanges.remove(idx);
			btnsRemoveFilter.remove(idx);
			
			//move up the components below the deleted filter
			int newcount = idx+1;
			for (Component comp : panel.getComponents()) {
				if(comp == null || gbl_panel ==null)
					continue;
				GridBagConstraints gbc = gbl_panel.getConstraints(comp);
				if(gbc!=null){
					if(gbc.gridy>idx){
						gbc.gridy--;
						//This condition is not safe if there is additional JLabel
						//However, we only have filterNames as JLabel
						if(comp instanceof JLabel){
							((JLabel)comp).setText(filterLabel+newcount);
							newcount++;
						}
					}
					gbl_panel.setConstraints(comp, gbc);
				}
			}
			
			count--;
			revalidate();
			repaint();
		}
		
		if (origin==btnOK || origin==btnCancel) {
			wasCanceled = origin==btnCancel;
			if(wasCanceled)
				staticCellFilterDialog = null;
			wasOKed = origin==btnOK;
			dispose();
		}
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode(); 
		IJ.setKeyDown(keyCode); 
		if (keyCode==KeyEvent.VK_ENTER) {
			wasOKed = true;
			dispose();
		} else if (keyCode==KeyEvent.VK_ESCAPE) { 
			wasCanceled = true;
			staticCellFilterDialog = null;
			dispose(); 
			IJ.resetEscape();
		} else if (keyCode==KeyEvent.VK_W && (e.getModifiers()&Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())!=0) { 
			wasCanceled = true; 
			dispose(); 
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		IJ.setKeyUp(keyCode);
		int flags = e.getModifiers();
		boolean control = (flags & KeyEvent.CTRL_MASK) != 0;
		boolean meta = (flags & KeyEvent.META_MASK) != 0;
		boolean shift = (flags & KeyEvent.SHIFT_MASK) != 0;
		if (keyCode==KeyEvent.VK_G && shift && (control||meta))
			new ScreenGrabber().run("");
		
	}
	
	/** Returns true if the user clicked on "Cancel". */
    public boolean wasCanceled() {
    	return wasCanceled;
    }
    
	/** Returns true if the user has clicked on "OK" or a macro is running. */
    public boolean wasOKed() {
    	return wasOKed;
    }
	
    public static void reset(){
    	staticCellFilterDialog = null;
    }
    
    public static void syncFilter(){
    	reset();
    	filterStrings = PluginStatic.getFilterStrings();
    }
}
