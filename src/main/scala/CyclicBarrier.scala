import DebugUtil.Ops
import cats.effect.{Deferred, IO, Ref}

trait CyclicBarrier {
  def await(id: Int): IO[Unit]
}

object CyclicBarrier {
  type Signal = Deferred[IO, Unit]

  val createNewSignal: IO[Signal] = IO.deferred[Unit]

  private def createInitialState(count: Int): IO[Ref[IO, State]] = for {
    signal <- createNewSignal
    ref <- IO.ref(State(count, signal))
  } yield ref

  private case class State(count: Int, signal: Signal) {
    def countDown: State = this.copy(count = this.count - 1)
  }

  def apply(count: Int): IO[CyclicBarrier] = for {
    initialState <- createInitialState(count)
  } yield {
    new CyclicBarrier {
      override def await(id: Int): IO[Unit] = createNewSignal.flatMap { newSignal =>
        initialState.modify {
          case State(1, signal) =>
            State(count, newSignal) -> (IO(s"[Task $id] release ${signal.hashCode()}. Newsignal ${newSignal.hashCode()}").debug >> signal.complete()).void
          case state =>
            state.countDown -> (IO(s"[Task $id] block by ${state.signal.hashCode()}").debug >> state.signal.get)
        }.flatten
      }
    }
  }
}