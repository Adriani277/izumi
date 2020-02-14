package izumi.distage.injector

import distage.{InjectorF, ModuleDef, PlannerInput}
import izumi.distage.model.effect.DIEffectRunner
import izumi.fundamentals.platform.functional.Identity
import org.scalatest.wordspec.AnyWordSpec

class InjectorFTest extends AnyWordSpec {

  import InjectorFTest._

  "InjectorF" should {
    "support for comprehension" in {
      val p = for {
        p1 <- InjectorF.plan[Identity](PlannerInput.noGc(new ModuleDef {
          make[Service0]
          make[Service1]
        }))
        l1 <- InjectorF.produce(p1)
        out0 <- InjectorF.use(l1) {
          (service0: Service0, service1: Service1) =>
            List(service0, service1)
        }
        p2 <- InjectorF.plan[Identity](PlannerInput.noGc(new ModuleDef {
          make[Service2]
          make[Service3]
        }))
        l2 <- InjectorF.produce(p2, l1)
        out <- InjectorF.use(l2) {
          (service0: Service0, service2: Service2) =>
            List(service0, service2)
        }
      } yield {
        (out0, out)
      }


      val result = DIEffectRunner[Identity].run(InjectorF.run(p))
      val expected = (List(Service0(), Service1(Service0())), List(Service0(), Service2()))
      assert(result == expected)
      //println(result)
    }
  }
}

object InjectorFTest {

  case class Service0()

  case class Service1(service0: Service0)

  case class Service2()

  case class Service3(service0: Service2)

}