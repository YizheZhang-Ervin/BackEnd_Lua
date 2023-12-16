# lua-resty-template

## 导入
```
下载 http://luarocks.org/modules/bungle/lua-resty-template
将template.lua文件复制到openresty/lualib/resty/目录下
```

## nginx
```
nginx.conf
设置模板路径：set $template_root /usr/local/openresty/nginx/html/templates;
设置mimeType类型：default_type text/html;
导入lua文件:content_by_lua_file /usr/local/openresty/nginx/conf/*.lua
```

## 模板文件
```
template1.lua
template2.lua
redis操作的封装文件：redis_iresty.lua
```