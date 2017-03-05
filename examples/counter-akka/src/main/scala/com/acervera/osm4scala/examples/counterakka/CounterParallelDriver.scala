package com.acervera.osm4scala.examples.counterakka

import akka.actor.{ActorSystem, Props}
import com.acervera.osm4scala.examples.counterakka.ParametersConfig._

/**
  * Example that count the number of primitives in the pbf file using AKKA.
  *
  */
object CounterParallelDriver extends App {

  // Check arguments before to start.
  val config = parser.parse(args, Config()).get

  // Create and start the controller actor.
  val system = ActorSystem("pbf_counter_controller")
  system.actorOf(Props(classOf[ControllerActor], config.input, config.osmType), "controller") ! ControllerActor.AddActorsMsg(config.actors)

}
