package org.eop.guide.testing

import zio.ZLayer

final case class Material(brittleness: Int)

object WoodMaterial:
  val live: ZLayer[Any, Nothing, Material] =
    ZLayer.succeed:
      Material(brittleness = 5)
end WoodMaterial

object PlasticMaterial:
  val live: ZLayer[Any, Nothing, Material] =
    ZLayer.succeed:
      Material(brittleness = 10)
end PlasticMaterial

object MetalMaterial:
  val live: ZLayer[Any, Nothing, Material] =
    ZLayer.succeed:
      Material(brittleness = 20)
end MetalMaterial
