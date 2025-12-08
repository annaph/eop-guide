package org.eop.guide

import zio.Duration

object StringOps:
  extension (str: String)
    def withGreenBackground: String =
      s"${Console.RESET}${Console.GREEN}$str${Console.RESET}"

    def withMagentaBackground: String =
      s"${Console.RESET}${Console.MAGENTA}$str${Console.RESET}"

    def withRedBackground: String =
      s"${Console.RESET}${Console.RED}$str${Console.RESET}"
  end extension
end StringOps

object IntOps:
  extension (n: Int) def seconds: Duration = Duration.fromSeconds(n)
  end extension
end IntOps
