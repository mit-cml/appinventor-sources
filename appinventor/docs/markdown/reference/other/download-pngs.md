---
layout: documentation
title: Download Block Images
---

Starting with version nb177 of MIT App Inventor, it is possible to export individual blocks in the Portable Network Graphics (PNG) image format. This feature makes it easier to write tutorials and curriculum materials by giving high quality images of blocks without needing to crop the larger workspace export image or stitch together screenshots of blocks.

## Exporting Blocks

To export a block, right click on the block to bring up the context menu and click on the "Download Blocks as PNG" menu item:

![Right click menu on a block showing the Download Blocks as PNG menu item](download-blocks-menu.png)

The browser will prompt you to download a file called blocks.png after selecting this menu item.

## Importing Blocks

Blocks images created in this way contain additional metadata in the image that stores the block code in a machine readable format. MIT App Inventor will read the content of this metadata if the image is drag-and-dropped into the blocks workspace. Below is a video demonstrating how you can drag and drop blocks images from the documentation into App Inventor. You can also drag files from your computer to the workspace (similar to cloud storage services).

<video width="640" height="320">
<source src="drag-and-drop.mp4" type="video/mp4" />
Your browser does not support playing this video.
</video>
