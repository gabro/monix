/*
 * Copyright (c) 2014-2021 by The Monix Project Developers.
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

package monix.reactive.consumers

import cats.laws._
import cats.laws.discipline._
import monix.execution.exceptions.DummyException
import monix.reactive.{BaseTestSuite, Consumer, Observable}
import scala.util.Failure

object MapConsumerSuite extends BaseTestSuite {
  test("consumer.map equivalence with task.map") { implicit s =>
    check1 { (obs: Observable[Int]) =>
      val consumer = Consumer.foldLeft[Long, Int](0L)(_ + _)
      val t1 = obs.consumeWith(consumer.map(_ + 100))
      val t2 = obs.consumeWith(consumer).map(_ + 100)
      t1 <-> t2
    }
  }

  test("consumer.map streams error") { implicit s =>
    check2 { (obs: Observable[Int], ex: Throwable) =>
      val withError = obs.endWithError(ex)
      val consumer = Consumer.foldLeft[Long, Int](0L)(_ + _)

      val t1 = withError.consumeWith(consumer.map(_ + 100))
      val t2 = withError.consumeWith(consumer).map(_ + 100)
      t1 <-> t2
    }
  }

  test("consumer.map protects against user code") { implicit s =>
    val ex = DummyException("dummy")
    val f = Observable(1)
      .consumeWith(Consumer.head[Int].map(_ => throw ex))
      .runToFuture

    s.tick()
    assertEquals(f.value, Some(Failure(ex)))
  }
}
