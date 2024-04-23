/* *****************************************************************************
 *  Name: Philipp Sick
 *  Date: July 27, 2023
 *  Description: Seam-carving is a content-aware image resizing technique where
 *  the image is reduced in size by one pixel of height (or width) at a time.
 *  A vertical seam in an image is a path of pixels connected from the top to
 *  the bottom with one pixel in each row; a horizontal seam is a path of pixels
 *  connected from the left to the right with one pixel in each column.
 *  Unlike standard content-agnostic resizing techniques (such as cropping and
 *  scaling), seam carving preserves the most interest features (aspect ratio,
 *  set of objects present, etc.) of the image.
 **************************************************************************** */

import edu.princeton.cs.algs4.Picture;

public class SeamCarver {
    private int width;
    private int height;
    private int horizontalCount;
    private int verticalCount;
    private int[][] picRGB;
    private boolean isHorizontalSeamCall;
    private boolean isTransposed;

    // Private class to represent a directed edge
    private class Edge {
        private int v;
        private int w;

        private Edge(int v, int w) {
            this.v = v;
            this.w = w;
        }

        public int from() {
            return v;
        }

        public int to() {
            return w;
        }

    }

    // Create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException();
        }

        width = picture.width();
        height = picture.height();
        picRGB = new int[picture.height()][picture.width()];
        isHorizontalSeamCall = false;
        isTransposed = false;
        horizontalCount = 0;
        verticalCount = 0;

        // Get color of each pixel as 32-bit int
        for (int row = 0; row < picture.height(); row++) {
            for (int col = 0; col < picture.width(); col++) {
                picRGB[row][col] = picture.getRGB(col, row);
            }
        }
    }

    // Return current picture
    public Picture picture() {
        Picture pic = new Picture(this.width, this.height);

        // Update picture with current color values
        for (int i = 0; i < this.height(); i++) {
            for (int j = 0; j < this.width(); j++) {
                pic.setRGB(j, i, picRGB[i][j]);
            }
        }
        horizontalCount = 0;
        verticalCount = 0;
        return pic;
    }

    // Width of current picture
    public int width() {
        return this.width;
    }

    // Height of current picture
    public int height() {
        return this.height;
    }

    // Energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
            throw new IllegalArgumentException();
        }

        if (x == (this.width - 1) || y == (this.height - 1) || x == 0 || y == 0) {
            return 1000;
        }

        // Get red, green, and blue color components for surrounding pixels
        int[] rgbRight = rgb(picRGB[y][x - 1]);
        int[] rgbLeft = rgb(picRGB[y][x + 1]);
        int[] rgbTop = rgb(picRGB[y - 1][x]);
        int[] rgbBottom = rgb(picRGB[y + 1][x]);

        int xDelta = 0;
        int yDelta = 0;
        for (int i = 0; i <= 2; i++) {
            xDelta += Math.pow((rgbRight[i] - rgbLeft[i]), 2);
            yDelta += Math.pow((rgbBottom[i] - rgbTop[i]), 2);
        }

        return Math.sqrt(xDelta + yDelta);
    }

    // Compute red, green, and blue color components
    private int[] rgb(int rgb) {
        int[] colors = {
                (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, (rgb >> 0) & 0xFF
        };
        return colors;
    }

    // Sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        if (!isTransposed) {
            isTransposed = true;
        }
        int[] seam = findVerticalSeam();
        isTransposed = false;
        return seam;
    }

    // Sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        int d = this.height * this.width;
        // Energy of each pixel
        double[] energy = new double[d];
        // Energy of path to pixel
        double[] energyTo = new double[d];
        // Edge to each pixel
        Edge[] edgeTo = new Edge[d];

        // Local width and height variables in case we're transposed
        // and need to swap width and height
        int widthT = isTransposed ? this.height : this.width;
        int heightT = isTransposed ? this.width : this.height;
        int[] seam = new int[heightT];

        // Corner case
        if (heightT == 1 || widthT == 1) {
            for (int i = 0; i < heightT; i++) {
                seam[i] = 0;
            }
            return seam;
        }

        // Get energy of each pixel
        for (int i = 0; i < d; i++) {
            if (i < widthT) {

                int y = i / this.width;
                int x = i - y * this.width;

                energy[i] = this.energy(x, y);
            }
            else {
                energyTo[i] = Double.POSITIVE_INFINITY;

                int y = i / this.width;
                int x = i - y * this.width;

                energy[i] = this.energy(x, y);
            }
        }

        // Swap energies if transposed
        if (isTransposed) {
            double[] tArray = new double[d];
            System.arraycopy(energy, 0, tArray, 0, d);
            int i = 0;
            for (int c = 0; c < widthT; c++) {
                for (int r = 0; r < heightT; r++) {
                    int j = r * widthT + c;
                    energy[j] = tArray[i];
                    i++;
                }
            }
        }

        // Relax any connected vertices, right, left, and bottom
        for (int i = 0; i < d - widthT; i++) {
            // Edge case
            if (i < widthT) {
                energyTo[i] = 1000;
            }

            if (i == 0 || (i >= widthT && (i % widthT == 0))) {
                Edge right = new Edge(i, i + widthT + 1);
                relax(right, energy, energyTo, edgeTo);
            }
            else if ((i == widthT - 1) || (i >= widthT && ((i + 1) % widthT == 0))) {
                Edge left = new Edge(i, i + widthT - 1);
                relax(left, energy, energyTo, edgeTo);
            }
            else {
                Edge left = new Edge(i, i + widthT - 1);
                relax(left, energy, energyTo, edgeTo);
                Edge right = new Edge(i, i + widthT + 1);
                relax(right, energy, energyTo, edgeTo);
            }

            Edge bottom = new Edge(i, i + widthT);
            relax(bottom, energy, energyTo, edgeTo);
        }

        // Find last pixel in the seam
        // Get first pixel in bottom row and energy to get to this pixel, then
        // see if energyTo is less for any other bottom pixel
        int vmin = d - widthT;
        double energyToMin = energyTo[vmin];
        for (int i = d - widthT + 1; i < d; i++) {
            if (energyTo[i] < energyToMin) {
                energyToMin = energyTo[i];
                vmin = i;
            }
        }
        seam[heightT - 1] = vmin;

        // Trace other pixels in seam from last pixel
        int source = edgeTo[vmin].from();
        for (int i = heightT - 2; i > 0; i--) {
            seam[i] = source;
            source = edgeTo[source].from();
        }
        seam[0] = edgeTo[seam[1]].from();

        for (int i = 0; i < seam.length; i++) {
            seam[i] = seam[i] - i * widthT;
        }
        return seam;

    }

    // Relax edge if energy to pixel is less than current energy to
    private void relax(Edge e, double[] energy, double[] energyTo, Edge[] edgeTo) {
        int v = e.from();
        int w = e.to();
        if (energyTo[w] > energyTo[v] + energy[w]) {
            energyTo[w] = energyTo[v] + energy[w];
            edgeTo[w] = e;
        }
    }

    // Remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException();
        }
        if (seam.length != this.width) {
            throw new IllegalArgumentException();
        }
        if (this.height <= 1) {
            throw new IllegalArgumentException();
        }
        isHorizontalSeamCall = true;
        removeVerticalSeam(seam);
    }

    // Remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException();
        }
        if (seam.length != this.height && !isHorizontalSeamCall) {
            throw new IllegalArgumentException();
        }
        if (!isHorizontalSeamCall && this.width <= 1) {
            throw new IllegalArgumentException();
        }

        if (isHorizontalSeamCall) {
            for (int x = 0; x < this.width; x++) {
                if (x < this.width - 1) {
                    if (Math.abs(seam[x] - seam[x + 1]) > 1) {
                        throw new IllegalArgumentException();
                    }
                }
                if (seam[x] < 0 || seam[x] >= this.height) {
                    throw new IllegalArgumentException();
                }
                // Shift pixels
                for (int y = seam[x]; y < this.height - 1; y++) {
                    picRGB[y][x] = picRGB[y + 1][x];
                }
                // Clear seam from picture
                picRGB[this.height - 1][x] = Integer.MIN_VALUE;
            }
            // Update height and count
            this.height--;
            isHorizontalSeamCall = false;
            horizontalCount++;
        }
        else {
            for (int y = 0; y < this.height; y++) {
                if (y < this.height - 1) {
                    if (Math.abs(seam[y] - seam[y + 1]) > 1) {
                        throw new IllegalArgumentException();
                    }
                }
                if (seam[y] < 0 || seam[y] >= this.width) {
                    throw new IllegalArgumentException();
                }
                // Shift pixels
                for (int x = seam[y]; x < this.width - 1; x++) {
                    picRGB[y][x] = picRGB[y][x + 1];
                }
                // Clear seam from pic
                picRGB[y][this.width - 1] = Integer.MIN_VALUE;
            }
            // Update width and count
            this.width--;
            verticalCount++;
        }
    }

    // Unit testing (optional)
    public static void main(String[] args) {
    }

}
