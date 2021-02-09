package com.truefilm.models

import com.typesafe.scalalogging.LazyLogging
import fs2.data.xml.XmlEvent

case class WikiFilm(title: String,url:String, filmAbstract :String)


object WikiFilm extends LazyLogging {
  def createTupleByEvents(start: XmlEvent.StartTag, value: XmlEvent.XmlTexty) : (String,String) ={
    start.name.local -> value.render
  }

  def fromMapToWiki(entry: Map[String,String]) : WikiFilm ={
    WikiFilm(title = cleanTitle(getEntry(entry,"title")).toUpperCase,url =getEntry(entry,"url"),filmAbstract= getEntry(entry,"abstract"))
  }

  def cleanTitle(string: String) : String = string.replace("Wikipedia: ","").replaceAll("\\(.*\\)", "").replaceAll("^\\s+", "").replaceAll("\\s+$", "")

  def getEntry(entry: Map[String,String], value: String) : String = entry.getOrElse(value, {logger.warn(s"No $value founded in list ${entry}");""})
}
