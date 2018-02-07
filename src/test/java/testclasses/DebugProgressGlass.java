package testclasses;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import ezcol.visual.visual2D.ProgressGlassPane;
import ij.IJ;

public class DebugProgressGlass implements ActionListener {

	private int n=10;
	private JFrame mainframe;
	private JButton analyze;
	private ProgressGlassPane pg;
	private JButton trial;
	
	public DebugProgressGlass(){GUI();}
	
	public void GUI()
	{
		
		mainframe = new JFrame("Name");
        mainframe.setSize(400, 585);
        if (!IJ.isWindows())mainframe.setSize(430, 610);
        mainframe.setLocation(0, (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2-mainframe.getHeight()/2));
        mainframe.setResizable(false);
        //mainframe.setIconImage(new ImageIcon(getClass().getResource("coloc.png")).getImage()); 
        analyze = new JButton("Analyze");
        analyze.addActionListener(this);
        trial = new JButton("Try");
        trial.addActionListener(this);
        JPanel jp = new JPanel();
        jp.add(analyze);
        jp.add(trial);
        mainframe.getContentPane().add(jp);
        
        pg = new ProgressGlassPane();
        mainframe.setGlassPane(pg);
		mainframe.setVisible(true);
		
		/*pg.setVisible(true);
		int[] processes=new int[]{0,20,40,60,80,100};
		for(int i=0;i<5;i++){
			pg.setValue(processes[i]);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		pg.setVisible(false);*/
	}
	
	public Thread testRun(int i)
	{
		Thread thread = new Thread(new Runnable(){
			public void run(){
				System.out.println("test "+i);
				/*try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			}
			}
		);
		thread.start();
		return thread;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Object origin=e.getSource();
		if(origin==analyze)
			execute().execute();
		if(origin==trial)
		{
			System.out.println("Action Performed");
		}
		
		/*Thread[] allThreads=new Thread[n];
		for(int i=0;i<n;i++)
			allThreads[i]=testRun(i);
		for(int i=0;i<n;i++){
				try {
					allThreads[i].join();
					pg.setProgress((int)((float)i/n*100));
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		}*/
		//pg.setVisible(false);
	}
	
	private SwingWorker<Void, Integer> execute(){
	
		return new SwingWorker<Void, Integer>(){
			    private Thread[] allThreads=new Thread[n];
			    @Override
			    protected Void doInBackground()
			    {
			    	pg.setVisible(true);
			        for (int i = 0; i <= 5; i++)
			        {
			            //Generate a bunch of numbers
			        	allThreads[i]=testRun(i);
			            //Pause in between
			        	try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		
			            //Store them in the 'chunks' list
			            publish(i);
			        }
		
			        return null;
			    }
		
			    @Override
			    protected void process(List<Integer> chunks)
			    {
			        for(int i : chunks)
			        {
			            //Get the numbers in the 'chunks' list
			            //To use wherever/however you want
			        	try {
							allThreads[i].join();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			            pg.setProgress(i*20);
			            
			            System.out.println(i);
			            
			        }
			    }
			    
			    @Override
			    protected void done()
			    {
			        //Something to do when everything is done
			        //pg.setProgress(50);
			        pg.setVisible(false);
			        pg.setValue(0);
			    }
			};
		}
}
