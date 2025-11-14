# 医院挂号系统 - 数据导入说明

## 文件清单

### CSV数据文件
1. **department.csv** - 科室数据（22条记录）
   - 包含4个父科室：内科、外科、妇产科、儿科
   - 包含18个子科室

2. **title_number_source.csv** - 医生职称号源配置（4条记录）
   - TITLE001: 主任医师 (30个号源, 200元)
   - TITLE002: 副主任医师 (50个号源, 100元)
   - TITLE003: 主治医师 (80个号源, 50元)
   - TITLE004: 住院医师 (100个号源, 20元)

3. **doctor_info.csv** - 医生信息（369条记录）
   - 包含医生id、姓名、职称、科室信息
   - 职称分配按医生稀有程度（出现频率）：
     * 主任医师: 18人 (4.9%)
     * 副主任医师: 55人 (14.9%)
     * 主治医师: 110人 (29.8%)
     * 住院医师: 186人 (50.4%)

### SQL导入脚本
1. **import_data.sql** - 固定路径导入脚本
   - CSV文件路径写死在脚本中
   - 适合快速测试

2. **import_data_config.sql** - 可配置路径导入脚本（推荐使用）
   - 可以通过修改变量指定CSV文件路径
   - 提供了导入函数，可重复使用
   - 包含完整的数据统计功能

## 使用方法

### 方法一：使用固定路径脚本（快速测试）

```bash
# 1. 确保CSV文件在 /mnt/user-data/outputs/ 目录下
# 2. 执行导入脚本
psql -U postgres -d hospital_db -f import_data.sql
```

### 方法二：使用可配置路径脚本（推荐）

```bash
# 1. 编辑 import_data_config.sql 文件，修改CSV路径：
#    \set department_csv_path '/your/path/department.csv'
#    \set title_csv_path '/your/path/title_number_source.csv'
#    \set doctor_csv_path '/your/path/doctor_info.csv'

# 2. 执行导入脚本
psql -U postgres -d hospital_db -f import_data_config.sql
```

### 方法三：使用psql命令行变量

```bash
psql -U postgres -d hospital_db \
  -v department_csv_path="'/path/to/department.csv'" \
  -v title_csv_path="'/path/to/title_number_source.csv'" \
  -v doctor_csv_path="'/path/to/doctor_info.csv'" \
  -f import_data_config.sql
```

## 导入顺序说明

数据必须按以下顺序导入（脚本已自动处理）：

1. **department.csv** - 科室数据（先导入父科室，再导入子科室）
2. **title_number_source.csv** - 职称号源数据
3. **doctor_info.csv** - 医生数据（会自动创建user、clinic、doctor表数据）

## 导入后的数据结构

### department表
```
父科室（4个）:
  DEP001 - 内科
  DEP002 - 外科
  DEP003 - 妇产科
  DEP004 - 儿科

子科室（18个）:
  内科下: 心内科门诊、肾内科门诊、血液科门诊、感染内科门诊、肝炎门诊
  外科下: 基本外科门诊、骨科门诊、胸外科门诊、泌尿外科门诊、心外科门诊、疼痛综合门诊
  妇产科下: 妇科门诊、产科门诊、妇科内分泌及生殖门诊、妇科计划生育门诊、综合妇科门诊
  儿科下: 儿科门诊、眼科门诊
```

### title_number_source表
```
TITLE001 - 主任医师 (30号源, 200元)
TITLE002 - 副主任医师 (50号源, 100元)
TITLE003 - 主治医师 (80号源, 50元)
TITLE004 - 住院医师 (100号源, 20元)
```

### user + doctor表
```
369位医生，分布如下：
- 主任医师: 18人
- 副主任医师: 55人
- 主治医师: 110人
- 住院医师: 186人

各科室医生数量（前5）:
- 基本外科门诊: 59人
- 眼科门诊: 53人
- 骨科门诊: 43人
- 泌尿外科门诊: 38人
- 心内科门诊: 31人
```

### clinic表
自动为每个科室创建一个默认诊室（与科室id对应）

## 数据验证

导入完成后，可以使用以下SQL查询验证数据：

```sql
-- 查看导入统计
SELECT * FROM show_import_statistics();

-- 查看医生详细信息（前10条）
SELECT * FROM v_doctor_detail LIMIT 10;

-- 查看主任医师列表
SELECT * FROM v_doctor_detail WHERE title_name = '主任医师';

-- 按科室统计医生数量
SELECT 
    parent_department_name, 
    department_name, 
    COUNT(*) as doctor_count 
FROM v_doctor_detail 
GROUP BY parent_department_name, department_name 
ORDER BY parent_department_name, department_name;

-- 查看所有科室层级结构
SELECT 
    d1.name as parent_dept,
    d2.name as child_dept,
    COUNT(DISTINCT doc.id) as doctor_count
FROM department d1
LEFT JOIN department d2 ON d2.father_id = d1.id
LEFT JOIN clinic c ON c.dep_id = d2.id
LEFT JOIN doctor doc ON doc.clinic_id = c.id
WHERE d1.father_id IS NULL
GROUP BY d1.name, d2.name
ORDER BY d1.name, d2.name;
```

## 注意事项

1. **字符编码**: 所有CSV文件使用UTF-8编码（带BOM），确保中文正确显示
2. **权限要求**: 执行导入需要数据库的INSERT权限
3. **重复导入**: 脚本使用 `ON CONFLICT` 处理重复数据，可安全重复执行
4. **自动生成信息**:
   - 用户邮箱: `{doctor_id}@hospital.com`
   - 用户账号: `doc_{doctor_id}`
   - 默认密码: MD5加密 (`{doctor_id}password`)
   - 手机号: 随机生成的138开头的11位号码

5. **诊室命名**: 自动为每个科室创建诊室，命名格式为 `{科室名称}1号`

## 医生职称分配算法

医生职称根据其在原始挂号数据中的出现频率（稀有程度）分配：

- **出现频率最低的前5%** → 主任医师（最稀缺）
- **出现频率低的15%** → 副主任医师
- **出现频率中等的30%** → 主治医师
- **其余50%** → 住院医师

例如：只在一个时间段出现过的医生会被分配为主任医师，而频繁出诊的医生则是住院医师。

## 扩展功能

导入脚本提供了以下可重用函数：

- `import_department_data(csv_path)` - 导入科室数据
- `import_title_data(csv_path)` - 导入职称数据
- `import_doctor_data(csv_path)` - 导入医生数据
- `show_import_statistics()` - 显示导入统计

可以在psql中单独调用：

```sql
SELECT import_department_data('/path/to/department.csv');
SELECT * FROM show_import_statistics();
```

## 故障排除

### 问题1: 编码错误
```
ERROR: invalid byte sequence for encoding "UTF8"
```
**解决方案**: 确保CSV文件使用UTF-8编码保存

### 问题2: 文件路径错误
```
ERROR: could not open file for reading: No such file or directory
```
**解决方案**: 
- 检查CSV文件路径是否正确
- 确保PostgreSQL进程有权限读取该文件
- 在Linux上，文件可能需要放在 `/tmp` 或其他PostgreSQL可访问的目录

### 问题3: 外键约束错误
```
ERROR: insert or update violates foreign key constraint
```
**解决方案**: 确保按正确顺序导入（脚本已处理）

### 问题4: CSV格式错误
```
ERROR: invalid input syntax for type
```
**解决方案**: 
- 检查CSV文件格式是否正确
- 确保有正确的标题行
- 检查数据类型是否匹配

## 联系支持

如有问题，请检查：
1. PostgreSQL版本 >= 17
2. 数据库schema已创建（运行 hospital_register_postgresql_17.sql）
3. CSV文件编码为UTF-8
4. CSV文件路径正确且可访问
