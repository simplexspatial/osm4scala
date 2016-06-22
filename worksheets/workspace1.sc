def deltaDecompression(compressed: Seq[Long]) = {
  compressed.scanLeft(0L)((accumulate, current) => {
    println(s"Accumulate: $accumulate + Current: $current")
    accumulate + current
  }).drop(1)
}

println(deltaDecompression(Seq(100L, 1L, 2L, 3L)))
assert(deltaDecompression(Seq(100L, 1L, 2L, 3L)) == Seq(100L, 101L, 103L, 106L))

