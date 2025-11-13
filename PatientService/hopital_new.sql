create table public.add_number_source_record (
                                                 foreign key ("1") references public.patient (id)
                                                     match simple on update cascade on delete cascade,
                                                 foreign key ("2") references public.doc_schedule_record (id)
                                                     match simple on update cascade on delete cascade
);
comment on table public.add_number_source_record is '加号申请记录表';

create table public.alternate_record (
                                         foreign key ("1") references public.patient (id)
                                             match simple on update cascade on delete cascade,
                                         foreign key ("2") references public.doc_schedule_record (id)
                                             match simple on update cascade on delete cascade
);
comment on table public.alternate_record is '候补挂号记录表';

create table public.blacklist (
                                  foreign key ("2") references public.sensitive_operation (id)
                                      match simple on update cascade on delete set null,
                                  foreign key ("3") references public.sensitive_operation (id)
                                      match simple on update cascade on delete set null,
                                  foreign key ("4") references public.sensitive_operation (id)
                                      match simple on update cascade on delete set null
);
comment on table public.blacklist is '黑名单表';

create table public.cancel_record (
                                      foreign key ("1") references public.patient (id)
                                          match simple on update cascade on delete cascade,
                                      foreign key ("2") references public.doc_schedule_record (id)
                                          match simple on update cascade on delete cascade
);
comment on table public.cancel_record is '取消挂号记录表';

create table public.clinic (
                               foreign key ("4") references public.department (id)
                                   match simple on update cascade on delete set null
);
create index idx_clinic_department on clinic using btree ("4");
comment on table public.clinic is '诊室信息表';

create table public.department (
                                   foreign key ("3") references public.department (id)
                                       match simple on update cascade on delete set null
);
create index idx_department_father on department using btree ("3");
create index idx_department_name on department using btree ("2");
comment on table public.department is '医院科室表';

create table public.doc_schedule_change_record (
                                                   foreign key ("1") references public.doctor (id)
                                                       match simple on update cascade on delete cascade,
                                                   foreign key ("2") references public.doc_schedule_record (id)
                                                       match simple on update cascade on delete cascade
);
comment on table public.doc_schedule_change_record is '医生排班变更记录表';

create table public.doc_schedule_record (
                                            foreign key ("5") references public.doctor (id)
                                                match simple on update cascade on delete cascade,
                                            foreign key ("2") references public.schedule_template (id)
                                                match simple on update cascade on delete set null
);
create index idx_schedule_record_date on doc_schedule_record using btree ("3");
create index idx_schedule_record_doctor on doc_schedule_record using btree ("5");
comment on table public.doc_schedule_record is '医生实际排班记录表';

create table public.doctor (
                               foreign key ("4") references public.clinic (id)
                                   match simple on update cascade on delete set null,
                               foreign key ("2") references public.title_number_source (id)
                                   match simple on update cascade on delete set null,
                               foreign key ("1") references public."user" (id)
                                   match simple on update cascade on delete cascade
);
create index idx_doctor_clinic on doctor using btree ("4");
create index idx_doctor_title on doctor using btree ("2");
create index idx_doctor_status on doctor using btree ("3");
comment on table public.doctor is '医生信息表';

create table public.medical_insurance (
);
comment on table public.medical_insurance is '医保账户信息表';

create table public.patient (
                                foreign key ("4") references public.medical_insurance (id)
                                    match simple on update cascade on delete set null,
                                foreign key ("5") references public.reimburse_type (id)
                                    match simple on update cascade on delete set null,
                                foreign key ("1") references public."user" (id)
                                    match simple on update cascade on delete cascade
);
create index idx_patient_id_num on patient using btree ("3");
create index idx_patient_medical_insurance on patient using btree ("4");
comment on table public.patient is '患者信息表';

create table public.pay_record (
                                   foreign key ("6") references public.patient (id)
                                       match simple on update cascade on delete set null,
                                   foreign key ("7") references public.doc_schedule_record (id)
                                       match simple on update cascade on delete set null
);
create index idx_pay_time on pay_record using btree ("2");
create index idx_pay_status on pay_record using btree ("3");
create index idx_pay_patient on pay_record using btree ("6");
comment on table public.pay_record is '支付记录表';

create table public.register_record (
                                        foreign key ("1") references public.patient (id)
                                            match simple on update cascade on delete cascade,
                                        foreign key ("2") references public.doc_schedule_record (id)
                                            match simple on update cascade on delete cascade
);
create index idx_register_time on register_record using btree ("3");
create index idx_register_status on register_record using btree ("4");
create index idx_register_patient on register_record using btree ("1");
create index idx_register_schedule on register_record using btree ("2");
comment on table public.register_record is '挂号记录表';

create table public.reimburse_type (
);
comment on table public.reimburse_type is '报销类型配置表';

create table public.schedule_template (
                                          foreign key ("4") references public.clinic (id)
                                              match simple on update cascade on delete set null
);
create index idx_schedule_template_clinic on schedule_template using btree ("4");
comment on table public.schedule_template is '医生排班时间模板';

create table public.sensitive_operation (
                                            foreign key ("2") references public.patient (id)
                                                match simple on update cascade on delete cascade
);
create index idx_sensitive_patient on sensitive_operation using btree ("2");
create index idx_sensitive_time on sensitive_operation using btree ("4");
create index idx_sensitive_type on sensitive_operation using btree ("3");
comment on table public.sensitive_operation is '敏感操作记录表';

create table public.title_number_source (
);
comment on table public.title_number_source is '医生职称号源配置表';

create table public."user" (
);
create index idx_user_email on "user" using btree ("2");
create index idx_user_account on "user" using btree ("5");
create index idx_user_phone on "user" using btree ("7");
comment on table public."user" is '用户基础信息表';

