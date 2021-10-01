import scala.util.matching.UnanchoredRegex
import cats.data.Kleisli
import java.nio.file.Path
import java.io.File
import java.io.InputStream
import java.nio.file.Files

package object captabula {
  implicit class UnanchoredRegexString(val sc: StringContext) extends AnyVal {
    def regex: UnanchoredRegex = sc.parts.mkString.r.unanchored
  }

  trait Capture {
    def rect(top: Number, left: Number, width: Number, height: Number): String
    def sheet(row: Int, column: Int): String
  }

  type Transform[A, B] = Kleisli[List, A, B]

  type Indexer[A] = List[Int] => Transform[A, Capture]
  object Indexer {
    def apply[A](implicit i: Indexer[A]): Indexer[A] = i
  }

  implicit def fileIndexer(implicit i: Indexer[Path]) = new Indexer[File] {
    def apply(indices: List[Int]) = {
      i.apply(indices).compose(f => List(f.toPath()))
    }
  }

  implicit def pathIndexer(implicit i: Indexer[InputStream]) = new Indexer[Path] {
    def apply(indices: List[Int]) = {
      i.apply(indices).compose(p => List(Files.newInputStream(p)))
    }
  }

  object dsl {
    def load[A: Indexer](indices: Int*): Transform[A, Capture] = Indexer[A].apply(indices.toList)
  }

}
