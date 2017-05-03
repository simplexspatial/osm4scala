/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Ángel Cervera Claudio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.acervera.osm4scala.model

import org.openstreetmap.osmosis.osmbinary.osmformat.{StringTable, Way}

/**
  * Created by angelcervera on 14/06/16.
  */
case class WayEntity(val id: Long, val nodes: Seq[Long], val tags: Map[String, String]) extends OSMEntity {

  override val osmModel: OSMTypes.Value = OSMTypes.Way

  object WayEntityTypes extends Enumeration { // TODO: How to know the type ?????
    val Open, Close, Area, CombinedClosedPolylineArea = Value
  }

}

object WayEntity {

  def apply(osmosisStringTable: StringTable, osmosisWay: Way) = {

    // Calculate nodes references in stored in delta compression.
    val nodes = osmosisWay.refs.scanLeft(0l) { _ + _ }.drop(1)

    // Calculate tags using the StringTable.
    val tags = (osmosisWay.keys, osmosisWay.vals).zipped.map { (k, v) => osmosisStringTable.s(k).toString("UTF-8") -> osmosisStringTable.s(v).toString("UTF-8") }.toMap


    new WayEntity(osmosisWay.id, nodes, tags)
  }

}
