package caseclass

case class ZeroCC(a: String)

@evolve(ZeroCC)
case class FirstCCSameFile(b: Int)

@evolve(from = ZeroCC)
case class FirstCCSameFileNamed(b: String)