# Deploy Apollo

## 部署
1. 快速单机部署
    - (1) 打包
      - Apollo> mvn clean package -pl apollo-assembly -am -DskipTests=true
      - demo.sh改mysql连接串
    - (2) 启动阿波罗服务
      - apollo-build-scripts> ./demo.sh start
    - (3) 启动阿波罗Demo App
      - apollo-build-scripts> ./demo.sh client
    
2. Docker启动(Centos7)
      - 配置中心启动: docker-compose up
      - 查看日志: docker exec -it apollo-quick-start bash
      - 客户端启动: docker exec -i apollo-quick-start /apollo-quick-start/demo.sh client
      
## 安装
1. Java
  - 创建目录: mkdir /usr/local/java/
  - 解压: tar -zxvf jdk8_301.tar.gz -C /usr/local/java/
  - 改jdk文件夹名: /usr/local/java/jdk8_301
  - 设环境变量: vim /etc/profile
    - export JAVA_HOME=/usr/local/java/jdk8_301
    - export JRE_HOME=${JAVA_HOME}/jre
    - export CLASSPATH=.:${JAVA_HOME}/lib:${JRE_HOME}/lib
    - export PATH=${JAVA_HOME}/bin:$PATH
    - source /etc/profile
  - 软链接: ln -s /usr/local/java/jdk8_301/bin/java /usr/bin/java
2. Mysql
  - mv /etc/yum.repos.d/CentOS-Base.repo /etc/yum.repos.d/CentOS-Base.repo.backup
  - wget -O /etc/yum.repos.d/CentOS-Base.repo http://mirrors.aliyun.com/repo/Centos-7.repo
  - yum makecache
  - yum -y update
  - yum install mysql
  - yum install mysql-devel
  - wget http://dev.mysql.com/get/mysql-community-release-el7-5.noarch.rpm
  - rpm -ivh mysql-community-release-el7-5.noarch.rpm
  - yum install mysql-community-server
  - service mysqld restart
  - mysql -u root
  - set password for 'root'@'localhost' =password('root001');
  - grant all privileges on *.* to root@'%'identified by 'root001';
  - flush privileges;
  - exit
  - firewall-cmd --zone=public --add-port=3306/tcp --permanent
  - firewall-cmd --zone=public --add-port=8070/tcp --permanent
  - firewall-cmd --reload
3. Docker
   - 下载Docker及Docker-compose
     - sudo yum install -y yum-utils
     - sudo yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
     - sudo sed -i 's/download.docker.com/mirrors.aliyun.com\/docker-ce/g' /etc/yum.repos.d/docker-ce.repo
     - sudo yum install docker-ce docker-ce-cli containerd.io
     - sudo curl -L https://download.fastgit.org/docker/compose/releases/download/1.27.4/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
     - sudo chmod +x /usr/local/bin/docker-compose
   - 启动Docker
     - sudo systemctl enable docker
     - sudo systemctl start docker
   - 创组
     - sudo groupadd docker
     - sudo usermod -aG docker $USER
   
## CentOS配置
1. 配置静态IP
   - windows:
     - 网卡IP 172.29.48.1
     - 子网掩码 255.255.240.0
   - centos:
     - BOOTPROTO=static
     - IPADDR=172.29.48.11
     - GATEWAY=172.29.48.1
     - NETMASK=255.255.240.0
     - DNS=172.29.48.1
2. 装ifconfig和wget
   - yum -y install net-tools
   - yum -y install wget
## 使用
- 登录
  - apollo-admin
- 启动样例应用
  - 输入key名timeout
  - quit
    