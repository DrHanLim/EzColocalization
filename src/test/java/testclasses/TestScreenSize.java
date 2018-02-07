package testclasses;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class TestScreenSize {

	public static void test(){
		//Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		//System.out.println("width: "+screenSize.getWidth()+", length: "+screenSize.getHeight());
		GraphicsDevice[] allScreens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		int i=0;
		for(GraphicsDevice curGs : allScreens)
		{
			System.out.println("Monitor: "+(i++));
		      GraphicsConfiguration curGc = curGs.getDefaultConfiguration();
		      Rectangle bounds = curGc.getBounds();
		      System.out.println(bounds.getX() + "," + bounds.getY() + " " + bounds.getWidth() + "x" + bounds.getHeight());
		}
		int numMonitor = allScreens.length-1;
		JFrame mainframe = new JFrame("PLUGIN_NAME");
		mainframe.setSize(400, 585);
		JPanel superPanel = new JPanel();
		superPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		JButton jbutton = new JButton("Switch");
		jbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				GraphicsDevice[] allScreens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
				GraphicsDevice myScreen = mainframe.getGraphicsConfiguration().getDevice();
				int myScreenIndex = -1;
				for (int i = 0; i < allScreens.length; i++) {
				    if (allScreens[i].equals(myScreen))
				    {
				        myScreenIndex = i;
				        break;
				    }
				}
				Rectangle oldBounds = allScreens[myScreenIndex].getDefaultConfiguration().getBounds();
				Rectangle bounds = allScreens[numMonitor-myScreenIndex].getDefaultConfiguration().getBounds();
				Rectangle mainBounds = mainframe.getBounds();
				mainBounds.x = mainBounds.x - oldBounds.x;
				mainBounds.y = mainBounds.y - oldBounds.y;
				mainframe.setBounds(new Rectangle(bounds.x+mainBounds.x,bounds.y+mainBounds.y,mainBounds.width,mainBounds.height));
			}
		});
		superPanel.add(jbutton);
		mainframe.setContentPane(superPanel);
		mainframe.setVisible(true);
	}
}
