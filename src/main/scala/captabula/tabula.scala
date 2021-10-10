package captabula

import java.io.File
import java.io.InputStream
import java.util.Collections

import scala.jdk.CollectionConverters._

import cats.data.Reader

import org.apache.pdfbox.pdmodel.PDDocument
import technology.tabula.extractors._
import technology.tabula.Page
import technology.tabula.ObjectExtractor
import technology.tabula.Rectangle

object tabula {

  trait TabulaReader extends DataFormatReader[List, Page] { type Decoder[T] = Reader[Page, T] }

  type Factory[A] = A => TabulaReader

  trait Kernel[A] {
    def apply[B](a: A)(r: Reader[Page, B]): List[B]
  }

  implicit def factory[A](implicit k: Kernel[A]): Factory[A] = a =>
    new TabulaReader {
      override def read[B](implicit f: Decoder[B]): List[B] = k(a)(f)
    }

  implicit val kernelDocument: Kernel[PDDocument] = new Kernel[PDDocument] {
    override def apply[B](a: PDDocument)(r: Reader[Page, B]): List[B] = try {
      new ObjectExtractor(a).extract().asScala.map(r.run).toList
    } finally {
      a.close()
    }
  }

  implicit def kernelInputStream(implicit k: Kernel[PDDocument]): Kernel[InputStream] = new Kernel[InputStream] {
    def apply[B](a: InputStream)(r: Reader[Page, B]): List[B] = try {
      k.apply(PDDocument.load(a))(r)
    } finally {
      a.close
    }
  }

  implicit def kernelFile(implicit k: Kernel[PDDocument]): Kernel[File] = new Kernel[File] {
    def apply[B](a: File)(r: Reader[Page, B]): List[B] = k.apply(PDDocument.load(a))(r)
  }

  implicit val rectPageCapture: Capture[Rect, Reader[Page, String]] = r =>
    Reader { p =>
      val re     = new Rectangle(r.top.floatValue(), r.left.floatValue(), r.width.floatValue(), r.height.floatValue())
      val tables = new BasicExtractionAlgorithm(Collections.emptyList()).extract(p.getArea(re))
      tables.asScala.headOption.map(_.getRows().asScala.map(_.get(0).getText()).mkString).getOrElse("")
    }

  implicit val cellPageCapture: Capture[Cell, Reader[Page, String]] = c =>
    Reader { p =>
      val tables = new SpreadsheetExtractionAlgorithm().extract(p)
      // TODO fix no table found.
      // import technology.tabula.detectors.SpreadsheetDetectionAlgorithm
      // val rects  = new SpreadsheetDetectionAlgorithm().detect(p)
      // println(s"------ ${rects.asScala}")
      tables.asScala.headOption.map(_.getCell(c.row, c.column).getText()).getOrElse("")
    }

}
