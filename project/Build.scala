import sbt._
import Keys._

object Build extends Build {

  val buildVersion = "1.3.3"

  val delvingReleases = "Delving Releases Repository" at "http://development.delving.org:8081/nexus/content/repositories/releases"
  val delvingSnapshots = "Delving Snapshot Repository" at "http://development.delving.org:8081/nexus/content/repositories/snapshots"
  val delvingRepository = if(buildVersion.endsWith("SNAPSHOT")) delvingSnapshots else delvingReleases

  val dependencies = Seq(
    "play"                 %%    "play"                        % "2.0.3",
    "eu.delving"           %%    "groovy-templates-plugin"     % "1.5.2",
    "com.novus"            %%    "salat-core"                  % "1.9.1",
    "org.joda"             %     "joda-convert"                % "1.2",
    "commons-collections"  %     "commons-collections"         % "3.2.1",
    "commons-httpclient"   %     "commons-httpclient"          % "3.1",
    "commons-io"           %     "commons-io"                  % "2.1",
    "org.apache.commons"   %     "commons-email"               % "1.2",
    "commons-lang"         %     "commons-lang"                % "2.6"  )

  val main = Project(
    id = "play2-extensions",
    base = file(".")).settings(
      organization := "eu.delving",

      version := buildVersion,

      resolvers +="repo.novus rels" at "http://repo.novus.com/releases/",
      resolvers += "repo.novus snaps" at "http://repo.novus.com/snapshots/",
      resolvers += Resolver.file("local-ivy-repo", file(Path.userHome + "/.ivy2/local"))(Resolver.ivyStylePatterns),
      resolvers += delvingReleases,
      resolvers += delvingSnapshots,

      libraryDependencies ++= dependencies,

      publishTo := Some(delvingRepository),

      credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),

      publishMavenStyle := true
    )

}
