module={}

module.index=1

function module.sum(a,b)
  return a+b
end

-- 使用：引入模块（模块名可以和lua文件名不一致）
-- require "module"
-- print(module.index)
-- print(module.sum(10,20))