name := "CatsEffect3"

version := "0.1"

scalaVersion := "2.13.5"

libraryDependencies += "org.typelevel" %% "cats-effect" % "3.0.1"

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps"
)