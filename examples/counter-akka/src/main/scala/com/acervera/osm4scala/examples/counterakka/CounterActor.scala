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

package com.acervera.osm4scala.examples.counterakka

import akka.actor.Actor
import com.acervera.osm4scala.EntityIterator.fromBlob
import com.acervera.osm4scala.examples.counterakka.CounterActor.{BlobTupleMsg, CounterResponse}
import com.acervera.osm4scala.model.OSMTypes
import org.openstreetmap.osmosis.osmbinary.fileformat.{Blob, BlobHeader}

object CounterActor {
  sealed trait CounterMsg
  case class BlobTupleMsg(header: BlobHeader, blob: Blob) extends CounterMsg
  case class CounterResponse(counter:Long) extends CounterMsg
}

class CounterActor(filterType: Option[OSMTypes.Value]) extends Actor {

  val count:Blob => Int = filterType match {
    case None => countWithoutFilter
    case _ => countWithFilter
  }

  def countWithFilter(blob: Blob): Int = {
    fromBlob(blob).count(_.osmModel == filterType.get)
  }

  def countWithoutFilter(blob: Blob): Int = {
    fromBlob(blob).length
  }

  override def receive = {
    case BlobTupleMsg(header, blob) =>
      sender ! CounterResponse(count(blob))
  }

}
