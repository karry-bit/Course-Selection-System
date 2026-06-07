# 完整开发与部署工作流（你需要手动执行的步骤）

下面说明如何在本地把项目（前端 + 后端）运行起来，以及在 DBeaver/MySQL 中的配置步骤、常见问题与排查建议。

## 前提
- 你已在本机安装：
  - Node.js（推荐 18+）和 npm
  - Java 17
  - Maven
  - MySQL 数据库（已安装并能通过 DBeaver 访问）
  - DBeaver（可选但推荐用于查看/管理数据库）

- 仓库路径：`C:\Users\karry\Desktop\Course Selection System`

---

## 一：数据库准备（MySQL）
1. 使用 DBeaver 或命令行登录 MySQL（使用 root 或具备建库权限的用户）。
2. 创建数据库：

```sql
CREATE DATABASE unicourse CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. （可选）为演示创建一个专用账号并授权：

```sql
CREATE USER '<DB_USERNAME>'@'localhost' IDENTIFIED BY '<DB_PASSWORD>';
GRANT ALL PRIVILEGES ON unicourse.* TO '<DB_USERNAME>'@'localhost';
FLUSH PRIVILEGES;
```

4. 确认 `backend/src/main/resources/application.properties` 中的连接配置。如果你使用上面创建的用户，请在系统环境变量或运行命令中传递：

- `JDBC_DATABASE_URL`（例如 `jdbc:mysql://localhost:3306/unicourse?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`）
- `JDBC_DATABASE_USERNAME`（例如 `<DB_USERNAME>`）
- `JDBC_DATABASE_PASSWORD`（例如 `<DB_PASSWORD>`）

说明：开发配置默认 `spring.jpa.hibernate.ddl-auto=update`，应用启动会自动创建表并执行 `data.sql`（若启用）。

---

## 二：后端启动（Spring Boot）
1. 打开 PowerShell，切换到后端目录：

```powershell
cd "C:\Users\karry\Desktop\Course Selection System\backend"
```

2. 构建并运行（本地开发）：

```powershell
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.datasource.url=jdbc:mysql://localhost:3306/unicourse?useSSL=false&serverTimezone=UTC -Dspring.datasource.username=<DB_USERNAME> -Dspring.datasource.password=<DB_PASSWORD>"
```

或者先构建再运行：

```powershell
mvn package
java -jar target/course-backend-0.0.1-SNAPSHOT.jar --spring.datasource.url=jdbc:mysql://localhost:3306/unicourse?useSSL=false&serverTimezone=UTC --spring.datasource.username=<DB_USERNAME> --spring.datasource.password=<DB_PASSWORD>
```

3. 启动后访问： http://localhost:8080 。示例接口：
- `POST http://localhost:8080/api/v1/enrollments`，请求体示例：

```json
{ "studentId": 1, "courseId": 1 }
```

返回 200 表示选课成功；若容量已满或冲突会返回 400 与错误信息。

---

## 三：前端启动（已生成 demo）
1. 在新的 PowerShell 窗口中切换到项目根：

```powershell
cd "C:\Users\karry\Desktop\Course Selection System"
```

2. 安装依赖（如果尚未安装）：

```powershell
npm install
```

3. 运行开发服务器：

```powershell
npm run dev
```

4. 打开浏览器访问 Vite 提示的地址（默认通常是 http://localhost:5173）。

说明：当前前端 demo 为静态示例，若要接入后端 `enroll` 接口，需要在前端添加 API 层并把 `CourseCard` 的选课事件改为调用后端接口。

---

## 四：把前端接入后端（手动改动指导）
1. 编辑 `src/App.vue` 或创建 `src/api/index.js`：实现 `POST /api/v1/enrollments` 调用。
2. 在发起请求前做前端初步冲突检查（例如比较课程 `timeSlot` 字段）；后端仍为最终校验点。
3. 示例 fetch 调用：

```js
await fetch('http://localhost:8080/api/v1/enrollments', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ studentId: 1, courseId: 2 })
})
```

4. 根据返回结果在前端展示 `Toast` 或 `Dialog`。

---

## 五：常见问题与排查
- 问题：后端启动失败，提示无法连接到 MySQL / 认证失败。
  - 检查 MySQL 服务是否启动（Windows 服务）。
  - 在 DBeaver 中使用相同的连接信息测试能否连通。
  - 检查 `application.properties` 中 URL、用户名与密码是否正确，或在运行命令中以 JVM 参数覆盖。

- 问题：表未自动创建或 `data.sql` 未生效。
  - 检查 `spring.jpa.hibernate.ddl-auto` 是否为 `update`（开发时可用），生产环境不建议使用 `update`。
  - `data.sql` 在 `spring.datasource.initialization-mode=always` 的老版本中自动运行，Spring Boot 新版本在使用 JPA 时有行为差异；如未加载，可手动在 DBeaver 导入 `data.sql`。

- 问题：选课接口返回 `Time conflict`。
  - 这是后端的示例冲突检测（基于 `timeSlot` 完全匹配）；如果你的课程时间格式不同，需要统一 timeSlot 格式或增强冲突算法（解析时间段并检测重叠）。

- 问题：前端无法访问后端（跨域问题）
  - 在后端控制器需要允许 CORS：可在后端添加 `@CrossOrigin(origins = "*")` 到控制器或配置全局 CORS（仅开发时允许 *，生产限定域）。

- 问题：端口占用
  - 若 8080 被占用，修改 `server.port` 或在运行命令中覆盖 `--server.port=8081`。

---

## 六：后续推荐（我可以继续为你完成）
- 增强冲突检测逻辑：把 `timeSlot` 改成结构化字段（周次/开始时间/结束时间/节次）并实现重叠检测算法。
- 实现完整权限（登录/角色鉴权，JWT）与管理端 API。
- 把前端 demo 完整接入 API，添加请求层、错误处理、Loading 与表单校验。
- 添加单元与集成测试（后端使用 Spring Boot Test；前端使用 Vitest / Cypress）。

---

如果你允许，我会继续：
- 1) 为后端添加跨域配置与更完善的错误返回；
- 2) 生成前端 `src/api` 层并把 `CourseCard` 的选课按钮接入后端；
- 3) 或者现在指导你手动在本机运行并解决可能出现的问题。 

请选择下一步：我代为修改代码并提交，或由你手动按 `WORKFLOW.md` 执行并在遇到问题时告诉我错误日志。