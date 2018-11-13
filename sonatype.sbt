sonatypeProfileName := "com.bbrownsound"

publishMavenStyle := true

// License of your choice
licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

// Where is the source code hosted
import xerial.sbt.Sonatype._

sonatypeProjectHosting := Some(GitHubHosting("brbrown25", "json-flattener4s", "brandon@bbrownsound.com"))
// or
sonatypeProjectHosting := Some(GitLabHosting("brbrown25", "json-flattener4s", "brandon@bbrownsound.com"))

// or if you want to set these fields manually
homepage := Some(url("https://(your project url)"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/brbrown25/json-flattener4s"),
    "scm:git:git@github.com:brbrown25/json-flattener4s.git"
  )
)

developers := List(Developer(
  id = "com.bbrownsound",
  name = "Brandon Brown",
  email = "brandon@bbrownsound.com",
  url = url("https://brbrown25.github.io/")))