package testclasses;

import javax.swing.JButton;

import ij.gui.GenericDialog;
import ij.process.AutoThresholder;

public class TestGenericDialog {

	public TestGenericDialog(){
		test();
	}
	
	private void test(){
		GenericDialog gd = new GenericDialog("Cell Filter");
		String[] filters = AutoThresholder.getMethods();
		gd.addChoice("cell filter", filters, filters[0]);
		gd.addStringField("", "0-Infinity");
		gd.add(new JButton("Add"));
		gd.showDialog();
	}
}
