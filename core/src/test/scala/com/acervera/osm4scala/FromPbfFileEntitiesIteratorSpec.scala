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

package com.acervera.osm4scala

import java.io.{FileInputStream, FileOutputStream, InputStream, ObjectOutputStream}

import com.acervera.osm4scala.EntityIterator._
import com.acervera.osm4scala.model.{NodeEntity, RelationEntity, WayEntity}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/**
  * Created by angelcervera on 24/07/16.
  */
class FromPbfFileEntitiesIteratorSpec extends AnyWordSpec with Matchers {

  "The FromPbfFileEntitiesIterator should" should {
    "Read X entities" in {
      val testFile = "core/src/test/resources/com/acervera/osm4scala/Madrid.bbbike.osm.pbf"
      var ( nodesCounter, waysCounter, relationsCounter, othersCounter, totalCounter ) = (0,0,0,0,0)
      var pbfIS: InputStream = null
      try {
        pbfIS = new FileInputStream(testFile)
        fromPbf(pbfIS).foreach( x => {
          x match {
            case _ : NodeEntity => nodesCounter += 1
            case _ : RelationEntity => relationsCounter += 1
            case _ : WayEntity => waysCounter += 1
            case _ => othersCounter += 1
          }

          totalCounter += 1
        })

      } finally {
        if (pbfIS != null) pbfIS.close()
      }

      assert(totalCounter == 2677227, "There are 2.677.227 entities in Madrid!")
      assert(nodesCounter == 2328075, "There are 2.328.075 nodes in Madrid!")
      assert(waysCounter == 338795, "There are 338.795 ways in Madrid!")
      assert(relationsCounter == 10357, "There are 10.357 relations in Madrid!")
      assert(othersCounter == 0, "No different type of entities!")

    }

    "Read Entities correctly" in {
      val testFile = "core/src/test/resources/com/acervera/osm4scala/delaware-latest.osm.pbf"
      var pbfIS: InputStream = null
      try {
        pbfIS = new FileInputStream(testFile)
        val readFile: EntityIterator = fromPbf(pbfIS)
        val out = new ObjectOutputStream(new FileOutputStream("core/src/test/resources/com/acervera/osm4scala/delaware-latest.txt"))
        readFile.foreach(x =>
          out.writeObject(x.toString)
        )
        out.close()


      } finally {
        if (pbfIS != null) pbfIS.close()
      }
    }
  }

}
