//package com.truefilm.flow
//
//import java.nio.file.Path
//
//import cats.effect.Blocker
//import com.truefilm.models.Film
//import com.truefilm.sqldb.ClientDB
//import com.truefilm.sqldb.ClientDB.ClientDB
//import com.truefilm.sqldb.CustomTransactor.DBTransactor
//import fs2.data.csv.RowDecoder
//import io.circe.generic.extras.Configuration
//import zio.stream.ZStream
//import zio.stream.ZTransducer.gunzip
//import zio.{Has, RIO, Task, ZLayer}
//
//object Stream {
//
//  type Stream = Has[Stream.Service[Any]]
//
//  trait Service[R] {
//    def start(): RIO[R,Film]
//  }
//
//  val live: ZLayer[ClientDB, Throwable, Stream] =
//    ZLayer.fromService[ClientDB with Configuration,Stream.Service[Any]] {(cli,conf) =>
//      new Service[Any] {
//
//        import zio.interop.catz._
//        import fs2._
//        import fs2.data.csv._
//        def readFirst(path: Path,blocker: Blocker,chunkSize: Int) = for{
//        stream <- fs2.io.file.readAll[Task](path,blocker,chunkSize).through(fs2.compression.gunzip()).through(decode[Task,Film](',',))
//
//
//        }
//
//
//      }
//
//    }
//
//}
