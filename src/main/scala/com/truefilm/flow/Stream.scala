package com.truefilm.flow

import java.nio.file.Path

import cats.effect.Blocker
import com.truefilm.flow.decoder.FilmDecoder
import com.truefilm.models.{Film, WikiFilm}
import com.truefilm.sqldb.ClientDB.{ClientDB, ClientImp}
import com.typesafe.scalalogging.LazyLogging
import zio.{Has, IO, RIO, Task, ZIO, ZLayer}
import fs2.data.csv._

import scala.language.higherKinds

object Stream extends StreamUtil with LazyLogging{

  type Stream = Has[Stream.Service[Any]]

  trait Service[R] {
    def readTopFilm(path: Path,blocker: Blocker,nTop:Int,separator: Char,chunkSize: Int = 1024 * 32): RIO[R,Map[String,Film]]
    def findAndAggregateTopFilm(wikiPath: Path,imdbPath:Path,blocker: Blocker,nTop:Int,separator: Char,chunkSize: Int = 1024 * 32) : RIO[R,List[Film]]
    def findAndDBinsert(wikiPath: Path,imdbPath:Path,blocker: Blocker,nTop:Int,separator: Char,chunkSize: Int = 1024 * 32) : RIO[R,Int]
  }

  val live: ZLayer[ClientDB, Throwable, Stream] =
    ZLayer.fromService[ClientImp,Stream.Service[Any]] { cli =>
      new Service[Any] {

        def accumulateTop[F[_]](nTop:Int) : fs2.Pipe[F,Film,Map[String,Film]] = {
          _.fold((Map.empty[String,Film],("",-1d))){case ((topFilm,(minKey,minRatio)),film) => defineTopMap(topFilm,minKey,minRatio,film,nTop)}.map(_._1)
        }

        def decodeToFilm(): fs2.Pipe[Task,Row,Film] = _.through(attemptDecode[Task,Film](FilmDecoder)).collect {
          case Right(value) => value
        }

        def completeFilm(film: Map[String,Film]) : fs2.Pipe[Task,WikiFilm,List[Film]] =
          _.fold(film){case (film,wiki) => film.get(wiki.title) match {
            case Some(value) => film.updated(wiki.title,value = value.copy(wikiLink = Some(wiki.url),wikiAbstract = Some(wiki.filmAbstract)))
            case None =>film
          }}.map(_.values.toList)

        def readTopFilm(path: Path,blocker: Blocker,nTop:Int,separator: Char,chunkSize: Int = 1024 * 32) : IO[Throwable,Map[String,Film]] = {
          import zio.interop.catz._
          for{
         stream <- streamFromFileToRow(path,blocker,separator,chunkSize).through(decodeToFilm()).through(accumulateTop(nTop)).compile.last
         topFilm <- ZIO.fromOption(stream).orElseFail(new RuntimeException("Error finding top films"))
        } yield topFilm
        }

        def findAndAggregateTopFilm(wikiPath: Path,imdbPath:Path,blocker: Blocker,nTop:Int,separator: Char,chunkSize: Int = 1024 * 32) : IO[Throwable,List[Film]] = {
          import zio.interop.catz._
          for{
            topFilm <- readTopFilm(imdbPath,blocker,nTop,separator,chunkSize)
            completeTop <- streamToXMLEvents(streamFromZippedFile(wikiPath,blocker,chunkSize)).through(filterElements(List("title","url","abstract"))).through(groupElements()).through(completeFilm(topFilm)).compile.last
            getList <- ZIO.fromOption(completeTop).orElseFail(new RuntimeException("Error finding  films metadata"))
          } yield getList
        }

        def findAndDBinsert(wikiPath: Path,imdbPath:Path,blocker: Blocker,nTop:Int,separator: Char,chunkSize: Int = 1024 * 32) : IO[Throwable,Int] = {
          import zio.interop.catz._
          for{
            topFilm <- findAndAggregateTopFilm(wikiPath, imdbPath, blocker, nTop, separator, chunkSize)
            inserted <- ZIO.foreach(topFilm)(cli.create)
          } yield inserted.size
        }

      }

    }

}
