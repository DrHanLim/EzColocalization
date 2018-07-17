package ezcol.main;

/**
 * This interface contains all static final options It is not recommended to
 * implement this interface It is used to group all options together
 * 
 * @author Huanjie Sheng
 *
 */
public interface PluginConstants {

	public static final int DO_NOTHING = 0;
	// The maximum index is 0x80000000;
	// All static final int must be different if it starts with DO_

	public static final int DO_MATRIX = 8;

	public static final int DO_PCC = 16;
	public static final int DO_SRC = 32;
	public static final int DO_MCC = 64;
	public static final int DO_ICQ = 128;
	// calculate the distance between center to the nearest pole
	public static final int DO_AVGINT = 256;

	public static final int DO_ALIGN = 16384;

	public static final int DO_SCATTER = 0x40000000;

	public static final int DO_HEAT = 0x80000;

	// heatmap options
	public static final int DO_HEAT_CELL  = 0x10000;
	public static final int DO_HEAT_IMG   = 0x20000;
	public static final int DO_HEAT_STACK = 0x40000;

	// TOS options
	public static final int DO_LINEAR_TOS = 0x200000;
	public static final int DO_LOG2_TOS   = 0x400000;
	// This has been removed from the UI
	public static final int DO_LN_TOS = 0x800000;
	// output metrics
	public static final int DO_RESULTTABLE = 0x1000000;

	public static final int DO_SUMMARY = 0x2000000;
	public static final int DO_HIST    = 0x10000000;
	public static final int DO_CUSTOM  = 0x20000000;
	// output options
	public static final int DO_MASKS = 4096;
	public static final int DO_ROIS  = 8192;

	// Here are the combinations of required outputs
	// They don't have to be different
	// The combination determines whether specific class needs to be run

	public static final int DO_TOS = DO_LINEAR_TOS | DO_LOG2_TOS;

	public static final int RUN_ALIGN = DO_ALIGN;
	public static final int RUN_HIST = DO_HIST;
	public static final int RUN_SCATTER = DO_SCATTER;
	public static final int RUN_HEAT = DO_HEAT;
	public static final int RUN_MATRIX = DO_MATRIX;
	public static final int RUN_TOS = DO_TOS;
	public static final int RUN_METRICS = DO_TOS | DO_PCC | DO_SRC | DO_AVGINT | DO_MCC | DO_ICQ | DO_MCC | DO_CUSTOM;
	public static final int RUN_CDP = RUN_MATRIX | RUN_METRICS | RUN_SCATTER;
	public static final int RUN_IDCELLS = RUN_CDP | DO_HEAT_CELL | DO_MASKS | DO_ROIS;
	public static final int RUN_SUMMARY = DO_SUMMARY;
	public static final int RUN_RTS = DO_RESULTTABLE | DO_HIST | RUN_SUMMARY;

	public static final int OPTS_HEAT = DO_HEAT_CELL | DO_HEAT_IMG | DO_HEAT_STACK;
	public static final int OPTS_TOS = DO_LINEAR_TOS | DO_LOG2_TOS | DO_LN_TOS;
	public static final int OPTS_OUTPUT = DO_RESULTTABLE | DO_SUMMARY | DO_HIST | DO_CUSTOM;

	public static final int RQD_REPORTER = RUN_HEAT | DO_AVGINT | DO_CUSTOM | RUN_HIST | RUN_SUMMARY;
	public static final int RQD_ALL_REPORTERS = RUN_MATRIX | RUN_SCATTER | DO_TOS | DO_PCC | DO_SRC | DO_MCC | DO_ICQ;
	public static final int RQD_CELLID = RUN_ALIGN | DO_HEAT_CELL | DO_MASKS | DO_ROIS;
	
	// Parameters
	public static final int DEFAULT_MIN_FT = 1;
	public static final int DEFAULT_MAX_FT = 99;
	public static final double DEFAULT_ROLLINGBALL_SIZE = 50.0;
	public static final double DEFAULT_MIN = 0.0;
	public static final double DEFAULT_MAX = Double.POSITIVE_INFINITY;
	public static final int DEFAULT_FT = 10;
	public static final int DEFAULT_CHOICE = 0;
	public static final boolean DEFAULT_BOOLEAN = false;
	
	// Masks to avoid interference
	public static final int MASK_ALIGNMENT  = RUN_ALIGN;
	public static final int MASK_VISUAL     = RUN_HEAT | OPTS_HEAT | RUN_SCATTER | RUN_MATRIX;

	public static final ImageInfo NOIMAGE = new ImageInfo(ImageInfo.NONE, ImageInfo.NONE_ID, 24);
	public static final ImageInfo ROIMANAGER_IMAGE = new ImageInfo(ImageInfo.ROI_MANAGER, ImageInfo.ROI_MANAGER_ID, 24);
}
