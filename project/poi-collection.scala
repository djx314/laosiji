import sbt._
import Keys._
import cn.gov.heshan.sbt.CustomSettings
import com.typesafe.sbt.SbtGit._

object `poi-collection` extends Build {
  
  val initPrintln = """
"""
  println(initPrintln)

  lazy val `poi-collection` = (project in file("."))
  //common settings
  .settings(CustomSettings.customSettings: _*)
  .settings(
    name := "laosiji",
    version := "0.0.1",
    libraryDependencies ++= Seq(
    )
  )

}
