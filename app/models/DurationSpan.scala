package models

object DurationSpan extends Enumeration {
    val Week = Value("week")
    val Month = Value("month")
    val Year = Value("year")
    type DurationSpan = Value
}
