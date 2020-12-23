package testclasses;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TestRemoveComponent {

	static boolean added = true;
	
	public static void test(){
		JFrame jframe = new JFrame("name");
		jframe.setSize(new Dimension(500,500));
		JLabel jlabel = new JLabel("test");
		
		JPanel jpanel = new JPanel();
		jframe.getContentPane().add(jpanel);
		GridBagLayout gbl = new GridBagLayout();
		gbl.columnWidths = new int[]{0, 0};
		gbl.rowHeights = new int[]{0, 0, 0};
		gbl.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		jpanel.setLayout(gbl);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		jpanel.add(jlabel,gbc);
		
		jpanel.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if(added){
					Container jc = jlabel.getParent();
					if(jc!=null)
						jc.remove(jlabel);
				}
				else{
					Container jc = jlabel.getParent();
					GridBagLayout tgbl = (GridBagLayout)jc.getLayout();
					GridBagConstraints tgbc = tgbl.getConstraints(jlabel);
					jpanel.add(jlabel,tgbc);
				}
				
				added = !added;
				
				jpanel.revalidate();
				jpanel.repaint();
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
		
		jframe.setVisible(true);
	}
}
