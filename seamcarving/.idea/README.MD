This is a simple set of extension functions to the BufferedImage class.

The choice to use extension functions instead of a class inheritance Model was made because this was for coursework
that did not require encapsulation for its MVP. 

The actual seam carving functions are in the Main.kt

The Algo.kt file holds the Dijkstra's implementation for seam carving.  This is not
an original piece of code, it was extracted and modified from https://rosettacode.org/wiki/Dijkstra%27s_algorithm

The ImageMethods.kt holds some extra functions that were created during the different stages of the course
as well as the image resizing functions that utilize Dijkstra's Algorith for creating TreeSet of nodes for our seam.

Hope this is helpful to anyone trying to implement a simple Kotlin Seam Carving program! 