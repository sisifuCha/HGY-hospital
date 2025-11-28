-- shift_schedule_date.sql
-- 用途：将 doc_schedule_record.schedule_date 向后平移指定天数
-- 包含预览与执行模式：调用函数时通过最后一个参数决定是否执行（false=预览，true=执行）
DROP FUNCTION IF EXISTS public.shift_doc_schedule_date(
    p_days integer,
    p_doc_id varchar,
    p_from_date date,
    p_to_date date,
    p_execute boolean
);
-- 创建或替换函数：
CREATE OR REPLACE FUNCTION public.shift_doc_schedule_date(
    p_days integer,
    p_doc_id varchar DEFAULT NULL,
    p_from_date date DEFAULT NULL,
    p_to_date date DEFAULT NULL,
    p_execute boolean DEFAULT false
)
RETURNS TABLE(schedule_id varchar, doctor_id varchar, old_date date, new_date date)
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT p_execute THEN
        -- 预览模式
        RETURN QUERY
        SELECT 
            dsr.id::varchar, 
            dsr.doc_id, 
            dsr.schedule_date, 
            (dsr.schedule_date + p_days)
        FROM doc_schedule_record dsr
        WHERE (p_doc_id IS NULL OR dsr.doc_id = p_doc_id)
          AND (p_from_date IS NULL OR dsr.schedule_date >= p_from_date)
          AND (p_to_date IS NULL OR dsr.schedule_date <= p_to_date)
        ORDER BY dsr.id;
    ELSE
        -- 执行模式
        RETURN QUERY
        UPDATE doc_schedule_record
        SET schedule_date = schedule_date + p_days
        WHERE (p_doc_id IS NULL OR doc_id = p_doc_id)
          AND (p_from_date IS NULL OR schedule_date >= p_from_date)
          AND (p_to_date IS NULL OR schedule_date <= p_to_date)
        RETURNING 
            id::varchar,
            doc_id,
            (schedule_date - p_days),
            schedule_date;
    END IF;
END;
$$;

-- 使用示例：
-- 1) 预览将要被修改的记录（不修改）
-- SELECT * FROM public.shift_doc_schedule_date(7, 'D001', '2025-11-01', '2025-12-31', false);

-- 2) 实际执行更新（会修改数据并返回修改后的记录）
-- BEGIN; SELECT * FROM public.shift_doc_schedule_date(7, 'D001', '2025-11-01', '2025-12-31', true); COMMIT;

-- 3) 全库范围（谨慎） - 仅预览
-- 注意：下面的示例已注释，避免误触发全量更新。需要执行时请手动解除注释并谨慎运行。
SELECT * FROM public.shift_doc_schedule_date(7, NULL, NULL, NULL, true);

-- 4) 重要提示：在生产库执行前，请先备份数据或在维护窗口执行。
