-- 函数
function sum(a,b) 
    return a+b
  end
c=sum(1,2)
print(c)

function fun(a,b,c,d)
    return a+1,b+1,c+1
end

-- 可返回多个结果
-- 调用时入参可少于定义的入参的个数
r1,r2,r3=fun(1,2,3)
print(r1,r2,r3)