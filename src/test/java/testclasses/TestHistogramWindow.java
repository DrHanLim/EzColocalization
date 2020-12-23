package testclasses;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import ezcol.files.FilesIO;
import ezcol.metric.StringCompiler;
import ezcol.visual.visual2D.HistogramGenerator;
import ezcol.visual.visual2D.ProgressGlassPane;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.ResultsTable;

public class TestHistogramWindow implements ActionListener{
	
	final int repeat = 2;
	private ProgressGlassPane pg;
	private JFrame mainframe;
	
	public TestHistogramWindow(){
		gui();
		execute();
	}
	
	public TestHistogramWindow(boolean t){
		finishStack();
	}
	
	private void gui(){
		mainframe = new JFrame(getClass().getSimpleName());
	    if (!IJ.isWindows())
	    	mainframe.setSize(430, 610);
	    else
	    	mainframe.setSize(400, 585);
	    mainframe.setLocation(0, (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2-mainframe.getHeight()/2));
	    mainframe.setResizable(false);
	    if(FilesIO.getResource("coloc.gif")!=null)
	    	mainframe.setIconImage(new ImageIcon(FilesIO.getResource("coloc.gif")).getImage());
	    
		mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainframe.setBounds(100, 100, 443, 650);
		
		JButton analyze = new JButton("Analyze");
		analyze.addActionListener(this);
		JPanel mainpanel = new JPanel();
		JTextArea jtb = new JTextArea(20,20);
		jtb.setLineWrap(true);
		jtb.setEditable(true);
		mainpanel.add(analyze);
		mainpanel.add(jtb);
		mainframe.getContentPane().add(mainpanel);
		
		pg=new ProgressGlassPane();
		pg.requestFocus();
		pg.addMouseListener(new MouseAdapter() {});
		pg.addMouseMotionListener(new MouseMotionAdapter() {});
		pg.addKeyListener(new KeyAdapter() {});
		mainframe.setGlassPane(pg);
	    mainframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    mainframe.setVisible(true);
	}
	
	
	private void finishStack(){
		HistogramGenerator histogramAll=new HistogramGenerator();
		ResultsTable rt = new ResultsTable();
		Random rand = new Random();
		
		double[] x=new double[3000];
		while(rt.getCounter()<x.length)
			rt.incrementCounter();
		for(int i=0;i<repeat;i++){
			for(int j=0;j<x.length;j++){
				if(rand.nextDouble()<0.2)
					x[j]=Double.NaN;
				else
					x[j]=rand.nextDouble();
				rt.setValue("Metric "+i, j, x[j]);
			}
			histogramAll.addToHistogramStack("Metric "+i, x);
			
		}
		histogramAll.showHistogramStack();
		//ImagePlus imp = WindowManager.getCurrentImage();
		//imp.duplicate().show();
		
		
		//TestStackWindow tsw = new TestStackWindow(imp.duplicate());
		
		//rt.show(getClass().getName());
	}
	
	private void execute(){
		SwingWorker<Void, Integer> swVI = new SwingWorker<Void, Integer>(){
		    
		    @Override
		    protected Void doInBackground()
		    {
		    	pg.setVisible(true);
		        for (int iFrame=1;iFrame<=repeat;iFrame++)
		        {
		        	if(isCancelled())
		        		break;
		            //Work on each element
		        	applyToStack(iFrame);
	
		            //Store them in the 'chunks' list
		            publish(iFrame);
		        }
		        return null;
		    }
	
		    @Override
		    protected void process(List<Integer> chunks)
		    {
		        for(int iFrame : chunks)
		        {
		            //Get the numbers in the 'chunks' list
		            //To use wherever/however you want
		        	if(isCancelled())
		        		break;
		        	
		            pg.setProgress(iFrame*100/(repeat));
		            //No delay should be put here otherwise
		            //the progress wouldn't show up
		            
		        }
		    }
		    
		    @Override
		    protected void done()
		    {
		        //Something to do when everything is done
		    	//We have to wait here until the analysis is done
		    	while(!isDone());
		    	if(!isCancelled())
		    		finishStack();
		        pg.setVisible(false);
		        pg.setValue(0);
		    }
		    
		};
		swVI.execute();
	}
	
	private void applyToStack(int iFrame){
		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		execute();
	}

	
}
