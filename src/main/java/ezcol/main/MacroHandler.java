package ezcol.main;

import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;

import ij.IJ;
import ij.ImagePlus;
import ij.Menus;
import ij.WindowManager;
import ij.plugin.frame.Recorder;

import ezcol.metric.BasicCalculator;
import ezcol.metric.StringCompiler;

/**
 * This class contains purely static variables to handle the recording and 
 * the interpretation of macro code.
 * @author Huanjie Sheng
 *
 */
public class MacroHandler extends PluginStatic{
	
	//cell filters macro
	private static final String[] MACRO_FILTERSTRINGS = macroStrArray(filterStrings);
	private static final String[] MACRO_SIZEFILTERS = macroStrArray(SIZE_FILTERS);
	//image macro
	private static final String[] MACRO_IMGLABELS = macroStrArray(imgLabels);
	//alignment macro
	private static final String[] MACRO_ALLTHOLDS = macroStrArray(ALLTHOLDS);
	//heatmaps macro
	private static final String[] MACRO_HEATMAPS = macroStrArray(HEATMAPS);
	private static final String[] MACRO_HEATMAPOPTS = macroStrArray(HEATMAPOPTS);
	//analysis macro
	private static final String[] MACRO_METRICNAMES = macroStrArray(METRICACRONYMS);
	private static final String[] MACRO_OTHERNAMES = macroStrArray(OTHERNAMES);
	//output macro
	private static final String[] MACRO_OUTPUTMETRICS = macroStrArray(OUTPUTMETRICS);
	private static final String[] MACRO_OUTPUTOTHERS = macroStrArray(OUTPUTOTHERS);
	
	private static final String[] MACRO_METRIC_THOLDS = macroStrArray(METRIC_THOLDS);
	/**
	 * This method reads the input string from ImageJ macro
	 * and retrieve all necessary parameters
	 * If the option is missing, the default value will be used
	 * @warning Please make sure all strings match to the corresponding strings in macroGenerator
	 * @param arg macro string
	 */
    public static void macroInterpreter(String arg){
    	if(arg==null||arg.length()<=0)
    		return;
        int start=0, end=0;
        //Check what should be done
        
        //Find the images' names
        ImagePlus temp = null;
        for (int ipic=0;ipic<imps.length;ipic++){
        	start=arg.indexOf(MACRO_IMGLABELS[ipic]+"=");
        	if(start==-1){
        		imps[ipic] = null;
        	}else{
        		start += (MACRO_IMGLABELS[ipic]+"=").length();
	            end=arg.indexOf(" ", start);
	            if ((arg.charAt(start)+"").equals("[")){
	                start++;
	                end=arg.indexOf("]", start);
	            }
	            String imgTitle=arg.substring(start, end);
	            
	            if(imgTitle.equals(ImageInfo.ROI_MANAGER)){
					imps[ipic] = roiManager2Mask(temp);
					// introduced in 1.1.3 force black background when ROI manager is used.
					lightBacks[ipic] = false;
	            }
				else if(imgTitle.equals(ImageInfo.NONE))
					imps[ipic] = null;
				else{
					imps[ipic] = WindowManager.getImage(imgTitle);
					temp = imps[ipic];
				}
        	}
        }
    	start=end+1;
    	//get rid of images' names to be safe
    	if(start>=arg.length())
    		return;
    	else
    		arg = arg.substring(start,arg.length());
    	
    	//Macro.getOptions() adds a space after the string before returning it
    	if(arg.charAt(arg.length()-1)!=' ')
    		arg = arg + " ";
    	//However, this is absolutely necessary to add a space before arg
    	if(arg.charAt(0)!=' ')
    		arg = " " + arg;
    	
    	
    	for(int iColumn=0;iColumn<metric_chckes.length;iColumn++){
    		start = arg.indexOf(MACRO_METRICNAMES[iColumn]);
  			if(start==-1)
  				metric_chckes[iColumn] = false;
  			else{
  				end = start + MACRO_METRICNAMES[iColumn].length();
  				metric_chckes[iColumn] = arg.charAt(end)==' ' && arg.charAt(start-1)==' ';
  			}
  		}
    	
    	boolean anyOtherMetric = false;
    	for(int iColumn=0;iColumn<other_chckes.length;iColumn++){
    		start = arg.indexOf(MACRO_OTHERNAMES[iColumn]);
  			if(start==-1)
  				other_chckes[iColumn] = false;
  			else{
  				end = start + MACRO_OTHERNAMES[iColumn].length();
  				other_chckes[iColumn] = arg.charAt(end)==' ' && arg.charAt(start-1)==' ';
  			}
  			anyOtherMetric |= other_chckes[iColumn];
  		}
    	
    	if(other_chckes[CUSTOM]){
    		custom_chck = true;
    		start=arg.indexOf("custom-javafile=");
    		if(start==-1){
    			customCode_text = "";
    		}else{
		    	start += ("custom-javafile=").length();
		        end=arg.indexOf(" ", start);
		        if ((arg.charAt(start)+"").equals("[")){
		            start++;
		            end=arg.indexOf("]", start);
		        }
		    	String path=arg.substring(start, end);
		    	customCode_text = StringCompiler.open(path);
    		}
    	}
    	
        for(int iThold=0;iThold<alignThold_combs.length;iThold++){
        	start=arg.indexOf("alignthold"+(iThold+1)+"=");
        	if(start==-1){
        		alignThold_combs[iThold] = DEFAULT_CHOICE;
        		if(iThold < align_chckes.length)
        			align_chckes[iThold] = false;
        	}else{
        		start += ("alignthold"+(iThold+1)+"=").length();
	            end=arg.indexOf(" ", start);
	            if ((arg.charAt(start)+"").equals("[")){
	                start++;
	                end=arg.indexOf("]", start);
	            }
	            String thresholdMethod = arg.substring(start, end);
	            alignThold_combs[iThold] = Arrays.asList(MACRO_ALLTHOLDS).indexOf(thresholdMethod);
	            if(iThold < align_chckes.length)
        			align_chckes[iThold] = true;
        	}
        }
        
        {
    		start = arg.indexOf("dows");
  			if(start==-1)
  				waterShed_chck = false;
  			else{
  				end = start + "dows".length();
  				waterShed_chck = (end>=arg.length() || arg.charAt(end)    ==' ') 
				           && (start<=0          || arg.charAt(start-1)==' ');
  			}
  		}

  		for (int iSize=0;iSize<MACRO_SIZEFILTERS.length;iSize++){
  			start=arg.indexOf(MACRO_SIZEFILTERS[iSize]+"=");
  			if(start==-1){
  				filterMinSize_texts[iSize] = DEFAULT_MIN;
  				filterMaxSize_texts[iSize] = DEFAULT_MAX;
  			}else{
  				start += (SIZE_FILTERS[iSize]+"=").length();
	            end=arg.indexOf(" ", start);
	            if ((arg.charAt(start)+"").equals("[")){
	                start++;
	                end=arg.indexOf("]", start);
	            }
	  			String tempSize=arg.substring(start, end);
	  			double[] range = str2doubles(tempSize);
	  			filterMinSize_texts[iSize] = range[0];
	  			filterMaxSize_texts[iSize] = range[1];
  			}
  			
  		} 
  		
  		for(int iFilter=0;iFilter<filter_combs.length;iFilter++){
  			//getFilterChoices
  			start=arg.indexOf("filter"+(iFilter+1)+"=");
  			if(start==-1){
  				filter_combs[iFilter] = DEFAULT_CHOICE;
  			}else{
  				start += (("filter"+(iFilter+1)+"=").length());
	            end=arg.indexOf(" ", start);
	            if ((arg.charAt(start)+"").equals("[")){
	                start++;
	                end=arg.indexOf( "]", start);
	            }
	            String thisFilter=arg.substring(start, end);
	            filter_combs[iFilter]=Arrays.asList(MACRO_FILTERSTRINGS).indexOf(thisFilter);
  			}
  			
  			//getFilterRange
  			start=arg.indexOf("range"+(iFilter+1)+"=");
  			if(start==-1){
  				filterMinRange_texts[iFilter] = DEFAULT_MIN;
  				filterMaxRange_texts[iFilter] = DEFAULT_MAX;
  			}else{
  				start += (("range"+(iFilter+1)+"=").length());
	            end=arg.indexOf(" ", start);
	            if ((arg.charAt(start)+"").equals("[")){
	                start++;
	                end=arg.indexOf("]", start);
	            }
	  			String filterRange=arg.substring(start, end);
	  			double[] range = str2doubles(filterRange);
	  			filterMinRange_texts[iFilter] = range[0];
	  			filterMaxRange_texts[iFilter] = range[1];
  			}
  			
  		}
  		
  		adFilterChoices.clear();
  		end=0;
  		while(true){
  			start=arg.indexOf("filterA",end);
  			if(start==-1)
  				break;
  			start=arg.indexOf("=", start) + 1 ;
  			end=arg.indexOf(" ", start);
            if ((arg.charAt(start)+"").equals("[")){
                start++;
                end=arg.indexOf( "]", start);
            }
            String thisFilter=arg.substring(start, end);
  			adFilterChoices.add(Arrays.asList(MACRO_FILTERSTRINGS).indexOf(thisFilter));
  		}
  		
  		adMinRanges.clear();
  		adMaxRanges.clear();
  		end=0;
  		while(true){
  			start=arg.indexOf("rangeA",end);
  			if(start==-1)
  				break;
  			start=arg.indexOf("=", start) + 1 ;
  			end=arg.indexOf(" ", start);
            if ((arg.charAt(start)+"").equals("[")){
                start++;
                end=arg.indexOf( "]", start);
            }
            String filterRange=arg.substring(start, end);
  			double[] range = str2doubles(filterRange);
  			adMinRanges.add(range[0]);
  			adMaxRanges.add(range[1]);
  		}
  		
  		{
    		start = arg.indexOf("dosp");
  			if(start==-1)
  				scatter_chck = false;
  			else{
  				end = start + "dosp".length();
  				scatter_chck = arg.charAt(end)==' ' && arg.charAt(start-1)==' ';
  			}
  		}
  		
  		{
    		start = arg.indexOf("domt");
  			if(start==-1)
  				matrix_chck = false;
  			else{
  				end = start + "domt".length();
  				matrix_chck = arg.charAt(end)==' ' && arg.charAt(start-1)==' ';
  			}
  			
  			if(matrix_chck){
  			
  				start=arg.indexOf("mmetric=");
  		  		if(start==-1){
  		  			matrixMetric_comb = DEFAULT_CHOICE;
  		  		}else{
  			  		start += ("mmetric=".length());
  			        end = arg.indexOf(" ", start);
  			        if (arg.charAt(start)=='['){
  			            start++;
  			            end=arg.indexOf("]", start);
  			        }
  			        matrixMetric_comb = Arrays.asList(matrixMetricList).indexOf(arg.substring(start, end));
  		  		}
  		  		
	  		  	start=arg.indexOf("mstats=");
	  	  		if(start==-1){
	  	  			matrixStats_comb = DEFAULT_CHOICE;
	  	  		}else{
	  		  		start += ("mstats=".length());
	  		        end = arg.indexOf(" ", start);
	  		        if (arg.charAt(start)=='['){
	  		            start++;
	  		            end=arg.indexOf("]", start);
	  		        }
	  		      	matrixStats_comb = Arrays.asList(STATS_METHODS).indexOf(arg.substring(start, end));
	  	  		}
  				
	  			for(int iTOS=0;iTOS<matrixFT_spin.length;iTOS++){
	  	  			start=arg.indexOf("ft"+(iTOS+1)+"=");
	  	  			if(start==-1){
	  	  				matrixFT_spin[iTOS] = DEFAULT_FT;
	  	  			}else{
	  	  				start += ("ft"+(iTOS+1)+"=").length();
	  		  	        end=arg.indexOf(" ", start);
	  		  	        if ((arg.charAt(start)+"").equals("[")){
	  		  	            start++;
	  		  	            end=arg.indexOf("]", start);
	  		  	        }
	  		  	        String thisFT = arg.substring(start, end);
	  		  			matrixFT_spin[iTOS] = (int)parseDouble(thisFT);
	  	  			}
	  	  		}
  			}
  		}
                
  		start=arg.indexOf("hmscale=");
  		if(start==-1){
  			heatmap_radio = DEFAULT_CHOICE;
  		}else{
	  		start += ("hmscale=".length());
	        end = arg.indexOf(" ", start);
	        if (arg.charAt(start)=='['){
	            start++;
	            end=arg.indexOf("]", start);
	        }
	        heatmap_radio = Arrays.asList(MACRO_HEATMAPOPTS).indexOf(arg.substring(start, end));
  		}
  		
  		for(int iHeat=0;iHeat<heatmapColor_combs.length;iHeat++){
  			start=arg.indexOf("hmcolor"+(iHeat+1)+"=");
  			if(start==-1){
  				heatmapColor_combs[iHeat] = DEFAULT_CHOICE;
  				heatmap_chckes[iHeat] = false;
  	  		}else{
	  			start += (("hmcolor"+(iHeat+1)+"=").length());
	  	        end=arg.indexOf(" ", start);
	  	        if ((arg.charAt(start)+"").equals("[")){
	  	            start++;
	  	            end=arg.indexOf("]", start);
	  	        }
	  	        String thisHeatmap=arg.substring(start, end);
	  			heatmapColor_combs[iHeat] = Arrays.asList(MACRO_HEATMAPS).indexOf(thisHeatmap);
	  			heatmap_chckes[iHeat] = true;
  	  		}
  		}
  		
  		int iFTs = metricThold_radios.length;
  		
  		for(int iMetric=0;iMetric<metricThold_radios.length;iMetric++){
  			start=arg.indexOf("metricthold"+(iMetric+1)+"=");
  			if(start==-1){
  				metricThold_radios[iMetric] = DEFAULT_CHOICE;
  			}else{
  				start += (("metricthold"+(iMetric+1)+"=").length());
	  	        end=arg.indexOf(" ", start);
	  	        if ((arg.charAt(start)+"").equals("[")){
	  	            start++;
	  	            end=arg.indexOf("]", start);
	  	        }
	  	        String thisThold = arg.substring(start, end);
  	      		metricThold_radios[iMetric] = Arrays.asList(MACRO_METRIC_THOLDS).indexOf(thisThold);
  			}
  			if(metricThold_radios[iMetric]==IDX_THOLD_FT)
  				iFTs+=allFT_spins.length;
  		}
  		
  		for(int iChannel=0;iChannel<allFT_spins.length;iChannel++){
  			for(int iMetric=0;iMetric<allFT_spins[iChannel].length;iMetric++){
	  			start=arg.indexOf("allft-c"+(iChannel+1)+"-"+(iMetric+1)+"=");
	  			if(start==-1){
	  				allFT_spins[iChannel][iMetric] = DEFAULT_FT;
	  			}else{
	  				start += ("allft-c"+(iChannel+1)+"-"+(iMetric+1)+"=").length();
		  	        end=arg.indexOf(" ", start);
		  	        if ((arg.charAt(start)+"").equals("[")){
		  	            start++;
		  	            end=arg.indexOf("]", start);
		  	        }
		  	        String thisFT = arg.substring(start, end);
		  	      	allFT_spins[iChannel][iMetric] = (int)parseDouble(thisFT);
	  			}
  			}
  		}
  		
  		/**
		 * retrieve metricTholds and allFTs to allTholds
		 * but not record it
		 */
  		allTholds = new int[iFTs+1];
		iFTs = 0;
		for(int iMetric=0;iMetric<metricThold_radios.length;iMetric++){
			if(metric_chckes[iMetric]){
				allTholds[iFTs++] = BasicCalculator.getThold(metricThold_radios[iMetric]);
				if(BasicCalculator.getThold(metricThold_radios[iMetric])==BasicCalculator.THOLD_FT)
					for(int iChannel=0;iChannel<allFT_spins.length;iChannel++)
						allTholds[iFTs++]=allFT_spins[iChannel][iMetric];
			}else{
				allTholds[iFTs++]=BasicCalculator.THOLD_NONE;
			}
		}
		if (anyOtherMetric)
			allTholds[allTholds.length-1] = BasicCalculator.THOLD_ALL;
		else
			allTholds[allTholds.length-1] = BasicCalculator.THOLD_NONE;

  		for(int iColumn=0;iColumn<outputMetric_chckes.length;iColumn++){
  			start = arg.indexOf(MACRO_OUTPUTMETRICS[iColumn]);
  			if(start==-1)
  				outputMetric_chckes[iColumn] = false;
  			else{
  				end = start + MACRO_OUTPUTMETRICS[iColumn].length();
  				outputMetric_chckes[iColumn] = arg.charAt(end)==' ' && arg.charAt(start-1)==' ';
  			}
  		}
  		
  		for(int iColumn=0;iColumn<outputOpt_chckes.length;iColumn++){
  			start = arg.indexOf(MACRO_OUTPUTOTHERS[iColumn]);
  			if(start==-1)
  				outputOpt_chckes[iColumn] = false;
  			else{
  				end = start + MACRO_OUTPUTOTHERS[iColumn].length();
  				outputOpt_chckes[iColumn] = arg.charAt(end)==' ' && arg.charAt(start-1)==' ';
  			}
  		}
    }
    
    /**
     * This method creates ImageJ macro code
     * by converting the current setting to a string
     * and passing it to the Recorder.
     * @warning Please make sure all strings match to the corresponding strings in macroInterpreter
     */
    private static void macroGenerator(){
    	@SuppressWarnings("rawtypes")
		Hashtable table = Menus.getCommands();
		String className = (String)table.get(pluginName);
		if (className == null)
			Recorder.record("//Warning: The Plugin class cannot be found");
    	
        Recorder.setCommand(pluginName);
        
        for (int ipic=0;ipic<imps.length;ipic++)
        	if(imps[ipic]!=null)
        		Recorder.recordOption(MACRO_IMGLABELS[ipic],imps[ipic].getTitle());
        
        for(int iColumn=0;iColumn<metric_chckes.length;iColumn++)
        	if(metric_chckes[iColumn])
				Recorder.recordOption(MACRO_METRICNAMES[iColumn]);
        
        for(int iColumn=0;iColumn<other_chckes.length;iColumn++)
        	if(other_chckes[iColumn])
				Recorder.recordOption(MACRO_OTHERNAMES[iColumn]);
        
        for(int iThold=0;iThold<alignThold_combs.length;iThold++)
        	Recorder.recordOption("alignthold"+(iThold+1),MACRO_ALLTHOLDS[alignThold_combs[iThold]]);
        
        if(waterShed_chck)
        	Recorder.recordOption("dows");
  		
  		for (int iSize=0;iSize<filterMinSize_texts.length;iSize++)
  			Recorder.recordOption(MACRO_SIZEFILTERS[iSize], getFilterRange(filterMinSize_texts[iSize],filterMaxSize_texts[iSize],false));
  		
  		for(int iFilter=0;iFilter<filter_combs.length;iFilter++){
  			Recorder.recordOption("filter"+(iFilter+1), MACRO_FILTERSTRINGS[filter_combs[iFilter]]);
  			Recorder.recordOption("range"+(iFilter+1), getFilterRange(filterMinRange_texts[iFilter],filterMaxRange_texts[iFilter],filterBackRatio_texts[iFilter]));
  		}
  		
  		for(int iFilter=0;iFilter<adFilterChoices.size();iFilter++){
  			Recorder.recordOption("filterA"+(iFilter+1), MACRO_FILTERSTRINGS[adFilterChoices.get(iFilter)]);
  			Recorder.recordOption("rangeA"+(iFilter+1), getFilterRange(adMinRanges.get(iFilter),adMaxRanges.get(iFilter),adBackRatios.get(iFilter)));
  		}
         
  		if(scatter_chck)
        	Recorder.recordOption("dosp");
  		
  		if(matrix_chck)
        	Recorder.recordOption("domt");
  		
  		Recorder.recordOption("hmscale", MACRO_HEATMAPOPTS[heatmap_radio]);
  		
  		for(int iHeat=0;iHeat<heatmapColor_combs.length;iHeat++)
  			Recorder.recordOption("hmcolor"+(iHeat+1), MACRO_HEATMAPS[heatmapColor_combs[iHeat]]);
  		
  		//Recorder.recordOption("tosscale",MACRO_TOSOPTS[mTOSscale]);
  		
		Recorder.recordOption("mmetric", matrixMetricList[matrixMetric_comb]);
		Recorder.recordOption("mstats", STATS_METHODS[matrixStats_comb]);
	
		for(int iFT=0;iFT<matrixFT_spin.length;iFT++){
			Recorder.recordOption("mft"+(iFT+1),""+matrixFT_spin[iFT]);
		}
  		
  		for(int iMetric=0;iMetric<metricThold_radios.length;iMetric++)
  			Recorder.recordOption("metricthold"+(iMetric+1),MACRO_METRIC_THOLDS[metricThold_radios[iMetric]]);
  		
  		for(int iChannel=0;iChannel<allFT_spins.length;iChannel++)
  			for(int iMetric=0;iMetric<allFT_spins[iChannel].length;iMetric++)
  				Recorder.recordOption("allft-c"+(iChannel+1)+"-"+(iMetric+1),""+allFT_spins[iChannel][iMetric]);
  		
  		for(int iColumn=0;iColumn<outputMetric_chckes.length;iColumn++)
        	if(outputMetric_chckes[iColumn])
				Recorder.recordOption(MACRO_OUTPUTMETRICS[iColumn]);
  		
  		for(int iColumn=0;iColumn<outputOpt_chckes.length;iColumn++)
        	if(outputOpt_chckes[iColumn])
				Recorder.recordOption(MACRO_OUTPUTOTHERS[iColumn]);
  		
        Recorder.saveCommand();
    }
    
    /**
     * This type of recorder only records necessary informations.
     */
    private static void macroSmartGenerator(){
    	@SuppressWarnings("rawtypes")
		Hashtable table = Menus.getCommands();
		String className = (String)table.get(pluginName);
		if (className == null)
			Recorder.record("//Warning: The Plugin class cannot be found");
		
		Recorder.setCommand(pluginName);
		
		retrieveOptions();
		
        for (int ipic=0;ipic<imps.length;ipic++)
        	if(imps[ipic]!=null)
        		Recorder.recordOption(MACRO_IMGLABELS[ipic],imps[ipic].getTitle());
        
        for(int iThold=0;iThold<alignThold_combs.length;iThold++)
        	if(iThold >= align_chckes.length ? ((options&RUN_ALIGN)!=0||(options&RUN_IDCELLS)!=0) : align_chckes[iThold])
        		Recorder.recordOption("alignthold"+(iThold+1),MACRO_ALLTHOLDS[alignThold_combs[iThold]]);
        
        if((options&RUN_IDCELLS)!=0){
        	 if(waterShed_chck)
        		 Recorder.recordOption("dows");
        
	  		for (int iSize=0;iSize<filterMinSize_texts.length;iSize++){
	  			if(filterMinSize_texts[iSize]!=DEFAULT_MIN || filterMaxSize_texts[iSize]!=DEFAULT_MAX)
	  				Recorder.recordOption(MACRO_SIZEFILTERS[iSize], getFilterRange(filterMinSize_texts[iSize],filterMaxSize_texts[iSize],false));
	  		}
	  		
	  		for(int iFilter=0;iFilter<filter_combs.length;iFilter++){
	  			if(filterMinRange_texts[iFilter]!=DEFAULT_MIN || filterMaxRange_texts[iFilter]!=DEFAULT_MAX){
		  			Recorder.recordOption("filter"+(iFilter+1), MACRO_FILTERSTRINGS[filter_combs[iFilter]]);
		  			Recorder.recordOption("range"+(iFilter+1), getFilterRange(filterMinRange_texts[iFilter],filterMaxRange_texts[iFilter],filterBackRatio_texts[iFilter]));
	  			}
	  		}
	  		
	  		for(int iFilter=0;iFilter<adFilterChoices.size();iFilter++){
	  			if(adMinRanges.get(iFilter)!=DEFAULT_MIN || adMaxRanges.get(iFilter)!=DEFAULT_MAX){
		  			Recorder.recordOption("filterA"+(iFilter+1), MACRO_FILTERSTRINGS[adFilterChoices.get(iFilter)]);
		  			Recorder.recordOption("rangeA"+(iFilter+1), getFilterRange(adMinRanges.get(iFilter),adMaxRanges.get(iFilter),adBackRatios.get(iFilter)));
	  			}
	  		}
	         
	  		if(scatter_chck)
	        	Recorder.recordOption("dosp");
	  		
	  		if(matrix_chck)
	  		 	Recorder.recordOption("domt");
        }
        
        if((options&RUN_HEAT)!=0){
	  		Recorder.recordOption("hmscale", MACRO_HEATMAPOPTS[heatmap_radio]);
	  		
	  		for(int iHeat=0;iHeat<heatmapColor_combs.length;iHeat++)
	  			if(heatmap_chckes[iHeat])
	  				Recorder.recordOption("hmcolor"+(iHeat+1), MACRO_HEATMAPS[heatmapColor_combs[iHeat]]);
        }
        
        if((options&RUN_MATRIX)!=0){
        	
        	if(matrixMetric_comb!=DEFAULT_CHOICE)
        		Recorder.recordOption("mmetric", matrixMetricList[matrixMetric_comb]);
        	if(matrixStats_comb!=DEFAULT_CHOICE)
        		Recorder.recordOption("mstats", STATS_METHODS[matrixStats_comb]);
	  		
	  		for(int iFT=0;iFT<nReporters;iFT++){
	  			if(matrixFT_spin[iFT]!=DEFAULT_FT)
	  			Recorder.recordOption("mft"+(iFT+1),""+matrixFT_spin[iFT]);
	  		}
        }
        
        int recordChannel = nReporters < allFT_spins.length ? nReporters : allFT_spins.length;
        for(int iMetric=0;iMetric<metric_chckes.length;iMetric++)
        	if(metric_chckes[iMetric]){
				Recorder.recordOption(MACRO_METRICNAMES[iMetric]);
				Recorder.recordOption("metricthold"+(iMetric+1),MACRO_METRIC_THOLDS[metricThold_radios[iMetric]]);		
				for(int iChannel=0;iChannel<recordChannel;iChannel++)
					Recorder.recordOption("allft-c"+(iChannel+1)+"-"+(iMetric+1),""+allFT_spins[iChannel][iMetric]);
        	}
        
        for(int iColumn=0;iColumn<other_chckes.length;iColumn++)
        	if(other_chckes[iColumn])
				Recorder.recordOption(MACRO_OTHERNAMES[iColumn]);
        
        if(other_chckes[CUSTOM]){
        	Recorder.recordPath("custom-javafile", StringCompiler.getDefaultPath());
        }
  		
  		for(int iColumn=0;iColumn<outputMetric_chckes.length;iColumn++)
        	if(outputMetric_chckes[iColumn])
				Recorder.recordOption(MACRO_OUTPUTMETRICS[iColumn]);
  		
  		for(int iColumn=0;iColumn<outputOpt_chckes.length;iColumn++)
        	if(outputOpt_chckes[iColumn])
				Recorder.recordOption(MACRO_OUTPUTOTHERS[iColumn]);
  		
        Recorder.saveCommand();
    }
    
    public static void macroRecorder(boolean smart){
    	if(smart)
    		macroSmartGenerator();
    	else
    		macroGenerator();
    }
    
    
    private static String[] macroStrArray(String[] str){
    	String[] result=new String[str.length];
    	for(int i=0;i<str.length;i++)
    		result[i]=str[i].replaceAll(" ","_").toLowerCase();
    	return result;
    }
    
    public static GUI getProgressBar(){
    	return null;
    }
    
    public static void recordCloseAll(){
    	Recorder.setCommand(pluginName);
    	Recorder.recordOption("close_all");
    	Recorder.saveCommand();
    }
    
    public static void recordSaveAll(String dir){
    	Recorder.setCommand(pluginName);
    	Recorder.recordPath("save_all",dir);
    	Recorder.saveCommand();
    }
    
    public static boolean macroSaveAndClose(String arg){
    	if(arg==null||arg.length()<=0)
    		return false;
    	boolean  saveOrClose = false;
        int start=0, end=0;
        String dir;
        start=arg.indexOf("save_all=");
    	if(start!=-1){
    		start += "save_all=".length();
            end=arg.indexOf(" ", start);
            if ((arg.charAt(start)+"").equals("[")){
                start++;
                end = arg.indexOf("]", start);
            }
            dir = arg.substring(start, end);
            //dir.replace("\\\\", "\\");
            if (!(new File(dir)).isDirectory())
            	IJ.error("Cannot find the directory: "+dir);
            else{
	            try{
	            	saveAllWindows(dir);
	            	saveOrClose = true;
	            }catch(Exception e){
	            	IJ.error("Error in saving the results");
	            }
            }
    	}
    	
    	start=arg.indexOf("close_all");
    	if(start!=-1){
     		saveOrClose = true;
     		closeAllWindows();
     	}
    	 
    	 return saveOrClose;
    }
}
