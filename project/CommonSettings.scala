/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Ángel Cervera Claudio
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

import Dependencies._
import com.jsuereth.sbtpgp.SbtPgp.autoImport.usePgpKeyHex
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys.assembly
import xerial.sbt.Sonatype.autoImport.sonatypePublishToBundle

object CommonSettings {

  lazy val commonSettings = Seq(
    organization := "com.acervera.osm4scala",
    organizationHomepage := Some(url("https://www.acervera.com")),
    licenses += ("MIT", url("https://opensource.org/licenses/MIT")),
    ThisBuild / homepage := Some(
      url(s"https://simplexspatial.github.io/osm4scala/")
    ),
    ThisBuild / scmInfo := Some(
      ScmInfo(
        url("https://github.com/simplexspatial/osm4scala"),
        "scm:git:git://github.com/simplexspatial/osm4scala.git",
        "scm:git:ssh://github.com:simplexspatial/osm4scala.git"
      )
    ),
    ThisBuild / developers := List(
      Developer(
        "angelcervera",
        "Angel Cervera Claudio",
        "angelcervera@silyan.com",
        url("https://www.acervera.com")
      )
    ),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % scalatestVersion % Test,
      "org.scalacheck" %% "scalacheck" % scalacheckVersion % Test,
      "commons-io" % "commons-io" % commonIOVersion % Test
    ),
    assembly / test := {},
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, x)) if x < 12 =>
          Seq(
            "-target:jvm-1.8",
            "-encoding",
            "utf8",
            "-deprecation",
            "-unchecked",
            "-Xlint"
          )
        case _ =>
          Seq(
            "-release:8",
            "-encoding",
            "utf8",
            "-deprecation",
            "-unchecked",
            "-Xlint"
          )
      }
    },
    javacOptions ++= Seq(
      "-Xlint:all",
      "-source",
      "1.8",
      "-target",
      "1.8",
      "-parameters"
    ),
    usePgpKeyHex("A047A2C5A9AFE4850537A00DFC14CE4C2E7B7CBB"),
    publishTo := sonatypePublishToBundle.value
  )
}
