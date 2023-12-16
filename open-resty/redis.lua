-- redis第三方连接库 位于 openresty/lualib/resty/redis.lua（可从lua-resty-redis下载）
-- 连接redis，并完成操作
local redis = require "resty.redis"
local red = redis:new()

red:set_timeout(1000) -- 1 sec

local ok, err = red:connect("192.168.137.10", 6379)
if not ok then
	ngx.say("failed to connect: ", err)
	return
end
--[[
local count
count, err = red:get_reused_times()
if 0 == count then
	-- 认证（对应redis配置的requirepass和protected-mode）
	ok, err = red:auth("admin")
	if not ok then
		ngx.say("failed to auth: ", err)
		return
	end
elseif err then
	ngx.say("failed to get reused times: ", err)
	return
end
--]]


ok, err = red:set("abc", "good school,good ......")
if not ok then
	ngx.say("failed to set abc: ", err)
	return
end

ngx.say("set result: ", ok)

local res, err = red:get("abc")
if not res then
	ngx.say("failed to get abc: ", err)
	return
end

if res == ngx.null then
	ngx.say("abc not found.")
	return
end

ngx.say("abc: ", res)

-- put it into the connection pool of size 100,
-- with 10 seconds max idle time
local ok, err = red:set_keepalive(10000, 100)
if not ok then
	ngx.say("failed to set keepalive: ", err)
	return
end
