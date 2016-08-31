/*
 * Copyright (c) 2014-2016 by its authors. Some rights reserved.
 * See the project homepage at: https://monix.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package monix.cats.tests

import cats.Eval
import minitest.SimpleTestSuite
import monix.types._
import monix.cats.reverse._
import scala.util.Try

object CatsToMonixSuite extends SimpleTestSuite with cats.instances.AllInstances {
  test("functor") {
    def test[F[_]](x: F[Int])(implicit F: Functor[F]): F[Int] =
      F.map(x)(_ + 1)

    assertEquals(test(Eval.always(1)).value, 2)
  }

  test("applicative") {
    def test[F[_]](x: F[Int => Int])(implicit F: Applicative[F]): F[Int] =
      F.ap(x)(F.pure(1))

    assertEquals(test(Eval.always((x: Int) => x + 1)).value, 2)
  }

  test("recoverable") {
    def test[F[_]](x: F[Int])(implicit F: Recoverable[F,Throwable]): F[Int] =
      F.onErrorHandle(x)(_ => 2)

    val ref = Try[Int](throw new RuntimeException)
    assertEquals(test(ref).get, 2)
  }

  test("monad") {
    def test[F[_]](x: F[Int])(implicit M: Monad[F], A: Applicative[F]): F[Int] =
      M.flatMap(x)(r => A.pure(r + 1))

    assertEquals(test(Eval.always(1)).value, 2)
  }

  test("coflatMap") {
    def test[F[_]](x: F[Int])(implicit F: CoflatMap[F]): F[Int] =
      F.coflatMap(x)(x => 2)

    assertEquals(test(Eval.always(1)).value, 2)
  }

  test("comonad") {
    def test[F[_]](x: F[Int])(implicit F: Comonad[F]): Int =
      F.extract(x)

    assertEquals(test(Eval.always(1)), 1)
  }

  test("monadFilter") {
    def test[F[_]](x: F[Int])(implicit M: MonadFilter[F]): F[Int] =
      M.filter(x)(_ % 2 == 0)

    val list = (0 until 100).toList
    assertEquals(test(list).sum, list.filter(_ % 2 == 0).sum)
  }

  test("semigroupK") {
    val ev = implicitly[SemigroupK[List]]
    assert(ev != null)
  }

  test("monoidK") {
    val ev = implicitly[MonoidK[List]]
    assert(ev != null)
  }

  test("monadRec") {
    val ev = implicitly[MonadRec[List]]
    assert(ev != null)
  }
}