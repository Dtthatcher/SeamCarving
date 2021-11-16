package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.pow
import kotlin.math.sqrt



val energyMap = mutableMapOf<String, Double>()

fun BufferedImage.findEnergy(x: Int, y: Int){
    //find a pixels energy based on how different its RGB value is from surrounding pixels
    val checkX = when { // account for 1st and last columns
        x - 1 < 0 -> x + 1
        x + 1 > this.width - 1 -> x - 1
        else -> x
    }

    val checkY = when { // account for top and bottom rows
        y - 1 < 0 -> y + 1
        y + 1 > this.height - 1 -> y - 1
        else -> y
    }

    val l = Color(this.getRGB(checkX - 1, y)) //left pixel
    val r = Color(this.getRGB(checkX + 1, y)) // right pixel
    val t = Color(this.getRGB(x, checkY - 1)) // top pixel
    val b = Color(this.getRGB(x, checkY + 1)) // bottom pixel

    val redX = (l.red - r.red).toDouble() // value change from left to right red pixel
    val greenX = (l.green - r.green).toDouble() // value change from left to right green pixel
    val blueX = (l.blue - r.blue).toDouble() // value change from left to right blue pixel
    val redY = (t.red - b.red).toDouble() // value change from top to bottom red pixel
    val greenY = (t.green - b.green).toDouble() // value change from top to bottom green pixel
    val blueY = (t.blue - b.blue).toDouble() // value change from top to bottom blue pixel

    val xDif = redX.pow(2.0) + greenX.pow(2.0) + blueX.pow(2.0) // total diff
    val yDif = redY.pow(2.0) + greenY.pow(2.0) + blueY.pow(2.0) // total diff

    val energy = sqrt(xDif + yDif)
    energyMap["$x,$y"] = energy // map the x and y coordinates of the pixel to its energy value
}
fun BufferedImage.mapEnergy(){ // get energy for every pixel

    for (col in 0 until this.width){
        for (row in 0 until this.height){
            this.findEnergy(col, row)
        }
    }
}
val seamPathMap = mutableMapOf<Double, MutableList<String>>() // path of seams

fun BufferedImage.findVSeams(num: Int){ // map the energy as the key and all the pixels in the vertical seam as value

    for (x in 0 until this.width - num){ // loop through the x vals
        val list = mutableListOf<String>() // create a list
        var curX = x
        var energy = energyMap["$x,0"]!!
        list.add("$x,0") // add first value to the list
        for (y in 0 until this.height){ // start traversing the y axis from current x node
            if (y == this.height - 1){ // break condition
                seamPathMap[energy] = list
                break
            }

            if (curX > 0 && curX < this.width - num - 1) { // if you're not on the edges
                val z = listOf( // grab the energy from the next three pixels
                    energyMap["${curX - 1},${y + 1}"]!!,
                    energyMap["$curX,${y + 1}"]!!,
                    energyMap["${curX + 1},${y + 1}"]!!
                )
                val m = z.minOrNull() // choose the one with the least value
                for (i in z) { // match the pixel to the energy and add its position to the list and its energy to toal energy for the seam
                    when {
                        z.indexOf(i) == 0 && i == m -> {list.add("${curX-1},${y+1}"); energy += i; curX--; break}
                        z.indexOf(i) == 1 && i == m -> {list.add("${curX},${y+1}"); energy += i; break}
                        z.indexOf(i) == 2 && i == m -> {list.add("${curX+1},${y+1}"); energy += i; curX++}
                    }
                }
            }
            else if (curX == 0) { // left edge case
                val z = listOf(
                    energyMap["$curX,${y + 1}"]!!,
                    energyMap["${curX + 1},${y + 1}"]!!
                )
                val m = z.minOrNull()
                for (i in z) {
                    when {
                        z.indexOf(i) == 0 && i == m -> {list.add("${curX},${y+1}"); energy += i; break}
                        z.indexOf(i) == 1 && i == m -> {list.add("${curX+1},${y+1}"); energy += i; curX++}
                    }
                }
            }
            else if (curX == this.width - num - 1) { // right edge case
                val z = listOf(
                    energyMap["$curX,${y + 1}"]!!,
                    energyMap["${curX - 1},${y + 1}"]!!
                )
                val m = z.minOrNull()
                for (i in z) {
                    when {
                        z.indexOf(i) == 0 && i == m -> {list.add("${curX},${y+1}"); energy += i; break}
                        z.indexOf(i) == 1 && i == m -> {list.add("${curX-1},${y+1}"); energy += i; curX--}
                    }
                }
            }
        }
    }
}

fun BufferedImage.findHSeams(num: Int){

    for (y in 0 until this.height - num){
        val list = mutableListOf<String>()
        var curY = y
        var energy = energyMap["0,$y"]!!
        list.add("0,$y")
        for (x in 0 until this.width){
            if (x == this.width - 1){

                seamPathMap[energy] = list
                break
            }

            if (curY > 0 && curY < this.height - num - 1) {
                val z = listOf(
                    energyMap["${x + 1},${curY - 1}"]!!,
                    energyMap["${x + 1},${curY}"]!!,
                    energyMap["${x + 1},${curY + 1}"]!!
                )
                val m = z.minOrNull()
                for (i in z) {
                    when {
                        z.indexOf(i) == 0 && i == m -> {list.add("${x + 1},${curY - 1}"); energy += i; curY--; break}
                        z.indexOf(i) == 1 && i == m -> {list.add("${x + 1},${curY}"); energy += i; break}
                        z.indexOf(i) == 2 && i == m -> {list.add("${x + 1},${curY + 1}"); energy += i; curY++}
                    }
                }
            }
            else if (curY == 0) {
                val z = listOf(
                    energyMap["${x + 1},${curY}"]!!,
                    energyMap["${x + 1},${curY + 1}"]!!
                )
                val m = z.minOrNull()
                for (i in z) {
                    when {
                        z.indexOf(i) == 0 && i == m -> {list.add("${x + 1},${curY}"); energy += i; break}
                        z.indexOf(i) == 1 && i == m -> {list.add("${x + 1},${curY + 1}"); energy += i; curY++}
                    }
                }
            }
            else if (curY == this.height - num - 1) {
                val z = listOf(
                    energyMap["${x + 1},${curY}"]!!,
                    energyMap["${x + 1},${curY - 1}"]!!
                )
                val m = z.minOrNull()
                for (i in z) {
                    when {
                        z.indexOf(i) == 0 && i == m -> {list.add("${x + 1},${curY}"); energy += i; break}
                        z.indexOf(i) == 1 && i == m -> {list.add("${x + 1},${curY - 1}"); energy += i; curY--}
                    }
                }
            }
        }
    }
}
// this function is for working with a vertical seam.
// it will start a loop on a pixel from that seam and traverse the image horizontally from that pixel.
// it will set the pixel its on to the RGB pattern to the right of it until it reaches a translucent pixel
// in which case it sets the last colored pixel to translucent and moves on to the next pixel in the seam list
fun BufferedImage.shrinkImageFromVSeam(seam: MutableList<String>){
// loop through the seam, on each pixel run a horizontal loop changing current position to next one over
    for (pixel in seam) { // loop through the seam,
        val pix = pixel.split(",")
        for (x in pix[0].toInt() until this.width){ // loop through the row starting with the seam
            // if the x value is this.width - 1 or the next value is transparent then set pixel to transparent
            if (x == this.width - 1 || this.getRGB(x + 1, pix[1].toInt()) == Color.TRANSLUCENT) {
                this.setRGB(x, pix[1].toInt(), Color.TRANSLUCENT)
                break
            } else {
                // set the current pixel in the row to the color from the next one
                this.setRGB(x, pix[1].toInt(), this.getRGB(x + 1, pix[1].toInt()))
            }
        }
    }
}

// this will repeat the shrink image cycle until the image reaches the specified size per the main fun args
fun repeatShrinkVertical(times: Int, test: BufferedImage){
    var num = 1

    while (num < times) {
        test.mapEnergy() // map the energy for the image
        test.findVSeams(num) // find the lowest energy seam
        test.shrinkImageFromVSeam(seamPathMap[seamPathMap.keys.minOrNull()]!!) // remove the pixels in that seam
        seamPathMap.clear() // reset the seam path map
        energyMap.clear() // reset the energy map set
        num++ // increment to repeat the process
    }
}

// refer to shrinkImageFromVSeam
fun BufferedImage.shrinkImageFromHSeam(seam: MutableList<String>){
// loop through the seam, .on each pixel run a horizontal loop changing current position to next one over
    for (pixel in seam) { // loop through the seam,
        val pix = pixel.split(",")
//        if (pix[1] != "-1" && pix[1] != "${this.height}") { //while in the main body of the picture
            for (y in pix[1].toInt() until this.height){ // loop through the row starting with the seam
                // if the x value is this.width - 1 or the next value is transparent then set pixel to transparent
                if (y == this.height - 1 || this.getRGB(pix[0].toInt(), y + 1) == Color.TRANSLUCENT) {
                    this.setRGB(pix[0].toInt(), y, Color.TRANSLUCENT)
                    break
                } else {
                    // set the current pixel in the row to the color from the next one
                    this.setRGB(pix[0].toInt(), y, this.getRGB(pix[0].toInt(), y + 1))
                }
            }
    }
}

// refer to repeatShrinkVertical
fun repeatShrinkHorizontal(times: Int, test: BufferedImage){

    var num = 0

    while (num < times + 1) {
        test.mapEnergy()
        test.findHSeams(num)
        test.shrinkImageFromHSeam(seamPathMap[seamPathMap.keys.minOrNull()]!!)
        seamPathMap.clear()
        energyMap.clear()
        num++
    }
}

// save the img to a new img file in a png format
fun BufferedImage.saveImg(file: String){
    val img = File(file)
    ImageIO.write(this, "png", img)
}

fun main(args: Array<String>) {
    val oldImg = args[1] //location of original image
    val newImg = args[3] //location of new image
    val test = ImageIO.read(File(oldImg)) //grab image from specified file "oldImg"

    val repeatVSeam = args[5].toInt() //how many pixels you want to reduce the width by
    val repeatHSeam = args[7].toInt() //how many pixels you want to reduce the heigth by
    val newWidth = test.width - repeatVSeam // find new width
    val newHeight = test.height - repeatHSeam // find new height
    val time = System.currentTimeMillis() // check for time
    repeatShrinkVertical(repeatVSeam, test) // shrink horizontally
    repeatShrinkHorizontal(repeatHSeam, test) // shrink vertically

    println((System.currentTimeMillis() - time) / 1000.0) // print total time it took to console
    val t2 = test.getSubimage(0,0,newWidth, newHeight) // grab the new image from the old one, cutting out all the transparent pixels
    t2.saveImg(newImg) // save it to a new img file
}