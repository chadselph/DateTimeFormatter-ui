package me.chadrs.datetimes
import be.doeraene.webcomponents.ui5.Bar.ComponentMod
import be.doeraene.webcomponents.ui5.configkeys.{IconName, MessageStripDesign, WrappingType}
import be.doeraene.webcomponents.ui5.{
  ComboBox, DateTimePicker, Icon, Input, Label, MessageStrip, RadioButton, Table
}
import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom
import org.scalajs.dom.HTMLDivElement

import scala.jdk.CollectionConverters.*
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter
import scala.util.Try

@main
def UI(): Unit = {
  lazy val container = dom.document.getElementById("app")
  render(container, Main.App())
}

object Main {
  val IsoDTFormat: String = "yyyy-MM-dd'T'HH:mm:ss"
  val IsoDTFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(IsoDTFormat)

  def RenderedDateTime(dt: Signal[ZonedDateTime], formatter: Signal[Option[DateTimeFormatter]]) = {
    val rendered =
      dt.combineWith(formatter).map((zdt, formatter) => formatter.map(_.format(zdt)).getOrElse(""))
    div(p(child.text <-- rendered, fontSize := "2.5rem", fontFamily := "monospace"))
  }

  def App(): ReactiveHtmlElement[HTMLDivElement] = {
    val pattern: Var[String] = Var(IsoDTFormat + "XXX")
    val dtf = pattern.signal.map(p => Try(DateTimeFormatter.ofPattern(p)))
    val validDtf = dtf.map(_.toOption)
    val (date, inputElement) = ChooseInput(Var(ZonedDateTime.now).writer)

    div(
      PatternTable(date),
      div(inputElement, PatternStringInput(pattern, dtf, validDtf, date), cls := "right-panel"),
      display := "flex"
    )
  }

  def PatternStringInput(
      pattern: Var[String],
      dtf: Signal[Try[DateTimeFormatter]],
      validDtf: Signal[Option[DateTimeFormatter]],
      zdtInput: Signal[ZonedDateTime]
  ): ReactiveHtmlElement[HTMLDivElement] = {
    val patternParseError = dtf.map(_.failed.map(_.getMessage).toOption)
    div(
      "DateTimeFormatter.ofPattern(",
      Input(
        value <-- pattern,
        onInput.mapToValue --> pattern,
        borderColor <-- validDtf.map(f => if f.isDefined then "" else "red")
      ),
      ").format(...)",
      div(
        div(child.maybe <-- patternParseError.map(_.map(parserError))),
        RenderedDateTime(zdtInput, validDtf),
        height := "400px",
        overflow := "break"
      )
    )

  }

  def ChooseInput(
      setZdt: Observer[ZonedDateTime]
  ): (Signal[ZonedDateTime], ReactiveHtmlElement[HTMLDivElement]) =
    val tz = Var(ZoneId.systemDefault())
    val timer = EventStream
      .periodic(1000)
      .toSignal(0)
      .combineWith(tz.signal)
      .map((_, zone) => ZonedDateTime.now(zone))
    val selectedZoneDateTime: Var[LocalDateTime] = Var(LocalDateTime.now)
    val specificTimeSelected: Var[Boolean] = Var(false)
    val outputBus = new EventBus[ZonedDateTime]
    val output = specificTimeSelected.signal
      .flatMap { isSelected =>
        if (isSelected)
          selectedZoneDateTime.signal.combineWith(tz).map((ldt, zone) => ldt.atZone(zone))
        else timer
      }
    val element = div(
      div(
        cls := "inputs",
        timer --> outputBus,
        div(
          RadioButton(
            width := "300px",
            _.text := "Current time",
            _.checked := true,
            _.wrappingType := WrappingType.Normal,
            _.name := "choose-input",
            _.events.onChange.mapToChecked
              .filter(identity)
              .mapTo(false) --> specificTimeSelected
          ),
          Icon(_.name := IconName.`fob-watch`, width := "3rem", height := "3rem")
        ),
        div(
          RadioButton(
            width := "200px",
            _.text := "Specific time",
            _.wrappingType := WrappingType.Normal,
            _.name := "choose-input",
            _.events.onChange.mapToChecked
              .filter(identity)
              .mapTo(true) --> specificTimeSelected
          ),
          JavaDateTimePicker(selectedZoneDateTime, specificTimeSelected.signal.map(!_))
        )
      ),
      div(Label(_.forId := "tzpicker", "In Timezone"), TimeZonePicker(tz))
    )
    (output, element)

  def TimeZonePicker(value: Var[ZoneId]) = ComboBox(
    _.placeholder := "System TZ",
    _.events.onSelectionChange.map(_.detail.item.text).map(ZoneId.of) --> value,
    ZoneId.getAvailableZoneIds.asScala.toList.sorted.map(tz => ComboBox.item(_.text := tz))
  )

  def JavaDateTimePicker(value: Var[LocalDateTime], disabledS: Signal[Boolean]) =
    DateTimePicker(
      _.formatPattern := IsoDTFormat,
      _.value := value.now().format(IsoDTFormatter),
      _.events.onChange
        .map(_.detail.value)
        .map(str => LocalDateTime.parse(str, IsoDTFormatter)) --> value,
      disabled <-- disabledS
    )

  def parserError(error: String) =
    MessageStrip(error, _.design := MessageStripDesign.Negative, _.hideCloseButton := true)

  def PatternTable(now: Signal[ZonedDateTime]): ReactiveHtmlElement[HTMLDivElement] =
    div(
      h2("Patterns Letters and Symbols"),
      Table(
        cls := "pattern-table",
        _.slots.columns := Table.column("Symbol"),
        _.slots.columns := Table.column("Meaning"),
        _.slots.columns := Table.column("Examples", width := "15rem"),
        DateTimeFormatterSymbols.symbols.map(symbol =>
          Table.row(
            _.cell(symbol.symbol),
            _.cell(symbol.meaning),
            _.cell(child.text <-- now.map(symbol.examples))
          )
        )
      )
    )

}
