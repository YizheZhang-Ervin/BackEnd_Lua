# BackEnd_Lua

## 1 Lua-Basic
```
# 安装（方法1）
tar zxf lua-5.4.6.tar.gz
cd lua-5.4.6
make linux test
make install

# 安装（方法2）
apt install lua5.4

# 数据类型
nil,boolean,number,string,function,userdata,thread,table

# 运行
lua xx.lua
```

## 2 Open-Resty
```
# 特点：
负载均衡、单机闭环、分布式闭环、插入网关

# 安装
yum install readline-devel pcre-devel openssl-devel perl gcc
wget https://openresty.org/download/openresty-1.21.4.1.tar.gz
tar -xzvf openresty-1.21.4.1.tar.gz
./configure
make && make install
```

## 3 实战 Example-Shopping
```
Twemproxy + redis：redis代理proxy，统一管理redis和memcached
lua-resty-template：模板渲染
```