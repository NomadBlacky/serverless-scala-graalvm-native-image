import sbtassembly.Log4j2MergeStrategy
import sbtrelease.Version

enablePlugins(GraalVMNativeImagePlugin)

name := "hello"

resolvers += Resolver.sonatypeRepo("public")
scalaVersion := "2.13.1"
releaseNextVersion := { ver =>
  Version(ver).map(_.bumpMinor.string).getOrElse("Error")
}
assemblyJarName in assembly := "hello.jar"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-events" % "2.2.7",
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
  "com.amazonaws" % "aws-lambda-java-log4j2" % "1.1.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.10.2",
  "org.scalaj" %% "scalaj-http" % "2.4.2"
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfatal-warnings"
)

assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last == "Log4j2Plugins.dat" =>
    Log4j2MergeStrategy.plugincache
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

// https://github.com/scala/bug/issues/11634#issuecomment-581028217
graalVMNativeImageGraalVersion := Some("19.3.1-java11")
graalVMNativeImageOptions ++= Seq(
  "--allow-incomplete-classpath",
  "-H:+ReportExceptionStackTraces",
  "--initialize-at-build-time=scala"
)
