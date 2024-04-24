My solution to the Seam Carving assignment from Princeton's Algorithms, Part II course. The assignment spec can be found here: https://coursera.cs.princeton.edu/algs4/assignments/seam/specification.php. A brief summary is below.

Seam-carving is a content-aware image resizing technique where the image is reduced in size by one pixel of height (or width) at a time. A vertical seam in an image is a path of pixels connected from the top to the bottom with one pixel in each row; a horizontal seam is a path of pixels connected from the left to the right with one pixel in each column. Unlike standard content-agnostic resizing techniques (such as cropping and scaling), seam carving preserves the most interest features (aspect ratio, set of objects present, etc.) of the image.

The first step is to calculate the energy of each pixel using the dual-gradient energy function. The energy is a measure of the importance of each pixel—the higher the energy, the less likely that the pixel will be included as part of a seam. I then find the seam of minimum total energy. This is similar to the classic shortest path problem in an edge-weighted digraph except for the following:

  - The weights are on the vertices instead of the edges.
  
  - We want to find the shortest path from any of the W pixels in the top row to any of the W pixels in the bottom row.
  
  - The digraph is acyclic, where there is a downward edge from pixel (x, y) to pixels (x − 1, y + 1), (x, y + 1), and (x + 1, y + 1), assuming that the coordinates are in the prescribed range.
  
The final step is to remove from the image all of the pixels along the seam.
