package com.acervera.osm4scala.examples.utilities

/**
  * Utilities to benchmarking
  */
trait Benchmarking {

  /**
    * Utility function to execute a function an calculate the time used.
    *
    * @param block Function to execute.
    * @tparam R Response type.
    * @return
    */
  def time[R](block: => R): (Long,R) = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    ((t1 - t0), result)
  }

}
