//package com.truefilm
//
//import cats.effect.ExitCode
//import zio._
//import zio.blocking.Blocking
//import zio.clock.Clock
//import zio.console.putStrLn
//
//
//object Main extends App {
//
//  type AppEnvironment = Clock with Blocking with Persistence
//
//
//  override def run(args: List[String]): URIO[ZEnv, zio.ExitCode] = {
//    val program: ZIO[ZEnv, Throwable, Unit] = for {
//
////      fullLayer = Clock.live ++ Blocking.live)
////      program <- server.provideLayer(fullLayer)
//    } yield program
//
//    program.foldM(
//      err => putStrLn(s"Execution failed with: $err") *> IO.succeed(zio.ExitCode(1)),
//      _ => IO.succeed(zio.ExitCode(0))
//    )
//  }
//}
