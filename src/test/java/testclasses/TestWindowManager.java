package testclasses;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ij.IJ;
import ij.WindowManager;
import ij.measure.ResultsTable;
import ij.plugin.frame.RoiManager;

public class TestWindowManager {

	public TestWindowManager(){
		Test();
	}
	
	public void Test(){
		ResultsTable rt = new ResultsTable();
		rt.incrementCounter();
		rt.addValue("Test", Double.NaN);
		rt.show("Test");
		
		if(RoiManager.getInstance()==null){
			RoiManager roiManager = new RoiManager();
		}
		
		IJ.createImage("Test", 400, 400, 2, 8).show();
		
		
		JFrame mainframe = new JFrame(getClass().getSimpleName());
	    if (!IJ.isWindows())
	    	mainframe.setSize(430, 610);
	    else
	    	mainframe.setSize(400, 585);
	    mainframe.setLocation(0, (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2-mainframe.getHeight()/2));
	    mainframe.setResizable(false);
	    
	    mainframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainframe.setBounds(100, 100, 443, 650);
		
		mainframe.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if(e.getButton() == MouseEvent.BUTTON3){
					JMenuItem anItem = new JMenuItem("Click Me!");
					JMenuItem anItem2 = new JMenuItem("Close All!");
					anItem2.addActionListener(new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e){
							WindowManager.closeAllWindows();
						}
					});
					JPopupMenu menu = new JPopupMenu();
			        menu.add(anItem);
			        menu.add(anItem2);
			        menu.show(e.getComponent(),e.getX(),e.getY());
				}
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

		});
		
		JButton analyze = new JButton("Analyze");
		analyze.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				WindowManager.closeAllWindows();
			}
		});
		
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
		tabs.setOpaque(true);
		tabs.addTab("Test", new JPanel());
		
		JMenuItem anItem = new JMenuItem("Click Me!");
		JMenuItem anItem2 = new JMenuItem("Close All!");
		anItem2.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				LookAndFeel laf = UIManager.getLookAndFeel();
				IJ.getDir("Test...");
				if(IJ.isWindows()){
					try {
						UIManager.setLookAndFeel(laf);
					} catch (UnsupportedLookAndFeelException e2) {
						IJ.error("Errors in saving windows");
					}
				}
			}
		});
		JMenu menu = new JMenu("Options");
        menu.add(anItem);
        menu.add(anItem2);
        
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
		
		JPanel panel = new JPanel();
		panel.add(analyze);
		//panel.add(tabs);
		mainframe.setJMenuBar(menuBar);
		mainframe.setContentPane(panel);
		
		mainframe.repaint();
		mainframe.setVisible(true);
	}
}
