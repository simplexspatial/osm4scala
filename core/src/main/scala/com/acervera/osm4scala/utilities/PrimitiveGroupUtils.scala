package com.acervera.osm4scala.utilities

import org.openstreetmap.osmosis.osmbinary.osmformat.PrimitiveGroup

object PrimitiveGroupType extends Enumeration {
  type PrimitiveGroupType = Value
  val Relations, Nodes, Ways, ChangeSets, DenseNodes, Unknown = Value
}

/**
  * Utilities to manage primitive groups.
  */
trait PrimitiveGroupUtils {


  import PrimitiveGroupType._

  /**
    * Detect the type of group.
    *
    * @param group PrimitiveGroup object too be analysed.
    * @return Group detected ot Unknown if it is Unknown.
    */
  def detectType(group: PrimitiveGroup) : PrimitiveGroupType = group match {
    case _ if group.relations.nonEmpty => Relations
    case _ if group.nodes.nonEmpty => Nodes
    case _ if group.ways.nonEmpty => Ways
    case _ if group.changesets.nonEmpty => ChangeSets
    case _ if group.dense.isDefined => DenseNodes
    case _ => Unknown
  }
}
