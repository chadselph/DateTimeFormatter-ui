package me.chadrs.datetimes

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object DateTimeFormatterSymbols {
  case class DtfSymbol(
      symbol: Char,
      meaning: String,
      altSymbol: Option[Char] = None,
      minCount: Int = 1,
      maxCount: Int = 1,
      skipCounts: Set[Int] = Set()
  ) {
    val dtfs: IndexedSeq[DateTimeFormatter] =
      Range
        .inclusive(minCount, maxCount)
        .filterNot(skipCounts)
        .map(count => DateTimeFormatter.ofPattern(symbol.toString * count))

    def examples(zdt: ZonedDateTime): String =
      dtfs.map(dtf => zdt.format(dtf)).toSet.mkString("; ")
  }

  val symbols = List(
    DtfSymbol('G', "era", maxCount = 5),
    DtfSymbol('u', "year", maxCount = 4),
    DtfSymbol('y', "year-of-era", maxCount = 4),
    DtfSymbol('D', "day-of-year"),
    DtfSymbol('M', "month-of-year", Some('L'), maxCount = 5),
    DtfSymbol('d', "day-of-month"),
    // DtfSymbol('g', "modified-julian-day-number"),
    DtfSymbol('Q', "quarter-of-year", Some('q'), maxCount = 5),
    DtfSymbol('Y', "week based year", maxCount = 2),
    DtfSymbol('d', "day-of-month", maxCount = 2),
    DtfSymbol('w', "week-of-week-based-year", maxCount = 2),
    DtfSymbol('W', "week-of-month"),
    DtfSymbol('E', "day-of-week", maxCount = 5),
    DtfSymbol('e', "localized day-of-week", Some('c'), maxCount = 5),
    DtfSymbol('F', "aligned-week-of-month"),
    DtfSymbol('a', "am-pm-of-day"),
    // DtfSymbol('B', "period-of-day"),
    DtfSymbol('h', "clock-hour-of-am-pm (1-12)", maxCount = 2),
    DtfSymbol('K', "clock-hour-of-am-pm (0-11)", maxCount = 2),
    DtfSymbol('k', "clock-hour-of-day (1-24)", maxCount = 2),
    DtfSymbol('H', "hour-of-am-pm (0-23)", maxCount = 2),
    DtfSymbol('m', "minute-of-hour", maxCount = 2),
    DtfSymbol('s', "second-of-minute", maxCount = 2),
    DtfSymbol('S', "fraction-of-second", maxCount = 4),
    DtfSymbol('A', "milli-of-day"),
    DtfSymbol('N', "nano-of-day"),
    DtfSymbol('n', "nano-of-second"),
    DtfSymbol('V', "time-zone ID", minCount = 2, maxCount = 2),
    // DtfSymbol('v', "generic time-zone name"),
    DtfSymbol('z', "time-zone name", maxCount = 4),
    DtfSymbol('n', "nano-of-second"),
    DtfSymbol('O', "localized zone-offset", maxCount = 4, skipCounts = Set(2, 3)),
    DtfSymbol('X', "zone-offset 'Z' for zero", maxCount = 3),
    DtfSymbol('x', "zone-offset", maxCount = 5),
    DtfSymbol('Z', "zone offset", maxCount = 5)
  )
  val test =
    """
      |p	pad next	pad modifier	1
      |	escape for text	delimiter
      |'	single quote	literal
      |[	optional section start
      |]	optional section end
      |#	reserved for future use
      |{	reserved for future use
      |}	reserved for future use		""".stripMargin

}
