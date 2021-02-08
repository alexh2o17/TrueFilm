package com.truefilm.flow

import cats.effect.Blocker
import zio.Task
import java.nio.file.Path

import com.truefilm.models.{Film, WikiFilm}
import fs2.Chunk
import fs2.data.csv.{Row, noHeaders, rows, skipHeaders}
import fs2.data.xml.{XmlEvent, events, namespaceResolver, normalize, referenceResolver, xmlEntities}
import zio.interop.catz._

trait StreamUtil {

  def streamFromZippedFile(path: Path, blocker: Blocker,chunkSize: Int) : fs2.Stream[Task,String] = fs2.io.file.readAll[Task](path,blocker,chunkSize).through(fs2.compression.gunzip()).flatMap(x => x.content).through(fs2.text.utf8Decode[Task])

  def streamToRow(stream: fs2.Stream[Task,String],separator: Char,skipHeader:Boolean=true) : fs2.Stream[Task,Row] = stream.flatMap(x => fs2.Stream.chunk(Chunk.array(x.toCharArray))).through(rows[Task](separator)).through(if(skipHeader) skipHeaders[Task] else noHeaders[Task])

  def streamToXMLEvents(stream: fs2.Stream[Task,String]) : fs2.Stream[Task,fs2.data.xml.XmlEvent] =
    stream.flatMap(x => fs2.Stream.chunk(Chunk.array(x.toCharArray))).through(events[Task]).through(namespaceResolver[Task]).through(referenceResolver[Task](xmlEntities)).through(normalize[Task])

  def filterElements(filter: List[String]) : fs2.Pipe[Task,XmlEvent,XmlEvent] = _.map(filterXMLEvents(_,filter)).unNone

  def groupElements() : fs2.Pipe[Task,XmlEvent,WikiFilm] = _.zipWithPrevious.map(x =>createWikiFromElements(x._1,x._2)).unNone.chunkN(3,false).map(x => WikiFilm.fromMapToWiki(x.toList.toMap))

  def streamFromFileToRow(path: Path, blocker: Blocker,separator: Char,chunkSize: Int,skipHeader:Boolean=true) : fs2.Stream[Task,Row] = streamToRow(streamFromZippedFile(path,blocker,chunkSize),separator,skipHeader)

  def defineTopMap(topMap: Map[String,Film], minKey:String, minRatio:Double, film: Film,nTop: Int) : (Map[String,Film], (String,Double)) = {
     if(topMap.size < nTop) {
       (topMap + (film.title -> film) ) -> {if(film.ratio < minRatio || minRatio < 0)(film.title -> film.ratio) else (minKey -> minRatio)}
     } else {
       if(film.ratio > minRatio) {
         val newTop = topMap - minKey + (film.title -> film)
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
