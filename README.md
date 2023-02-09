# lifZMaxprojection_code
This is a Groovy script to do z-max projection, change display range per channel and select channel to be saved as Tiff or Jpeg

## Running ImageJ/Fiji with Multiple parameters
- Command on macOS:
``./ImageJ-macosx --ij2 --headless --console --run hello.py 'name1="Mr",name2="Mrs Kraken"'``

## Running lifZMaxprojection_code in headless mode through macOS Terminal (ALL parameters)

``/path/to/ImageJ-macosx --ij2 --headless --run "/absolute_path/to/groovyscript/zMaxProjection.groovy" "headless=true, inputFilesDir='/absolute_path/to/inputFiles/images',outputDir='/absolute_path/to/outputDirectory/results',startSlice=1,stopSlice=4,channelComb='1,2,3',fileFormat='Tiff',minValueChOne=25,maxValueChOne=255,minValueChTwo=25,maxValueChTwo=155,minValueChThree=25,maxValueChThree=235,minValueChFour=25,maxValueChFour=200"``

## Running lifZMaxprojection_code in headless mode through macOS Terminal  (BASIC parameters)

``/path/to/ImageJ-macosx --ij2 --headless --run "/absolute_path/to/groovyscript/zMaxProjection.groovy" "headless=true, inputFilesDir='/absolute_path/to/inputFiles/images',outputDir='/absolute_path/to/outputDirectory/results',fileFormat='Tiff'"``

## Running lifZMaxprojection_code in headless mode through macOS Terminal  (My OWN parameters)

``/Applications/Fiji.app/Contents/MacOS/ImageJ-macosx --ij2 --headless --run "/Users/Marga/Desktop/lifZMaxprojection_code-main/src/main/zMaxProjection.groovy" "headless=true,inputFilesDir='/Users/Marga/Desktop/Images',outputDir='/Users/Marga/Desktop/Results',startSlice=1,stopSlice=1,channelComb='1,2,3',fileFormat='Tiff',minValueChOne=1,maxValueChOne=1,minValueChTwo=1,maxValueChTwo=1,minValueChThree=1,maxValueChThree=1,minValueChFour=1,maxValueChFour=1"`

### Parameters Explanation:
- ``headless`` : true. 
- ``inputFilesDir`` : Directory in which the images (.lif files) to be analyzed are located. ``'/home/anaacayuela/Ana_pruebas_imageJ/margarita/images'`` (*Try to have just ".lif" files in this directory).
- ``outputDir`` : Directory in which the image outputs are saved. ``'/home/anaacayuela/Ana_pruebas_imageJ/margarita/results'``
- ``startSlice`` : Start slice for z-max projection. 
- ``stopSlice`` : Start slice for z-max projection. 
- ``channelComb`` : Combination of channels separated by commas. ``'1,2,3'`` or ``'2,4'`` or ``'1,3'``...
- ``fileFormat`` : Image File Format. ``'Tiff'`` or ``'Jpeg'``
- ``minValueChOne`` : Min value for display range in channel 1. ``25``
- ``maxValueChOne`` : Max value for display range in channel 1. ``255``
- ``minValueChTwo`` : Min value for display range in channel 2. ``25``
- ``maxValueChTwo`` : Max value for display range in channel 2. ``255``
- ``minValueChThree`` : Min value for display range in channel 3. ``25``
- ``maxValueChThree`` : Max value for display range in channel 3. ``255``
- ``minValueChFour`` : Min value for display range in channel 4. ``25``
- ``maxValueChFour`` : Max value for display range in channel 4. ``255``
