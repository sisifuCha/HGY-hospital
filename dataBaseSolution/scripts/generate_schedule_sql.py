# -*- coding: utf-8 -*-
"""
生成 DEP003 科室的医生排班记录 SQL（20条，分布在3周内）
不需要数据库连接，根据 CSV 数据生成 SQL
"""
import csv
from datetime import datetime, timedelta
from pathlib import Path
import random

def get_week_start(date):
    """获取给定日期所在周的周一"""
    return date - timedelta(days=date.weekday())

def read_csv(filepath, has_header=True):
    """读取 CSV 文件"""
    rows = []
    with open(filepath, 'r', encoding='utf-8-sig', newline='') as f:
        reader = csv.reader(f)
        if has_header:
            header = next(reader, None)
        for row in reader:
            if row:
                rows.append(row)
    return rows

def main():
    base_path = Path(__file__).resolve().parents[1]
    
    # 1. 读取医生信息（doctor_info.csv）
    doctor_info_rows = read_csv(base_path / 'doctor_info.csv')
    # doctor_id, doctor_name, title_id, department_name, department_id
    dep003_doctors = [(row[0], row[2], row[4]) for row in doctor_info_rows if len(row) >= 5 and row[4] == 'DEP013']
    
    if not dep003_doctors:
        print("警告: 未找到 DEP013 科室的医生")
        print("可用的科室:")
        depts = set(row[4] for row in doctor_info_rows if len(row) >= 5)
        for dept in sorted(depts):
            count = sum(1 for row in doctor_info_rows if len(row) >= 5 and row[4] == dept)
            print(f"  {dept}: {count} 位医生")
        return
    
    print(f"找到 {len(dep003_doctors)} 位 DEP013（泌尿外科门诊）科室的医生:")
    for doc_id, title_id, dept_id in dep003_doctors[:5]:
        print(f"  {doc_id} - 职称: {title_id}")
    
    # 2. 读取医生的诊室信息（doctor.csv）
    doctor_rows = read_csv(base_path / 'doctor.csv')
    # 建立 doctor_id -> clinic_id 的映射
    doc_clinic_map = {}
    for row in doctor_rows:
        if len(row) >= 4:
            doc_clinic_map[row[0]] = row[3]
    
    # 3. 读取职称号源配置
    title_source_rows = read_csv(base_path / 'title_number_source.csv')
    # id, name, number_source_count, ori_cost
    title_sources = {row[0]: int(row[2]) for row in title_source_rows if len(row) >= 3}
    print(f"\n职称号源配置: {title_sources}")
    
    # 4. 读取排班模板（schedule_time.csv 无表头）
    template_file = base_path / 'schedule_time.csv'
    if template_file.exists():
        template_rows = read_csv(template_file, has_header=False)
        # CSV格式: id, start_time, end_time, clinic_id
        templates = [(row[0].strip(), row[1].strip(), row[2].strip(), row[3].strip() if len(row) > 3 else '') for row in template_rows if len(row) >= 4]
    else:
        # 使用默认模板
        templates = [
            ('TIME0001', '08:00:00', '12:30:00', 'CLIN005'),
            ('TIME0002', '13:30:00', '18:00:00', 'CLIN005')
        ]
    
    print(f"\n找到 {len(templates)} 个排班模板:")
    for tpl in templates:
        print(f"  {tpl[0]}: {tpl[1]}-{tpl[2]} (诊室: {tpl[3]})")
    
    # 5. 计算当周开始日期（2025-11-03 周一）
    today = datetime(2025, 11, 6)  # 当前日期
    week_start = get_week_start(today)
    print(f"当周开始日期: {week_start.date()}")
    
    # 6. 生成3周的日期范围（21天）
    date_range = [week_start + timedelta(days=i) for i in range(21)]
    
    # 7. 生成20条排班记录
    records = []
    start_id = 1001  # 起始ID
    
    for i in range(20):
        # 随机选择医生
        doc_id, title_id, dept_id = random.choice(dep003_doctors)
        
        # 随机选择日期
        schedule_date = random.choice(date_range)
        
        # 随机选择模板
        template_id, start_time, end_time, _ = random.choice(templates)
        
        # 获取该职称的初始号源数
        initial_count = title_sources.get(title_id, 50)
        # 剩余号源随机减少0-20%
        left_count = int(initial_count * random.uniform(0.8, 1.0))
        
        # 生成记录ID
        record_id = f"SCH{str(start_id + i).zfill(4)}"
        
        records.append({
            'id': record_id,
            'template_id': template_id,
            'schedule_date': schedule_date.strftime('%Y-%m-%d'),
            'left_source_count': left_count,
            'doc_id': doc_id,
            'title_id': title_id,
            'initial_count': initial_count,
            'start_time': start_time,
            'end_time': end_time
        })
    
    # 8. 生成 SQL 插入语句
    sql_file = Path(__file__).resolve().parent / 'insert_schedule_records_DEP013.sql'
    with open(sql_file, 'w', encoding='utf-8') as f:
        f.write("-- 向 DEP013（泌尿外科门诊）科室插入3周排班记录（20条）\n")
        f.write(f"-- 生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write(f"-- 日期范围: {week_start.date()} 至 {(week_start + timedelta(days=20)).date()}\n")
        f.write(f"-- 共 {len(dep003_doctors)} 位医生参与排班\n\n")
        
        for rec in records:
            comment = f"-- {rec['doc_id']}({rec['title_id']}) 在 {rec['schedule_date']} {rec['template_id']} 初始号源:{rec['initial_count']} 剩余:{rec['left_source_count']}"
            sql = f"INSERT INTO doc_schedule_record (id, template_id, schedule_date, left_source_count, doc_id) VALUES ('{rec['id']}', '{rec['template_id']}', '{rec['schedule_date']}', {rec['left_source_count']}, '{rec['doc_id']}');"
            f.write(f"{comment}\n{sql}\n\n")
    
    print(f"\n✓ 成功生成 {len(records)} 条排班记录到文件:")
    print(f"  {sql_file}")
    
    print("\n前10条记录预览:")
    for i, rec in enumerate(records[:10]):
        print(f"{i+1}. {rec['id']}: {rec['doc_id']}({rec['title_id']}) {rec['schedule_date']} {rec['template_id']} 剩余号源:{rec['left_source_count']}/{rec['initial_count']}")
    
    print(f"\n统计信息:")
    dates = set(rec['schedule_date'] for rec in records)
    doctors_used = set(rec['doc_id'] for rec in records)
    print(f"  涉及日期: {len(dates)} 天")
    print(f"  涉及医生: {len(doctors_used)} 位")
    print(f"  总记录数: {len(records)} 条")
    
    print(f"\n下一步:")
    print(f"  方式1: 在数据库客户端中执行生成的 SQL 文件")
    print(f"  方式2: 使用以下命令执行:")
    print(f'    psql -h localhost -p 63333 -U root -d hospital_new -f "{sql_file}"')

if __name__ == '__main__':
    main()
