package caseclass

case class ZeroCC(a: String)

@Evolve(ZeroCC)
case class FirstCCSameFile(b: String)

@Evolve(from = ZeroCC)
case class FirstCCSameFileNamed(b: String)