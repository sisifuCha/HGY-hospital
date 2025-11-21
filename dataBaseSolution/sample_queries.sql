/*==============================================================*/
/* 示例查询 - 数据验证和常用查询                                 */
/*==============================================================*/

-- ============================================
-- 1. 数据统计查询
-- ============================================

-- 查看导入统计概览
SELECT * FROM show_import_statistics();

-- 各科室医生数量统计
SELECT 
    parent_dept.name as 父科室,
    child_dept.name as 子科室,
    COUNT(DISTINCT d.id) as 医生数量
FROM department parent_dept
LEFT JOIN department child_dept ON child_dept.father_id = parent_dept.id
LEFT JOIN clinic c ON c.dep_id = child_dept.id
LEFT JOIN doctor d ON d.clinic_id = c.id
WHERE parent_dept.father_id IS NULL
GROUP BY parent_dept.name, child_dept.name
ORDER BY parent_dept.name, 医生数量 DESC;

-- 各职称医生统计
SELECT 
    CASE doc.doc_title_id
        WHEN 'TITLE001' THEN '主任医师'
        WHEN 'TITLE002' THEN '副主任医师'
        WHEN 'TITLE003' THEN '主治医师'
        WHEN 'TITLE004' THEN '住院医师'
    END as 职称,
    COUNT(*) as 人数,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM doctor), 2) as 占比
FROM doctor doc
GROUP BY doc.doc_title_id
ORDER BY doc.doc_title_id;

-- ============================================
-- 2. 医生信息查询
-- ============================================

-- 查看所有主任医师信息
SELECT 
    doctor_id as 医生id,
    doctor_name as 姓名,
    title_name as 职称,
    ori_cost as 挂号费,
    parent_department_name as 父科室,
    department_name as 科室,
    clinic_number as 诊室,
    phone_num as 联系电话,
    email as 邮箱
FROM v_doctor_detail
WHERE title_name = '主任医师'
ORDER BY parent_department_name, department_name, doctor_name;

-- 按科室查询医生（示例：心内科）
SELECT 
    doctor_name as 姓名,
    title_name as 职称,
    ori_cost as 挂号费,
    number_source_count as 号源数,
    clinic_number as 诊室,
    status as 状态
FROM v_doctor_detail
WHERE department_name = '心内科门诊'
ORDER BY ori_cost DESC, doctor_name;

-- 查找挂号费最贵的前10位医生
SELECT 
    doctor_name as 姓名,
    title_name as 职称,
    parent_department_name as 父科室,
    department_name as 科室,
    ori_cost as 挂号费
FROM v_doctor_detail
ORDER BY ori_cost DESC, doctor_name
LIMIT 10;

-- ============================================
-- 3. 科室结构查询
-- ============================================

-- 查看完整的科室树形结构
WITH RECURSIVE dept_tree AS (
    -- 根节点（父科室）
    SELECT 
        id,
        name,
        father_id,
        name as path,
        1 as level
    FROM department
    WHERE father_id IS NULL
    
    UNION ALL
    
    -- 子节点
    SELECT 
        d.id,
        d.name,
        d.father_id,
        dt.path || ' > ' || d.name as path,
        dt.level + 1 as level
    FROM department d
    INNER JOIN dept_tree dt ON d.father_id = dt.id
)
SELECT 
    REPEAT('  ', level - 1) || name as 科室层级,
    id as 科室id,
    level as 层级
FROM dept_tree
ORDER BY path;

-- 查看每个科室的诊室信息
SELECT 
    parent_dept.name as 父科室,
    child_dept.name as 子科室,
    c.clinic_number as 诊室,
    c.location as 位置,
    COUNT(DISTINCT d.id) as 医生数
FROM clinic c
JOIN department child_dept ON c.dep_id = child_dept.id
LEFT JOIN department parent_dept ON child_dept.father_id = parent_dept.id
LEFT JOIN doctor d ON d.clinic_id = c.id
GROUP BY parent_dept.name, child_dept.name, c.clinic_number, c.location
ORDER BY parent_dept.name, child_dept.name;

-- ============================================
-- 4. 职称号源配置查询
-- ============================================

-- 查看所有职称配置
SELECT 
    id as 职称id,
    name as 职称名称,
    number_source_count as 每日号源数,
    ori_cost as 挂号费用
FROM title_number_source
ORDER BY ori_cost DESC;

-- 按职称统计总号源数和总收入潜力
SELECT 
    tns.name as 职称,
    COUNT(d.id) as 医生数,
    tns.number_source_count as 单人号源,
    COUNT(d.id) * tns.number_source_count as 总号源数,
    tns.ori_cost as 单价,
    COUNT(d.id) * tns.number_source_count * tns.ori_cost as 每日收入潜力
FROM title_number_source tns
LEFT JOIN doctor d ON d.doc_title_id = tns.id
GROUP BY tns.id, tns.name, tns.number_source_count, tns.ori_cost
ORDER BY tns.ori_cost DESC;

-- ============================================
-- 5. 用户信息查询
-- ============================================

-- 查看所有医生用户的登录信息（脱敏）
SELECT 
    u.id as 用户id,
    u.name as 姓名,
    u.account as 账号,
    LEFT(u.email, 10) || '***' as 邮箱脱敏,
    u.phone_num as 电话,
    d.status as 状态
FROM "user" u
JOIN doctor d ON u.id = d.id
ORDER BY u.id
LIMIT 20;

-- 统计用户类型
SELECT 
    CASE 
        WHEN EXISTS (SELECT 1 FROM doctor WHERE doctor.id = u.id) THEN '医生'
        WHEN EXISTS (SELECT 1 FROM patient WHERE patient.id = u.id) THEN '患者'
        ELSE '未知'
    END as 用户类型,
    COUNT(*) as 数量
FROM "user" u
GROUP BY 
    CASE 
        WHEN EXISTS (SELECT 1 FROM doctor WHERE doctor.id = u.id) THEN '医生'
        WHEN EXISTS (SELECT 1 FROM patient WHERE patient.id = u.id) THEN '患者'
        ELSE '未知'
    END;

-- ============================================
-- 6. 数据完整性检查
-- ============================================

-- 检查外键完整性
SELECT '检查项' as 检查类型, '结果' as 检查结果
UNION ALL

-- 检查是否有医生没有对应的用户记录
SELECT 
    '医生-用户关联',
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ 通过'
        ELSE '✗ 发现' || COUNT(*) || '个问题'
    END
FROM doctor d
LEFT JOIN "user" u ON d.id = u.id
WHERE u.id IS NULL

UNION ALL

-- 检查是否有医生没有对应的诊室
SELECT 
    '医生-诊室关联',
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ 通过'
        ELSE '✗ 发现' || COUNT(*) || '个问题'
    END
FROM doctor d
LEFT JOIN clinic c ON d.clinic_id = c.id
WHERE c.id IS NULL

UNION ALL

-- 检查是否有医生没有职称
SELECT 
    '医生-职称关联',
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ 通过'
        ELSE '✗ 发现' || COUNT(*) || '个问题'
    END
FROM doctor d
LEFT JOIN title_number_source tns ON d.doc_title_id = tns.id
WHERE tns.id IS NULL

UNION ALL

-- 检查是否有诊室没有对应的科室
SELECT 
    '诊室-科室关联',
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ 通过'
        ELSE '✗ 发现' || COUNT(*) || '个问题'
    END
FROM clinic c
LEFT JOIN department dept ON c.dep_id = dept.id
WHERE dept.id IS NULL;

-- ============================================
-- 7. 高级分析查询
-- ============================================

-- 每个父科室下的医生职称分布
SELECT 
    parent_dept.name as 父科室,
    CASE d.doc_title_id
        WHEN 'TITLE001' THEN '主任医师'
        WHEN 'TITLE002' THEN '副主任医师'
        WHEN 'TITLE003' THEN '主治医师'
        WHEN 'TITLE004' THEN '住院医师'
    END as 职称,
    COUNT(*) as 人数
FROM doctor d
JOIN clinic c ON d.clinic_id = c.id
JOIN department child_dept ON c.dep_id = child_dept.id
JOIN department parent_dept ON child_dept.father_id = parent_dept.id
GROUP BY parent_dept.name, d.doc_title_id
ORDER BY parent_dept.name, d.doc_title_id;

-- 找出医生最多的前5个子科室
SELECT 
    child_dept.name as 科室,
    parent_dept.name as 所属,
    COUNT(d.id) as 医生数,
    AVG(tns.ori_cost) as 平均挂号费
FROM department child_dept
LEFT JOIN department parent_dept ON child_dept.father_id = parent_dept.id
LEFT JOIN clinic c ON c.dep_id = child_dept.id
LEFT JOIN doctor d ON d.clinic_id = c.id
LEFT JOIN title_number_source tns ON d.doc_title_id = tns.id
WHERE child_dept.father_id IS NOT NULL
GROUP BY child_dept.name, parent_dept.name
ORDER BY 医生数 DESC
LIMIT 5;

-- 计算每个父科室的潜在日收入
SELECT 
    parent_dept.name as 父科室,
    COUNT(DISTINCT d.id) as 医生总数,
    SUM(tns.number_source_count) as 总号源数,
    ROUND(SUM(tns.number_source_count * tns.ori_cost)::NUMERIC, 2) as 潜在日收入
FROM department parent_dept
LEFT JOIN department child_dept ON child_dept.father_id = parent_dept.id
LEFT JOIN clinic c ON c.dep_id = child_dept.id
LEFT JOIN doctor d ON d.clinic_id = c.id
LEFT JOIN title_number_source tns ON d.doc_title_id = tns.id
WHERE parent_dept.father_id IS NULL
GROUP BY parent_dept.name
ORDER BY 潜在日收入 DESC;

-- ============================================
-- 8. 导出查询结果示例
-- ============================================

-- 导出医生花名册（可用于Excel）
\copy (SELECT doctor_id as "医生id", doctor_name as "姓名", title_name as "职称", parent_department_name as "父科室", department_name as "科室", clinic_number as "诊室", ori_cost as "挂号费", phone_num as "联系电话", email as "邮箱" FROM v_doctor_detail ORDER BY parent_department_name, department_name, ori_cost DESC) TO '/tmp/doctor_roster.csv' WITH CSV HEADER ENCODING 'UTF8';

-- 导出科室医生统计（可用于报表）
\copy (SELECT parent_dept.name as "父科室", child_dept.name as "子科室", COUNT(DISTINCT d.id) as "医生数", AVG(tns.ori_cost) as "平均挂号费" FROM department parent_dept LEFT JOIN department child_dept ON child_dept.father_id = parent_dept.id LEFT JOIN clinic c ON c.dep_id = child_dept.id LEFT JOIN doctor d ON d.clinic_id = c.id LEFT JOIN title_number_source tns ON d.doc_title_id = tns.id WHERE parent_dept.father_id IS NULL GROUP BY parent_dept.name, child_dept.name ORDER BY parent_dept.name, "医生数" DESC) TO '/tmp/department_stats.csv' WITH CSV HEADER ENCODING 'UTF8';

/*==============================================================*/
/* 使用说明:                                                     */
/*==============================================================*/
/*
1. 在psql中执行本文件:
   psql -U postgres -d hospital_db -f sample_queries.sql

2. 或者在psql交互模式中执行单个查询:
   \i sample_queries.sql

3. 复制粘贴想要的查询到psql中执行

4. 导出查询需要修改文件路径
*/
