# crawler
java 爬虫和 Elasticsearch 数据简单分析

#### 第三方包:

+ httpclient: 发送 http 请求

+ jsoup: HTML 解析器

+ h2database: h2 数据库

+ findbugs: 静态代码分析

+ mybatis: ORM,对象关系映射

+ mysql: mysql 数据库

+ rest-high: elasticsearch 搜索

#### 插件:

+ maven-checkstyle-plugin: 自动化代码编写规范检查

+ com.github.spotbugs: 自动化代码 bug 检查

+ org.flywaydb: 自动化数据库构建

#### 数据库使用:

##### mysql:
在 flywaydb 插件中配置:
```xml
<url>jdbc:mysql://#{host}:#{port}/news?characterEncoding=utf-8</url>
<user>#{database_name}</user>
<password>#{database_password}</password>
```
在 mybatis/config.xml 文件中配置:
```xml
<property name="driver" value="com.mysql.cj.jdbc.Driver"/>
<property name="url" value="jdbc:mysql://#{host}:#{port}/news?characterEncoding=utf-8"/>
<property name="username" value="#{database_name}"/>
<property name="password" value="#{database_password}"/>
```
自动化构建数据库:
```java
 mvn flyway:migrate
```
清除数据库:
```java
mvn flyway:clean
```

#### 代码测试:
执行 maven 的 verify 阶段
```java
mvn verify
```
