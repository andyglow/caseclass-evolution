package caseclass

import org.scalatest.matchers.should.Matchers._
import org.scalatest.funsuite.AnyFunSuite


class EvolveSpec extends AnyFunSuite {

  test("compiles. same file") {
    // by name in order
    """FirstCCSameFile(a = "a", b = 12)
      |""".stripMargin should compile

    // by name not in order
    """FirstCCSameFile(b = 12, a = "a")
      |""".stripMargin should compile

    // argument order
    """FirstCCSameFile("a", 12)
      |""".stripMargin should compile
  }

  test("compiles. same file. named") {
    """FirstCCSameFileNamed(a = "a", b = "b")
      |""".stripMargin should compile
  }

  test("compiles. same package reference") {
    """FirstCCSamePkg(a = "a", b = "b")
      |""".stripMargin should compile
  }

  test("compiles. another package. full path") {
    """FirstCCAnotherPkgFullClass(a = "a", b = "b")
      |""".stripMargin should compile
  }

  test("compiles. another package. import") {
    """FirstCCAnotherPkgImport(a = "a", b = "b")
      |""".stripMargin should compile
  }

  test("compiles. members of generic type") {
    """SecondCC(a = "a", o = None, l = Nil, m = Map("foo" -> new java.math.BigDecimal(12)))
      |""".stripMargin should compile
  }

  test("compiles. fields removed") {
    """ThirdCC(a = "a", o = None, l = Nil)
      |""".stripMargin should compile
  }

  test("not compiles. referencing removed removed") {
    """ThirdCC(a = "a", o = None, l = Nil, m = Map("foo" -> new java.math.BigDecimal(12)))
      |""".stripMargin shouldNot compile
  }

  test("compiles. fields renamed") {
    """ForthCC(a = "a", opt = Some("opt"), list = List(1, 2, 3))
      |""".stripMargin should compile
  }

  test("compiles. withSuper") {
    """ForthCC(a = "a", opt = Some("opt"), list = List(1, 2, 3))
      | .withThirdCC(ThirdCC(a = "a", o = None, l = Nil))
      |""".stripMargin should compile
  }

  test("withSuper") {
    val f = ForthCC(a = "a", opt = Some("opt"), list = List(1, 2, 3))
    val r = f.withThirdCC(ThirdCC(a = "aa", o = None, l = Nil))
    r.a shouldBe "aa"
    r.opt shouldBe Symbol("empty")
    r.list shouldBe Symbol("empty")
  }
}
