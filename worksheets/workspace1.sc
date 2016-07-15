import java.text.DecimalFormat

//def deltaDecompression(compressed: Seq[Long]) = {
//  compressed.scanLeft(0L)((accumulate, current) => {
//    println(s"Accumulate: $accumulate + Current: $current")
//    accumulate + current
//  }).drop(1)
//}
//
//println(deltaDecompression(Seq(100L, 1L, 2L, 3L)))
//assert(deltaDecompression(Seq(100L, 1L, 2L, 3L)) == Seq(100L, 101L, 103L, 106L))
//
//


val s = Seq(1l,2l,0l,3l,4l,5l)
val iter = s.toIterator

val tags1 = iter takeWhile( _ != 0l)
tags1.size

tags1.grouped(2).map({(tag) => println(s"$tag")})

val tags2 = iter takeWhile( _ != 0l)
tags2.size


println()

var strDouble = new DecimalFormat("#.######").format(20.123456789123456789d)

println(strDouble)
println()

