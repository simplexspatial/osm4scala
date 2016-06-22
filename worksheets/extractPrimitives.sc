import com.acervera.pbf4scala.utils.PrimitivesExtractor._

//val pbfFile = "/home/angelcervera/projects/pbf4scala/src/test/resources/com/acervera/pbf4scala/osmblock/ways_block_0.osm.pbf"
//val pbfFile = "/home/angelcervera/projects/osm/andorra-latest.osm.pbf"
// val pbfFile="/home/angelcervera/projects/pbf4scala/src/test/resources/com/acervera/pbf4scala/fileblock/ten_blocks.osm.pbf"
val pbfFile = "/home/angelcervera/projects/osm/spain-latest.osm.pbf"
val extractRootFolder = "/home/angelcervera/projects/osm/primitives/spain"
fromPbf(pbfFile, extractRootFolder)