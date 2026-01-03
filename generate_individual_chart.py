#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
个人代码量统计折线图生成脚本
Individual Workload Statistics Chart Generator
"""

import subprocess
import re
from datetime import datetime, timedelta
import matplotlib.pyplot as plt
import matplotlib.dates as mdates
from collections import defaultdict

# 设置中文字体
plt.rcParams['font.sans-serif'] = ['Microsoft YaHei', 'SimHei', 'Arial Unicode MS']
plt.rcParams['axes.unicode_minus'] = False

def get_individual_git_stats():
    """获取每个人的每日代码统计"""
    # 获取每次提交的作者、日期和代码变更
    cmd = ['git', 'log', 'dev', '--date=short', '--pretty=format:%ad|%an', '--numstat']
    result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8')
    
    # 个人每日新增代码行数
    person_daily_additions = defaultdict(lambda: defaultdict(int))
    current_date = None
    current_author = None
    
    for line in result.stdout.split('\n'):
        line = line.strip()
        # 匹配日期和作者行 (格式: 2025-10-23|ZhiYuan-23301024)
        if '|' in line:
            parts = line.split('|')
            if len(parts) == 2 and re.match(r'^\d{4}-\d{2}-\d{2}$', parts[0]):
                current_date = parts[0]
                current_author = parts[1]
        # 匹配numstat行 (additions deletions filename)
        elif current_date and current_author and re.match(r'^\d+\s+\d+\s+', line):
            parts = line.split('\t')
            if len(parts) >= 2 and parts[0].isdigit():
                additions = int(parts[0])
                person_daily_additions[current_author][current_date] += additions
    
    return person_daily_additions

def calculate_cumulative_data(person_daily_additions):
    """计算每个人的累计工作量"""
    # 获取所有日期范围
    all_dates = set()
    for person_data in person_daily_additions.values():
        all_dates.update(person_data.keys())
    
    if not all_dates:
        return {}, [], []
    
    all_dates = sorted(all_dates)
    start_date = datetime.strptime(all_dates[0], '%Y-%m-%d')
    end_date = datetime.strptime(all_dates[-1], '%Y-%m-%d')
    
    # 生成完整的日期列表
    date_list = []
    current = start_date
    while current <= end_date:
        date_list.append(current)
        current += timedelta(days=1)
    
    # 计算每个人的累计数据
    person_cumulative = {}
    for person, daily_data in person_daily_additions.items():
        cumulative = []
        total = 0
        
        for date in date_list:
            date_str = date.strftime('%Y-%m-%d')
            if date_str in daily_data:
                total += daily_data[date_str]
            cumulative.append(total)
        
        person_cumulative[person] = cumulative
    
    # 计算总工作量（所有人的总和）
    total_workload = [sum(person_cumulative[p][i] for p in person_cumulative) 
                     for i in range(len(date_list))]
    
    return person_cumulative, date_list, total_workload

def plot_individual_workload_chart(person_cumulative, date_list, total_workload):
    """绘制个人工作量折线图"""
    # 定义颜色方案
    colors = ['#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', '#9467bd', '#8c564b', 
              '#e377c2', '#7f7f7f', '#bcbd22', '#17becf']
    
    # 创建图表
    fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(16, 10))
    
    # === 子图1: 个人累计工作量 ===
    sorted_persons = sorted(person_cumulative.items(), 
                           key=lambda x: x[1][-1] if x[1] else 0, 
                           reverse=True)
    
    for idx, (person, cumulative) in enumerate(sorted_persons):
        color = colors[idx % len(colors)]
        final_lines = cumulative[-1] if cumulative else 0
        
        ax1.plot(date_list, cumulative, 
                linewidth=2.5, 
                label=f'{person} ({final_lines:,}行)', 
                marker='o', 
                markersize=4,
                color=color,
                alpha=0.8)
    
    # 添加总计线
    ax1.plot(date_list, total_workload, 
            'k-', 
            linewidth=3, 
            label=f'总计 ({total_workload[-1]:,}行)', 
            marker='s',
            markersize=5,
            alpha=0.6,
            linestyle='--')
    
    ax1.set_title('医院挂号系统 (dev分支) - 个人累计工作量统计', fontsize=18, fontweight='bold', pad=20)
    ax1.set_xlabel('日期', fontsize=13)
    ax1.set_ylabel('累计代码行数', fontsize=13)
    ax1.legend(loc='upper left', fontsize=11, framealpha=0.9)
    ax1.grid(True, alpha=0.3, linestyle='--')
    
    # 标注最终数据点
    for idx, (person, cumulative) in enumerate(sorted_persons[:3]):  # 只标注前3名
        if cumulative:
            ax1.annotate(f'{cumulative[-1]:,}', 
                        xy=(date_list[-1], cumulative[-1]),
                        xytext=(10, 0), 
                        textcoords='offset points',
                        fontsize=9,
                        color=colors[idx % len(colors)],
                        fontweight='bold')
    
    # 格式化x轴
    ax1.xaxis.set_major_formatter(mdates.DateFormatter('%m/%d'))
    ax1.xaxis.set_major_locator(mdates.WeekdayLocator(interval=1))
    plt.setp(ax1.xaxis.get_majorticklabels(), rotation=45, ha='right')
    
    # === 子图2: 个人工作量占比饼图 ===
    final_contributions = {person: cumulative[-1] 
                          for person, cumulative in person_cumulative.items() 
                          if cumulative}
    
    sorted_contributions = sorted(final_contributions.items(), 
                                 key=lambda x: x[1], 
                                 reverse=True)
    
    names = [item[0] for item in sorted_contributions]
    values = [item[1] for item in sorted_contributions]
    percentages = [v / sum(values) * 100 for v in values]
    
    # 绘制饼图
    wedges, texts, autotexts = ax2.pie(values, 
                                        labels=None,
                                        autopct='%1.1f%%',
                                        startangle=90,
                                        colors=[colors[i % len(colors)] for i in range(len(values))],
                                        textprops={'fontsize': 11, 'weight': 'bold'})
    
    ax2.set_title('个人工作量占比分布', fontsize=14, fontweight='bold', pad=15)
    
    # 添加图例
    legend_labels = [f'{name}: {value:,}行 ({pct:.1f}%)' 
                    for name, value, pct in zip(names, values, percentages)]
    ax2.legend(legend_labels, loc='center left', bbox_to_anchor=(1, 0, 0.5, 1), fontsize=10)
    
    # 添加统计信息文本框
    total = sum(values)
    avg = total / len(values) if values else 0
    max_person = names[0] if names else 'N/A'
    max_lines = values[0] if values else 0
    
    stats_text = f'''统计信息:
    总代码行数: {total:,} 行
    贡献者数量: {len(values)} 人
    人均贡献: {avg:,.0f} 行
    最高贡献: {max_person}
                {max_lines:,} 行 ({percentages[0]:.1f}%)
    '''
    
    ax1.text(0.02, 0.98, stats_text, transform=ax1.transAxes,
            fontsize=11, verticalalignment='top', horizontalalignment='left',
            bbox=dict(boxstyle='round', facecolor='wheat', alpha=0.7),
            family='monospace')
    
    plt.tight_layout()
    
    # 保存图表
    output_file = 'Individual_Workload_Chart_dev.png'
    plt.savefig(output_file, dpi=300, bbox_inches='tight')
    print(f'\n✓ 个人工作量图表已保存: {output_file}')
    
    plt.show()

def print_statistics(person_cumulative):
    """打印详细统计信息"""
    print('\n=== 个人工作量排名 ===\n')
    
    final_contributions = {person: cumulative[-1] 
                          for person, cumulative in person_cumulative.items() 
                          if cumulative}
    
    sorted_contributions = sorted(final_contributions.items(), 
                                 key=lambda x: x[1], 
                                 reverse=True)
    
    total = sum(final_contributions.values())
    
    print(f'{"排名":<6} {"贡献者":<25} {"代码行数":>12} {"占比":>8}')
    print('-' * 60)
    
    for idx, (person, lines) in enumerate(sorted_contributions, 1):
        percentage = (lines / total * 100) if total > 0 else 0
        print(f'{idx:<6} {person:<25} {lines:>12,} {percentage:>7.1f}%')
    
    print('-' * 60)
    print(f'{"总计":<6} {"":25} {total:>12,} {100.0:>7.1f}%')

def main():
    print('=== 医院挂号系统 - 个人工作量统计图表生成器 ===\n')
    
    print('1. 从Git仓库获取个人数据...')
    person_daily_additions = get_individual_git_stats()
    print(f'   ✓ 共收集 {len(person_daily_additions)} 位贡献者的数据')
    
    print('\n2. 计算累计工作量...')
    person_cumulative, date_list, total_workload = calculate_cumulative_data(person_daily_additions)
    print(f'   ✓ 生成 {len(date_list)} 个时间点的数据')
    
    print('\n3. 生成统计报告...')
    print_statistics(person_cumulative)
    
    print('\n4. 绘制图表...')
    plot_individual_workload_chart(person_cumulative, date_list, total_workload)
    
    print('\n完成！')

if __name__ == '__main__':
    main()
