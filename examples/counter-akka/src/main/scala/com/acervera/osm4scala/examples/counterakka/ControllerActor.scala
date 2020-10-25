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

import java.io.{File, FileInputStream, InputStream}

import akka.actor.{Actor, ActorRef, Props}
import com.acervera.osm4scala.BlobTupleIterator
import com.acervera.osm4scala.examples.counterakka.ControllerActor.AddActorsMsg
import com.acervera.osm4scala.examples.counterakka.CounterActor.{BlobTupleMsg, CounterResponse}
import com.acervera.osm4scala.model.OSMTypes

import scala.collection.mutable.ListBuffer

object ControllerActor {
  sealed trait ControllerMsg
  case class AddActorsMsg(actors:Int) extends ControllerMsg
}

class ControllerActor(pbfFile: File, filterByOsmType: Option[OSMTypes.Value]) extends Actor {

  val pbfIS:InputStream = new FileInputStream(pbfFile)
  val blobIterator:BlobTupleIterator = BlobTupleIterator.fromPbf(pbfIS)

  val actorPool:ListBuffer[ActorRef] = ListBuffer.empty
  var counter = 0L

  val startTime = System.currentTimeMillis()

  override def postStop(): Unit = pbfIS.close()

  override def receive = {

    // Adding actors to the process.
    case AddActorsMsg(actors) => {

      assert(actors>0) // No less than one :)

      // Create the pool of actors and start the process.
      val originalPoolSize = actorPool.size
      for(idx <- 1 to actors) {
        val newActor = context.actorOf(Props(classOf[CounterActor], filterByOsmType), s"counter_${originalPoolSize+idx}")
        actorPool += newActor
        nextBlob(newActor)
      }

    }

    // Handling the count when the counter finish and process other blob if it is available.
    case CounterResponse(count) => {
      counter += count
      nextBlob(sender())
    }

  }

  /**
    * Sends a new blob to the actor, or stop it if there are not more blobs.
    *
    * @param counterActor
    */
  def nextBlob(counterActor: ActorRef) = blobIterator.hasNext match {
    case true => {
      val nextBlob = blobIterator.next()
      counterActor ! BlobTupleMsg(nextBlob._1, nextBlob._2)
    }
    case false => {
      actorPool -= counterActor
      context.stop(counterActor)

      if(actorPool.isEmpty) {
        println(f"Found [${counter}%,d] primitives of type [${filterByOsmType}] in ${pbfFile.getAbsolutePath} in ${(System.currentTimeMillis() - startTime) / 1e3}%,2.2f sec.")
        context.system.terminate()
      }
    }
  }

}
