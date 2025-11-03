/*==============================================================*/
/* PostgreSQL 数据导入脚本                                       */
/* 功能: 从CSV文件导入科室、职称号源、医生等数据                   */
/* 使用方法: psql -U username -d database_name -f import_data.sql */
/*==============================================================*/

-- 设置客户端编码
SET client_encoding = 'UTF8';

/*==============================================================*/
/* 1. 导入科室数据 (department.csv)                              */
/*==============================================================*/
DO $$
DECLARE
    csv_path TEXT := '/mnt/user-data/outputs/department.csv';
BEGIN
    RAISE NOTICE '开始导入科室数据...';
    
    -- 创建临时表
    CREATE TEMP TABLE temp_department (
        id VARCHAR(20),
        name VARCHAR(100),
        father_id VARCHAR(20)
    );
    
    -- 从CSV文件导入数据
    EXECUTE format('COPY temp_department FROM %L WITH CSV HEADER ENCODING ''UTF8''', csv_path);
    
    -- 插入父科室（father_id为NULL的记录）
    INSERT INTO department (id, name, father_id)
    SELECT id, name, father_id
    FROM temp_department
    WHERE father_id IS NULL OR father_id = 'None'
    ON CONFLICT (id) DO UPDATE SET
        name = EXCLUDED.name,
        father_id = EXCLUDED.father_id;
    
    -- 插入子科室（有father_id的记录）
    INSERT INTO department (id, name, father_id)
    SELECT id, name, father_id
    FROM temp_department
    WHERE father_id IS NOT NULL AND father_id != 'None'
    ON CONFLICT (id) DO UPDATE SET
        name = EXCLUDED.name,
        father_id = EXCLUDED.father_id;
    
    DROP TABLE temp_department;
    
    RAISE NOTICE '科室数据导入完成: % 条记录', (SELECT COUNT(*) FROM department);
END $$;

/*==============================================================*/
/* 2. 导入职称号源数据 (title_number_source.csv)                */
/*==============================================================*/
DO $$
DECLARE
    csv_path TEXT := '/mnt/user-data/outputs/title_number_source.csv';
BEGIN
    RAISE NOTICE '开始导入职称号源数据...';
    
    -- 创建临时表
    CREATE TEMP TABLE temp_title (
        id VARCHAR(20),
        name VARCHAR(10),
        number_source_count SMALLINT,
        ori_cost NUMERIC(10,2)
    );
    
    -- 从CSV文件导入数据
    EXECUTE format('COPY temp_title FROM %L WITH CSV HEADER ENCODING ''UTF8''', csv_path);
    
    -- 插入数据
    INSERT INTO title_number_source (id, name, number_source_count, ori_cost)
    SELECT id, name, number_source_count, ori_cost
    FROM temp_title
    ON CONFLICT (id) DO UPDATE SET
        name = EXCLUDED.name,
        number_source_count = EXCLUDED.number_source_count,
        ori_cost = EXCLUDED.ori_cost;
    
    DROP TABLE temp_title;
    
    RAISE NOTICE '职称号源数据导入完成: % 条记录', (SELECT COUNT(*) FROM title_number_source);
END $$;

/*==============================================================*/
/* 3. 导入医生数据 (doctor_info.csv)                            */
/* 注意: 需要先导入到user表，再导入到doctor表                     */
/*==============================================================*/
DO $$
DECLARE
    csv_path TEXT := '/mnt/user-data/outputs/doctor_info.csv';
    rec RECORD;
    clinic_id VARCHAR(20);
    v_email VARCHAR(100);
    v_account VARCHAR(50);
    v_phone VARCHAR(20);
BEGIN
    RAISE NOTICE '开始导入医生数据...';
    
    -- 创建临时表
    CREATE TEMP TABLE temp_doctor (
        doctor_id VARCHAR(20),
        doctor_name VARCHAR(50),
        title_id VARCHAR(20),
        department_name VARCHAR(100),
        department_id VARCHAR(20)
    );
    
    -- 从CSV文件导入数据
    EXECUTE format('COPY temp_doctor FROM %L WITH CSV HEADER ENCODING ''UTF8''', csv_path);
    
    -- 为每个科室创建默认诊室（如果不存在）
    FOR rec IN (SELECT DISTINCT department_id, department_name FROM temp_doctor)
    LOOP
        clinic_id := 'CLIN' || LPAD(SUBSTRING(rec.department_id FROM 4)::TEXT, 3, '0');
        
        INSERT INTO clinic (id, clinic_number, location, dep_id)
        VALUES (
            clinic_id,
            rec.department_name || '1号诊室',
            '门诊楼',
            rec.department_id
        )
        ON CONFLICT (id) DO NOTHING;
    END LOOP;
    
    -- 导入用户和医生数据
    FOR rec IN (SELECT * FROM temp_doctor)
    LOOP
        -- 生成医生的默认信息
        v_email := rec.doctor_id || '@hospital.com';
        v_account := 'doc_' || rec.doctor_id;
        v_phone := '138' || LPAD((RANDOM() * 100000000)::BIGINT::TEXT, 8, '0');
        
        -- 插入用户表
        INSERT INTO "user" (id, email, pass, name, account, sex, phone_num)
        VALUES (
            rec.doctor_id,
            v_email,
            MD5(rec.doctor_id || 'password'),  -- 默认密码加密
            rec.doctor_name,
            v_account,
            NULL,  -- 性别未知
            v_phone
        )
        ON CONFLICT (id) DO UPDATE SET
            name = EXCLUDED.name,
            email = EXCLUDED.email;
        
        -- 获取诊室id
        clinic_id := 'CLIN' || LPAD(SUBSTRING(rec.department_id FROM 4)::TEXT, 3, '0');
        
        -- 插入医生表
        INSERT INTO doctor (id, doc_title_id, status, clinic_id)
        VALUES (
            rec.doctor_id,
            rec.title_id,
            '在职',
            clinic_id
        )
        ON CONFLICT (id) DO UPDATE SET
            doc_title_id = EXCLUDED.doc_title_id,
            status = EXCLUDED.status,
            clinic_id = EXCLUDED.clinic_id;
    END LOOP;
    
    DROP TABLE temp_doctor;
    
    RAISE NOTICE '医生数据导入完成: % 条记录', (SELECT COUNT(*) FROM doctor);
END $$;

/*==============================================================*/
/* 4. 数据验证和统计                                             */
/*==============================================================*/
DO $$
BEGIN
    RAISE NOTICE '======================================';
    RAISE NOTICE '数据导入统计:';
    RAISE NOTICE '科室数量: %', (SELECT COUNT(*) FROM department);
    RAISE NOTICE '  - 父科室: %', (SELECT COUNT(*) FROM department WHERE father_id IS NULL);
    RAISE NOTICE '  - 子科室: %', (SELECT COUNT(*) FROM department WHERE father_id IS NOT NULL);
    RAISE NOTICE '诊室数量: %', (SELECT COUNT(*) FROM clinic);
    RAISE NOTICE '职称类型: %', (SELECT COUNT(*) FROM title_number_source);
    RAISE NOTICE '用户数量: %', (SELECT COUNT(*) FROM "user");
    RAISE NOTICE '医生数量: %', (SELECT COUNT(*) FROM doctor);
    RAISE NOTICE '  - 主任医师: %', (SELECT COUNT(*) FROM doctor WHERE doc_title_id = 'TITLE001');
    RAISE NOTICE '  - 副主任医师: %', (SELECT COUNT(*) FROM doctor WHERE doc_title_id = 'TITLE002');
    RAISE NOTICE '  - 主治医师: %', (SELECT COUNT(*) FROM doctor WHERE doc_title_id = 'TITLE003');
    RAISE NOTICE '  - 住院医师: %', (SELECT COUNT(*) FROM doctor WHERE doc_title_id = 'TITLE004');
    RAISE NOTICE '======================================';
END $$;

/*==============================================================*/
/* 5. 创建视图查看导入结果                                       */
/*==============================================================*/
CREATE OR REPLACE VIEW v_doctor_detail AS
SELECT 
    d.id as doctor_id,
    u.name as doctor_name,
    u.phone_num,
    u.email,
    d.status,
    tns.ori_cost,
    tns.number_source_count,
    CASE d.doc_title_id
        WHEN 'TITLE001' THEN '主任医师'
        WHEN 'TITLE002' THEN '副主任医师'
        WHEN 'TITLE003' THEN '主治医师'
        WHEN 'TITLE004' THEN '住院医师'
        ELSE '未知'
    END as title_name,
    c.clinic_number,
    c.location,
    dep.name as department_name,
    parent_dep.name as parent_department_name
FROM doctor d
JOIN "user" u ON d.id = u.id
LEFT JOIN title_number_source tns ON d.doc_title_id = tns.id
LEFT JOIN clinic c ON d.clinic_id = c.id
LEFT JOIN department dep ON c.dep_id = dep.id
LEFT JOIN department parent_dep ON dep.father_id = parent_dep.id;

COMMENT ON VIEW v_doctor_detail IS '医生详细信息视图';

-- 查询示例
-- SELECT * FROM v_doctor_detail WHERE title_name = '主任医师' LIMIT 10;
-- SELECT parent_department_name, department_name, COUNT(*) as doctor_count 
-- FROM v_doctor_detail 
-- GROUP BY parent_department_name, department_name 
-- ORDER BY parent_department_name, department_name;
