# 更新说明 - title_number_source 表添加 name 字段

## 📋 更新内容

为 `title_number_source` 表添加了 `name` 字段，用于存储职称名称。

### 数据库表结构变更

**之前**:
```sql
CREATE TABLE title_number_source (
   ID                   VARCHAR(20)          NOT NULL,
   number_source_count  SMALLINT             NULL,
   ori_cost             NUMERIC(10,2)        NULL,
   CONSTRAINT PK_TITLE_NUMBER_SOURCE PRIMARY KEY (ID)
);
```

**现在**:
```sql
CREATE TABLE title_number_source (
   ID                   VARCHAR(20)          NOT NULL,
   name                 VARCHAR(10)          NOT NULL,  -- 新增字段
   number_source_count  SMALLINT             NULL,
   ori_cost             NUMERIC(10,2)        NULL,
   CONSTRAINT PK_TITLE_NUMBER_SOURCE PRIMARY KEY (ID)
);
```

### CSV文件变更

**title_number_source.csv 之前**:
```csv
ID,number_source_count,ori_cost
TITLE001,30,200.0
TITLE002,50,100.0
TITLE003,80,50.0
TITLE004,100,20.0
```

**title_number_source.csv 现在**:
```csv
ID,name,number_source_count,ori_cost
TITLE001,主任医师,30,200.0
TITLE002,副主任医师,50,100.0
TITLE003,主治医师,80,50.0
TITLE004,住院医师,100,20.0
```

## 🔄 已更新的文件

1. ✅ **hospital_register_postgresql_17.sql**
   - 添加 name 字段定义
   - 添加字段注释

2. ✅ **title_number_source.csv**
   - 添加 name 列
   - 填充职称名称数据

3. ✅ **import_data.sql**
   - 更新临时表结构
   - 更新 INSERT 语句

4. ✅ **import_data_config.sql**
   - 更新临时表结构
   - 更新 INSERT 语句

5. ✅ **sample_queries.sql**
   - 简化查询，直接使用 name 字段
   - 不再需要 CASE 语句

6. ✅ **项目交付文档.md**
   - 更新 CSV 结构说明

7. ✅ **文件清单.txt**
   - 更新文件大小信息

## 💡 优势

### 1. 数据完整性
- 职称名称直接存储在数据库中
- 避免了硬编码的 CASE 语句
- 更容易维护和扩展

### 2. 查询简化
**之前需要 CASE 语句**:
```sql
SELECT 
    CASE ID
        WHEN 'TITLE001' THEN '主任医师'
        WHEN 'TITLE002' THEN '副主任医师'
        ...
    END as 职称名称
FROM title_number_source;
```

**现在直接使用**:
```sql
SELECT name as 职称名称
FROM title_number_source;
```

### 3. 易于扩展
添加新职称时，只需在 CSV 中添加一行，无需修改查询语句：
```csv
TITLE005,特聘专家,20,500.0
```

## 🔧 使用方法

### 对于新部署
直接使用更新后的文件即可，按照原有的导入流程操作：

```bash
# 1. 创建数据库表
psql -U postgres -d hospital_db -f hospital_register_postgresql_17.sql

# 2. 导入数据
psql -U postgres -d hospital_db -f import_data.sql
```

### 对于已有数据库

如果您已经创建了旧版本的表，需要执行以下 SQL 更新表结构：

```sql
-- 添加 name 字段
ALTER TABLE title_number_source 
ADD COLUMN name VARCHAR(10);

-- 更新现有数据
UPDATE title_number_source SET name = '主任医师' WHERE ID = 'TITLE001';
UPDATE title_number_source SET name = '副主任医师' WHERE ID = 'TITLE002';
UPDATE title_number_source SET name = '主治医师' WHERE ID = 'TITLE003';
UPDATE title_number_source SET name = '住院医师' WHERE ID = 'TITLE004';

-- 设置为 NOT NULL
ALTER TABLE title_number_source 
ALTER COLUMN name SET NOT NULL;

-- 添加注释
COMMENT ON COLUMN title_number_source.name IS '职称名称';
```

或者，更简单的方法是清空表并重新导入：

```sql
-- 清空职称表（需要先删除相关的外键数据）
TRUNCATE TABLE doctor CASCADE;
TRUNCATE TABLE title_number_source;

-- 重新执行导入脚本
\i import_data.sql
```

## 📊 数据示例

更新后的职称数据：

| ID | name | number_source_count | ori_cost |
|----|------|---------------------|----------|
| TITLE001 | 主任医师 | 30 | 200.0 |
| TITLE002 | 副主任医师 | 50 | 100.0 |
| TITLE003 | 主治医师 | 80 | 50.0 |
| TITLE004 | 住院医师 | 100 | 20.0 |

## ✅ 验证更新

导入数据后，执行以下查询验证：

```sql
-- 查看职称表结构
\d title_number_source

-- 查看职称数据
SELECT * FROM title_number_source ORDER BY ori_cost DESC;

-- 验证 name 字段不为空
SELECT COUNT(*) as total, 
       COUNT(name) as has_name 
FROM title_number_source;
-- 结果应该显示 total = has_name = 4
```

## 🎯 向后兼容性

⚠️ **注意**: 此更新**不向后兼容**旧的 CSV 文件。

- 如果您使用旧的 CSV 文件（不包含 name 列），导入将会失败
- 请确保使用更新后的 `title_number_source.csv` 文件

## 📞 问题排查

### 问题1: 导入时提示列数不匹配

**错误信息**:
```
ERROR: extra data after last expected column
```

**解决方案**: 确保使用的是更新后的 CSV 文件，包含 name 列。

### 问题2: NOT NULL 约束违反

**错误信息**:
```
ERROR: null value in column "name" violates not-null constraint
```

**解决方案**: 检查 CSV 文件中 name 列是否有空值，所有记录的 name 字段都必须有值。

## 📝 更新日期

2025-10-24

---

**总结**: 此次更新增强了数据完整性和查询便利性，建议所有用户在新部署时使用更新后的文件。
