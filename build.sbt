ThisBuild / scalaVersion := "2.13.8"

ThisBuild / version := "1.0"

lazy val root = (project in file("."))
  .settings(
    name := """Cyclic-barrier-cats-effect""",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.3.14",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
    )
  )