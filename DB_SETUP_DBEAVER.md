# 使用 DBeaver 在本地 MySQL 上建库并导入示例数据（中文版）

本文档说明在中文版 DBeaver 中如何创建 `unicourse` 数据库、创建演示用户并导入仓库内的 `data.sql` 示例数据。完成后可按 `WORKFLOW.md` 启动后端。

---

## 前提
- 已安装并能运行 MySQL 服务（Windows：请确认 MySQL 服务已启动）。
- 已安装 DBeaver（中文版）并可以连接到本地 MySQL。
- 项目路径：`C:\Users\karry\Desktop\Course Selection System`（含 `backend/src/main/resources/data.sql`）。

---

## 一：在 DBeaver 中新建 MySQL 连接（已有连接可跳过）
1. 打开 DBeaver，点击工具栏的“新建连接”按钮，或通过菜单 `数据库 -> 新建数据库连接`。
2. 在弹窗中选择 **MySQL**，点击“下一步”。
3. 填写连接信息：
   - 主机（Host）：`localhost`
   - 端口（Port）：`3306`（若你使用不同端口请替换）
   - 数据库（Database）：可留空或填 `mysql`（用于 root 登录）
   - 用户名（User name）：`root`（或其他有建库权限的账号）
   - 密码（Password）：输入你的 root 密码
4. 点击“测试连接”（若提示驱动未安装，按提示安装驱动）。
5. 测试通过后保存连接，可命名为“本地 MySQL”。

---

## 二：在 DBeaver 中创建数据库与用户（以 root 用户执行）
1. 右键你创建的连接，选择 `SQL 编辑器 -> 新建 SQL 脚本`，打开 SQL 编辑器窗口，确保左上角连接为刚建的本地连接。
2. 在编辑器中粘贴并执行以下 SQL：

```sql
CREATE DATABASE IF NOT EXISTS unicourse CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'unicourse_user'@'localhost' IDENTIFIED BY 'change_me';
GRANT ALL PRIVILEGES ON unicourse.* TO 'unicourse_user'@'localhost';
FLUSH PRIVILEGES;
```

3. 执行成功后，在连接上右键选择“刷新”，你应能看到 `unicourse` 数据库。

---

## 三：在 DBeaver 中导入示例数据（`data.sql`）
有两种方法：直接执行 SQL 文件，或将文件内容粘贴到编辑器运行。

方法 A（推荐）——使用脚本执行：
1. 在 DBeaver 左侧找到 `unicourse` 数据库，右键选择 `工具 -> 执行脚本`（不同版本菜单名可能为“运行 SQL 脚本”）。
2. 在弹出的文件选择对话框中，选择项目里的 `backend/src/main/resources/data.sql`，点击运行。
3. 检查输出日志，确认没有错误。

方法 B——在 SQL 编辑器中运行：
1. 打开 `backend/src/main/resources/data.sql` 文件（或把内容粘贴到 SQL 编辑器）。
2. 在工具栏的数据库下拉选择目标数据库为 `unicourse`，然后点击运行（执行）。

注意：如果执行时出现“找不到表”或相关错误，说明表尚未创建。解决方法：先启动后端让 Hibernate 自动创建表，再回到此处导入；或先手动在 DBeaver 创建表结构后再导入。

---

## 四：检查导入结果
1. 在 DBeaver 左侧展开 `unicourse -> 表`（Tables），应看到 `student`、`teacher`、`course`、`enrollment` 等表（若你已先启动后端并让 Hibernate 建表）。
2. 右键任意表，选择 `查看数据 -> 查看全部数据`，确认示例记录存在（例如 `student` 表应包含 id=1 的示例学生）。

---

## 五：后端使用该数据库（在启动时确认或覆盖数据源）
后端配置文件：`backend/src/main/resources/application.properties`（我已把默认用户名/密码设为 `unicourse_user/change_me`）。

如果需要在启动时临时覆盖配置，可以在 PowerShell 中这样运行（在后端目录）：

```powershell
cd "C:\Users\karry\Desktop\Course Selection System\backend"
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.datasource.url=jdbc:mysql://localhost:3306/unicourse?useSSL=false&serverTimezone=UTC -Dspring.datasource.username=root -Dspring.datasource.password=你的密码"
```

或者设置环境变量 `JDBC_DATABASE_USERNAME` / `JDBC_DATABASE_PASSWORD` 后运行 `mvn spring-boot:run`。

---

## 六：常见问题与中文排查建议
- 测试连接失败：请确认 MySQL 服务已启动；在 DBeaver 中再次点击“测试连接”以检查用户名/密码是否正确。
- 无权创建数据库或用户：请使用具有足够权限的账号（如 root）执行建库/授权语句。
- data.sql 报错找不到表：说明表尚未创建。解决：先启动后端让 Hibernate 建表，或手动建表后再导入数据。
- 驱动/端口问题：若提示缺少驱动，按提示安装 MySQL JDBC 驱动；端口被占用可改用其他端口并在后端配置中同步修改。

---

## 七：导入完成后可做的验证
1. 在 DBeaver 中查看 `unicourse` 下的表与数据，确保 `student` 表包含示例数据，`course` 表包含示例课程。
2. 启动后端并调用选课接口（示例）：

POST http://localhost:8080/api/v1/enrollments

请求体示例：
```json
{ "studentId": 1, "courseId": 1 }
```

若返回 HTTP 200 并包含 enrollment 对象表示选课成功；若返回 400 且 `error` 字段为 `Time conflict` 或 `Course full` 表示被后端校验拦截。

---

## 八：我可以继续为你做的事
- 我可以把上述建库 SQL 写成 `db_init.sql` 并放入仓库；
- 我可以生成 `docker-compose.yml` 启动 MySQL 容器并预置数据库；
- 或者在你执行过程中实时帮助排查（把 DBeaver 或后端日志粘贴给我）。

如需我把文件改为 `db_init.sql` 或生成 `docker-compose.yml`，请回复我想要的选项。
