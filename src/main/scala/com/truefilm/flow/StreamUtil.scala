package com.truefilm.flow

import cats.effect.{Blocker, ConcurrentEffect}
import zio.{Task, ZIO}
import java.nio.file.Path

import cats.data.NonEmptyList
import com.truefilm.models.{Film, WikiFilm}
import com.typesafe.scalalogging.LazyLogging
import fs2.data.csv.internals.RowParser
import fs2.{Chunk, Pipe, RaiseThrowable}
import fs2.data.csv.{QuoteHandling, Row, rows}
import fs2.data.xml.{XmlEvent, events, namespaceResolver, normalize, referenceResolver, xmlEntities}
import zio.interop.catz._

import scala.util.{Failure, Success, Try}

trait StreamUtil extends LazyLogging{

  def streamFromZippedFile(path: Path, blocker: Blocker,chunkSize: Int) : fs2.Stream[Task,String] = fs2.io.file.readAll[Task](path,blocker,chunkSize).through(fs2.compression.gunzip()).flatMap(x => x.content).through(fs2.text.utf8Decode[Task])
  /** Transforms a stream of raw CSV rows into rows. */
  def noHeaders: Pipe[Task, NonEmptyList[String], Row] =
    _.evalMap[Task,Option[Row]](x => for{
      x <- ZIO.effect(new Row(x)).foldM(er => ZIO.none, x => ZIO.some(x))
    } yield x).unNone

  /** Transforms a stream of raw CSV rows into rows, skipping the first row to ignore the headers. */
  def skipHeaders: Pipe[Task, NonEmptyList[String], Row] =
    _.tail.evalMap[Task,Option[Row]](x => for{
    x <- ZIO.effect(new Row(x)).foldM(er => ZIO.succeed(None), x => ZIO.some(x))
    } yield x).unNone



  def streamToNonEmpty(separator: Char) : fs2.Pipe[Task,String,NonEmptyList[String]] = {
    _.through(fs2.text.lines).map(x => x).map(x => x.split(separator)).map(x => x.toList match {
      case Nil => None
      case ::(head, tl) => Some(NonEmptyList[String](head,tl))
    }).unNone
  }
  def streamToRow(stream: fs2.Stream[Task,String],separator: Char,skipHeader:Boolean=true) : fs2.Stream[Task,Row] = stream.through(streamToNonEmpty(separator)).through(if(skipHeader) skipHeaders else noHeaders)

  def streamToXML(blocker: Blocker)(implicit concurrentEffect: ConcurrentEffect[Task]) : fs2.Pipe[Task,Byte,javax.xml.stream.events.XMLEvent] ={
    import javax.xml.stream.events.XMLEvent
    import xs4s._
    import xs4s.fs2compat._
    import xs4s.syntax.fs2._
    import zio.interop.catz._
    import zio.interop.catz.implicits._

    _.through(byteStreamToXmlEventStream[Task](blocker)).map(x => x)
  }

  def streamToXMLEvents(stream: fs2.Stream[Task,String]) : fs2.Stream[Task,fs2.data.xml.XmlEvent] =
    stream.flatMap(x => fs2.Stream.chunk(Chunk.array(x.toCharArray))).through(events[Task])//.through(namespaceResolver[Task]).through(referenceResolver[Task](xmlEntities)).through(normalize[Task])

  def filterElements(filter: List[String]) : fs2.Pipe[Task,XmlEvent,XmlEvent] = _.map(filterXMLEvents(_,filter)).unNone

  def groupElements : fs2.Pipe[Task,XmlEvent,WikiFilm] = _.zipWithPrevious.map(x =>createWikiFromElements(x._1,x._2)).unNone.chunkN(3,false).map(x => WikiFilm.fromMapToWiki(x.toList.toMap))

  def streamFromFileToRow(path: Path, blocker: Blocker,separator: Char,chunkSize: Int,skipHeader:Boolean=true) : fs2.Stream[Task,Row] = streamToRow(streamFromZippedFile(path,blocker,chunkSize),separator,skipHeader)

  def defineTopMap(topMap: Map[String,Film], minKey:String, minRatio:Double, film: Film,nTop: Int) : (Map[String,Film], (String,Double)) = {
     if(topMap.size < nTop) {
       (topMap + (film.title.toUpperCase -> film) ) -> {if(film.ratio < minRatio || minRatio < 0)(film.title.toUpperCase -> film.ratio) else (minKey -> minRatio)}
     } else {
       if(film.ratio > minRatio) {
         val newTop = topMap - minKey + (film.title.toUpperCase -> film)
         val newMin = newTop.minBy(x => x._2.ratio)
         newTop ->  (newMin._1 -> newMin._2.ratio)
       } else {
         topMap -> (minKey -> minRatio)
       }
     }
  }

  def createWikiFromElements(start: Option[XmlEvent],value: XmlEvent) : Option[(String,String)] = start match {
      case Some(first) => if(first.isInstanceOf[XmlEvent.StartTag] && value.isInstanceOf[XmlEvent.XmlString]) Some(WikiFilm.createTupleByEvents(first.asInstanceOf[XmlEvent.StartTag], value.asInstanceOf[XmlEvent.XmlString])) else None
      case None => None
    }


  def filterXMLEvents(event: XmlEvent,filter: List[String]) : Option[XmlEvent] = {
    event match {
      case texty: XmlEvent.XmlTexty => Some(texty)
      case start: XmlEvent.StartTag => if(filter.contains(start.name.local)) Some(start) else None
      case _ => None
    }

  }
}
