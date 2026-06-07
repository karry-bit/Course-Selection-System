# 后端启动与数据库初始化（Hibernate 创建表并执行 data.sql）

下面的步骤按顺序执行，可让 Spring Boot 连接到 `unicourse` 数据库，启动后让 Hibernate/JPA 自动创建表并运行 `data.sql` 导入示例数据。

前提：MySQL 已安装并可通过 `localhost:3306` 访问；DBeaver 已可用（可用命令行替代）。

注意：我无法在你的机器上直接运行命令，你需要在本机的 PowerShell 或 DBeaver 中执行下列操作。

---

## 1) 在 MySQL 中创建数据库与用户（DBeaver 或命令行均可）

在 DBeaver 中执行以下 SQL（以 root 登录）：

```sql
CREATE DATABASE IF NOT EXISTS unicourse CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'unicourse_user'@'localhost' IDENTIFIED BY 'change_me';
GRANT ALL PRIVILEGES ON unicourse.* TO 'unicourse_user'@'localhost';
FLUSH PRIVILEGES;
```

或者在 PowerShell 使用 mysql 客户端（若已在 PATH）：

```powershell
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS unicourse CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; CREATE USER IF NOT EXISTS 'unicourse_user'@'localhost' IDENTIFIED BY 'change_me'; GRANT ALL PRIVILEGES ON unicourse.* TO 'unicourse_user'@'localhost'; FLUSH PRIVILEGES;"
```

确认：在 DBeaver 刷新连接后能看到 `unicourse` 数据库。

---

## 2) 确认后端配置（已帮你预置默认值）

文件： `backend/src/main/resources/application.properties`

默认内容（我已设置）：

- `spring.datasource.url` 默认指向 `jdbc:mysql://localhost:3306/unicourse?...`
- `spring.datasource.username` 默认 `unicourse_user`
- `spring.datasource.password` 默认 `change_me`
- `spring.jpa.hibernate.ddl-auto=update`（让 Hibernate 自动建表/更新模式）
- `spring.sql.init.mode=always`（在开发环境下执行 `data.sql`）

如果你想临时用不同凭据在启动时覆盖，请在 PowerShell 中按下列方式运行（示例使用 root）：

```powershell
# 在 backend 目录运行
cd "C:\Users\karry\Desktop\Course Selection System\backend"
# 用 JVM 参数覆盖数据源
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.datasource.url=jdbc:mysql://localhost:3306/unicourse?useSSL=false&serverTimezone=UTC -Dspring.datasource.username=root -Dspring.datasource.password=你的密码"
```

---

## 3) 启动后端（PowerShell）

在项目后端目录执行：

```powershell
cd "C:\Users\karry\Desktop\Course Selection System\backend"
mvn spring-boot:run
```

或者先构建再运行：

```powershell
cd "C:\Users\karry\Desktop\Course Selection System\backend"
mvn package
java -jar target/course-backend-0.0.1-SNAPSHOT.jar
```

注意观察控制台日志：

- 查找 Hibernate 输出：包含类似 `Hibernate: create table ...` 或 `HHH000` 日志，表明 JPA 正在建表。
- 查找 Spring SQL 初始化日志：会有 `Executing SQL script from class path resource [data.sql]` 或类似信息，表明 `data.sql` 被执行。

示例关键日志片段（成功时应该看到）：

```
Hibernate: alter table course ...
...
Executing SQL script from class path resource [data.sql]
```

如果启动失败，请把完整的异常栈粘贴给我以便诊断。

---

## 4) 验证表与数据是否创建/导入成功

方法 A：使用 DBeaver

1. 在 DBeaver 中连接 `unicourse` 数据库，展开 `Tables`，应看到 `student`、`teacher`、`course`、`enrollment` 等表。
2. 右键表 -> **View Data -> View All Data**，查看记录，例如 `student` 表应包含 id=1 的示例学生。

方法 B：命令行（mysql 客户端）

```powershell
mysql -u unicourse_user -p unicourse
# 然后在 mysql 提示符中执行：
SHOW TABLES;
SELECT * FROM student LIMIT 10;
SELECT * FROM course LIMIT 10;
```

如果 `SHOW TABLES` 没有显示表，说明 Hibernate 没有建表或连接有问题；检查启动日志与 `application.properties` 配置。

---

## 5) 手动导入 `data.sql`（如果没有自动执行或需要重试）

在 DBeaver 中：右键 `unicourse` -> **Tools -> Execute script**，选择仓库中的 `backend/src/main/resources/data.sql` 并运行。

或者使用 mysql 命令行导入：

```powershell
mysql -u unicourse_user -p unicourse < "C:\Users\karry\Desktop\Course Selection System\backend\src\main\resources\data.sql"
```

注意：如果 data.sql 中的 INSERT 语句引用的表尚未创建，请先启动后端让 Hibernate 创建表，或先手动创建表结构。

---

## 6) 测试后端接口（示例：选课）

使用 curl（PowerShell）或 Postman：

```powershell
curl -X POST http://localhost:8080/api/v1/enrollments -H "Content-Type: application/json" -d '{"studentId":1,"courseId":1}'
```

期望返回：HTTP 200 并包含 enrollment 对象。若返回 400，响应体含 `{ "error": "Time conflict" }` 或 `{ "error": "Course full" }`。

---

## 7) 常见错误与排查

- 数据库连接失败：检查 `application.properties` 或启动时覆盖的参数，确认用户名/密码/端口/主机正确。
- Hibernate 未建表：检查 `spring.jpa.hibernate.ddl-auto=update` 是否存在，查看启动日志是否有 Hibernate 建表的输出。
- data.sql 未执行：确保 `spring.sql.init.mode=always` 已设置（我已在 `application.properties` 中设置），并检查日志中是否有 `Executing SQL script` 记录；若无，手动在 DBeaver 导入。
- 权限问题：若使用 `unicourse_user` 报权限错误，可使用 `root` 启动一次后导入数据，或在 MySQL 授予正确权限。

---

## 8) 如果你遇到问题请把以下信息粘贴给我：

1. 后端启动时的完整控制台错误/异常堆栈（或截图）。
2. `application.properties` 的当前内容（隐藏敏感密码可替换为 `***`）。
3. DBeaver 中 `SHOW TABLES;` 的输出结果（或截图）。

我会根据这些信息协助你定位并修复问题。

---

完成这些步骤后，后端应已连接到 `unicourse` 并成功导入示例数据，你就可以用前端 demo 测试选课流程。祝顺利！
