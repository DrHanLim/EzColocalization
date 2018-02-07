package testclasses;

import java.awt.Panel;

import javax.swing.JButton;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.StackWindow;

@SuppressWarnings("serial")
public class TestStackWindow extends StackWindow {

	
	private boolean doUpdate;
	private ImagePlus srcImp;

	public TestStackWindow(ImagePlus imp) {
		super(imp);
		setup(imp);
		// TODO Auto-generated constructor stub
	}
	
	private void setup(ImagePlus imp) {
		// TODO Auto-generated method stub
		Panel buttons = new Panel();
		buttons.add(new JButton("A"));
		buttons.add(new JButton("B"));
		buttons.add(new JButton("C"));
		add(buttons);
		
		pack();
		
		buttons.setVisible(false);
		pack();
	}

	@Override
	public void run() {
		while (!done) {
			
			if (doUpdate && srcImp!=null) {
				if (srcImp.getRoi()!=null)
					IJ.wait(50);	//delay to make sure the roi has been updated
				if (srcImp!=null) {
					System.out.println("srcImp!=null");
				}
			}
			synchronized(this) {
				if (doUpdate) {
					doUpdate = false;		//and loop again
				} else {
					try {wait();}	//notify wakes up the thread
					catch(InterruptedException e) { //interrupted tells the thread to exit
						return;
					}
				}
			}
			
			
			synchronized(this) {
				try {wait();}
				catch(InterruptedException e) {}
			}
			if (done) return;
			if (slice>0) {
				int s = slice;
				slice = 0;
				if (s!=imp.getCurrentSlice())
					imp.setSlice(s);
			}
		}
	}

}
