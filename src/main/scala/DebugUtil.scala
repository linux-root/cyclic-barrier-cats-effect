import cats.effect.IO

object DebugUtil {
  implicit class Ops[A](val io: IO[A]) extends AnyVal {
    def debug: IO[A] = for {
      v <- io
      _ <- IO.println(s"[${Thread.currentThread().getName}] - $v")
    } yield v
  }
}