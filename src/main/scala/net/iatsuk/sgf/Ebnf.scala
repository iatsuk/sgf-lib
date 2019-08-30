package net.iatsuk.sgf

import fastparse.MultiLineWhitespace._
import fastparse._

/**
 * <a href="https://www.red-bean.com/sgf/sgf4.html#2">Basic (EBNF) Definition</a>
 * "..." : terminal symbols
 * [...] : option: occurs at most once
 * {...} : repetition: any number of times, including zero
 * (...) : grouping
 * |     : exclusive or
 *
 * <a href="https://www.red-bean.com/sgf/sgf4.html#ebnf-def">EBNF Definition</a>
 * Collection = GameTree { GameTree }
 * GameTree   = "(" Sequence { GameTree } ")"
 * Sequence   = Node { Node }
 * Node       = ";" { Property }
 * Property   = PropIdent PropValue { PropValue }
 * PropIdent  = UcLetter { UcLetter }
 * PropValue  = "[" CValueType "]"
 * CValueType = (ValueType | Compose)
 * ValueType  = (None | Number | Real | Double | Color | SimpleText | Text | Point  | Move | Stone)
 *
 * <a href="https://www.red-bean.com/sgf/sgf4.html#types">Property Value Types</a>
 * Compose    = ValueType ":" ValueType
 * Double     = ("1" | "2")
 * Color      = ("B" | "W")
 * UcLetter   = "A".."Z"
 * Real       = Number ["." Digit { Digit }]
 * Number     = [("+"|"-")] Digit { Digit }
 * Digit      = "0".."9"
 * SimpleText = { any character (handling see below) }
 * Text       = { any character (handling see below) }
 * None       = ""
 * Point      = game-specific
 * Move       = game-specific
 * Stone      = game-specific
 */
sealed trait SgfElem
case class Collection(value: Seq[GameTree]) extends SgfElem
case class GameTree(value: Sequence, tree: Seq[GameTree]) extends SgfElem
case class Sequence(value: Seq[Node]) extends SgfElem
case class Node(value: Seq[Property]) extends SgfElem
case class Property(value: (PropIdent, Seq[PropValue])) extends SgfElem
case class PropIdent(value: String) extends SgfElem
case class PropValue(value: CValueType) extends SgfElem
case class CValueType(value: ValueType) extends SgfElem

sealed trait ValueType extends SgfElem
case class Compose(value1: ValueType, value2: ValueType) extends ValueType
case class Text(value: String) extends ValueType
case class UcLetter(value: Char) extends ValueType
case class Real(value: Double) extends ValueType
case class Number(value: Int) extends ValueType
case class Digit(value: Byte) extends ValueType
case class None() extends ValueType


object Ebnf extends App {
  def collection[_: P]: P[Collection] = P(gameTree.rep(1).map(v => Collection(v)))
  def gameTree[_: P]: P[GameTree] = P(("(" ~ sequence ~ gameTree.rep(0) ~ ")").map(v => GameTree(v._1, v._2)))
  def sequence[_: P]: P[Sequence] = P(node.rep(1).map(v => Sequence(v)))
  def node[_: P]: P[Node] = P((";" ~ property.rep(0)).map(v => Node(v)))
  def property[_: P]: P[Property] = P((propIdent ~ propValue.rep(1)).map(v => Property(v)))
  def propIdent[_: P]: P[PropIdent] = P(letter.rep(1).map(letters => PropIdent(letters.map(_.value).mkString)))
  def propValue[_: P]: P[PropValue] = P(cValueType.map(v => PropValue(v)))
  def cValueType[_: P]: P[CValueType] = P((compose | valueType).map(v => CValueType(v)))
  def compose[_: P]: P[Compose] = P((valueType ~ ":" ~ valueType).map(t => Compose(t._1, t._2)))
  def valueType[_: P]: P[ValueType] = P(real | number | digit | letter | none)

  def letter[_: P]: P[UcLetter] = P(CharIn("A-Z").!.map(s => UcLetter(s.charAt(0))))
  def real[_: P]: P[Real] = P((number ~ "." ~ digit.rep(1)).!.map(s => Real(s.toDouble)))
  def number[_: P]: P[Number] = P((("+" | "-") ~ digit.rep(1)).!.map(s => Number(s.toInt)))
  def digit[_: P]: P[Digit] = P(CharIn("0-9").!.map(s => Digit(s.toByte)))
  def none[_: P]: P[None] = P("".!.map(_ => None()))

  def parser[_: P] = P(collection)

  val res = fastparse.parse("T:+24.153", parser(_))
  println(res)
}

