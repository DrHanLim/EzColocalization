package ezcol.main;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.SwingWorker;

import ezcol.align.BackgroundProcessor;
import ezcol.align.ImageAligner;
import ezcol.cell.CellData;
import ezcol.cell.CellDataProcessor;
import ezcol.cell.CellFinder;
import ezcol.debug.Debugger;
import ezcol.debug.ExceptionHandler;
import ezcol.metric.BasicCalculator;
import ezcol.metric.CostesThreshold;
import ezcol.metric.MatrixCalculator3D;
import ezcol.metric.MatrixCalculator;
import ezcol.metric.MetricCalculator;
import ezcol.metric.StringCompiler;
import ezcol.visual.visual2D.HeatChart;
import ezcol.visual.visual2D.HeatChartStackWindow;
import ezcol.visual.visual2D.HeatGenerator;
import ezcol.visual.visual2D.HistogramGenerator;
import ezcol.visual.visual2D.OutputWindow;
import ezcol.visual.visual2D.ProgressGlassPane;
import ezcol.visual.visual2D.ScatterPlotGenerator;
import ezcol.visual.visual3D.ScatterPlot3DWindow;
import ezcol.visual.visual3D.Spot3D;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class AnalysisOperator extends PluginStatic {

	private ProgressGlassPane pg;
	private GUI gui;
	private SwingWorker<Void, Integer> swVI;
	// The first image which is not null
	private int markedIMG;

	// compiler is the only class needs to be declared so that it will only
	// compile once
	private StringCompiler customCompiler;
	public static final int SAMPLE_SIZE = 1;
	// private RoiManager roiCells;
	private int width, height;
	private int frames = 1, curFrame = 1;

	private Calibration cal;
	private double[][] scalar = newArray(new double[] { Double.POSITIVE_INFINITY, 0.0 }, MAX_NREPORTERS);

	// Outputs
	private ResultsTable outputRT;
	private ResultsTable[] outputRTArray, mTOSRTArray;
	private Object[] saveRois;
	private ImageProcessor[] outMaskArray;
	private int[] numOfRois;
	private double[][] mMetricValues;
	private CellData[][] cellData;
	private CellData[][][] cellDataCs;

	private ImageStack[] alignedStacks;
	private ImagePlus[] alignedImps;
	private ImageStack[] heatmapStack;
	private ImagePlus[] heatmapImp;

	// This initiator is used for GUI
	public AnalysisOperator(GUI gui) {
		resetOutArray();
		options = getOptions();
		this.gui = gui;
		pg = gui.getProgressGlassPane();
	}

	// This initiator is used for Macro
	public AnalysisOperator() {
		resetOutArray();
		checkParams();
		retrieveOptions();
		prepAll();
		pg = null;
	}

	public void setOptions(int option) {
		options = option;
		resetOutArray();
	}

	public void execute(boolean doStack) {

		if (!prepStack(doStack))
			return;
		ImagePlus tempCurrentImg = WindowManager.getCurrentImage();
		WindowManager.setTempCurrentImage(null);

		try {
			if (curFrame == frames) {
				applyToStack(curFrame);
				finishStack();
			} else if (gui != null) {
				swVI = new SwingWorker<Void, Integer>() {

					@Override
					protected Void doInBackground() {
						pg.setVisible(true);
						for (int iFrame = curFrame; iFrame <= frames; iFrame++) {
							if (isCancelled())
								break;
							// Work on each element
							try {
								applyToStack(iFrame);
							} catch (Exception e) {
								e.printStackTrace();
								ExceptionHandler.handleException(e);
							}
							// Store them in the 'chunks' list
							publish(iFrame);
						}
						return null;
					}

					@Override
					protected void process(List<Integer> chunks) {
						for (int iFrame : chunks) {
							// Get the numbers in the 'chunks' list
							// To use wherever/however you want
							if (isCancelled())
								break;

							pg.setProgress(iFrame * 100 / (frames - curFrame + 1));
							// No delay should be put here otherwise
							// the progress wouldn't show up

						}
					}

					@Override
					protected void done() {
						// Something to do when everything is done
						// We have to wait here until the analysis is done
						try {
							if (!isCancelled())
								finishStack();
						} catch (Exception e) {
							e.printStackTrace();
							ExceptionHandler.handleException(e);
						}
						pg.setVisible(false);
						pg.setValue(0);
					}

				};
				swVI.execute();
			} else {
				for (int iFrame = curFrame; iFrame <= frames; iFrame++)
					applyToStack(iFrame);
				finishStack();
			}
		} catch (Exception e) {
			// System.out.println("thrown");
			e.printStackTrace();
			/*
			 * Debugger.log("************Error**************");
			 * Debugger.log(e.toString()); for (StackTraceElement ste :
			 * e.getStackTrace()) Debugger.log(ste + "");
			 * Debugger.log("************Error**************");
			 */
			ExceptionHandler.handleException(e);
		}

		// DO NOT wait outside the other thread. This will block the display of
		// glasspane
		// while(!swVI.isDone());
		WindowManager.setTempCurrentImage(tempCurrentImg);

	}

	public void cancel() {
		if (swVI != null)
			swVI.cancel(true);
	}

	public boolean prepStack(boolean doStack) {
		// input here
		frames = checkStack();
		if (frames == -1)
			return false;

		if (doStack) {
			curFrame = 1;
			if (saveRois != null)
				saveRois = new Object[frames];
			if (numOfRois != null)
				numOfRois = new int[frames];
			if (outputRTArray != null)
				outputRTArray = new ResultsTable[frames];
			if (mTOSRTArray != null)
				mTOSRTArray = new ResultsTable[frames];
			if (heatmapStack != null)
				for (int iHeat = 0; iHeat < heatmapStack.length; iHeat++)
					if (heatmapStack[iHeat] != null)
						heatmapStack[iHeat] = imps[iHeat].getStack().duplicate();
			if (outMaskArray != null)
				outMaskArray = new ImageProcessor[frames];

			if (cellData != null)
				cellData = new CellData[frames * SAMPLE_SIZE][nReporters];

			// Error
			if (cellDataCs != null)
				cellDataCs = new CellData[nReporters][frames][];

		} else {
			frames = markedIMG >= 0 ? imps[markedIMG].getCurrentSlice() : 1;
			for (int i = 0; i < imps.length - 1; i++) {
				if (imps[i] != null) {
					imps[i].setSlice(frames);
					imps[i].updateAndDraw();
				}
			}
			curFrame = frames;
		}
		return true;
	}

	private void applyToStack(int iFrame) {

		ImageProcessor tempMask = null;
		ImageProcessor ip = imps[imps.length - 1] == null ? null
				: imps[imps.length - 1].getStack().getProcessor(iFrame).duplicate();
		ImageProcessor[] oldips = new ImageProcessor[imps.length - 1];
		for (int i = 0; i < oldips.length; i++)
			oldips[i] = imps[i] == null ? null : imps[i].getStack().getProcessor(iFrame).duplicate();

		for (int i = 0; i < oldips.length; i++)
			if (oldips[i] != null)
				oldips[i].resetMinAndMax();

		ImageProcessor[] newips = new ImageProcessor[oldips.length];
		for (int i = 0; i < newips.length; i++)
			newips[i] = oldips[i] == null ? null : oldips[i].duplicate();

		int numOfCell = 0;
		// all data are calibrated
		// Error here!!!
		CellData[][] cellCs = new CellData[nReporters][];

		// alignment module
		if (didAlignment) {
		} else if ((options & RUN_ALIGN) != 0 && alignedStacks != null) {
			// always subtract background before alignment
			// (the last input argument of runAlignment is set to be true)
			ImageAligner doAlign = new ImageAligner();
			for (int iAlign = 0; iAlign < align_chckes.length; iAlign++) {

				if (align_chckes[iAlign]) {
					newips[iAlign] = doAlign.runAlignment(ip, oldips[iAlign], "Translation",
							new String[] { ALLTHOLDS[alignThold_combs[alignThold_combs.length - 1]],
									ALLTHOLDS[alignThold_combs[iAlign]] },
							new boolean[] { true, false }, true);
					if (alignedStacks[iAlign] != null)
						alignedStacks[iAlign].setProcessor(newips[iAlign].duplicate(), iFrame);
				}
			}
		}

		// Identify cells using Phase Contrast Image
		ResultsTable tempCellTable = new ResultsTable();
		RoiManager roiCells = null;
		ImagePlus countCells = null;
		if ((options & RUN_IDCELLS) != 0 && ip != null) {
			BackgroundProcessor impBackground = new BackgroundProcessor(ip,
					ALLTHOLDS[alignThold_combs[alignThold_combs.length - 1]]);

			if (!ip.isBinary())
				impBackground.rollSubBackground(ip, PHASE_ROLINGBALL_SIZE, impBackground.detectBackground(ip));

			// IDcell module
			// because we have calculated the background so ipThred should have
			// been
			// generated, in case that ipThred is null, do it again.
			ByteProcessor ipMask;
			if (ip.isBinary())
				ipMask = (ByteProcessor) ip;
			else {
				ipMask = impBackground.getThredMask();
				if (ipMask == null) {
					ipMask = impBackground.thredImp(BackgroundProcessor.DEFAULT_LIGHTBACKGROUND);
					ipMask.invert();
				}
			}
			impBackground = null;

			CellFinder impCells = new CellFinder();
			// ipMask is the mask not phase image
			// impCells.setMask(ipMask);
			impCells.setMask(ipMask, cal);
			ImageProcessor[] copyNewIps = new ImageProcessor[newips.length];
			for (int i = 0; i < copyNewIps.length; i++)
				copyNewIps[i] = newips[i] == null ? null : newips[i].duplicate();
			doIDCells(impCells, roiCells, tempCellTable, copyNewIps);

			countCells = impCells.getOutputImg(true);
			if (countCells == null)
				ExceptionHandler.addError(Thread.currentThread(), "Error in cell identification on slice " + iFrame);

			countCells.getProcessor().resetMinAndMax();
			roiCells = impCells.getOutputManager(true);
			if (roiCells == null)
				ExceptionHandler.addError(Thread.currentThread(), "Error in cell identification on slice " + iFrame);

			if (roiCells.getCount() > 0) {
				// noCell=false;
				GUI.roiCells = roiCells;
			}
			tempCellTable = impCells.getOutputTable(true);
		}

		// heatmap module
		if ((options & RUN_HEAT) != 0) {
			RoiManager tempRoi = null;

			double[][] tempScalar = new double[AnalysisOperator.this.scalar.length][2];

			switch (options & OPTS_HEAT) {
			case DO_HEAT_CELL:
				tempRoi = roiCells;
				break;
			case DO_HEAT_IMG:
				for (int i = 0; i < tempScalar.length; i++) {
					tempScalar[i][0] = (oldips[i] == null ? DEFAULT_MIN : oldips[i].getMin());
					tempScalar[i][1] = (oldips[i] == null ? DEFAULT_MAX : oldips[i].getMax());
				}
				break;
			case DO_HEAT_STACK:
				for (int i = 0; i < tempScalar.length; i++) {
					tempScalar[i][0] = AnalysisOperator.this.scalar[i][0];
					tempScalar[i][1] = AnalysisOperator.this.scalar[i][1];
				}
				break;
			default:
				tempScalar = null;
				break;
			}
			// heatmap module
			HeatGenerator myHeat = new HeatGenerator();
			for (int iHeat = 0; iHeat < heatmapStack.length; iHeat++)
				if (heatmapStack != null && heatmapStack[iHeat] != null) {
					heatmapStack[iHeat].setProcessor(newips[iHeat].duplicate(), iFrame - curFrame + 1);
					myHeat.heatmap(heatmapStack[iHeat].getProcessor(iFrame - curFrame + 1), tempRoi, tempScalar[iHeat]);
				}

		}

		// convert the results from impCells to cellData
		// always coexist with impCells
		if ((options & RUN_CDP) != 0) {
			CellDataProcessor cdp = new CellDataProcessor();
			cdp.setLabel("image " + iFrame);
			cdp.setCalibration(cal);
			// The Calibration table of each channel can be different
			for (int i = 0; i < newips.length; i++)
				if (newips[i] != null && imps[i] != null)
					newips[i].setCalibrationTable(imps[i].getStack().getProcessor(iFrame).getCalibrationTable());

			if (!cdp.mask2data(countCells == null ? null : countCells.getProcessor(), newips, true))
				ExceptionHandler.addError(Thread.currentThread(), "Error in cell data processor on slice " + iFrame);

			numOfCell = cdp.getNumOfCell();
			for (int iC = 0; iC < cellCs.length; iC++) {
				cellCs[iC] = cdp.getCellData(iC);
				if (cellDataCs != null) {
					cellDataCs[iC][iFrame - curFrame] = cellCs[iC];
				}
			}

			while (tempCellTable.getCounter() < numOfCell)
				tempCellTable.incrementCounter();
		}

		if ((options & RUN_SCATTER) != 0) {

			if (numOfCell <= SAMPLE_SIZE) {
				for (int iChannel = 0; iChannel < nReporters; iChannel++) {
					for (int i = 0; i < numOfCell; i++) {
						cellData[i + (iFrame - curFrame) * SAMPLE_SIZE][iChannel] = cellCs[iChannel][i];
						if (numOfCell > 1)
							cellData[i + (iFrame - curFrame) * SAMPLE_SIZE][iChannel]
									.setLabel("image " + iFrame + ": cell " + i);
						else
							cellData[i + (iFrame - curFrame) * SAMPLE_SIZE][iChannel].setLabel("image " + iFrame);
					}
					for (int i = numOfCell; i < SAMPLE_SIZE; i++) {
						cellData[i + (iFrame - curFrame) * SAMPLE_SIZE][iChannel] = null;
					}
				}
			} else {
				int[] idx = shuffleIdx(numOfCell);
				for (int iChannel = 0; iChannel < nReporters; iChannel++) {
					for (int i = 0; i < SAMPLE_SIZE; i++) {
						cellData[i + (iFrame - curFrame) * SAMPLE_SIZE][iChannel] = cellCs[iChannel][idx[i]];
						cellData[i + (iFrame - curFrame) * SAMPLE_SIZE][iChannel]
								.setLabel("image " + iFrame + ": cell " + (idx[i] + 1));
					}
				}
			}
		}

		// calculate metrics module
		MetricCalculator callMetric = null;
		if ((options & RUN_METRICS) != 0) {
			callMetric = new MetricCalculator(options, customCompiler);
			if (!callMetric.calMetrics(cellCs, allTholds))
				ExceptionHandler.addError(Thread.currentThread(), "Error in calculating metrics on slice " + iFrame);
		}

		MatrixCalculator callmTOS = null;
		if ((options & RUN_MATRIX) != 0 && nReporters == 2) {
			callmTOS = new MatrixCalculator(options, matrixFT_spin);
			if (!callmTOS.calMetrics(cellCs[0], cellCs[1], matrixMetric_comb))
				ExceptionHandler.addError(Thread.currentThread(), "Error in calculating mTOS on slice " + iFrame);
			if (mTOSRTArray != null) {
				mTOSRTArray[iFrame - curFrame] = callmTOS.getResultsTable();
			}
		}

		/*
		 * D3TOSCalculator callD3TOS = null; if ((options & RUN_TOS) != 0 &&
		 * nReporters == 3) { callD3TOS = new D3TOSCalculator(options); if
		 * (!callD3TOS.calMetrics(cellCs, tosTholds))
		 * Debugger.addError(Thread.currentThread(),
		 * "Error in calculating 3D TOS on slice " + iFrame); }
		 */

		// calculate distances module
		/*
		 * DistancesCalculator callDist = null; if((options&RUN_DIST)!=0){
		 * if((options&DO_DIST_FT)!=0) callDist = new
		 * DistancesCalculator(options,numOfDistFTs); else
		 * if((options&DO_DIST_THOLD)!=0){ String[] algorithms = new
		 * String[whichDistTholds.length]; for(int i=0;i<algorithms.length;i++){
		 * algorithms[i]=ALLDISTTHOLDS[whichDistTholds[i]]; } callDist = new
		 * DistancesCalculator(options,algorithms); } else{ callDist = new
		 * DistancesCalculator(); Debugger.addError(Thread.currentThread(),
		 * "Input options of Distances cannot be found on slice "+iFrame); }
		 * if(!callDist.calMetrics(cellC1,cellC2,tempCellTable))
		 * Debugger.addError(Thread.currentThread(),
		 * "Error in calculating distances on slice "+iFrame); }
		 */

		// tempCellTable.show("tempCellTable");;
		if (outputRTArray != null)
			outputRTArray[iFrame - curFrame] = printResults(tempCellTable, callMetric, iFrame);

		// tempCellTable.show("RT-"+iFrame);
		// get cell mask
		if (outMaskArray != null && roiCells != null) {
			tempMask = roi2mask(ip, roiCells);
			// update output mask
			if (tempMask != null)
				outMaskArray[iFrame - curFrame] = tempMask;
		}

		// roi reset and save
		if (saveRois != null && numOfRois != null && roiCells != null) {
			saveRois[iFrame - curFrame] = (Object) roiCells.getRoisAsArray();
			numOfRois[iFrame - curFrame] = roiCells.getCount();
		}
		// IJ.showProgress(iFrame,frames);
	}

	private void finishStack() {
		cleanWindow();

		if ((options & RUN_ALIGN) != 0 && alignedStacks != null) {
			Calibration cal, globalcal;
			for (int ipic = 0; ipic < align_chckes.length; ipic++) {
				if (align_chckes[ipic] && imps[ipic] != null) {
					ImagePlus tempImg = (ImagePlus) imps[ipic].clone();
					alignedImps[ipic] = new ImagePlus("Aligned " + imps[ipic].getTitle(), alignedStacks[ipic]);
					// imgIO=false;
					cal = imps[ipic].getCalibration();
					globalcal = imps[ipic].getGlobalCalibration();
					imps[ipic].setImage(alignedImps[ipic]);
					imps[ipic].setCalibration(cal);
					imps[ipic].setGlobalCalibration(globalcal);
					imps[ipic].setTitle(alignedImps[ipic].getTitle());
					imps[ipic].updateAndDraw();
					if (Macro.getOptions() == null)
						gui.updateImgList(imps[ipic], tempImg);
					// imgIO=true;
					// noCell=false;
				} else
					alignedImps[ipic] = null;
			}
		}

		// ExceptionReporter.addError(Thread.currentThread(),"test");
		// after stack output of results
		/*
		 * if(noCell) { ExceptionReporter.addError(pluginName+" error",
		 * "No cell or region is selected"); return; } else noCell=true;
		 */

		if (saveRois != null && numOfRois != null) {
			stackAllRois(new ImagePlus("template", new ByteProcessor(width, height)), true);
			addWindow(roiCells);
		}

		if (outMaskArray != null) {
			ImageStack outMaskStack = new ImageStack(width, height, outMaskArray.length);
			for (int iOut = 1; iOut <= outMaskArray.length; iOut++) {
				outMaskStack.setSliceLabel("Mask-Shot" + iOut, iOut);
				outMaskStack.setProcessor(outMaskArray[iOut - 1], iOut);

			}
			String windowTitle = "Mask(s) of " + (markedIMG >= 0 ? imps[markedIMG].getTitle() : "Your Images(s)");
			int index = 2;
			while (WindowManager.getImage(windowTitle) != null)
				windowTitle = windowTitle + "-" + (index++);

			ImagePlus outMaskImp = new ImagePlus(windowTitle, outMaskStack);
			outMaskImp.show();
			addWindow(outMaskImp);
		}

		if (outputRTArray != null) {
			outputRT = appendResultsTables(outputRTArray);
			if (outputRT.getCounter() > 0) {
				if ((options & DO_RESULTTABLE) != 0) {
					String windowTitle = "Metric(s) of "
							+ (markedIMG >= 0 ? imps[markedIMG].getTitle() : "selected calculations");
					int index = 2;
					while (WindowManager.getImage(windowTitle) != null)
						windowTitle = windowTitle + "-" + (index++);
					outputRT.show(windowTitle);
					// I'm very suprised that ResultsTable is not extended from
					// Frame or Winodw
					// It doesn't even have a field of window
					addWindow(WindowManager.getFrame(windowTitle));
				}
			} else
				ExceptionHandler.addError(Thread.currentThread(), "No result to be printed");
		}

		// mTOS module print raw values of TOS and mTOS heat map
		if ((options & RUN_MATRIX) != 0) {

			if (nReporters == 2) {
				String mTitle = MatrixCalculator.getNames(matrixMetric_comb);
				ResultsTable mTOSrawRT = appendResultsTables(mTOSRTArray);
				// MatrixCalculator callmTOS=new MatrixCalculator(numOfFTs);
				if (mTOSrawRT != null) {
					String imgTitle = (markedIMG >= 0 ? imps[markedIMG].getTitle() : "selected calculations");
					String windowTitle = mTitle + " of " + imgTitle;
					String tempTitle = windowTitle;
					int index = 2;
					while (WindowManager.getWindow(tempTitle) != null)
						tempTitle = windowTitle + "-" + (index++);
					windowTitle = tempTitle;
					tempTitle = null;
					
					MatrixCalculator callmTOS = new MatrixCalculator(new int[] { DEFAULT_FT, DEFAULT_FT });
					int matrixLength = BasicCalculator.ft2length(DEFAULT_FT);
					mMetricValues = new double[matrixLength][matrixLength];
					HeatChart heatChart = callmTOS.getHeatChart(mMetricValues, windowTitle);
					heatChart.setRawData(append2D(cellDataCs[0], CellData.class),
							append2D(cellDataCs[1], CellData.class));
					heatChart.setFixChart(true);
					heatChart.setTitle(imgTitle);
					// heatChart.setRawResultsTable(mTOSrawRT);
					heatChart.setOptions(options);
					heatChart.setCalculator(matrixMetric_comb);
					heatChart.setNumOfFTs(matrixFT_spin);
					heatChart.setStatsMethod(matrixStats_comb);
					//
					// heatChart.resetChange();
					HeatChartStackWindow hcs = new HeatChartStackWindow(heatChart);
					heatChart.updateImage(true);
					hcs.updateImage();
					hcs.setOrgTitle(imgTitle);
					hcs.getImagePlus().setTitle(windowTitle);

					addWindow(hcs);

				}
			} else {

				MatrixCalculator3D callD3Matrix = new MatrixCalculator3D(options, matrixFT_spin);
				if (!callD3Matrix.calMetrics(append3D(cellDataCs), matrixMetric_comb)) {
					ExceptionHandler.addError(Thread.currentThread(), "D3 MATRIX ERROR");
				}

				callD3Matrix.setStatsMethod(matrixStats_comb);
				ImageWindow sdw = callD3Matrix.getD3Heatmap();
				addWindow(sdw);

			}
		}
		// end of callmTOS

		if ((options & RUN_SCATTER) != 0 && cellData != null) {

			float[][] xData = new float[cellData.length][];
			float[][] yData = new float[cellData.length][];
			float[][] zData = new float[cellData.length][];
			String[] sliceLabels = new String[cellData.length];
			int count = 0;
			ImageWindow spw = null;

			if (nReporters == 2) {

				CostesThreshold costesTholder = new CostesThreshold();
				for (int i = 0; i < cellData.length; i++) {
					if (cellData[i][0] == null || cellData[i][1] == null) {
						continue;
					}
					xData[count] = cellData[i][0].getData();
					yData[count] = cellData[i][1].getData();
					sliceLabels[count] = cellData[i][0].getLabel() != null ? cellData[i][0].getLabel()
							: cellData[i][1].getLabel();
					count++;
				}
				xData = Arrays.copyOfRange(xData, 0, count);
				yData = Arrays.copyOfRange(yData, 0, count);

				String windowTitle = "Scatterplots of random cells"
						+ (markedIMG >= 0 ? (" in " + imps[markedIMG].getTitle()) : "");
				int index = 2;
				while (WindowManager.getImage(windowTitle) != null)
					windowTitle = windowTitle + "-" + (index++);

				ScatterPlotGenerator spg = new ScatterPlotGenerator(windowTitle, "Channel 1", "Channel 2", xData, yData,
						sliceLabels);
				
				//spg.addCostes();
				spw = spg.show();

			} else {
				for (int i = 0; i < cellData.length; i++) {
					if (cellData[i][0] == null || cellData[i][1] == null) {
						continue;
					}
					xData[count] = cellData[i][0].getData();
					yData[count] = cellData[i][1].getData();
					zData[count] = cellData[i][2].getData();
					sliceLabels[count] = cellData[i][0].getLabel() != null ? cellData[i][0].getLabel()
							: cellData[i][1].getLabel();
					count++;
				}
				xData = Arrays.copyOfRange(xData, 0, count);
				yData = Arrays.copyOfRange(yData, 0, count);
				zData = Arrays.copyOfRange(zData, 0, count);
				String windowTitle = "Scatterplots of random cells"
						+ (markedIMG >= 0 ? (" in " + imps[markedIMG].getTitle()) : "");
				int index = 2;
				while (WindowManager.getImage(windowTitle) != null)
					windowTitle = windowTitle + "-" + (index++);

				spw = new ScatterPlot3DWindow(windowTitle, "Channel 1", "Channel 2", "Channel 3", xData, yData, zData,
						Spot3D.CIRCLE + Spot3D.ALL_SHAPES + 1, sliceLabels);
				((ScatterPlot3DWindow) spw).draw();
			}

			addWindow(spw);
		}

		if ((options & RUN_HEAT) != 0) {
			if (!IJ.isMacro() && gui != null)
				gui.setImageListener(false, null);
			HeatGenerator myHeat = new HeatGenerator();
			for (int ipic = 0; ipic < heatmapImp.length; ipic++) {
				if (heatmapStack[ipic] != null) {
					heatmapImp[ipic] = new ImagePlus("Heatmap(s) of " + imps[ipic].getTitle(), heatmapStack[ipic]);
					// do not convert to RGB and display the image
					// do not add the heatmap to the selection list
					// gui.imgIO = false;

					ImagePlus heatImp = myHeat.applyHeatMap(heatmapImp[ipic], HEATMAPS[heatmapColor_combs[ipic]], false,
							true);

					String windowTitle = heatImp.getTitle();
					int index = 2;
					while (WindowManager.getImage(windowTitle) != null)
						windowTitle = windowTitle + "-" + (index++);
					heatImp.setTitle(windowTitle);

					addWindow(heatImp);
					// gui.imgIO = true;
					// imgIO=true;
				}
			}
			if (!IJ.isMacro() && gui != null)
				gui.setImageListener(true, null);
		}
		// heat is not useful anymore

		// histogram module
		if ((options & RUN_HIST) != 0) {

			if (outputRT != null) {
				HistogramGenerator histogramAll = new HistogramGenerator();

				int numColumns = outputRT.getLastColumn();
				String[] columnHeadings = outputRT.getHeadings();
				List<String> defaultHeadings = new ArrayList<String>();
				for (int i = 0; i < ResultsTable.LAST_HEADING; i++)
					defaultHeadings.add(ResultsTable.getDefaultHeading(i));

				for (int iMetric = 0; iMetric < columnHeadings.length; iMetric++) {
					if (defaultHeadings.contains(columnHeadings[iMetric]))
						continue;
					int tempIdx = outputRT.getColumnIndex(columnHeadings[iMetric]);
					if (tempIdx >= 0 && tempIdx <= numColumns)
						histogramAll.addToHistogramStack(columnHeadings[iMetric], outputRT.getColumnAsDoubles(tempIdx));
				}

				/*
				 * int tempIdx=outputRT.getColumnIndex("Custom");
				 * if(tempIdx>=0&&tempIdx<=numColumns)
				 * tempResult=outputRT.getColumnAsDoubles(tempIdx); else
				 * tempResult=null; if(tempResult!=null)
				 * histogramAll.addToHistogramStack("Custom", tempResult);
				 */
				String windowTitle = "Histogram of metrics"
						+ (markedIMG >= 0 ? (" of " + imps[markedIMG].getTitle()) : "");
				int index = 2;
				while (WindowManager.getImage(windowTitle) != null)
					windowTitle = windowTitle + "-" + (index++);

				if (histogramAll.showHistogramStack(windowTitle)) {
					addWindow(histogramAll.getWindow());
				} else
					ExceptionHandler.addError(Thread.currentThread(), "Histogram Stack is empty");
			} else {
				ExceptionHandler.addError(Thread.currentThread(), "Histogram ResultsTable is empty");
			}
			// histogramAll = null;
		}

		// @Warning Please change here
		// This must be run at last to collect all errors and warnings
		if ((options & RUN_OUTPUT) != 0) {
			OutputWindow summary = new OutputWindow();
			summary.setOption(options);
			for (int i = 0; i < nReporters; i++)
				summary.addImage(imgLabels[i], imps[i], align_chckes[i]);
			summary.addImage(imgLabels[imgLabels.length - 1], imps[imps.length - 1], null);

			if (outputRT != null) {
				int columnIdx = ResultsTable.COLUMN_NOT_FOUND;
				// create an array here including all subclasses extended from
				// BasicCalculator
				// DistancesCalculator used to be here
				for (int index = 0; index < BasicCalculator.getNum(); index++) {
					columnIdx = outputRT.getColumnIndex(BasicCalculator.getNames(index));
					if (columnIdx != ResultsTable.COLUMN_NOT_FOUND)
						summary.addMetric(outputRT.getColumnAsDoubles(columnIdx), BasicCalculator.getIntpn(index));

					for (int iReporter = 1; iReporter <= nReporters; iReporter++) {
						columnIdx = outputRT.getColumnIndex(BasicCalculator.getNames(index) + iReporter);
						if (columnIdx != ResultsTable.COLUMN_NOT_FOUND)
							summary.addMetric(outputRT.getColumnAsDoubles(columnIdx), BasicCalculator.getIntpn(index, iReporter));
					}
				}
			} else {
				ExceptionHandler.addError(this, Thread.currentThread(), "Output ResultsTable was not initiated");
			}
			summary.showLogWindow();

		} else if (ExceptionHandler.getCounter() > 0) {
			ExceptionHandler.print2log();
		}
		addWindow(WindowManager.getFrame("Log"));

	}

	public ResultsTable printResults(ResultsTable tempResultTable, MetricCalculator callMetric, int iFrame) {// combine
																												// Average
		// Intensity, and Custom
		ResultsTable outputRT = new ResultsTable();

		if (callMetric == null)
			return outputRT;
		if (tempResultTable != null) {
			double[] tempResult;
			for (int iCell = 0; iCell < tempResultTable.getCounter(); iCell++) {
				outputRT.incrementCounter();
				outputRT.addLabel("image " + (iFrame) + ": cell " + (iCell + 1));

				if (callMetric != null) {
					for (int iMetric = 0; iMetric < MetricCalculator.getNum(); iMetric++) {
						tempResult = callMetric.getMetrics(iMetric, iCell);
						if (tempResult != null) {
							if (tempResult.length == 1)
								outputRT.addValue(MetricCalculator.getNames(iMetric), tempResult[0]);
							else
								for (int iChannel = 0; iChannel < tempResult.length; iChannel++)
									outputRT.addValue(MetricCalculator.getNames(iMetric) + (iChannel + 1),
											tempResult[iChannel]);
						}

					}
				}

				// add the measurements of the cell
				for (int iColumn = 0; iColumn <= tempResultTable.getLastColumn(); iColumn++) {
					String head = tempResultTable.getColumnHeading(iColumn);
					if (head != null)
						outputRT.addValue(head, tempResultTable.getValueAsDouble(iColumn, iCell));
				}
			}
		}
		return outputRT;
	}

	private ResultsTable appendResultsTables(ResultsTable[] rts) {
		if (rts == null)
			return null;
		ResultsTable outputRT = new ResultsTable();
		for (int iFrame = 0; iFrame < rts.length; iFrame++) {
			if (rts[iFrame] == null)
				continue;
			String[] headings = rts[iFrame].getHeadings();
			int[] headIdxes = new int[headings.length];
			for (int i = 0; i < headings.length; i++)
				headIdxes[i] = rts[iFrame].getColumnIndex(headings[i]);

			for (int iCell = 0; iCell < rts[iFrame].getCounter(); iCell++) {
				outputRT.incrementCounter();
				outputRT.addLabel(rts[iFrame].getLabel(iCell));
				for (int iColumn = 0; iColumn < headIdxes.length; iColumn++)
					if (rts[iFrame].columnExists(headIdxes[iColumn]))
						outputRT.addValue(headings[iColumn], rts[iFrame].getValueAsDouble(headIdxes[iColumn], iCell));
			}
		}
		return outputRT;
	}

	private ByteProcessor roi2mask(ImageProcessor ip, RoiManager roiManager) {
		if (roiManager == null || ip == null)
			return null;
		Roi[] rois = roiManager.getRoisAsArray();

		return roi2mask(ip, rois);
	}

	private void stackAllRois(ImagePlus template, boolean showManager) {
		if (showManager) {
			roiCells = RoiManager.getInstance();
			if (roiCells == null)
				roiCells = new RoiManager();

		}
		for (int iFrame = curFrame; iFrame <= frames; iFrame++) {
			Roi[] printRoi = (Roi[]) saveRois[iFrame - curFrame];
			for (int iRoi = 0; iRoi < numOfRois[iFrame - curFrame]; iRoi++) {
				// This is necessary because roiCells.add will automatically add
				// a sufix number
				printRoi[iRoi].setName("image " + iFrame + ": cell");
				printRoi[iRoi].setPosition(iFrame);
				roiCells.add(template, printRoi[iRoi], (iRoi + 1));
				printRoi[iRoi].setName("image " + iFrame + ": cell " + (iRoi + 1));
			}
		}
		template = null;

	}

	public void prepAlignment() {
		// This is done in GUIframe at overrided retrieveOptions();
		/*
		 * for (int ipic=0;ipic<imps.length;ipic++) { if(imps[ipic]!=null){
		 * oldImps[ipic]=imps[ipic].duplicate();
		 * oldImps[ipic].setTitle(imps[ipic].getTitle()); } else
		 * oldImps[ipic]=null; }
		 */
		alignedStacks = new ImageStack[align_chckes.length];
		alignedImps = new ImagePlus[align_chckes.length];

		for (int iAlign = 0; iAlign < align_chckes.length; iAlign++) {
			if (align_chckes[iAlign] && imps[iAlign] != null) {
				alignedStacks[iAlign] = imps[iAlign].getStack();
				alignedImps[iAlign] = imps[iAlign].duplicate();
			} else {
				alignedStacks[iAlign] = null;
				alignedImps[iAlign] = null;
			}
		}
	}

	public void prepIDCells() {
		// if any chioce other than histogram is selected, identify cells
		/*
		 * if(((options&(~DO_HIST))!=0)) { doOptions|=RUN_IDCELLS;
		 * doOptions|=RUN_CDP; } else { doOptions&=~RUN_IDCELLS;
		 * doOptions&=~RUN_CDP; }
		 */
		// no need to do anything here

		markedIMG = imps.length - 1;
		while (markedIMG >= 0) {
			if (imps[markedIMG] != null)
				break;
			else
				markedIMG--;
		}

		cal = markedIMG < 0 ? null
				: (imps[markedIMG].getGlobalCalibration() == null ? imps[markedIMG].getCalibration()
						: imps[markedIMG].getGlobalCalibration());

	}

	public void prepVisual() {
		if ((options & RUN_SCATTER) != 0)
			cellData = new CellData[SAMPLE_SIZE][nReporters];
		else
			cellData = null;

		heatmapStack = new ImageStack[heatmapColor_combs.length];
		heatmapImp = new ImagePlus[heatmapColor_combs.length];

		for (int iHeat = 0; iHeat < heatmap_chckes.length; iHeat++) {
			if (heatmap_chckes[iHeat] && imps[iHeat] != null)
				heatmapStack[iHeat] = new ImageStack(imps[iHeat].getWidth(), imps[iHeat].getHeight(), 1);
			else
				heatmapStack[iHeat] = null;
		}

		// use stack
		if ((options & DO_HEAT_STACK) != 0) {
			for (int ipic = 0; ipic < heatmapStack.length; ipic++) {
				if (heatmapStack[ipic] == null)
					continue;
				for (int iFrame = 1; iFrame <= frames; iFrame++) {
					if (imps[ipic] != null) {
						ImageProcessor ip = imps[ipic].getStack().getProcessor(iFrame);
						if (scalar[ipic][0] > ip.getStatistics().min)
							scalar[ipic][0] = ip.getStatistics().min;
						if (scalar[ipic][1] < ip.getStatistics().max)
							scalar[ipic][1] = ip.getStatistics().max;
					} else {
						scalar[ipic][0] = DEFAULT_MIN;
						scalar[ipic][1] = DEFAULT_MAX;
					}
				}
			}
		}

		if ((options & RUN_MATRIX) != 0) {
			cellDataCs = new CellData[nReporters][1][];
			mMetricValues = new double[BasicCalculator.ft2length(matrixFT_spin[0])][BasicCalculator
					.ft2length(matrixFT_spin[1])];
			mTOSRTArray = new ResultsTable[1];

		} else {
			cellDataCs = null;

			mMetricValues = null;
			mTOSRTArray = null;
		}
	}

	public void prepMetrics() {
		if ((options & RUN_RTS) != 0)
			outputRTArray = new ResultsTable[1];
		else
			outputRTArray = null;
	}

	public void prepOthers() {
		// output mask initialization
		if ((options & DO_MASKS) != 0)
			outMaskArray = new ImageProcessor[1];
		else
			outMaskArray = null;

		// add roi
		if ((options & DO_ROIS) != 0) {
			saveRois = new Object[1];
			numOfRois = new int[1];
		} else {
			saveRois = null;
			numOfRois = null;
		}

		// initiate doDist
		// doDist=true;
	}

	public void prepCustom() {

		if (!IJ.isMacro() && (options & DO_CUSTOM) != 0) {
			customCompiler = new StringCompiler();
			try {
				if (customCompiler.compileCustom(customCode_text)) {
					gui.setCustomStatus(GUI.SUCCESS);
				} else {
					gui.setCustomStatus(GUI.FAILURE);
					customCompiler = null;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ExceptionHandler.handleException(e);
			}
		} else
			customCompiler = null;
	}

	public void prepAll() {
		markedIMG = imps.length - 1;
		while (markedIMG >= 0) {
			if (imps[markedIMG] != null)
				break;
			else
				markedIMG--;
		}

		cal = markedIMG < 0 ? null
				: (imps[markedIMG].getGlobalCalibration() == null ? imps[markedIMG].getCalibration()
						: imps[markedIMG].getGlobalCalibration());

		if (!didAlignment)
			prepAlignment();
		prepIDCells();
		prepMetrics();
		prepCustom();
		prepOthers();
		prepVisual();

	}

	// check if three images match
	private int checkStack() {
		if (imps == null) {
			IJ.error(pluginName + " error", "Missing Images Stack");
			return -1;
		}

		int slice = -1, channel = -1, frame = -1;
		int type = whichToCheck();

		String testWidth = "", testHeight = "", testSlice = "", testChannel = "", testFrame = "", testCalWidth = "",
				testCalHeight = "";
		Calibration cal = null;
		for (int i = 0; i < imps.length; i++) {
			if ((type & (1 << i)) != 0) {
				if (imps[i] == null) {
					IJ.error(pluginName + " error", "Missing Input in " + imgLabels[i] + " for selected operation");
					return -1;
				}
				if (imps[i].getType() == ImagePlus.COLOR_RGB) {
					IJ.error(pluginName + " error", "Input cannot be RGB images");
					return -1;
				}else if(imps[i].getType() == ImagePlus.COLOR_256){
					IJ.error(pluginName + " error", "Input cannot be 8-bit color images");
					return -1;
				}

				cal = imps[i].getGlobalCalibration() == null ? imps[i].getCalibration()
						: imps[i].getGlobalCalibration();

				slice = imps[i].getNSlices();
				channel = imps[i].getNChannels();
				frame = imps[i].getNFrames();

				if (frame == 1) {
					frame = slice;
					slice = 1;
				}

				if (cal != null) {
					testCalWidth = testCalWidth + cal.pixelWidth + " ";
					testCalHeight = testCalHeight + cal.pixelHeight + " ";
				}

				width = imps[i].getWidth();
				height = imps[i].getHeight();

				testWidth = testWidth + imps[i].getWidth() + " ";
				testHeight = testHeight + imps[i].getHeight() + " ";
				testSlice = testSlice + slice + " ";
				testChannel = testChannel + channel + " ";
				testFrame = testFrame + frame + " ";
			}
		}

		if (!(isDimensionEqual(testWidth) && isDimensionEqual(testHeight) && isDimensionEqual(testSlice)
				&& isDimensionEqual(testChannel) && isDimensionEqual(testFrame))) {
			IJ.error(pluginName + " error", "Stacks' dimensions mismatch");
			return -1;
		}

		if (!(isDimensionEqual(testCalWidth) && isDimensionEqual(testCalHeight))) {
			IJ.error(pluginName + " error", "Scaling factors must be global");
			return -1;
		}

		if (frame == -1) {
			if (type == 0)
				IJ.error(pluginName + " error", "None of the operations is selected");
			else
				ExceptionHandler.addError(Thread.currentThread(), "Unknown error");
			return -1;
		}

		if (slice != 1) {
			IJ.error(pluginName + " error", "Input cannot have multiple frames as well as mutiple slices");
			return -1;
		}

		if (channel != 1) {
			IJ.error(pluginName + " error", "Input cannot have multiple channels");
			return -1;
		}

		boolean isBinaryImp = false;
		if (imps[imps.length - 1] != null) {
			for (int iFrame = 1; iFrame <= frame; iFrame++) {
				ImageProcessor ip = imps[imps.length - 1].getStack().getProcessor(iFrame);
				if (ip.isBinary()) {
					isBinaryImp = true;
				} else if (isBinaryImp) {
					IJ.error(pluginName + " error", "Phase or Mask Stack must be all or no binary");
					return -1;
				}
			}
		}
		return frame;
	}

	// compare substrings separated by space, skip if the substring is #
	private boolean isDimensionEqual(String str) {
		String[] strs = str.split(" ");
		String temp;
		if (strs.length > 0)
			temp = strs[0];
		else
			return false;
		for (int i = 0; i < strs.length; i++) {
			if (!strs[i].equals(temp)) {
				return false;
			}
		}
		return true;
	}

	private void doIDCells(CellFinder impCells, RoiManager roiManager, ResultsTable rt, ImageProcessor[] ips) {
		// There is no guarantee that this roiManager will be used for the same
		// thread
		// However, it doesn't matter as long as each thread uses its own
		impCells.setRoiManager(roiManager);
		impCells.setResultsTable(rt);
		impCells.setSizeFilters(filterMinSize_texts, filterMaxSize_texts);
		// do watershed when inputValues[2] = true
		impCells.getParticles(waterShed_chck);
		impCells.getMeasurements(ips);
		impCells.initialFilters(getAllfilters(), getAllMinRanges(), getAllMaxRanges(), getAllBackRatios());
		impCells.applyFilters();
	}

	// run after preparing(e.g. prepAll()), determine which channel(s) is/are
	// required
	// 1=channel 1; 2=channel 2; 4=phase contrast
	// OR
	// 1=channel 1; 2=channel 2; 4=phase contrast; 8=channel 3;
	private int whichToCheck() {
		int type = 0;
		// use the last n digits to indicate whether to check nth channel
		if (imps == null)
			return type;

		if ((options & RQD_ALL_REPORTERS) != 0) {
			for (int i = 0; i < nReporters; i++)
				type |= (1 << i);
		}

		if ((options & RQD_REPORTER) != 0) {
			boolean noIMG = true;
			for (int i = 0; i < nReporters; i++) {
				if (imps[i] != null) {
					type |= (1 << i);
					noIMG = false;
				}
			}
			if (noIMG)
				return 1;
		}

		if ((options & RQD_CELLID) != 0 || (((options & RUN_IDCELLS) != 0) && (imps[imps.length-1] != null))){
			type |= (1 << (MAX_NCHANNELS - 1));
		}

		if ((options & RUN_HEAT) != 0) {
			for (int iHeat = 0; iHeat < heatmap_chckes.length; iHeat++)
				if (heatmap_chckes[iHeat])
					type |= (1 << (iHeat));
		}

		if ((options & RUN_ALIGN) != 0) {
			for (int iAlign = 0; iAlign < align_chckes.length; iAlign++)
				if (align_chckes[iAlign])
					type |= (1 << (iAlign));
		}
		return type;

	}

	public void resetOutArray() {
		// These were usd as markers as well, so be careful
		// options=0;
		saveRois = null;
		numOfRois = null;
		outMaskArray = null;
		mTOSRTArray = null;
		outputRTArray = null;
		cellData = null;

		// These were not used as markers, so just to free memory
		mMetricValues = null;
		outputRT = null;

		roiCells = null;
		// cen2Npole=null;

		// Reset ImageJ progressbar
		IJ.getInstance().getProgressBar().show(-1.0);
	}

	public void resetAlignOutput() {

		alignedStacks = new ImageStack[align_chckes.length];
		;
		alignedImps = new ImagePlus[align_chckes.length];
		;
	}

	public static String[] getChoices(String[] strs, int[] choices) {
		String[] allFilters = new String[choices.length];
		for (int iFilter = 0; iFilter < allFilters.length; iFilter++)
			allFilters[iFilter] = strs[choices[iFilter]];
		return allFilters;
	}

	private int[] shuffleIdx(int size) {
		Random rnd = new Random();
		int arr[] = new int[size];
		for (int i = 0; i < size; i++)
			arr[i] = i;
		// Shuffle array
		for (int i = size; i > 1; i--) {
			int j = rnd.nextInt(i);
			int tmp = arr[i - 1];
			arr[i - 1] = arr[j];
			arr[j] = tmp;
		}
		return arr;
	}

	private <T> T[] append2D(T[][] data, Class<T> type) {
		if (data == null)
			return null;

		int length = 0;
		for (int i = 0; i < data.length; i++) {
			if (data[i] != null)
				length += data[i].length;
		}
		@SuppressWarnings("unchecked")
		T[] result = (T[]) Array.newInstance(type, length);

		length = 0;

		for (int i = 0; i < data.length; i++) {
			if (data[i] != null) {
				for (int j = 0; j < data[i].length; j++)
					result[length + j] = data[i][j];
				length += data[i].length;
			}
		}

		return result;
	}

	private CellData[][] append3D(CellData[][][] data) {
		if (data == null)
			return null;

		int[] length = new int[data.length];

		for (int i = 0; i < data.length; i++) {
			if (data[i] != null) {
				for (int j = 0; j < data[i].length; j++) {
					if (data[i][j] != null) {
						length[i] += data[i][j].length;
					}
				}
			}
		}

		CellData[][] result = new CellData[data.length][];

		for (int i = 0; i < data.length; i++) {
			int temp = 0;
			if (data[i] != null) {
				result[i] = new CellData[length[i]];
				for (int j = 0; j < data[i].length; j++) {
					if (data[i][j] != null) {
						for (int k = 0; k < data[i][j].length; k++)
							result[i][temp + k] = data[i][j][k];
						temp += data[i][j].length;
					}
				}
			}
		}

		return result;
	}

	/*
	 * @SuppressWarnings("unchecked") private <T> T[][] append3D(T[][][] data,
	 * Class<T> type) { if (data == null) return null;
	 * 
	 * int length = 0; for (int i = 0; i < data.length; i++) { if (data[i] !=
	 * null) length += data[i].length; }
	 * 
	 * T[] temp = (T[]) Array.newInstance(type,0); T[][] result = (T[][])
	 * Array.newInstance(temp.getClass(), length);
	 * 
	 * length = 0;
	 * 
	 * for (int i = 0; i < data.length; i++) { if (data[i] != null) { for (int j
	 * = 0; j < data[i].length; j++) result[length + j] = data[i][j]; length +=
	 * data[i].length; } }
	 * 
	 * return result; }
	 */

	/*
	 * public void shuffle(List<?> list, Random rnd) { int size = list.size();
	 * if (size < SHUFFLE_THRESHOLD || list instanceof RandomAccess) { for (int
	 * i=size; i>1; i--) swap(list, i-1, rnd.nextInt(i)); } else { Object arr[]
	 * = list.toArray();
	 * 
	 * // Shuffle array for (int i=size; i>1; i--) swap(arr, i-1,
	 * rnd.nextInt(i));
	 * 
	 * // Dump array back into list // instead of using a raw type here, it's
	 * possible to capture // the wildcard but it will require a call to a
	 * supplementary // private method ListIterator it = list.listIterator();
	 * for (int i=0; i<arr.length; i++) { it.next(); it.set(arr[i]); } } }
	 */
}
