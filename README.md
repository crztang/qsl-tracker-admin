# qsl-tracker-admin

QSL Tracker 后端单体服务，采用单模块 Spring Boot 工程。

## 技术栈

- Spring Boot 3.3.5
- JDK 17
- Maven
- Sa-Token
- MyBatis-Plus
- Druid
- MySQL 8.0
- springdoc-openapi

## 目录结构

- `common`：统一响应、业务异常、密码和追踪号工具
- `config`：鉴权、MyBatis-Plus、异常处理、默认管理员初始化
- `controller`：HTTP 接口
- `domain`：数据库实体和枚举
- `dto`：请求和响应对象
- `mapper`：MyBatis-Plus Mapper
- `service`：业务接口和实现

## 数据库配置

配置文件位于 `src/main/resources/application.yml`。

## 默认管理员

首次启动且 `sys_user` 表为空时，会自动创建默认超级管理员，并绑定内置
`SUPER_ADMIN` 角色：

- 用户名：`admin`
- 密码：`admin123`

生产部署后应及时修改默认密码。

## 多用户与权限

- 业务账号统一存储在 `sys_user`。
- RBAC 使用 `sys_role`、`sys_permission`、`sys_user_role` 和
  `sys_role_permission`。
- 普通用户的数据范围为 `SELF`，超级管理员的数据范围为 `ALL`。
- QSO、QSL、打印模板和文件均保存 `user_id`，服务端强制执行属主校验。
- 文件接口仅暴露随机 `fileKey`，数据库自增 ID 不对外提供。

已有数据库应先确保 `username=admin` 的账号唯一存在，再执行：

```text
docs/migration-20260612-multi-user-rbac.sql
```

全新数据库直接执行 `docs/database.sql`。

## 运行环境与日志

默认使用 `dev` 环境，也可以通过 `SPRING_PROFILES_ACTIVE` 切换：

```powershell
$env:SPRING_PROFILES_ACTIVE='prod'
mvn spring-boot:run
```

- `dev`：控制台输出应用 DEBUG 日志和 MyBatis SQL，异常打印完整堆栈。
- `prod`：不输出 MyBatis SQL，控制台和 `logs/qsl-tracker-admin.log` 输出 INFO
  及以上日志；文件按日期和 100MB 大小滚动，保留 30 天。
- 可通过 `logging.file.path` 修改生产日志目录。

## 启动

需要先切换到 JDK 17。

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn spring-boot:run
```

接口文档地址：

- `http://localhost:8080/swagger-ui.html`
