# SQL Version1 Merge Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将 `sql/version1/` 下的多版本脚本合并为单一最终态初始化脚本 `V1.0__init.sql`，建表以 `model` 目录为主，保留全部种子数据，并删除旧版本脚本（保留 `example.sql`）。

**Architecture:** 采用“最终态初始化”策略：不保留历史迁移 `ALTER` 步骤，直接产出可一次性初始化的新库脚本。表结构基线来自 `src/main/java/com/photo/mvc/entity/model/*PO.java` 与现有索引约束，数据基线来自 `V0.2` 与 `V0.3`，字段命名采用最终态（如 `preview_key`，且 `t_sys_user` 无 `role` 列）。

**Tech Stack:** MySQL 8.0 SQL、Spring Boot + MyBatis-Plus 实体映射、Claude Code 文件编辑与命令校验。

---

### Task 1: 建立合并输入基线

**Files:**
- Read: `sql/version1/V0.1__init.sql`
- Read: `sql/version1/V0.2__rbac_seed_data.sql`
- Read: `sql/version1/V0.3__drop_user_role_column.sql`
- Read: `sql/version1/V0.4__rename_preview_url_to_key.sql`
- Read: `src/main/java/com/photo/mvc/entity/model/*.java`

**Step 1: 列出待合并文件并确认目标文件不存在**

Run: `ls "E:/learn/photo-backend/sql/version1"`
Expected: 存在 `V0.1~V0.4` 与 `example.sql`，不存在 `V1.0__init.sql`

**Step 2: 读取 SQL 与 model 实体并记录最终态差异**

重点确认：
- `t_sys_user` 最终无 `role` 列（由 `t_sys_user_role` 维护角色）
- `preview_url/photo_preview_url` 最终为 `preview_key/photo_preview_key`
- 字段命名与实体保持一致（如 `user_id`）

**Step 3: 生成“最终态规则清单”并固定**

规则示例（必须执行）：
```text
1) CREATE TABLE 以 model + 现有有效索引约束为准
2) 不保留 ALTER 迁移步骤
3) 保留 RBAC + 测试用户 + user_role 种子数据
4) 保留 sql/version1/example.sql
5) 删除 V0.1~V0.4
```

**Step 4: 自检规则完整性**

Run: `git diff -- sql/version1`
Expected: 此时仍无改动或仅注释性准备改动

**Step 5: Commit（可选检查点）**

```bash
git add sql/version1
git commit -m "chore: prepare rules for sql version1 merge"
```

---

### Task 2: 编写最终态建表 SQL（V1.0）

**Files:**
- Create: `sql/version1/V1.0__init.sql`
- Reference: `sql/version1/V0.1__init.sql`
- Reference: `src/main/java/com/photo/mvc/entity/model/*.java`

**Step 1: 写失败校验（先验证目标文件尚不存在）**

Run: `test -f "E:/learn/photo-backend/sql/version1/V1.0__init.sql" && echo EXISTS || echo MISSING`
Expected: `MISSING`

**Step 2: 写最小可用文件头与数据库说明**

```sql
-- ============================================================
-- 摄影作品平台 数据库初始化脚本
-- 版本：V1.0（merged final-state）
-- 数据库：MySQL 8.0+
-- 说明：最终态初始化，不包含历史迁移 ALTER
-- ============================================================
```

**Step 3: 写完整 13 张表 CREATE TABLE（最终态）**

关键代码要求（必须体现在最终 SQL）：
```sql
-- t_sys_user 不包含 role
CREATE TABLE t_sys_user (
  id BIGINT NOT NULL COMMENT '主键ID',
  openid VARCHAR(64) NOT NULL COMMENT '微信openid',
  nickname VARCHAR(64) NOT NULL DEFAULT '' COMMENT '微信昵称',
  avatar VARCHAR(512) NOT NULL DEFAULT '' COMMENT '头像URL',
  points INT NOT NULL DEFAULT 0 COMMENT '积分余额',
  is_banned TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否封禁 0-正常 1-封禁',
  org_id BIGINT DEFAULT NULL COMMENT '所属组织ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  update_by BIGINT DEFAULT NULL COMMENT '修改人ID',
  version INT NOT NULL DEFAULT 1 COMMENT '乐观锁版本号',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- t_biz_photo 使用 preview_key
-- t_biz_order 使用 photo_preview_key
-- t_biz_license 使用 photo_preview_key
```

**Step 4: 运行文本校验确保无历史字段**

Run: `python - <<'PY'
from pathlib import Path
p=Path('E:/learn/photo-backend/sql/version1/V1.0__init.sql').read_text(encoding='utf-8')
for bad in [' preview_url', 'photo_preview_url', ' role ', 'DROP COLUMN role', 'ALTER TABLE']:
    print(bad, 'FOUND' if bad in p else 'OK')
PY`
Expected: 关键坏模式全部 `OK`

**Step 5: Commit（可选检查点）**

```bash
git add sql/version1/V1.0__init.sql
git commit -m "feat: add merged final-state v1.0 init sql"
```

---

### Task 3: 合并并保留全部种子数据

**Files:**
- Modify: `sql/version1/V1.0__init.sql`
- Reference: `sql/version1/V0.2__rbac_seed_data.sql`
- Reference: `sql/version1/V0.3__drop_user_role_column.sql`

**Step 1: 写失败校验（确认种子段尚未完整）**

Run: `python - <<'PY'
from pathlib import Path
p=Path('E:/learn/photo-backend/sql/version1/V1.0__init.sql').read_text(encoding='utf-8')
keys=['INSERT INTO t_sys_role','INSERT INTO t_sys_menu','INSERT INTO t_sys_role_menu','INSERT INTO t_sys_user ','INSERT INTO t_sys_user_role']
for k in keys:
    print(k, 'YES' if k in p else 'NO')
PY`
Expected: 初始可能有 `NO`

**Step 2: 追加完整种子数据块**

必须包含：
```sql
INSERT INTO t_sys_role (...);
INSERT INTO t_sys_menu (...);
INSERT INTO t_sys_role_menu (...);
INSERT INTO t_sys_user (id, openid, nickname, avatar, points, is_banned, org_id) VALUES
(1, 'oTest_admin_001', '管理员小张', '', 0, 0, NULL),
(2, 'oTest_photographer_001', '摄影师老李', '', 500, 0, NULL),
(3, 'oTest_buyer_001', '买家小王', '', 1000, 0, NULL);
INSERT INTO t_sys_user_role (id, user_id, role_id) VALUES
(1,1,1),(2,2,2),(3,3,3);
```

**Step 3: 运行校验确保数据与最终态字段兼容**

Run: `python - <<'PY'
from pathlib import Path
p=Path('E:/learn/photo-backend/sql/version1/V1.0__init.sql').read_text(encoding='utf-8')
assert 't_sys_user (id, openid, nickname, avatar, role, points)' not in p
assert 'ALTER TABLE t_sys_user DROP COLUMN role' not in p
print('seed schema compatibility OK')
PY`
Expected: 输出 `seed schema compatibility OK`

**Step 4: Commit（可选检查点）**

```bash
git add sql/version1/V1.0__init.sql
git commit -m "feat: merge rbac and test seed data into v1.0 init"
```

---

### Task 4: 删除旧版本 SQL 并保留 example.sql

**Files:**
- Delete: `sql/version1/V0.1__init.sql`
- Delete: `sql/version1/V0.2__rbac_seed_data.sql`
- Delete: `sql/version1/V0.3__drop_user_role_column.sql`
- Delete: `sql/version1/V0.4__rename_preview_url_to_key.sql`
- Keep: `sql/version1/example.sql`
- Keep: `sql/version1/V1.0__init.sql`

**Step 1: 先执行删除前清单核对**

Run: `ls "E:/learn/photo-backend/sql/version1"`
Expected: 同时看到 V0.1~V0.4、example.sql、V1.0__init.sql

**Step 2: 删除 4 个历史版本脚本**

Run: `rm "E:/learn/photo-backend/sql/version1/V0.1__init.sql" "E:/learn/photo-backend/sql/version1/V0.2__rbac_seed_data.sql" "E:/learn/photo-backend/sql/version1/V0.3__drop_user_role_column.sql" "E:/learn/photo-backend/sql/version1/V0.4__rename_preview_url_to_key.sql"`
Expected: 命令成功，无报错

**Step 3: 删除后清单校验**

Run: `ls "E:/learn/photo-backend/sql/version1"`
Expected: 仅剩 `V1.0__init.sql` 与 `example.sql`

**Step 4: git 状态核验**

Run: `git status --short`
Expected: 4 个删除 + 1 个新增/修改（V1.0__init.sql）

**Step 5: Commit（最终）**

```bash
git add sql/version1/V1.0__init.sql sql/version1/example.sql
git add -u sql/version1
git commit -m "refactor: merge version1 sql scripts into final-state v1.0 init"
```

---

### Task 5: 完整验证与交付说明

**Files:**
- Verify: `sql/version1/V1.0__init.sql`

**Step 1: 结构关键字核验**

Run: `python - <<'PY'
from pathlib import Path
p=Path('E:/learn/photo-backend/sql/version1/V1.0__init.sql').read_text(encoding='utf-8')
checks={
  'create_count': p.count('CREATE TABLE'),
  'has_preview_key': 'preview_key' in p,
  'has_photo_preview_key': 'photo_preview_key' in p,
  'no_user_role_column': ' role ' not in p,
  'has_rbac_seed': 'INSERT INTO t_sys_role' in p and 'INSERT INTO t_sys_menu' in p and 'INSERT INTO t_sys_role_menu' in p,
  'has_test_users': "oTest_admin_001" in p and "oTest_photographer_001" in p and "oTest_buyer_001" in p,
}
print(checks)
PY`
Expected: `create_count` 为 13，其他关键项为 True

**Step 2: 输出变更摘要给用户**

摘要必须说明：
```text
1) 新文件：sql/version1/V1.0__init.sql
2) 删除文件：V0.1~V0.4
3) 保留文件：sql/version1/example.sql
4) 冲突处理：按用户确认的“model 优先 + 最终态初始化”执行
```

**Step 3: 等待用户验收**

Run: `git diff -- sql/version1`
Expected: 用户可直接审阅全部 SQL 差异

**Step 4: Commit（若前面未提交）**

```bash
git add sql/version1/V1.0__init.sql
git add -u sql/version1
git commit -m "refactor: consolidate version1 sql into single final-state init script"
```

**Step 5: 准备后续（仅在用户要求时）**

可选：同步更新 `docs/version1/sql.md` 中脚本文件引用为 `V1.0__init.sql`。
