/*==============================================================*/
/* PostgreSQL 数据导入脚本 - 可配置版本                          */
/* 功能: 从指定路径的CSV文件导入数据                              */
/* 使用方法:                                                     */
/*   1. 修改下方的变量配置部分，设置CSV文件路径                   */
/*   2. 执行: psql -U username -d database_name -f import_data_config.sql */
/*==============================================================*/

SET client_encoding = 'UTF8';

/*==============================================================*/
/* 变量配置 - 请根据实际情况修改CSV文件路径                       */
/*==============================================================*/
\set department_csv_path '/path/to/department.csv'
\set title_csv_path '/path/to/title_number_source.csv'
\set doctor_csv_path '/path/to/doctor_info.csv'

/*==============================================================*/
/* 创建导入函数                                                  */
/*==============================================================*/

-- 函数1: 导入科室数据
CREATE OR REPLACE FUNCTION import_department_data(csv_file_path TEXT)
RETURNS INTEGER AS $$
DECLARE
    record_count INTEGER;
BEGIN
    RAISE NOTICE '开始导入科室数据: %', csv_file_path;
    
    -- 创建临时表
    CREATE TEMP TABLE IF NOT EXISTS temp_department (
        id VARCHAR(20),
        name VARCHAR(100),
        father_id VARCHAR(20)
    ) ON COMMIT DROP;
    
    -- 清空临时表
    TRUNCATE temp_department;
    
    -- 从CSV文件导入数据
    EXECUTE format('COPY temp_department FROM %L WITH (FORMAT CSV, HEADER TRUE, ENCODING ''UTF8'', NULL '''')', csv_file_path);
    
    -- 处理NULL值
    UPDATE temp_department SET father_id = NULL WHERE father_id = 'None' OR father_id = '';
    
    -- 插入父科室
    INSERT INTO department (id, name, father_id)
    SELECT id, name, father_id
    FROM temp_department
    WHERE father_id IS NULL
    ON CONFLICT (id) DO UPDATE SET
        name = EXCLUDED.name,
        father_id = EXCLUDED.father_id;
    
    -- 插入子科室
    INSERT INTO department (id, name, father_id)
    SELECT id, name, father_id
    FROM temp_department
    WHERE father_id IS NOT NULL
    ON CONFLICT (id) DO UPDATE SET
        name = EXCLUDED.name,
        father_id = EXCLUDED.father_id;
    
    GET DIAGNOSTICS record_count = ROW_COUNT;
    
    RAISE NOTICE '科室数据导入完成: % 条记录', (SELECT COUNT(*) FROM department);
    
    RETURN record_count;
END;
$$ LANGUAGE plpgsql;

-- 函数2: 导入职称号源数据
CREATE OR REPLACE FUNCTION import_title_data(csv_file_path TEXT)
RETURNS INTEGER AS $$
DECLARE
    record_count INTEGER;
BEGIN
    RAISE NOTICE '开始导入职称号源数据: %', csv_file_path;
    
    -- 创建临时表
    CREATE TEMP TABLE IF NOT EXISTS temp_title (
        id VARCHAR(20),
        name VARCHAR(10),
        number_source_count SMALLINT,
        ori_cost NUMERIC(10,2)
    ) ON COMMIT DROP;
    
    -- 清空临时表
    TRUNCATE temp_title;
    
    -- 从CSV文件导入数据
    EXECUTE format('COPY temp_title FROM %L WITH (FORMAT CSV, HEADER TRUE, ENCODING ''UTF8'')', csv_file_path);
    
    -- 插入数据
    INSERT INTO title_number_source (id, name, number_source_count, ori_cost)
    SELECT id, name, number_source_count, ori_cost
    FROM temp_title
    ON CONFLICT (id) DO UPDATE SET
        name = EXCLUDED.name,
        number_source_count = EXCLUDED.number_source_count,
        ori_cost = EXCLUDED.ori_cost;
    
    GET DIAGNOSTICS record_count = ROW_COUNT;
    
    RAISE NOTICE '职称号源数据导入完成: % 条记录', (SELECT COUNT(*) FROM title_number_source);
    
    RETURN record_count;
END;
$$ LANGUAGE plpgsql;

-- 函数3: 导入医生数据
CREATE OR REPLACE FUNCTION import_doctor_data(csv_file_path TEXT)
RETURNS INTEGER AS $$
DECLARE
    rec RECORD;
    clinic_id VARCHAR(20);
    v_email VARCHAR(100);
    v_account VARCHAR(50);
    v_phone VARCHAR(20);
    doctor_count INTEGER := 0;
BEGIN
    RAISE NOTICE '开始导入医生数据: %', csv_file_path;
    
    -- 创建临时表
    CREATE TEMP TABLE IF NOT EXISTS temp_doctor (
        doctor_id VARCHAR(20),
        doctor_name VARCHAR(50),
        title_id VARCHAR(20),
        department_name VARCHAR(100),
        department_id VARCHAR(20)
    ) ON COMMIT DROP;
    
    -- 清空临时表
    TRUNCATE temp_doctor;
    
    -- 从CSV文件导入数据
    EXECUTE format('COPY temp_doctor FROM %L WITH (FORMAT CSV, HEADER TRUE, ENCODING ''UTF8'')', csv_file_path);
    
    -- 为每个科室创建默认诊室
    FOR rec IN (SELECT DISTINCT department_id, department_name FROM temp_doctor WHERE department_id IS NOT NULL)
    LOOP
        clinic_id := 'CLIN' || LPAD(SUBSTRING(rec.department_id FROM 4)::TEXT, 3, '0');
        
        INSERT INTO clinic (id, clinic_number, location, dep_id)
        VALUES (
            clinic_id,
            rec.department_name || '1号',
            '门诊楼',
            rec.department_id
        )
        ON CONFLICT (id) DO NOTHING;
    END LOOP;
    
    RAISE NOTICE '诊室创建完成';
    
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
            MD5(rec.doctor_id || 'password'),
            rec.doctor_name,
            v_account,
            NULL,
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
        
        doctor_count := doctor_count + 1;
    END LOOP;
    
    RAISE NOTICE '医生数据导入完成: % 条记录', doctor_count;
    
    RETURN doctor_count;
END;
$$ LANGUAGE plpgsql;

-- 函数4: 数据统计
CREATE OR REPLACE FUNCTION show_import_statistics()
RETURNS TABLE (
    category VARCHAR(50),
    count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT '总科室数量'::VARCHAR(50), COUNT(*)::BIGINT FROM department
    UNION ALL
    SELECT '  - 父科室', COUNT(*)::BIGINT FROM department WHERE father_id IS NULL
    UNION ALL
    SELECT '  - 子科室', COUNT(*)::BIGINT FROM department WHERE father_id IS NOT NULL
    UNION ALL
    SELECT '诊室数量', COUNT(*)::BIGINT FROM clinic
    UNION ALL
    SELECT '职称类型', COUNT(*)::BIGINT FROM title_number_source
    UNION ALL
    SELECT '用户数量', COUNT(*)::BIGINT FROM "user"
    UNION ALL
    SELECT '医生数量', COUNT(*)::BIGINT FROM doctor
    UNION ALL
    SELECT '  - 主任医师', COUNT(*)::BIGINT FROM doctor WHERE doc_title_id = 'TITLE001'
    UNION ALL
    SELECT '  - 副主任医师', COUNT(*)::BIGINT FROM doctor WHERE doc_title_id = 'TITLE002'
    UNION ALL
    SELECT '  - 主治医师', COUNT(*)::BIGINT FROM doctor WHERE doc_title_id = 'TITLE003'
    UNION ALL
    SELECT '  - 住院医师', COUNT(*)::BIGINT FROM doctor WHERE doc_title_id = 'TITLE004';
END;
$$ LANGUAGE plpgsql;

/*==============================================================*/
/* 主执行脚本 - 按顺序导入所有数据                               */
/*==============================================================*/
DO $$
DECLARE
    dept_path TEXT := :'department_csv_path';
    title_path TEXT := :'title_csv_path';
    doctor_path TEXT := :'doctor_csv_path';
    result INTEGER;
BEGIN
    RAISE NOTICE '======================================';
    RAISE NOTICE '开始数据导入流程';
    RAISE NOTICE '======================================';
    
    -- 导入科室数据
    result := import_department_data(dept_path);
    
    -- 导入职称号源数据
    result := import_title_data(title_path);
    
    -- 导入医生数据
    result := import_doctor_data(doctor_path);
    
    RAISE NOTICE '======================================';
    RAISE NOTICE '所有数据导入完成！';
    RAISE NOTICE '======================================';
    
EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION '导入过程中发生错误: %', SQLERRM;
END $$;

-- 显示导入统计
SELECT * FROM show_import_statistics();

/*==============================================================*/
/* 使用说明:                                                     */
/*==============================================================*/
/*
1. 修改文件开头的CSV路径变量:
   \set department_csv_path '/path/to/department.csv'
   \set title_csv_path '/path/to/title_number_source.csv'
   \set doctor_csv_path '/path/to/doctor_info.csv'

2. 执行导入:
   psql -U postgres -d hospital_db -f import_data_config.sql

3. 也可以单独调用函数:
   SELECT import_department_data('/path/to/department.csv');
   SELECT import_title_data('/path/to/title_number_source.csv');
   SELECT import_doctor_data('/path/to/doctor_info.csv');

4. 查看统计:
   SELECT * FROM show_import_statistics();
*/
