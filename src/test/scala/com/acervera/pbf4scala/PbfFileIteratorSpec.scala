package com.acervera.pbf4scala

import java.io.{FileInputStream, InputStream}

import com.acervera.pbf4scala.model.{NodeEntity, RelationEntity, WayEntity}
import org.scalatest.{Matchers, WordSpec}

/**
  * Created by angelcervera on 24/07/16.
  */
class PbfFileIteratorSpec extends WordSpec with Matchers {

  "The PbfFileIterator should" should {
    "Read X entities" in {
      val testFile = "src/test/resources/com/acervera/pbf4scala/Madrid.bbbike.osm.pbf"
      var ( nodesCounter, waysCounter, relationsCounter, othersCounter, totalCounter ) = (0,0,0,0,0)
      var pbfIS: InputStream = null
      try {
        pbfIS = new FileInputStream(testFile)
        val iter = PbfFileIterator(pbfIS)
        iter.foreach( x => {
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
  }

}
