# Twemproxy

## 安装
```
# 安装 autoconf
wget http://ftp.gnu.org/gnu/autoconf/autoconf-2.69.tar.gz
tar -zxvf autoconf-2.69.tar.gz
cd autoconf-2.69
./configure && make && make install

# 安装automake
wget http://ftp.gnu.org/gnu/automake/automake-1.15.tar.gz
tar -zvxf automake-1.15.tar.gz
cd automake-1.15
./configure && make && make install

# 安装libtool
wget https://ftp.gnu.org/gnu/libtool/libtool-2.4.6.tar.gz
tar -zvxf libtool-2.4.6.tar.gz
cd libtool-2.4.6
./configure && make && make install

# twemproxy
wget https://github.com/twitter/twemproxy/archive/master.zip
unzip master
cd twemproxy-master
aclocal
autoreconf -f -i -Wall,no-obsolete
mkdir /usr/local/twemproxy
./configure --prefix=/usr/local/twemproxy/
make && make install
```

## 配置
```
将twemproxy-master下的conf目录复制到/usr/local/twemproxy下
cp -r ./conf /usr/local/twemproxy/
cd /usr/local/twemproxy/

修改conf目录下nutcracker.yml
alpha:
  listen: 192.168.93.134:22121
  hash: fnv1a_64
  distribution: ketama
  auto_eject_hosts: true
  redis: true
  server_retry_timeout: 2000
  server_failure_limit: 1
  servers:
   - 192.168.93.133:6379:1
   - 192.168.93.134:6379:1

关闭防火墙 service iptables stop
启动
```