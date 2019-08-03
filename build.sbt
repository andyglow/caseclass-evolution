val commons = Seq(
  version := "0.1",
  scalaVersion := "2.11.12",
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.8" % Test))

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

lazy val core = (project in file("core"))
  .settings(
    commons,
    name := "caseclass-core",
    scalacOptions ++= Seq(
      "-encoding", "utf8",
      "-language:experimental.macros"),
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value)
  )

lazy val example = (project in file("example"))
  .dependsOn(core)
  .settings(
    commons,
    name := "caseclass-example"
  )

lazy val root = (project in file("."))
  .aggregate(core, example)
  .settings(
    commons,
    name := "caseclass-root"
  )

