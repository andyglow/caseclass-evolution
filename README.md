Scala Case Class Evolution
--------------------------

EXPERIMENTAL
============

With Scala developer can't extends case classes. 

That's an axiom.

But sometimes, especially if you, like me, work with big data, you really want scala could do that.
Otherwise you stuck with doing boilerplate like this.

```scala
case class User(
  id: String,
  firstName: String,
  middleName: Option[String],
  lastName: String,
  ... // ton of properties 
)

case class BankUser(
  id: String,
  firstName: String,
  middleName: Option[String],
  lastName: String,
  ... // ton of properties

  bank: String,
  bankAccount: String,
  bankSwift: String,
  bankAccountCreatedAt: LocalDate
)
```

With the library I wanted to introduce this boilerplate gets reduced.

```scala
@Evolve(User)
case class BankUser(
  bank: String,
  bankAccount: String,
  bankSwift: String,
  bankAccountCreatedAt: LocalDate)
``` 

Sounds good so far?

The resulted class definition will be the same as an example from previous snippet.
With even one helper method added `withUser`, which takes a `User` and applies it to `BankUser`.

There also 2 extra features.
- we can reduce case class definition by removing unnecessary fields
- we can rename fields

```scala
@Evolve(from = User, removed = Set("middleName"), renamed = Map("id" -> "userId"))
case class UserV2()
``` 

Even though it may look cool, there is a huge obstacle. 
*IDE support is absent*. Code in IDE will be all highlighted and look ugly.

TODO
----
- same name, but type changed scenario
- test with Apache Spark