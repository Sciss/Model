# Model

[![Build Status](https://github.com/Sciss/Model/workflows/Scala%20CI/badge.svg?branch=main)](https://github.com/Sciss/Model/actions?query=workflow%3A%22Scala+CI%22)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.sciss/model_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.sciss/model_2.13)

## statement

Model is a simple building block for the Scala programming language, providing a typed publisher-observer mechanism. 
It is (C)opyright 2013&ndash;2020 by Hanns Holger Rutz. All rights reserved. This project is released under the
[GNU Lesser General Public License](https://raw.github.com/Sciss/Model/main/LICENSE) and comes with absolutely no 
warranties. To contact the author, send an e-mail to `contact at sciss.de`.

## linking

To link to this library:

    libraryDependencies += "de.sciss" %% "model" % v

The current version `v` is `"0.3.5"`

## building

This project with sbt against Scala 2.13, 2.12, Dotty (JVM) and Scala 2.13 (JS).
The last version to support Scala 2.11 was v0.3.4.

## example

You would declare the mixin of `Model[U]` where `U` is the type of update sent by the model. The actual 
implementation would then most likely use the implementation `impl.ModelImpl`. Observers register with the model 
using `addListener` which takes a partial function. In that respect a model is similar to Scala-Swing's `Reactor`, 
however being specific in the argument type `U`. The `addListener` method returns the partial function for future 
reference, useful in unregistering the observer via `removeListener` (be careful not to use Scala's 
method-to-function conversion for listeners, as repeated calls will produce non-identical partial function 
instances, so `removeListener` would fail).

Typically you declare the update type in the model's companion object as a sealed trait. Here is an example of an 
observed set (included in the test sources):

```scala

    object SetModel {
      sealed trait Update[A]
      case class Added  [A](elem: A) extends Update[A]
      case class Removed[A](elem: A) extends Update[A]

      def empty[A]: SetModel[A] = new Impl[A]

      private class Impl[A] extends SetModel[A] with impl.ModelImpl[Update[A]] {
        private val peer = mutable.Set.empty[A]

        def add(elem: A): Boolean = {
          peer.synchronized {
            val res = peer.add(elem)
            if (res) dispatch(Added(elem))
            res
          }
        }

        def remove(elem: A): Boolean = {
          peer.synchronized {
            val res = peer.remove(elem)
            if (res) dispatch(Removed(elem))
            res
          }
        }
      }
    }
    trait SetModel[A] extends Model[SetModel.Update[A]] {
      def add   (elem: A): Boolean
      def remove(elem: A): Boolean
    }

    val set = SetModel.empty[Int]
    val obs = set.addListener {
      case SetModel.Added  (elem) => println(s"Observed addition of $elem")
      case SetModel.Removed(elem) => println(s"Observed removal  of $elem")
    }
    set.add(1)            // observed
    set.add(2)            // observed
    set.add(1)            // no-op
    set.remove(1)         // observed
    set.remove(3)         // no-op
    set.removeListener(obs)
    assert(set.remove(2)) // unobserved
```

## changes

- 0.3.4 relaxes the synchronization in `ModelImpl`, making it impossible to create dead-locks by
  registering or un-registering listeners during dispatch. Note that `startListening` and `stopListening`
  is still called under synchronization, therefore it must be avoided that sub-classes of `ModelImpl`
  call into anything locking within these two methods. The synchronization was preserved here to
  guarantee a strict sequential operation of `startListening` and `stopListening`.
