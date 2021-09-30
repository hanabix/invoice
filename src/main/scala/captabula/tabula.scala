package captabula

import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.Collections

import scala.jdk.CollectionConverters._

import cats.data._

import org.apache.pdfbox.pdmodel.PDDocument
import technology.tabula._
import technology.tabula.extractors._

object tabula {

  trait PageResource[A] {
    def using(a: A)(f: Page => String): String
  }

  object PageResource {
    def apply[A](implicit ps: PageResource[A]) = ps

    implicit def documentResource = new PageResource[PDDocument] {
      def using(doc: PDDocument)(f: Page => String) = {
        try { f(new ObjectExtractor(doc).extract(1)) }
        finally doc.close()
      }
    }

    implicit def inputStreamResource(implicit ps: PageResource[PDDocument]) = new PageResource[InputStream] {
      def using(a: InputStream)(f: Page => String) = {
        try ps.using(PDDocument.load(a))(f)
        finally a.close()
      }
    }

    implicit def pathResource(implicit ps: PageResource[InputStream]) = new PageResource[Path] {
      def using(a: Path)(f: Page => String): String = ps.using(Files.newInputStream(a))(f)
    }

    implicit def fileResource(implicit ps: PageResource[Path]) = new PageResource[File] {
      def using(a: File)(f: Page => String) = ps.using(a.toPath())(f)
    }

  }

  implicit def extractByRect[A: PageResource]: ReaderFactory[Rect, A] = { case Rect(top, left, width, height) =>
    reader { page =>
      val r      = new Rectangle(top.floatValue(), left.floatValue(), width.floatValue(), height.floatValue())
      val tables = new BasicExtractionAlgorithm(Collections.emptyList()).extract(page.getArea(r))
      tables.asScala.headOption.map(_.getRows().asScala.map(_.get(0).getText()).mkString).getOrElse("")
    }
  }

  implicit def extractBySheet[A: PageResource]: ReaderFactory[Sheet, A] = { case Sheet(row, column) =>
    reader { page =>
      val tables = new SpreadsheetExtractionAlgorithm().extract(page)
      tables.asScala.headOption.map(_.getCell(row, column).getText()).getOrElse("")
    }
  }

  private def reader[A: PageResource](f: Page => String): Reader[A, String] = Reader { a => PageResource[A].using(a)(f) }
}
