关于构建部分无 model 的面向函数的 List & HList & typeclass base 系统的初步构想
===
目前 MVC 模式的 web 应用一般分为 model，control，view 三层，有些会在 model 和 control 之
间加上 dao 和 service 2 层，这个结构适合于几乎所有的 web 应用，但在很多时候，即使建立了 model，
仍然有很多场景更适合于跳过 model 层，直接借用 Tuple 表示数据结构来操作数据（HList 类似于 Tuple）。
这篇文章就是对在哪些情境下适合跳过 model 层和用怎样的语法来跳过 model 层以达到简化代码的目的进行
可行性分析。