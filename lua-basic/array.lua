-- 数组（下标从1开始）
arr={"aaa","bbb","ccc"}
-- 使用数值for通过下标来遍历数组
for i=1,#arr
do
    print(arr[i])
end

--使用泛型for遍历数组
for i,v in ipairs(arr)
do
    print(i,v)
end

print(arr[0])
print(arr[1])
print(arr[4])
