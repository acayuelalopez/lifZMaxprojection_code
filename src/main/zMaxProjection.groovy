import bunwarpj.BSplineModel
import bunwarpj.MainDialog
import bunwarpj.Mask
import bunwarpj.MiscTools
import bunwarpj.PointHandler
import bunwarpj.Transformation
import ij.IJ
import ij.ImageJ
import ij.ImagePlus
import ij.ImageStack
import ij.gui.ImageCanvas
import ij.gui.ImageWindow
import ij.measure.ResultsTable
import ij.plugin.ChannelSplitter
import ij.plugin.RGBStackMerge
import ij.plugin.ScaleBar
import ij.plugin.ZProjector
import ij.process.ImageProcessor
import ij.process.LUT
import loci.plugins.BF
import loci.plugins.in.ImporterOptions
import net.imagej.display.ColorMode
import org.apache.commons.io.FilenameUtils

import java.awt.Font
import java.awt.Point


// INPUT UI
//
#@File(label = "Input File Directory", style = "directory") inputFilesDir
#@File(label = "Output directory", style = "directory") outputDir
#@Integer(label = "Start Slice", value = 1) startSlice
#@Integer(label = "Stop Slice", value = 1) stopSlice
#@String(label = "Channel Combination", value = "1,2,3,4") channelComb
#@String(label = "File Format", value = "Tiff") fileFormat
#@String(label = "Font Size", value = "24") fontSize
#@Boolean(label = "Apply Scale Bar", value = true) applyScaleBar
#@Integer(label = "Scale Bar Width", value = 100) scaleWidth
#@String(label = "Color Channel One", value = "Green") colorChOne
#@String(label = "Color Channel Two", value = "Red") colorChTwo
#@String(label = "Color Channel Three", value = "Cyan") colorChThree
#@String(label = "Color Channel Four", value = "Blue") colorChFour
#@Integer(label = "Min Value Channel One", value = 1) minValueChOne
#@Integer(label = "Max Value Channel One", value = 1) maxValueChOne
#@Integer(label = "Min Value Channel Two", value = 1) minValueChTwo
#@Integer(label = "Max Value Channel Two", value = 1) maxValueChTwo
#@Integer(label = "Min Value Channel Three", value = 1) minValueChThree
#@Integer(label = "Max Value Channel Three", value = 1) maxValueChThree
#@Integer(label = "Min Value Channel Four", value = 1) minValueChFour
#@Integer(label = "Max Value Channel Four", value = 1) maxValueChFour


// IDE
//
//def inputFilesDir = new File("/home/anaacayuela/Ana_pruebas_imageJ/margarita/images");
//def outputDir = new File("/home/anaacayuela/Ana_pruebas_imageJ/margarita/results");
//def startSlice = 1;
//def stopSlice = 1;
//def channelComb = "3"
//def fileFormat = "Tiff"
//def scaleWidth = 100;
//def colorChOne = "Green"
//def colorChTwo = "Red"
//def colorChThree = "Cyan"
//def colorChFour= "Blue"
//def minValueChOne = 1;
//def maxValueChOne = 150;
//def minValueChTwo = 1;
//def maxValueChTwo = 150;
//def minValueChThree = 1;
//def maxValueChThree = 150;
//def minValueChFour = 1;
//def maxValueChFour = 150;
//def fontSize = "14"
//def applyScaleBar = false;
//
//def headless = true;
//new ImageJ().setVisible(true);

IJ.log("-Parameters selected: ")
IJ.log("    -inputFileDir: " + inputFilesDir)
IJ.log("    -outputDir: " + outputDir)
IJ.log("    -startSlice: " + startSlice)
IJ.log("    -stopSlice: " + stopSlice)
IJ.log("    -channelComb: " + channelComb)
IJ.log("    -minValueChOne: " + minValueChOne + "  -maxValueChOne: " + maxValueChOne + "  -minValueChTwo: " + minValueChTwo + "  -maxValueChTwo: " + maxValueChTwo + "  -minValueChThree: " + minValueChThree + "  -maxValueChThree: " + maxValueChThree + "  -minValueChFour: " + minValueChFour + "  -maxValueChFour: " + maxValueChFour)
IJ.log("                                                           ");
/** Get files (images) from input directory */
def listOfFiles = inputFilesDir.listFiles();

for (def i = 0; i < listOfFiles.length; i++) {
    if (!listOfFiles[i].getName().contains("DS")) {
        IJ.log("Analyzing file: " + listOfFiles[i].getName());
        /** Define output directory per file */
        def outputImageDir = new File(
                outputDir.getAbsolutePath() + File.separator + listOfFiles[i].getName().replaceAll(".lif", ""));

        if (!outputImageDir.exists()) {
            def results = false;

            try {
                outputImageDir.mkdir();
                results = true;
            } catch (SecurityException se) {
            }
        }
        IJ.log("    -Creating output dir for image " + listOfFiles[i].getName().replaceAll(".lif", "") + " in " + outputDir.getAbsolutePath());
        /** Importer options for .lif file */
        def options = new ImporterOptions();
        options.setId(inputFilesDir.getAbsolutePath() + File.separator + listOfFiles[i].getName());
        options.setSplitChannels(false);
        options.setSplitTimepoints(false);
        options.setSplitFocalPlanes(false);
        options.setAutoscale(true);
        options.setStackFormat(ImporterOptions.VIEW_HYPERSTACK);
        options.setStackOrder(ImporterOptions.ORDER_XYCZT);
        options.setColorMode(ImporterOptions.COLOR_MODE_COMPOSITE);
        options.setCrop(false);
        options.setOpenAllSeries(true);
        def imps = BF.openImagePlus(options);

        for (int j = 0; j < imps.length; j++) {
            IJ.log("        -Analyzing serie: " + (j + 1).toString())
            /** Declare each image to process within input directory */
            def imp = imps[j];
            def impTitleSerie = null;
            if (imp.getTitle().contains("/")) {
                impTitleSerie = imp.getTitle().replaceAll("/", "");
            } else {
                impTitleSerie = imp.getTitle();
            }


            /** Get calibration from non-transformed image. */
            def cal = imp.getCalibration();
            /** Zmax projection */
            def impMax = null;
            /** Define stop slice depending on value set by user. */
            if (stopSlice == 1) {
                impMax = ZProjector.run(imp, "max", startSlice, imp.getNSlices());
            } else {
                impMax = ZProjector.run(imp, "max", startSlice, stopSlice);
            }

            /** Split channels */
            def channels = ChannelSplitter.split(impMax);
            if (channels.length == 4) {
                /** Get channel 1 */
                def chOne = channels[0];
                /** Get channel 2 */
                def chTwo = channels[1];
                /** Get channel 3 */
                def chThree = channels[2];
                /** Get channel 4 */
                def chFour = channels[3];

                /** Set display range on channel 1 */
                if (minValueChOne != 1 || maxValueChOne != 1)
                    chOne.setDisplayRange(minValueChOne.doubleValue(), maxValueChOne.doubleValue())
                /** Set color on channel 1 */
                IJ.run(chOne, colorChOne.toString(), "");
                /** Set display range on channel 2 */
                if (minValueChTwo != 1 || maxValueChTwo != 1)
                    chTwo.setDisplayRange(minValueChTwo.doubleValue(), maxValueChTwo.doubleValue())
                /** Set color on channel 2 */
                IJ.run(chTwo, colorChTwo.toString(), "");
                /** Set display range on channel 3 */
                if (minValueChThree != 1 || maxValueChThree != 1)
                    chThree.setDisplayRange(minValueChThree.doubleValue(), maxValueChThree.doubleValue())
                /** Set color on channel 3 */
                IJ.run(chThree, colorChThree.toString(), "");
                /** Set display range on channel 4 */
                if (minValueChFour != 1 || maxValueChFour != 1)
                    chFour.setDisplayRange(minValueChFour.doubleValue(), maxValueChFour.doubleValue())
                /** Set color on channel 4 */
                IJ.run(chFour, colorChFour.toString(), "");

                def channelsIntensity = new ImagePlus[]{chOne, chTwo, chThree, chFour};
                def compositeImp = null;
                def channelsToMerge = null;
                def channelStrings = channelComb.split(",");
//            if (channelStrings.length() == 1) {
//                compositeImp = channelsIntensity[channelStrings[0].toInteger().intValue()-1];
//            } else {
                //def channelStrings = channelComb.split(",");
                if (channelStrings.length != 1) {
                    channelsToMerge = new ImagePlus[channelStrings.length];
                    for (int z = 0; z < channelsToMerge.length; z++)
                        channelsToMerge[z] = channelsIntensity[channelStrings[z].toInteger().intValue() - 1];
                    compositeImp = RGBStackMerge.mergeChannels(channelsToMerge, false);
                } else {
                    compositeImp = channelsIntensity[channelStrings[0].toInteger().intValue() - 1]
                }
                // }
                /** Set original calibration */
                compositeImp.setCalibration(cal);
                /** Set scale */
                if (applyScaleBar) {
                    def resol = (1 / (imp.getCalibration().pixelWidth)).toString();
                    IJ.run(compositeImp, "Set Scale...", "distance=" + resol + " known=1 unit=micron");
                    /** Set scale bar */
                    IJ.run(compositeImp, "Scale Bar...", "width=" + scaleWidth.toString() + " height=4 thickness=4 font=" + fontSize + " color=White background=None location=[Lower Right] horizontal bold overlay");
                }
                IJ.log("        -Saving serie: " + (j + 1).toString() + " in " + outputImageDir.getAbsolutePath() + " as " + impTitleSerie + "_" + channelComb.replaceAll(",", ""))
                /** Save each serie  as set in file format ("Tiff" or "Jpeg") */
                IJ.saveAs(compositeImp, fileFormat, outputImageDir.getAbsolutePath()
                        + File.separator + impTitleSerie + "_" + channelComb.replaceAll(",", ""));

            }else{
                IJ.log("It is needed to have 4 channels to do the analysis.")
            }
        }

    }
}
IJ.log("Done!!!")
// exit
//
//if (headless)
//    System.exit(0)

//double computeDefaultBarWidth(ImagePlus imp) {
//    def cal = imp.getCalibration();
//    def win = imp.getWindow();
//    def mag = (win != null) ? win.getCanvas().getMagnification() : 1.0;
//    if (mag > 1.0)
//        mag = 1.0;
//
//    def pixelHeight = cal.pixelHeight;
//    if (pixelHeight == 0.0)
//        pixelHeight = 1.0;
//    def imageHeight = (imp.getHeight().doubleValue() * pixelHeight).doubleValue();
//
//    def vBarHeight = (80.0 * pixelHeight) / mag;
//    if (vBarHeight > 0.67 * imageHeight)
//        vBarHeight = 0.67 * imageHeight;
//    if (vBarHeight > 5.0)
//        vBarHeight = (int) vBarHeight;
//
//    return vBarHeight;
//
//}
//
//int getVBoxHeightInPixels(ImagePlus imp) {
//    def hideText = true;
//    updateFont(imp);
//    ImageProcessor ip = imp.getProcessor();
//    int vLabelHeight = hideText ? 0 : ip.getStringWidth(getVLabel());
//    int vBoxHeight = Math.max(vBarHeightInPixels, vLabelHeight);
//    return (config.showVertical ? vBoxHeight : 0);
//}
//
//int getHBoxHeightInPixels2() {
//    def hideText = false;
//    def fontSize = 14;
//    def showHorizontal = true;
//    def barThicknessInPixels = 4;
//    int hLabelHeight = hideText ? 0 : fontSize;
//    int hBoxHeight = barThicknessInPixels + (int) (hLabelHeight * 1.25);
//    return (showHorizontal ? hBoxHeight : 0);
//}
//
//String getVUnit(ImagePlus imp) {
//    String vUnits = imp.getCalibration().getYUnit();
//    if (vUnits.equals("microns"))
//        vUnits = IJ.micronSymbol + "m";
//    return vUnits;
//}
//
//void updateFont(ImagePlus imp) {
//    def boldText = true;
//    def serifFont = false;
//    def fontSize = 14.intValue();
//    int fontType = boldText ? Font.BOLD : Font.PLAIN;
//    String font = serifFont ? "Serif" : "SanSerif";
//    ImageProcessor ip = imp.getProcessor();
//    ip.setFont(new Font(font, fontType, fontSize));
//    ip.setAntialiasedText(true);
//}
