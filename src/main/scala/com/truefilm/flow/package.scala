package com.truefilm

import java.nio.file.Path

import cats.effect.Blocker
import com.truefilm.flow.Stream.Stream
import com.truefilm.models.Film
import zio.RIO

package object flow  extends Stream.Service[Stream] {

  def readTopFilm(path: Path,blocker: Blocker,nTop: Int = 1000,separator: Char,chunkSize: Int = 1024 * 32): RIO[Stream,Map[String,Film]] = RIO.accessM(_.get.readTopFilm(path, blocker,nTop,separator, chunkSize))

}
