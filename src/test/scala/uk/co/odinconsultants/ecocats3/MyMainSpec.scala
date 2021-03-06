package uk.co.odinconsultants.ecocats3

import fs2.Stream
import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.implicits.*

class MyMainSpec extends CatsEffectSuite:

  import MyMain._

  test("Parse HTML") {
    val text = "Random joke"
    val html = "<li><a href=\"/\">" + text + "</a></li>"
    val textAsByteArray: Array[StreamType] = html.toArray.map(_.toByte)
    assertIO({
      val htmlStream: Stream[IO, StreamType] = Stream.fromIterator[IO](textAsByteArray.iterator, 10)
      val result = MyMain.parsing(htmlStream.chunks).compile.toList
      println(s"result = $result")
      result
    }, text.toList.map(_.toByte))
  }


