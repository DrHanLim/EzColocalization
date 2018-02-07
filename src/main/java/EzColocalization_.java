import ezcol.main.AnalysisOperator;
import ezcol.main.GUI;
import ezcol.main.MacroHandler;
import ezcol.main.PluginStatic;
import ij.IJ;
import ij.Macro;
import ij.plugin.PlugIn;

/**
 * This class is put in the default package so that
 * ImageJ could automatically load the plugin
 * Some of 
 * @author Huanjie Sheng
 *
 */
public class EzColocalization_ implements PlugIn{

	public void run(String arg) {
		//pass the class name to the macrohandler in case of recording
		PluginStatic.setPlugInName(getClass().getSimpleName());
		if (Macro.getOptions()==null){
        	new GUI();
        }
        else{
        	//MyDebug.printSelectedOptions(AnalysisOperator.getOptions());
        	if(!MacroHandler.macroSaveAndClose(Macro.getOptions())){
	        	MacroHandler.macroInterpreter(Macro.getOptions());
	        	new AnalysisOperator().execute(true);
        	}
        	
        }
	}

}
    





