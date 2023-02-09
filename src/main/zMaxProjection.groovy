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
import ij.measure.ResultsTable
import ij.plugin.ChannelSplitter
import ij.plugin.RGBStackMerge
import ij.plugin.ZProjector
import ij.process.ImageProcessor
import ij.process.LUT
import loci.plugins.BF
import loci.plugins.in.ImporterOptions
import net.imagej.display.ColorMode

import java.awt.Point


// INPUT UI
//
#@Boolean (label="Run headless", default="false") headless
#@File(label = "Input File Directory", style = "directory") inputFilesDir
#@File(label = "Output directory", style = "directory") outputDir
#@Integer(label = "Start Slice", value = 1) startSlice
#@Integer(label = "Stop Slice", value = 1) stopSlice
#@String(label = "Channel Combination", value = "1,2,3,4") channelComb
#@String(label = "File Format", value = "Tiff") fileFormat
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
//def channelComb = "1,2,3,4"
//def fileFormat = "Tiff"
//def minValueChOne = 1;
//def maxValueChOne = 1;
//def minValueChTwo = 1;
//def maxValueChTwo = 1;
//def minValueChThree = 1;
//def maxValueChThree = 1;
//def minValueChFour = 1;
//def maxValueChFour = 1;
//def headless = true;
//new ImageJ().setVisible(false);

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
            chOne.setDisplayRange(minValueChOne.intValue(), maxValueChOne.intValue())
        /** Set display range on channel 2 */
        if (minValueChTwo != 1 || maxValueChTwo != 1)
            chTwo.setDisplayRange(minValueChTwo.intValue(), maxValueChTwo.intValue())
        /** Set display range on channel 3 */
        if (minValueChThree != 1 || maxValueChThree != 1)
            chThree.setDisplayRange(minValueChThree.intValue(), maxValueChThree.intValue())
        /** Set display range on channel 4 */
        if (minValueChFour != 1 || maxValueChFour != 1)
            chFour.setDisplayRange(minValueChFour.intValue(), maxValueChFour.intValue())

        def channelsIntensity = new ImagePlus[]{chOne, chTwo, chThree, chFour};
        def compositeImp = null;
        def channelsToMerge = null;
        if (channelComb.length() == 1) {
            compositeImp = channelsIntensity[channelComb.toInteger()];
        } else {
            def channelStrings = channelComb.split(",");
            channelsToMerge = new ImagePlus[channelStrings.length];
            for (int z = 0; z < channelsToMerge.length; z++)
                channelsToMerge[z] = channelsIntensity[z];
            compositeImp = RGBStackMerge.mergeChannels(channelsToMerge, false);
        }
        IJ.log("        -Saving serie: " + (j + 1).toString() + " in " + outputImageDir.getAbsolutePath() + " as " + impTitleSerie + "_" + channelComb.replaceAll(",", ""))
        /** Save each serie  as set in file format ("Tiff" or "Jpeg") */
        IJ.saveAs(compositeImp, fileFormat, outputImageDir.getAbsolutePath()
                + File.separator + impTitleSerie + "_" + channelComb.replaceAll(",", ""));

    }


}
IJ.log("Done!!!")
// exit
//
if (headless)
    System.exit(0)


