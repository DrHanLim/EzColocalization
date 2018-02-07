/*
 * ImageInfo.java modified from ImgInfo.java
 *
 * Created on 14 janvier 2008, 21:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ezcol.main;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import ij.*;

/**
 *
 * @author Fabrice Cordelies
 */
public class ImageInfo implements Comparable<ImageInfo>{
	
	public static int default_length = 25;
	public static final String NONE = "*None*";
	public static final String ROI_MANAGER = "ROI Manager";
	public static final int NONE_ID = 0;
	public static final int ROI_MANAGER_ID = 1;
    public String title;
    public int ID;
    public int bitDepth;
    public static final ImageInfoListCellRenderer RENDERER = new ImageInfoListCellRenderer();
    
    /** Creates a new instance of ImgInfo */
    public ImageInfo() {
        this.title=NONE;
        this.ID=NONE_ID;
        //Surprisingly, ImageJ does not have a static final field for bitDepth
        //What a shame!
        this.bitDepth=24;
    }
    /** Creates a new instance of ImgInfo */
    public ImageInfo(String title,int ID,int bitDepth) {
        this.title=title;
        this.ID=ID;
        this.bitDepth=bitDepth;
     }
    
    public ImageInfo(ImagePlus imp)
    {
    	this.title=imp.getTitle();
        this.ID=imp.getID();
        this.bitDepth=imp.getBitDepth();
    }
    
    /**
     * Do not check image title or bitdepth but only image ID
     * @param imp
     * @return
     */
    public boolean equalID(ImagePlus imp)
    {
    	//if(imp.getTitle()==title&&imp.getID()==ID&&imp.getBitDepth()==depth)
    	if(imp.getID()==ID)
    		return true;
		else
			return false;
    }
    
    public boolean equal(ImageInfo imp)
    {
    	if(imp.title==this.title&&imp.ID==this.ID&&imp.bitDepth==this.bitDepth)
    		return true;
		else
			return false;
    }
    
    /*@Override
    public String toString(){
    	return getTrimTitle(default_length);
    }*/
    
    public String getTrimTitle(int length){
    	if(title==null)
    		return title;
    	if(title.length()<length)
    		return title;
    	return title.substring(0, length);
    }
    
	@Override
	public int compareTo(ImageInfo o) {
		// TODO Auto-generated method stub
		return equal(o)?0:this.ID-o.ID;
	}
    
}

/**
 * Custom Renderer for ImageInfo being used in JCombobox
 * @author Huanjie Sheng
 *
 */
@SuppressWarnings("serial")
class ImageInfoListCellRenderer extends DefaultListCellRenderer {

    @SuppressWarnings("rawtypes")
	public Component getListCellRendererComponent(
                                   JList list,
                                   Object value,
                                   int index,
                                   boolean isSelected,
                                   boolean cellHasFocus) {
        if (value instanceof ImageInfo) {
            value = ((ImageInfo)value).getTrimTitle(ImageInfo.default_length);
        }
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        return this;
    }
}
