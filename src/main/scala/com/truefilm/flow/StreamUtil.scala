package com.truefilm.flow

import cats.effect.Blocker
import zio.Task
import java.nio.file.Path
import com.truefilm.models.Film
import fs2.Chunk
import fs2.data.csv.{Row, noHeaders, rows, skipHeaders}
import zio.interop.catz._

trait StreamUtil {

  def streamFromZippedFile(path: Path, blocker: Blocker,chunkSize: Int) : fs2.Stream[Task,String] = fs2.io.file.readAll[Task](path,blocker,chunkSize).through(fs2.compression.gunzip()).flatMap(x => x.content).through(fs2.text.utf8Decode[Task])

  def streamToRow(stream: fs2.Stream[Task,String],separator: Char,skipHeader:Boolean=true) : fs2.Stream[Task,Row] = stream.flatMap(x => fs2.Stream.chunk(Chunk.array(x.toCharArray))).through(rows[Task](separator)).through(if(skipHeader) skipHeaders[Task] else noHeaders[Task])

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
}
