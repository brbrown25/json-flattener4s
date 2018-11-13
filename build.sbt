import Dependencies._

lazy val core = (project in file("./core")).
  settings(
    inThisBuild(List(
      organization := "com.bbrownsound",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "json-flattener4s-core",
    libraryDependencies ++= Seq(
      jsonFlatten,
      betterFiles,
      scalaTest % Test
    )
  )

//lazy val scalaz = (project in file("./scalaz")).
//  settings(
//    inThisBuild(List(
//      organization := "com.bbrownsound",
//      scalaVersion := "2.12.7",
//      version      := "0.1.0-SNAPSHOT"
//    )),
//    name := "json-flattener4s-scalaz",
//    libraryDependencies ++= Seq(
//      scalaZ
//    )
//  ).dependsOn(core)
// Add the default sonatype repository setting
publishTo := sonatypePublishTo.value

// latest sbt-gpg plugin needs to know these explicitly
pgpSecretRing := file("/Users/brown/.sbt/gpg/secring.asc")

pgpPublicRing := file("/Users/brown/.sbt/gpg/pubring.asc")

// needed to publish to maven central
publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/brbrown25/json-flattener4s</url>
  <licenses>
    <license>
      <name>Apache License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <connection>scm:git:git@github.com:brbrown25/json-flattener4s.git</connection>
    <developerConnection>scm:git:git@github.com:brbrown25/json-flattener4s.git</developerConnection>
    <url>git@github.com:brbrown25/json-flattener4s.git</url>
  </scm>
  <developers>
    <developer>
      <id>bbrownsound</id>
      <name>Brandon Brown</name>
      <email>brandon@bbrownsound.com</email>
      <timezone>UTC</timezone>
    </developer>
  </developers>)

