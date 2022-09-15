import DebugUtil.Ops
import cats.effect.{Deferred, IO, Ref}
import cats.implicits.catsSyntaxParallelTraverse1

trait CyclicBarrier {
  def await(id: Int): IO[Unit]
}

object CyclicBarrier {
  type Signal = Deferred[IO, Unit]

  val createNewSignal: IO[Signal] = IO.deferred[Unit]

  def createInitialState(count: Int): IO[Ref[IO, State]] = for {
    signal <- createNewSignal
    ref <- IO.ref(State(Nil))
  } yield ref

  case class State(signals: List[Signal]) {
    def addedSignal(s: Signal): State = this.copy(signals = s :: this.signals)
  }

  def apply(count: Int): IO[CyclicBarrier] = for {
    initialState <- createInitialState(count)
  } yield {
    new CyclicBarrier {
      override def await(id: Int): IO[Unit] = createNewSignal.flatMap { newSignal =>
        initialState.modify {
          case State(signals) if signals.size == count - 1 =>
            State(Nil) -> (IO(s"[Task $id] release defer c=${signals.size}").debug >> signals.parTraverse(_.complete()).void)
          case state =>
            state.addedSignal(newSignal) -> (IO(s"[Task $id] block").debug >> newSignal.get)
        }.flatten
      }
    }
  }
}