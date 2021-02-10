package com.truefilm.flow

import cats.effect.{Blocker, ConcurrentEffect}
import zio.{Task, ZIO}
import java.nio.file.Path

import cats.data.NonEmptyList
import com.truefilm.models.{Film, WikiFilm}
import com.typesafe.scalalogging.LazyLogging
import fs2.data.csv.internals.RowParser
import fs2.{Chunk, Pipe, RaiseThrowable}
import fs2.data.csv.{QuoteHandling, Row, noHeaders, rows, skipHeaders}
import fs2.data.xml.{XmlEvent, events, namespaceResolver, normalize, referenceResolver, xmlEntities}
import zio.interop.catz._

import scala.util.{Failure, Success, Try}

trait StreamUtil extends LazyLogging{

  def streamFromZippedFile(path: Path, blocker: Blocker,chunkSize: Int) : fs2.Stream[Task,String] = fs2.io.file.readAll[Task](path,blocker,chunkSize).through(fs2.compression.gunzip()).flatMap(x => x.content).through(fs2.text.utf8Decode[Task])


  def streamToNonEmpty(separator: Char) : fs2.Pipe[Task,String,NonEmptyList[String]] = {
    _.through(fs2.text.lines).map(x => x.split(separator)).map(x => x.toList match {
      case Nil => None
      case ::(head, tl) => Some(NonEmptyList[String](head,tl))
    }).unNone
  }
  def streamToRow(separator: Char,skipHeader:Boolean=true) : fs2.Pipe[Task,String,Row] = _.through(streamToNonEmpty(separator)).through(if(skipHeader) skipHeaders else noHeaders)

  def streamToXMLEvents : fs2.Pipe[Task,String,fs2.data.xml.XmlEvent] =
    _.flatMap(x => fs2.Stream.chunk(Chunk.array(x.toCharArray))).through(events[Task])//.through(namespaceResolver[Task]).through(referenceResolver[Task](xmlEntities)).through(normalize[Task])

  def filterElements(filter: List[String]) : fs2.Pipe[Task,XmlEvent,XmlEvent] = _.map(filterXMLEvents(_,filter)).unNone

  def groupElements : fs2.Pipe[Task,XmlEvent,WikiFilm] = _.zipWithPrevious.map(x =>createWikiFromElements(x._1,x._2)).unNone.chunkN(3,false).map(x => WikiFilm.fromMapToWiki(x.toList.toMap))

  def streamFromFileToRow(path: Path, blocker: Blocker,separator: Char,chunkSize: Int,skipHeader:Boolean=true) : fs2.Stream[Task,Row] = streamFromZippedFile(path,blocker,chunkSize).through(streamToRow(separator,skipHeader))

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
