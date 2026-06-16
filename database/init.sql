CREATE DATABASE IF NOT EXISTS smartestate_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smartestate_db;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone VARCHAR(32) NOT NULL UNIQUE,
  password_hash VARCHAR(160) NOT NULL,
  nickname VARCHAR(64) NOT NULL,
  avatar VARCHAR(255) DEFAULT NULL,
  role ENUM('resident','staff','admin') NOT NULL DEFAULT 'resident',
  building VARCHAR(32) DEFAULT NULL,
  unit VARCHAR(32) DEFAULT NULL,
  room VARCHAR(32) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS repairs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(120) NOT NULL,
  description TEXT NOT NULL,
  type ENUM('water_power','furniture','public_facility','other') NOT NULL DEFAULT 'other',
  images TEXT DEFAULT NULL,
  status ENUM('pending','assigned','processing','done','closed') NOT NULL DEFAULT 'pending',
  handler_id BIGINT DEFAULT NULL,
  rating INT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_repairs_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_repairs_handler FOREIGN KEY (handler_id) REFERENCES users(id),
  INDEX idx_repairs_status (status),
  INDEX idx_repairs_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  fee_type ENUM('property','parking','utilities') NOT NULL DEFAULT 'property',
  amount DECIMAL(10,2) NOT NULL,
  month VARCHAR(7) NOT NULL,
  status ENUM('unpaid','paid','overdue') NOT NULL DEFAULT 'unpaid',
  paid_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_payments_user_month (user_id, month),
  INDEX idx_payments_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS announcements (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(160) NOT NULL,
  content TEXT NOT NULL,
  category ENUM('notice','event','urgent') NOT NULL DEFAULT 'notice',
  publisher_id BIGINT NOT NULL,
  publish_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  top TINYINT(1) NOT NULL DEFAULT 0,
  read_count INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_announcements_publisher FOREIGN KEY (publisher_id) REFERENCES users(id),
  INDEX idx_announcements_top_publish (top, publish_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS announcement_reads (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  announcement_id BIGINT NOT NULL,
  read_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_reads_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_reads_announcement FOREIGN KEY (announcement_id) REFERENCES announcements(id),
  UNIQUE KEY uk_user_announcement (user_id, announcement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(32) NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS permissions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(80) NOT NULL UNIQUE,
  name VARCHAR(80) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS role_permissions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(32) NOT NULL,
  permission_code VARCHAR(80) NOT NULL,
  UNIQUE KEY uk_role_permission (role_code, permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS operation_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT DEFAULT NULL,
  role VARCHAR(32) DEFAULT NULL,
  action VARCHAR(80) NOT NULL,
  entity_name VARCHAR(80) NOT NULL,
  entity_id BIGINT DEFAULT NULL,
  message VARCHAR(500) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_operation_logs_action (action),
  INDEX idx_operation_logs_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO roles(code, name) VALUES
('resident', '业主'),
('staff', '物业'),
('admin', '管理员')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO permissions(code, name) VALUES
('dashboard:view', '查看工作台'),
('repair:view', '查看报修'),
('repair:create', '提交报修'),
('repair:assign', '分配报修'),
('repair:update', '更新报修'),
('payment:view', '查看费用'),
('payment:pay', '模拟缴费'),
('announcement:view', '查看公告'),
('announcement:publish', '发布公告'),
('user:profile', '编辑个人资料'),
('operationLog:view', '查看操作日志'),
('facility:view', '查看设施'),
('facility:book', '预约设施'),
('facility:manage', '管理设施'),
('facility:slotManage', '管理时段'),
('facility:bookingView', '查看预约')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO role_permissions(role_code, permission_code) VALUES
('resident','dashboard:view'),
('resident','repair:view'),
('resident','repair:create'),
('resident','payment:view'),
('resident','payment:pay'),
('resident','announcement:view'),
('resident','user:profile'),
('resident','facility:view'),
('resident','facility:book'),
('staff','dashboard:view'),
('staff','repair:view'),
('staff','repair:assign'),
('staff','repair:update'),
('staff','payment:view'),
('staff','announcement:view'),
('staff','announcement:publish'),
('staff','user:profile'),
('staff','facility:view'),
('staff','facility:manage'),
('staff','facility:slotManage'),
('staff','facility:bookingView'),
('admin','dashboard:view'),
('admin','repair:view'),
('admin','repair:create'),
('admin','repair:assign'),
('admin','repair:update'),
('admin','payment:view'),
('admin','payment:pay'),
('admin','announcement:view'),
('admin','announcement:publish'),
('admin','user:profile'),
('admin','operationLog:view'),
('admin','facility:view'),
('admin','facility:book'),
('admin','facility:manage'),
('admin','facility:slotManage'),
('admin','facility:bookingView')
ON DUPLICATE KEY UPDATE permission_code = VALUES(permission_code);

INSERT INTO users(id, phone, password_hash, nickname, avatar, role, building, unit, room) VALUES
(1, '13800000001', '1000:demo-admin-salt:74d2c1e9f73', '林经理', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200', 'admin', '物业中心', 'A', '001'),
(2, '13800000002', '1000:demo-staff-salt:4e6f8a2121a', '周管家', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200', 'staff', '物业中心', 'B', '002'),
(3, '13800000003', '1000:demo-resident-salt:8d9e3c6b2a1', '陈业主', 'https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=200', 'resident', '8栋', '2单元', '1201')
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), role = VALUES(role);

INSERT INTO repairs(id, user_id, title, description, type, images, status, handler_id, rating) VALUES
(1, 3, '厨房水槽下方渗水', '夜间用水后柜体底部积水，需要尽快检查管线。', 'water_power', '', 'processing', 2, NULL),
(2, 3, '单元门禁无法识别', '门禁刷卡成功但门锁不弹开，影响晚归通行。', 'public_facility', '', 'assigned', 2, NULL),
(3, 3, '客厅吊灯闪烁', '灯具频闪，疑似线路接触不良。', 'water_power', '', 'pending', NULL, NULL)
ON DUPLICATE KEY UPDATE title = VALUES(title), status = VALUES(status);

INSERT INTO payments(id, user_id, fee_type, amount, month, status, paid_at) VALUES
(1, 3, 'property', 426.00, '2026-06', 'unpaid', NULL),
(2, 3, 'parking', 280.00, '2026-06', 'paid', '2026-06-03 10:20:00'),
(3, 3, 'utilities', 168.50, '2026-05', 'paid', '2026-05-28 18:00:00')
ON DUPLICATE KEY UPDATE amount = VALUES(amount), status = VALUES(status);

INSERT INTO announcements(id, title, content, category, publisher_id, publish_at, top, read_count) VALUES
(1, '暴雨天气地下车库巡检安排', '6月15日晚间物业将加强排水设备巡检，请车主留意车库广播。', 'urgent', 2, '2026-06-15 09:30:00', 1, 42),
(2, '端午社区便民服务开放预约', '本周六开放家电清洗、磨刀、义诊服务，业主可在物业前台预约。', 'event', 2, '2026-06-12 14:00:00', 0, 128),
(3, '6月公共区域消杀通知', '6月18日9:00-11:30进行楼道及地库消杀，请提前收好门口物品。', 'notice', 2, '2026-06-10 08:40:00', 0, 87)
ON DUPLICATE KEY UPDATE title = VALUES(title), top = VALUES(top);

CREATE TABLE IF NOT EXISTS facilities (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  description TEXT,
  image VARCHAR(255) DEFAULT NULL,
  location VARCHAR(160) DEFAULT NULL,
  status ENUM('active','inactive') NOT NULL DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_facilities_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS facility_time_slots (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  facility_id BIGINT NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  capacity INT NOT NULL DEFAULT 1,
  weekday TINYINT DEFAULT NULL COMMENT '1-7 对应周一到周日，NULL表示每天',
  status ENUM('active','inactive') NOT NULL DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_slots_facility FOREIGN KEY (facility_id) REFERENCES facilities(id),
  INDEX idx_slots_facility (facility_id),
  INDEX idx_slots_weekday (weekday)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS facility_bookings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  facility_id BIGINT NOT NULL,
  slot_id BIGINT NOT NULL,
  booking_date DATE NOT NULL,
  status ENUM('booked','cancelled','completed') NOT NULL DEFAULT 'booked',
  remark VARCHAR(200) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_bookings_facility FOREIGN KEY (facility_id) REFERENCES facilities(id),
  CONSTRAINT fk_bookings_slot FOREIGN KEY (slot_id) REFERENCES facility_time_slots(id),
  UNIQUE KEY uk_user_slot_date (user_id, slot_id, booking_date),
  INDEX idx_bookings_facility_date (facility_id, booking_date),
  INDEX idx_bookings_user (user_id),
  INDEX idx_bookings_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO facilities(id, name, description, image, location, status) VALUES
(1, '社区健身房', '配备跑步机、哑铃、椭圆机等基础健身器材，业主凭门禁卡入场。', 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=600', '2号楼负一层', 'active'),
(2, '室内游泳池', '25米标准恒温泳池，设有浅水区与深水区，需佩戴泳帽入场。', 'https://images.unsplash.com/photo-1576013551627-0cc20b96c2a7?w=600', '会所一层', 'active'),
(3, '棋牌室', '配备麻将桌与棋牌桌椅，适合业主休闲娱乐。', 'https://images.unsplash.com/photo-1606167668584-78701c57f13d?w=600', '3号楼架空层', 'active'),
(4, '多功能厅', '可用于社区活动、会议、小型演出，可通过物业预约使用。', 'https://images.unsplash.com/photo-1505373877841-8d25f7d46678?w=600', '会所二层', 'active')
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status);

INSERT INTO facility_time_slots(id, facility_id, start_time, end_time, capacity, weekday, status) VALUES
(1, 1, '06:00:00', '08:00:00', 10, NULL, 'active'),
(2, 1, '08:00:00', '10:00:00', 15, NULL, 'active'),
(3, 1, '10:00:00', '12:00:00', 15, NULL, 'active'),
(4, 1, '14:00:00', '16:00:00', 15, NULL, 'active'),
(5, 1, '16:00:00', '18:00:00', 20, NULL, 'active'),
(6, 1, '18:00:00', '20:00:00', 20, NULL, 'active'),
(7, 1, '20:00:00', '22:00:00', 10, NULL, 'active'),
(8, 2, '07:00:00', '09:00:00', 20, NULL, 'active'),
(9, 2, '09:00:00', '11:00:00', 25, NULL, 'active'),
(10, 2, '14:00:00', '16:00:00', 25, NULL, 'active'),
(11, 2, '16:00:00', '18:00:00', 30, NULL, 'active'),
(12, 2, '18:00:00', '20:00:00', 25, NULL, 'active'),
(13, 2, '20:00:00', '21:30:00', 15, NULL, 'active'),
(14, 3, '09:00:00', '11:00:00', 8, NULL, 'active'),
(15, 3, '14:00:00', '17:00:00', 8, NULL, 'active'),
(16, 3, '19:00:00', '22:00:00', 8, NULL, 'active'),
(17, 4, '09:00:00', '12:00:00', 50, NULL, 'active'),
(18, 4, '14:00:00', '17:00:00', 50, NULL, 'active'),
(19, 4, '18:00:00', '21:00:00', 50, NULL, 'active')
ON DUPLICATE KEY UPDATE start_time = VALUES(start_time), end_time = VALUES(end_time), capacity = VALUES(capacity);
