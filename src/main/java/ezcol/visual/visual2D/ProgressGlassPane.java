package ezcol.visual.visual2D;

//This is a modified version of the following class
//Retrived from http://www.java2s.com/Code/Java/Swing-Components/GlasspanePainting.htm
/*
 * Copyright (c) 2007, Romain Guy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   * Neither the name of the TimingFramework project nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 *
 * @author Romain Guy
 */
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Random;

import javax.swing.JComponent;

import ezcol.main.PluginStatic;

@SuppressWarnings("serial")
public class ProgressGlassPane extends JComponent{
    private static final int BAR_WIDTH = 200;
    private static final int BAR_HEIGHT = 10;
    
    private static final Color TEXT_COLOR = new Color(0x333333);
    //private static final Color BORDER_COLOR = new Color(0x333333);
    
    private static final float[] GRADIENT_FRACTIONS = new float[] {
        0.0f, 0.499f, 0.5f, 1.0f
    };
    private static final Color[] GRADIENT_COLORS = new Color[] {
        Color.GRAY, Color.DARK_GRAY, Color.BLACK, Color.GRAY
    };
    private static final Color GRADIENT_COLOR2 = Color.WHITE;
    private static final Color GRADIENT_COLOR1 = Color.GRAY;

    private int wholeProgress = 100;
    private String message = "Analyses in progress...";
    private int progress = 0;
    
    //new field(s)
    private float alpha=0.65f;
    private int oldProgress;
    private Random random;
    private String[] tips = {"Do you know?\nYou could hold \"Shift\" while pressing the button to ONLY run on the current slice of phase contrast channel.",
    						 "Do you know?\nYou could send us an email by clicking the \"Email\" button at the bottom right corner.",
    						 "Do you know?\nPutting a tip here is inspired by video games.",
    						 "Do you know?\nYou could hold \"Alt\" while pressing any button to boost the performance. (Warning: it might be unstable)",
    						 "Do you know?\nTo learn more about the metric, click the name in the subtab of \"Metrics info\" in \"Analysis\"",
    						 "Do you know?\nThere might be additional information when the cursor hovers over buttons or labels",
    						 "Do you know?\nThis plugin is available on GitHub at https://github.com/DrHanLim/EzColocalization",
    						 "Do you know?\nYou can set the parameters of background subtraction in \"Parameters...\" of \"Settings\" menu"};
    
    public ProgressGlassPane() {
        setBackground(Color.WHITE);
        setFont(new Font("Default", Font.BOLD, 16));
        random = new Random();
    }

    public int getProgress() {
        return progress;
    }
    
    /**
     * This doesn't allow progress bar to roll back once increased to a higher value
     * @param progress a integer from 0 to 100 determine the current progress
     */
    public void setProgress(int progress) {
    	if(progress>wholeProgress)
    		progress=wholeProgress;
    	if(progress<0)
    		progress=0;
        oldProgress = this.progress;
        this.progress = progress;
        
        // computes the damaged area
        FontMetrics metrics = getGraphics().getFontMetrics(getFont()); 
        int w = (int) (BAR_WIDTH * ((float) oldProgress / wholeProgress));
        int x = w + (getWidth() - BAR_WIDTH) / 2;
        int y = (getHeight() - BAR_HEIGHT) / 2;
        y += metrics.getDescent() / 2;
        
        w = (int) (BAR_WIDTH * ((float) progress / wholeProgress)) - w;
        int h = BAR_HEIGHT;
        repaint(x, y, w, h);
    }
    
    /**
     * This is more flexible than setProgress allowing the progress bar to roll back
     * @param progress a integer from 0 to 100 determine the current progress
     */
    public void setValue(int progress) {
    	if(progress>wholeProgress)
    		progress=wholeProgress;
    	if(progress<0)
    		progress=0;
        oldProgress = this.progress;
        this.progress = progress;
        
        // computes the damaged area
        FontMetrics metrics = getGraphics().getFontMetrics(getFont()); 
        int w = (int) (BAR_WIDTH);
        int x = (getWidth() - BAR_WIDTH) / 2;
        int y = (getHeight() - BAR_HEIGHT) / 2;
        y += metrics.getDescent() / 2;
        
        w = (int) (BAR_WIDTH) ;
        int h = BAR_HEIGHT;
        repaint(x, y, w, h);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
    	
        // enables anti-aliasing
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        // gets the current clipping area
        Rectangle clip = g.getClipBounds();
        
        // sets a 65% translucent composite
        AlphaComposite alpha = AlphaComposite.SrcOver.derive(this.alpha);
        Composite composite = g2.getComposite();
        g2.setComposite(alpha);
        
        // fills the background
        g2.setColor(getBackground());
        g2.fillRect(clip.x, clip.y, clip.width, clip.height);
        
        // centers the progress bar on screen
        FontMetrics metrics = g.getFontMetrics();        
        int x = (getWidth() - BAR_WIDTH) / 2;
        int y = (getHeight() - BAR_HEIGHT - metrics.getDescent()) / 2;
        
        // draws the text
        g2.setColor(TEXT_COLOR);
        g2.drawString(message, x, y);
        
        // goes to the position of the progress bar
        y += metrics.getDescent();
        
        // computes the size of the progress indicator
        int w = (int) (BAR_WIDTH * ((float) progress / wholeProgress));
        int h = BAR_HEIGHT;
        
        // draws the content of the progress bar
        Paint paint = g2.getPaint();
        
        // bar's background
        Paint gradient = new GradientPaint(x, y, GRADIENT_COLOR1,
                x, y + h, GRADIENT_COLOR2);
        g2.setPaint(gradient);
        g2.fillRect(x, y, BAR_WIDTH, BAR_HEIGHT);
        
        // actual progress
        gradient = new LinearGradientPaint(x, y, x, y + h,
                GRADIENT_FRACTIONS, GRADIENT_COLORS);
        g2.setPaint(gradient);
        g2.fillRect(x, y, w, h);
        
        g2.setPaint(paint);
        // draws the progress bar border
        g2.drawRect(x, y, BAR_WIDTH, BAR_HEIGHT);
        
        //draw the tip
        drawStringMultiLine(g2,tips[random.nextInt(tips.length)], getWidth()/5*4,getWidth()/10, getHeight()/4*3);
        
        g2.setComposite(composite);
    }
    
    /**
     * A method to draw long text spliting automaticaly by giving the line width.
     * Retrived from stackoverflow with sligntly modification so that '\n' is considered as a line splitter
     * http://stackoverflow.com/questions/4413132/problems-with-newline-in-graphics2d-drawstring
     * @author Ivan De Sousa Paz
     * @param g
     * @param text
     * @param lineWidth
     * @param x
     * @param y
     */
    protected void drawStringMultiLine(Graphics2D g, String wholeText, int lineWidth, int x, int y) {
        FontMetrics m = g.getFontMetrics();
        String[] breakedTexts = wholeText.split("\n");
        for(String text : breakedTexts){
	        if(m.stringWidth(text) < lineWidth) {
	            g.drawString(text, x, y);
	        } else {
	        	String[] words = text.split(" ");
	            String currentLine = words[0];
	            for(int i = 1; i < words.length; i++) {
	                if(m.stringWidth(currentLine+words[i]) < lineWidth) {
	                    currentLine += " "+words[i];
	                } else {
	                    g.drawString(currentLine, x, y);
	                    y += m.getHeight();
	                    currentLine = words[i];
	                }
	            }
	            if(currentLine.trim().length() > 0) {
	                g.drawString(currentLine, x, y);
	            }
	        }
	        y += m.getHeight();
        }
    }
    
}
