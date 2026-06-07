-- sample data for demo
INSERT INTO teacher (id, teacher_no, name, title) VALUES (1, 'T1001', '张老师', '讲师');
INSERT INTO student (id, student_no, name, phone, clazz) VALUES (1, 'S1001', '学生甲', '13800000000', '计科2022');
INSERT INTO course (id, code, name, credit, time_slot, place, capacity, enrolled, teacher_id) VALUES (1, 'C001', '高等数学', 3, 'MON_09_11', 'A101', 30, 0, 1);
INSERT INTO course (id, code, name, credit, time_slot, place, capacity, enrolled, teacher_id) VALUES (2, 'C002', '程序设计基础', 2, 'TUE_14_16', 'B201', 25, 0, 1);
