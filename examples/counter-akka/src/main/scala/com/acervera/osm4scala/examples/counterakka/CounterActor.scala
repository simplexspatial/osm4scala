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
