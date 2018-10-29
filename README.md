# EzColocalization

## About
EzColocalization is an open source ImageJ plugin developed to assist in colocalization studies. To allow for its use with as many data types as possible, the program takes a variety of inputs. Details of the possible inputs are available in the EzColocalization publication, but briefly, monochromatic images can be used as reporters, and ROI manager lists, binary masks, and light microscopy images can be used as to identify areas for analysis. This allows images or hand drawn regions for identification of areas to be analyzed. Outputs include everything necessary to visualize data, to decide upon appropriate analysis, to analyze data, and to analyze populations within experiments. EzColocalization is meant to simplify colocalization analysis for experimentalists, however EzColocalization can also be simply be used to identify cells with particular characteristics, align images, analyze colocalization in whole images, and analyze average signal of cells.

## Prerequisites
[ImageJ](http://imagej.nih.gov/ij/download.html) (or [Fiji](https://fiji.sc/)) must be installed to use the plugin. 

## Install
On the ImageJ menu bar select "Plugins">"Install..." and select the EzColocalization plugin file (.jar format). The EzColocalization plugin will now be selectable in the "Plugins" menu. Alternatively, the .jar file version of the plugin can be moved to the "plugins" folder found in the ImageJ folder, and ImageJ must be reopened. Finally, the source code and .jar file of EzColocalization plugin can also be downloaded from GitHub.

## Running the tests
To use the EzColocalization plugin, select it from the "Plugins" menu on ImageJ. The plugin has tabs dedicated to the multitude of functions included. The function of each tab can be performed individually with “Preview” buttons, or all selected functions can be ran together with the “Analyze” button at the bottom of the plugin window. Test images of bacteria are included within the plugin to allow users to tryout the plugin. To open these images, within the EzColocalization window, select “File”>”Test images”.

## Built With:
* [Eclipse DSL Tools](https://www.eclipse.org/)
Version: Mars.2 Release (4.5.2)
Build id: 20160218-0600
* [ImageJ 1.50e](https://imagej.nih.gov/ij/)
* [Java 1.8.0](https://java.com/en/download/)
* Tutorial on how to develop ImageJ plugins in Eclipse can be found [here](http://imagejdocu.tudor.lu/doku.php?id=howto:plugins:the_imagej_eclipse_howto)

## Common issues
* Images used in the plugin cannot be in the RGB format, and must be monochromatic. 
* Images within stacks must be in the same order between all of the stacks for different channels. 
* If analysis is performed on cells with too few pixels, then “NaN” values may be produced in analysis. If this happens, try imposing a size filter to eliminate these values.
* Including saturated pixels in colocalization may produce variable results. Since many pixels will have equal values, the calculation of several colocalization metrics, which depend on rank ordering, will be disrupted. Overexposed regions which max out a cameras dynamic range should not be analyzed. If they must be analyzed a filter should be used to remove overexposed cells.

## Contributing
In the interest of making the code interpretable and thus modifiable, the code within EzColocalization is organized as modularly as possible. The code is organized into packages of classes with related functions. A more detailed description of these classes and their functions is proved in the supplementary material of the EzColocalization publication.

## Versioning
We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/DrHanLim/EzColocalization/tags). 

## Authors
* **Weston Staufer** - *Initial work* - [westonstauffer](https://github.com/westonstauffer)
* **Huanjie Sheng** - *Initial work* - [david190810](https://github.com/david190810)
* **Han N. Lim** - *Project design* - [DrHanLim](https://github.com/DrHanLim)

## License
<EzColocalization, An ImageJ plugin for visualizing and measuring colocalization in cells and organisms.>
Copyright (C) <2018> <Weston Stauffer, Huanjie Sheng, and Han N. Lim>
This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
This program is distributed in the hope that it will be useful,but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.

## DEMO
[![A demo on YouTube](https://imgur.com/a/jF5SXsq)](https://www.youtube.com/watch?v=OlXrA3613EE&feature=youtu.be)
