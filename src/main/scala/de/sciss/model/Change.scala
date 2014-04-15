/*
 *  Change.scala
 *  (Model)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.model

/**
 * Value based events fire instances of `Change` which provides the value before
 * and after modification.
 *
 * @param before  the value before the modification
 * @param now     the value after the modification
 */
final case class Change[@specialized +A](before: A, now: A) {
  /**
   * A change is significant if `before != now`
   */
  def isSignificant: Boolean = before != now

  /**
   * Wraps this `Change` in an `Option` depending on its significance.
   *
   * @return  if `isSignificant` is `true`, returns a `Some`, otherwise `None`
   */
  def toOption: Option[Change[A]] = if (isSignificant) Some(this) else None

  /**
   * Converts the change into a `Tuple2` consisting of `_1 = before` and `_2 = now`
   */
  def toTuple: (A, A) = (before, now)

  /**
   * Zips two changes into one change with before and now being tupled.
   */
  def zip[B](that: Change[B]): Change[(A, B)] = Change((before, that.before), (now, that.now))

  /**
   * Unzips a change whose elements are of type `Tuple2` into two separate changes.
   */
  def unzip[A1, A2](implicit asPair: A => (A1, A2)): (Change[A1], Change[A2]) = {
    val (before1, before2) = asPair(before)
    val (now1, now2) = asPair(now)
    Change(before1, now1) -> Change(before2, now2)
  }

  /**
   * Constructs a new change where `before` and `now` are swapped.
   */
  def swap: Change[A] = Change(now, before)

  /**
   * Constructs a new change where `before` and `now` are individually mapped by a function.
   */
  def map[B](fun: A => B): Change[B] = Change(fun(before), fun(now))

  /**
   * Constructs a new change where the tuple `(before, now)` is mapped into a new tuple.
   */
  def mapTuple[B](fun: ((A, A)) => (B, B)): Change[B] = {
    val (newBefore, newNow) = fun(toTuple)
    Change(newBefore, newNow)
  }

  //   def flatMap[ B ]( fun: (A, A) => Change[ B ]) : Change[ B ]
}