package testclasses;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import ij.IJ;
//https://web.archive.org/web/20100114122417/http://exampledepot.com/egs/javax.swing.undo/UndoText.html
public class TestUndoTextArea {

	@SuppressWarnings("serial")
	public TestUndoTextArea(){
		JTextArea textcomp = new JTextArea();
		final UndoManager undo = new UndoManager();
		Document doc = textcomp.getDocument();

		// Listen for undo and redo events
		doc.addUndoableEditListener(new UndoableEditListener() {
		    public void undoableEditHappened(UndoableEditEvent evt) {
		        undo.addEdit(evt.getEdit());
		    }
		});

		// Create an undo action and add it to the text component
		textcomp.getActionMap().put("Undo",
		    new AbstractAction("Undo") {
		        public void actionPerformed(ActionEvent evt) {
		            try {
		                if (undo.canUndo()) {
		                    undo.undo();
		                }
		            } catch (CannotUndoException e) {
		            }
		        }
		   });

		// Bind the undo action to ctl-Z
		textcomp.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

		// Create a redo action and add it to the text component
		textcomp.getActionMap().put("Redo",
		    new AbstractAction("Redo") {
		        public void actionPerformed(ActionEvent evt) {
		            try {
		                if (undo.canRedo()) {
		                    undo.redo();
		                }
		            } catch (CannotRedoException e) {
		            }
		        }
		    });

		// Bind the redo action to ctl-Y
		textcomp.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
		
		JFrame mainframe = new JFrame(getClass().getSimpleName());
	    if (!IJ.isWindows())
	    	mainframe.setSize(430, 610);
	    else
	    	mainframe.setSize(400, 585);
	    mainframe.setLocation(0, (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2-mainframe.getHeight()/2));
	    mainframe.setResizable(false);
	    
	    mainframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainframe.setBounds(100, 100, 443, 650);
		
		mainframe.add(textcomp);
		mainframe.setVisible(true);
	}
}
