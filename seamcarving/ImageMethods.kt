package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage

fun BufferedImage.createNegative(){

    val maxEnergy =  energyMap.values.maxOrNull()

    var intensity: Int
    for (row in 0 until this.width){
        for (col in 0 until this.height){
            try {
                intensity = (255.0 * energyMap["$row,$col"]!! / maxEnergy!!).toInt()
                val newColor = Color(intensity,intensity,intensity)
                this.setRGB(row, col, newColor.rgb)
            } catch (e: Exception){
                println(e); print(" $row, $col")
            }
        }
    }
}


fun BufferedImage.colorSeamRedVerticle(){
    // get the value from minimum energy from the seamEnergyMap.keys
    for (pixel in path) {
        val pix = pixel.split(",")
        if (pix[1] != "-1" && pix[1] != "${this.height}") {
            this.setRGB(pix[0].toInt(), pix[1].toInt(), Color.red.rgb)
        }
    }
}
fun BufferedImage.colorSeamRedHorizontal(){
    // get the value from minimum energy from the seamEnergyMap.keys
    for (pixel in path) {
        val pix = pixel.split(",")
        if (pix[0] != "-1" && pix[0] != "${this.width}") {
            this.setRGB(pix[0].toInt(), pix[1].toInt(), Color.red.rgb)
        }
    }
}

// used for
fun BufferedImage.shrinkImageFromHSeam(){
// loop through the seam, on each pixel run a horizontal loop changing current position to next one over
    for (pixel in path) { // loop through the seam,
        val pix = pixel.split(",")
        if (pix[0] != "-1" && pix[0] != "${this.width}") { //while in the main body of the picture
            for (y in pix[1].toInt() until this.height){ // loop through the row starting with the seam
                // if the x value is this.width - 1 or the next value is transparent then set pixel to transparent
                if (y == this.height - 1 || this.getRGB(pix[0].toInt(),y + 1) == Color.TRANSLUCENT) {
                    this.setRGB(pix[0].toInt(), y, Color.TRANSLUCENT)
                } else {
                    // set the current pixel in the col to the color from the next one
                    this.setRGB(pix[0].toInt(), y, this.getRGB(pix[1].toInt(), y + 1))
                }
            }
        }
    }
}

// create a list column of nodes with the value of 0. This helps by making an easy entry and exit point for horizontal seam
fun BufferedImage.createVerticalEdgeList(): List<Edge>{
    val list = mutableListOf<Edge>()
    //nested for loop going through all the pixels

    for (x in 0 until this.width){
        val checkX = when {
            x - 1 < 0 -> x + 1
            x + 1 > this.width - 1 -> x - 1
            else -> x
        }
        val checkFor0 = if (x == 0) x else checkX
        //add the link from first node to 0 node line
        list.add(Edge("$x,-1","$x,0", energyMap["$x,0"]!!))
        //add the link from 0 node to 0 node in same 0 node row
        list.add(Edge("$x,-1","${checkFor0 + 1},-1", 0.0))
        //add the link from last node to 0 node line
        list.add(Edge("$x,${this.height - 1}", "$x,${this.height}", 0.0))
        list.add(Edge("$x,${this.height}", "${checkFor0 + 1},${this.height}", 0.0))
        for (y in 0 until this.height){
            if (y == this.height - 1) break
            if (x != this.width - 1){
                list.add(Edge(
                    "$x,$y",
                    "${if (x == this.width - 1) x - 1 else checkX - 1},${y + 1}",
                    energyMap["${if (x == this.width - 1) x - 1 else checkX - 1},${y + 1}"]!!
                )
                )
            }
            list.add(Edge(
                "$x,$y",
                "${checkX},${y + 1}",
                energyMap["${checkX},${y + 1}"]!!))
            if (x != 0){
                list.add(Edge(
                    "$x,$y",
                    "${if (x == 0) x + 1 else checkX + 1},${y + 1}",
                    energyMap["${if (x == 0) x + 1 else checkX + 1},${y + 1}"]!!))
            }
        }
    }
    //take the current pixel as a key and then the pixel thats x-1,y+1 and the energy of that pixel
    // and add an Edge object from it
    return list
}
fun BufferedImage.createHorizontalEdgeList(): List<Edge>{
    val list = mutableListOf<Edge>()
    //nested for loop going through all the pixels

    for (y in 0 until this.height){
        val checkY = when {
            y - 1 < 0 -> y + 1
            y + 1 > this.height - 1 -> y - 1
            else -> y
        }
        val checkFor0 = if (y == 0) y else checkY
        //add the link from first node to 0 node line
        list.add(Edge("-1,$y","0,$y", energyMap["0,$y"]!!))
        //add the link from 0 node to 0 node in same 0 node row
        list.add(Edge("-1,$y","-1,${checkFor0 + 1}", 0.0))
        //add the link from last node to 0 node line
        list.add(Edge("${this.width - 1},$y", "${this.width},$y", 0.0))
        list.add(Edge("${this.width},$y", "${this.width},${checkFor0 + 1}", 0.0))
        for (x in 0 until this.width){
            if (x == this.width - 1) break
            if (y != 0){
                list.add(Edge(
                    "$x,$y",
                    "${x + 1},${if (y == 0) y + 1 else checkY + 1}",
                    energyMap["${x + 1},${if (y == 0) y + 1 else checkY + 1}"]!!))
            }
            list.add(Edge(
                "$x,$y",
                "${x + 1},$checkY",
                energyMap["${x + 1},$checkY"]!!))
            if (y != this.height - 1){
                list.add(Edge(
                    "$x,$y",
                    "${x + 1},${if (y == this.height - 1) y - 1 else checkY - 1}",
                    energyMap["${x + 1},${if (y == this.height - 1) y - 1 else checkY - 1}"]!!
                )
                )
            }
        }
    }
    //take the current pixel as a key and then the pixel thats x-1,y+1 and the energy of that pixel
    // and add an Edge object from it
    return list
}

//fun repeatShrinkVertical(times: Int, test: BufferedImage) {
//    var num = 0
//    while (num < times) {
//        test.mapEnergy()
//        val edgeListVertical = test.createVerticalEdgeList()
//        for (i in edgeListVertical) (println(i.v1))
//        with(Graph(edgeListVertical, true)) {
//            dijkstra("0,-1") // for vertical seam
//            buildPath("${test.width - 1},${test.height}") // for vertical seam
//        }
////        test.shrinkImageFromVSeam()
//        energyMap.clear()
//        path.clear()
//        num++
//        println("v" + num)
//    }
//}
//
//fun repeatShrinkHorizontal(times: Int, test: BufferedImage){
//    var num = 0
//    while (num < times) {
//        test.mapEnergy()
//        val edgeListHorizontal = test.createHorizontalEdgeList()
//        with(Graph(edgeListHorizontal, true)) {
//            dijkstra("-1,0") // for horizontal seam
//            buildPath("${test.width},${test.height - 1}") // for horizontal seam
//        }
//        test.shrinkImageFromHSeam()
//        energyMap.clear()
//        path.clear()
//        num++
//        println(num)
//    }
//}