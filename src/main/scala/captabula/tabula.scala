package captabula

import java.io.File
import java.io.InputStream
import java.util.Collections

import scala.jdk.CollectionConverters._

import cats.Id
import cats.data.Reader

import org.apache.pdfbox.pdmodel.PDDocument
import technology.tabula.extractors._
import technology.tabula.Page
import technology.tabula.ObjectExtractor
import technology.tabula.Rectangle

object tabula {

  trait Kernel[F[_], A] {
    def apply[B](a: A)(f: Page => B): F[B]
  }

  implicit def unmarshallerOfPage[F[_], A](implicit k: Kernel[F, A]): A => Unmarshaller[F, Page] = a =>
    new Unmarshaller[F, Page] {
      override def read[B](implicit f: Page => B): F[B] = k(a)(f)
    }

  implicit val kernelForAllPages: Kernel[List, PDDocument] = new Kernel[List, PDDocument] {
    override def apply[B](a: PDDocument)(f: Page => B): List[B] = {
      new ObjectExtractor(a).extract().asScala.map(f).toList
    }
  }

  implicit val kernelForHeadPage: Kernel[Id, PDDocument] = new Kernel[Id, PDDocument] {
    override def apply[B](a: PDDocument)(f: Page => B): Id[B] = {
      f(new ObjectExtractor(a).extract(1))
    }
  }

  implicit def kernelInputStream[F[_]](implicit k: Kernel[F, PDDocument]): Kernel[F, InputStream] = new Kernel[F, InputStream] {
    def apply[B](a: InputStream)(f: Page => B): F[B] = {
      val doc = PDDocument.load(a)
      try k.apply(doc)(f)
      finally doc.close
    }
  }

  implicit def kernelFile[F[_]](implicit k: Kernel[F, PDDocument]): Kernel[F, File] = new Kernel[F, File] {
    def apply[B](a: File)(f: Page => B): F[B] = {
      val doc = PDDocument.load(a)
      try k.apply(doc)(f)
      finally doc.close
    }
  }

  implicit val rectPageCapture: Capture[Rect, Page] = r =>
    Reader { p =>
      val re     = new Rectangle(r.top.floatValue(), r.left.floatValue(), r.width.floatValue(), r.height.floatValue())
      val tables = new BasicExtractionAlgorithm(Collections.emptyList()).extract(p.getArea(re))
      tables.asScala.headOption.map(_.getRows().asScala.map(_.get(0).getText()).mkString).getOrElse("")
    }

  implicit val cellPageCapture: Capture[Cell, Page] = c =>
    Reader { p =>
      val tables = new SpreadsheetExtractionAlgorithm().extract(p)
      // TODO fix no table found.
      // import technology.tabula.detectors.SpreadsheetDetectionAlgorithm
      // val rects  = new SpreadsheetDetectionAlgorithm().detect(p)
      // println(s"------ ${rects.asScala}")
      tables.asScala.headOption.map(_.getCell(c.row, c.column).getText()).getOrElse("")
    }

}
