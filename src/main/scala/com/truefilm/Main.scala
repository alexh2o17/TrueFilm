package com.truefilm

import java.nio.file.{Path, Paths}
import java.util.Date

import cats.effect.Blocker
import com.truefilm.configuration.Configuration
import com.truefilm.flow.Stream
import com.truefilm.sqldb.{ClientDB, CustomTransactor}
import zio._
import zio.blocking.Blocking
import zio.console.putStrLn
import com.truefilm.flow._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.global

object Main extends App with LazyLogging{

  def getPath(wiki:String,meta:String) : IO[Throwable,(Path,Path)] = for{
    wikiPath <- ZIO.effect(Paths.get("src/main/resources" ++ wiki)).foldM(_ => ZIO.fail(new RuntimeException(s"${wiki} not found in Resources")), x => ZIO.succeed(x))
    metaPath <- ZIO.effect(Paths.get("src/main/resources" ++meta)).foldM(_ => ZIO.fail(new RuntimeException(s"${meta} not found in Resources")), x => ZIO.succeed(x))
  } yield wikiPath -> metaPath

  override def run(args: List[String]): URIO[ZEnv, zio.ExitCode] = {
    val program: ZIO[ZEnv, Throwable, Unit] = for {
        startDate <- ZIO.succeed(new Date())
        _ = logger.info(s"Starting process data on ${startDate}")
        configurationLayer = Configuration.live ++ Blocking.live
        fullLayer = configurationLayer >>> CustomTransactor.transactorLive >>> ClientDB.live >>> Stream.live
        (wikiPath, metaPath) <- getPath("/wiki.xml.gz","/metadata.csv.gz")
        blocker = Blocker.liftExecutionContext(global)
        program <- findAndDBinsert(wikiPath,metaPath,blocker,1000,';').provideLayer(fullLayer)
        endDate <- ZIO.succeed(new Date())
        _ = logger.info(s"Saved $program Films in DB after ${endDate.getTime - startDate.getTime} ms")
        _ = logger.info(s"You can view results in PgAdmin (http://localhost:5050)")
    } yield ()

    program.foldM(
      err => putStrLn(s"Execution failed with: $err") *> IO.succeed(zio.ExitCode(1)),
      _ => IO.succeed(zio.ExitCode(0))
    )
  }
}
