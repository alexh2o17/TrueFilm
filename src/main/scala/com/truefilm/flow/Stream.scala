package com.truefilm.flow

import java.nio.file.Path

import cats.effect.Blocker
import com.truefilm.flow.decoder.FilmDecoder
import com.truefilm.models.Film
import com.truefilm.sqldb.ClientDB.ClientDB
import zio.{Has, IO, RIO, Task, ZIO, ZLayer}
import fs2.data.csv._
import scala.language.higherKinds

object Stream extends StreamUtil {

  type Stream = Has[Stream.Service[Any]]

  trait Service[R] {
    def readTopFilm(path: Path,blocker: Blocker,chunkSize: Int): RIO[R,Map[String,Film]]
  }

  val live: ZLayer[Has[ClientDB], Throwable, Stream] =
    ZLayer.fromService[ClientDB,Stream.Service[Any]] {cli =>
      new Service[Any] {

        def accumulateTop[F[_]]() : fs2.Pipe[F,Film,Map[String,Film]] = {
          _.fold((Map.empty[String,Film],("",-1d))){case ((topFilm,(minKey,minRatio)),film) => defineTopMap(topFilm,minKey,minRatio,film)}.map(_._1)
        }

        def decodeToFilm(): fs2.Pipe[Task,Row,Film] = _.through(attemptDecode[Task,Film](FilmDecoder)).collect {
          case Right(value) => value
        }

        def readTopFilm(path: Path,blocker: Blocker,chunkSize: Int) : IO[Throwable,Map[String,Film]] = {
          import zio.interop.catz._
          for{
         stream <- streamFromFileToRow(path,blocker,chunkSize).through(decodeToFilm()).through(accumulateTop()).compile.last
         topFilm <- ZIO.fromOption(stream).orElseFail(new RuntimeException("Error finding top films"))
        } yield topFilm
        }
      }

    }

}
