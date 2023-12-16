-- 获取uri请求参数，header，body
local arg = ngx.req.get_uri_args()
for k,v in pairs(arg) do
   ngx.say("[GET ] key:", k, " v:", v)
   ngx.say("<br>")
end

-- 获取post请求时 请求参数
ngx.req.read_body() -- 解析 body 参数之前一定要先读取 body
local arg = ngx.req.get_post_args()
for k,v in pairs(arg) do
   ngx.say("[POST] key:", k, " v:", v)
    ngx.say("<br>")
end

-- 获取header
local headers = ngx.req.get_headers()  
for k,v in pairs(headers) do
   ngx.say("[header] name:", k, " v:", v)
    ngx.say("<br>")
end

-- 获取body信息（先预读取再获取数据）
ngx.req.read_body()
local data = ngx.req.get_body_data()
ngx.say(data)