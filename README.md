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

当前连接信息：

- 地址：`192.168.100.128:3306`
- 数据库：`qsl_tracker`
- 用户名：`root`
- 密码：`123456`

## 默认管理员

首次启动且 `admin_user` 表为空时，会自动创建默认管理员：

- 用户名：`admin`
- 密码：`admin123`

生产部署后应及时修改默认密码。

## 启动

需要先切换到 JDK 17。

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn spring-boot:run
```

接口文档地址：

- `http://localhost:8080/swagger-ui.html`

## 主要接口

- `POST /api/auth/login`：后台登录
- `POST /api/auth/logout`：退出登录
- `GET /api/qso-logs`：通联日志分页
- `POST /api/qso-logs`：新增通联日志
- `PUT /api/qso-logs/{id}`：修改通联日志
- `DELETE /api/qso-logs/{id}`：删除通联日志
- `GET /api/qsl-cards`：QSL 卡片分页
- `POST /api/qsl-cards`：新增 QSL 卡片
- `PUT /api/qsl-cards/{id}`：修改 QSL 卡片
- `DELETE /api/qsl-cards/{id}`：删除 QSL 卡片
- `GET /api/public/qsl-cards/{trackingNo}`：公开查询 QSL 卡片确认信息
- `POST /api/public/qsl-cards/{trackingNo}/confirm`：公开确认收件
