import DebugUtil.Ops
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.parallel._
import org.scalatest.concurrent.TimeLimits
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.time.{Seconds, Span}

class CyclicBarrierSuite extends AnyFunSuite with TimeLimits {

  def simpleAccess(cb: CyclicBarrier)(id: Int): IO[Unit] = for {
    _ <- IO(s"(Task-$id) IS ACCESSING ------").debug
    _ <- cb.await(id)
    _ <- IO(s"[Task $id] finished").debug
  } yield ()

  def startInParallel(totalTask: Int, cyclicBarrier: CyclicBarrier): IO[Unit] = {
    (1 to totalTask).toList.map(simpleAccess(cyclicBarrier)).parSequence.void
  }

  test("CyclicBarrier(3) will end becaseu 27 is divisible for 3") {
    failAfter(Span(5, Seconds)) {
      CyclicBarrier(3).flatMap { cb =>
        startInParallel(27, cb)
      }.unsafeRunSync()
    }
  }

  test("This will run forever be cause 27 is not divisible for 5") {
    CyclicBarrier(5).flatMap { cb =>
      startInParallel(27, cb)
    }.unsafeRunSync()
  }
}

