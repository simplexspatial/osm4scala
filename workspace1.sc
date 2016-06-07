import java.io.{FileInputStream, InputStream}

import com.acervera.pbf4scala.PbfFileBlockIterator

var pbfIS: InputStream = null
try {
  pbfIS = new FileInputStream("/home/angelcervera/projects/pbf4scala/src/test/resources/com/acervera/pbf4scala/fileblock/ten_blocks.osm.pbf")
  val blockIterator = PbfFileBlockIterator(pbfIS)

  blockIterator foreach println

} finally {
  if (pbfIS != null) pbfIS.close()
}