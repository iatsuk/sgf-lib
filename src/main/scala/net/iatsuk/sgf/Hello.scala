package net.iatsuk.sgf

import fastparse.NoWhitespace._
import fastparse._

sealed trait Phrase

case class Word(s: String) extends Phrase

case class Pair(lhs: Phrase, rhs: Phrase) extends Phrase

object Hello extends App {
  def prefix[_: P] = P("hello" | "goodbye").!.map(Word)

  def suffix[_: P] = P("world" | "seattle").!.map(Word)

  def ws[_: P] = P(" ".rep(1))

  def parened[_: P] = P("(" ~ parser ~ ")")

  def parser[_: P]: P[Phrase] = P((parened | prefix) ~ ws ~ (parened | suffix)).map {
    case (lhs, rhs) => Pair(lhs, rhs)
  }

  val res = fastparse.parse("hello (goodbye seattle)", parser(_))
  println(res)
}

