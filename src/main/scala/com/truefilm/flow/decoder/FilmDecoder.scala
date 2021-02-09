package com.truefilm.flow.decoder

import cats.data.NonEmptyList
import com.truefilm.models.Film
import fs2.data.csv.{CellDecoder, DecoderError, DecoderResult, RowDecoder}
import shapeless._

object FilmDecoder extends RowDecoder[Film] {

  val BUDGET_CELL= 2
  val TITLE_CELL = 8
  val PROD_CELL =12
  val YEAR_CELL = 14
  val REVENUE_CELL = 15
  val RATING_CELL = 22
  def apply(cells: NonEmptyList[String]): DecoderResult[Film] =
    if(cells.size < 23)
      Left(new DecoderError("row is not parsable"))
    else {
      val cellList = cells.toList
      for {
        budget <- if(cellList(BUDGET_CELL).isEmpty) Right(0d) else CellDecoder[Int].apply(cellList(BUDGET_CELL)).map(x => x.toDouble)
        originalTitle <- CellDecoder[String].apply(cellList(TITLE_CELL)) match {
          case Left(value) => Left(new DecoderError(s"Invalid value for Title: ${cellList(TITLE_CELL)}"))
          case Right(value) =>Right(value = value.replaceAll("\\(.*\\)", "").replaceAll("^\\s+", "").replaceAll("\\s+$", ""))
        }
        productionCompany <- CellDecoder[String].apply(cellList(PROD_CELL)) match {
          case Left(value) => Left(new DecoderError(s"Invalid value for Company: ${cellList(TITLE_CELL)}"))
          case Right(value) =>Right(value = value)
        }
        year <- cellList(YEAR_CELL).split("/").lastOption match {
          case Some(value) => Right(if(value.isEmpty) 0 else if(value forall(_.isDigit)) value.toInt else 0)
          case None => Right(0)
        }
        revenue <- if(cellList(REVENUE_CELL).isEmpty) Right(0d) else CellDecoder[Int].apply(cellList(REVENUE_CELL)).map(x => x.toDouble)
        rating <- if(cellList(RATING_CELL).isEmpty) Right(0d) else CellDecoder[Double].apply(cellList(RATING_CELL))
      } yield Film(originalTitle,budget,year,revenue,rating,productionCompany)
    }
}