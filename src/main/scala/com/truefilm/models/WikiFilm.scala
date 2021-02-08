package com.truefilm.models

import com.typesafe.scalalogging.LazyLogging
import fs2.data.xml.XmlEvent

case class WikiFilm(title: String,url:String, filmAbstract :String)


object WikiFilm extends LazyLogging {
  def createTupleByEvents(start: XmlEvent.StartTag, value: XmlEvent.XmlTexty) : (String,String) ={
    start.name.local -> value.render
  }

  def fromMapToWiki(entry: Map[String,String]) : WikiFilm ={
    WikiFilm(title = getEntry(entry,"title"),url =getEntry(entry,"url"),filmAbstract= getEntry(entry,"abstract"))
  }

  def getEntry(entry: Map[String,String], value: String) : String = entry.getOrElse(value, {logger.warn(s"No $value founded");""})
}
