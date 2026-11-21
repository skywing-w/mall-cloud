# 商城微服务系统 (mall-cloud)

基于 Spring Boot + Spring Cloud Alibaba 的微服务电商系统，覆盖用户、商品、购物车、订单、支付五个核心域。

## 模块说明

| 模块 | 说明 |
|------|------|
| `mall-common` | 公共模块：统一返回 `R`、分页 `PageDTO/PageQuery`、异常体系与全局异常处理、通用工具类、Jackson/MyBatis-Plus 自动配置 |
| `mall-service` | 核心业务服务：用户、商品、购物车、订单、支付，端口 8081 |
| `mall-gateway` | API 网关：统一入口与路由、跨域处理，端口 18088 |

## 技术栈

- **Spring Boot 2.7.12** / **Spring Cloud 2021.0.3**
- **Spring Cloud Alibaba 2021.0.4.0**（Nacos 服务发现）
- **Spring Cloud Gateway**（网关路由 + 全局 CORS）
- **MyBatis-Plus 3.5.5**（分页插件、条件构造器）
- **MySQL 8.0** / **HikariCP**
- **JWT（RSA256）** + BCrypt 密码存储
- **Knife4j** 接口文档
- **Hutool 5.8.0.M3**、**Lombok**、**Java 17**

## 功能

| 域 | 能力 |
|---|---|
| 用户 | 登录签发 JWT、余额查询、余额扣减（支付密码二次校验） |
| 商品 | 分页查询、批量查询、新增/修改/删除、上下架、库存扣减 |
| 购物车 | 添加（已存在则累加）、修改数量、列表（回填最新价格与库存）、批量删除、条目上限控制 |
| 订单 | 创建订单（事务内写订单 + 详情 + 清购物车 + 扣库存）、查询、标记已支付 |
| 支付 | 支付单幂等申请（同单复用/渠道变更重置）、余额支付、状态乐观锁更新 |

## 目录结构

```
mall-cloud
├── mall-common                      # 公共能力
│   └── src/main/java/com/mall/common
│       ├── advice/                # 全局异常处理
│       ├── config/                # Jackson、MyBatis-Plus 自动配置
│       ├── domain/                # R / PageDTO / PageQuery
│       ├── exception/             # 业务异常体系
│       └── utils/                 # 工具类（BeanUtils、UserContext 等）
├── mall-service                     # 核心服务
│   └── src/main/java/com/mall
│       ├── config/                # MVC、Security、配置属性
│       ├── controller/            # 8 个 REST 控制器
│       ├── domain/                # dto / po / vo / query
│       ├── interceptor/           # 登录拦截器
│       ├── mapper/                # MyBatis Mapper
│       ├── service/               # 业务接口与实现
│       └── utils/                 # JwtTool
└── mall-gateway                     # 网关
```

## 快速启动

### 1. 准备环境

- JDK 17、Maven 3.8+
- MySQL 8.0，建库 `mall`（字符集 utf8mb4）
- Nacos（网关做服务发现用；未启动时网关仍可启动，仅打印告警）

### 2. 修改配置

`mall-service/src/main/resources/application-dev.yaml`：

```yaml
mall:
  db:
    host: 127.0.0.1   # 你的 MySQL 地址
    pw: 你的密码
```

JWT 密钥库为 `mall-service/src/main/resources/mall.jks`（别名 `mall`，口令见 `application.yaml`），
生产环境请替换为自己的密钥库，并改为从环境变量注入口令。

### 3. 编译与启动

```bash
# 编译
mvn clean package -DskipTests

# 启动核心服务（8081）
mvn -pl mall-service spring-boot:run

# 启动网关（18088）
mvn -pl mall-gateway spring-boot:run
```

接口文档：启动后访问 `http://localhost:8081/doc.html`。

## 接口约定

- 统一响应结构：`{ "code": 200, "msg": "OK", "data": ... }`
- 认证方式：请求头 `authorization: <token>`；登录、搜索、商品查询接口免登录
- 分页参数：`pageNo`（默认 1）、`pageSize`（默认 20，服务端上限 1000）、`sortBy`、`isAsc`
- 错误码：400 参数错误、401 未登录、403 无权限、500 服务端异常

## 说明

本项目为个人项目，用于演示 Spring Cloud 微服务拆分、网关统一入口与 JWT 鉴权链路。
