package uk.co.odinconsultants.ecocats3
import munit.CatsEffectSuite
class MyRefStreamSpec extends CatsEffectSuite {
  import MyRefStream._

  test("state over stream") {
    assertIO(
    stateStream.compile.toList,
      List(false, true, false))
  }

}
