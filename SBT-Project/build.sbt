name := "ScalaFinal_FacialDetection"

val javacvVersion = "1.1"

val javacppVersion = "1.1"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-optimize", "-Xlint")

// Some dependencies like `javacpp` are packaged with maven-plugin packaging
classpathTypes += "maven-plugin"

// Determine current platform
val platform = {
  // Determine platform name using code similar to javacpp
  // com.googlecode.javacpp.Loader.java line 60-84
  val jvmName = System.getProperty("java.vm.name").toLowerCase
  var osName = System.getProperty("os.name").toLowerCase
  var osArch = System.getProperty("os.arch").toLowerCase
  if (jvmName.startsWith("dalvik") && osName.startsWith("linux")) {
    osName = "android"
  } else if (jvmName.startsWith("robovm") && osName.startsWith("darwin")) {
    osName = "ios"
    osArch = "arm"
  } else if (osName.startsWith("mac os x")) {
    osName = "macosx"
  } else {
    val spaceIndex = osName.indexOf(' ')
    if (spaceIndex > 0) {
      osName = osName.substring(0, spaceIndex)
    }
  }
  if (osArch.equals("i386") || osArch.equals("i486") || osArch.equals("i586") || osArch.equals("i686")) {
    osArch = "x86"
  } else if (osArch.equals("amd64") || osArch.equals("x86-64") || osArch.equals("x64")) {
    osArch = "x86_64"
  } else if (osArch.startsWith("arm")) {
    osArch = "arm"
  }
  val platformName = osName + "-" + osArch
  println("platform: " + platformName)
  platformName
}

libraryDependencies ++= Seq(
  "org.bytedeco" % "javacv" % javacvVersion excludeAll(
    ExclusionRule(organization = "org.bytedeco.javacpp-presets"),
    ExclusionRule(organization = "org.bytedeco.javacpp")
    ),
  "org.bytedeco.javacpp-presets" % "opencv"  % ("3.0.0-" + javacppVersion) classifier "",
  "org.bytedeco.javacpp-presets" % "opencv"  % ("3.0.0-" + javacppVersion),
  "org.bytedeco.javacpp-presets" % "opencv"  % ("3.0.0-" + javacppVersion) classifier "linux-x86",
  "org.bytedeco.javacpp-presets" % "opencv"  % ("3.0.0-" + javacppVersion) classifier "linux-x86_64",
  "org.bytedeco.javacpp-presets" % "opencv"  % ("3.0.0-" + javacppVersion) classifier "macosx-x86_64",
  "org.bytedeco.javacpp-presets" % "opencv"  % ("3.0.0-" + javacppVersion) classifier "windows-x86",
  "org.bytedeco.javacpp-presets" % "opencv"  % ("3.0.0-" + javacppVersion) classifier "windows-x86_64",
  "org.bytedeco.javacpp-presets" % "opencv"  % ("3.0.0-" + javacppVersion) classifier platform,

  "org.bytedeco"                 % "javacpp" % javacppVersion,
  "net.imagej"                   % "ij"              % "1.47v",
  "junit"                        % "junit"           % "4.11" % "test",
  "com.novocode"                 % "junit-interface" % "0.10" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.3.12",

  "com.typesafe.akka" %% "akka-stream-experimental" % "2.0.3",

  "com.typesafe.akka" %% "akka-testkit" % "2.4.14",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.4.14",


  "org.scala-lang.modules" %% "scala-swing" % "2.0.0-M2",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"

)

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  "ImageJ Releases" at "http://maven.imagej.net/content/repositories/releases/",
  // Use local maven repo for local javacv builds
  "Local Maven Repository" at "file:///" + Path.userHome.absolutePath + "/.m2/repository"
)

autoCompilerPlugins := true

// fork a new JVM for 'run' and 'test:run'
fork := true

// add a JVM option to use when forking a JVM for 'run'
javaOptions += "-Xmx1G"

// Set the prompt (for this build) to include the project id.
shellPrompt in ThisBuild := { state => "sbt:" + Project.extract(state).currentRef.project + "> "}

