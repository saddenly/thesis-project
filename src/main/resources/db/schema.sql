-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS progress CASCADE;
DROP TABLE IF EXISTS enrollments CASCADE;
DROP TABLE IF EXISTS lessons CASCADE;
DROP TABLE IF EXISTS courses CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create roles table
CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE
);

-- Create users table
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       locked BOOLEAN NOT NULL DEFAULT FALSE,
                       provider VARCHAR(50) DEFAULT 'local',
                       provider_id VARCHAR(255),
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP
);

-- Create a junction table for users and roles (many-to-many)
CREATE TABLE user_roles (
                            user_id INTEGER NOT NULL,
                            role_id INTEGER NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Create courses table
CREATE TABLE courses (
                         id SERIAL PRIMARY KEY,
                         title VARCHAR(255) NOT NULL,
                         description TEXT,
                         image_url TEXT,
                         instructor_id INTEGER NOT NULL,
                         published BOOLEAN NOT NULL DEFAULT FALSE,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP,
                         published_at TIMESTAMP,
                         FOREIGN KEY (instructor_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create lessons table
CREATE TABLE lessons (
                         id SERIAL PRIMARY KEY,
                         title VARCHAR(255) NOT NULL,
                         content TEXT,
                         video_url TEXT,
                         order_index INTEGER NOT NULL,
                         published BOOLEAN NOT NULL DEFAULT FALSE,
                         duration_minutes INTEGER,
                         additional_resources TEXT,
                         course_id INTEGER NOT NULL,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP,
                         FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- Create enrollments table
CREATE TABLE enrollments (
                             id SERIAL PRIMARY KEY,
                             student_id INTEGER NOT NULL,
                             course_id INTEGER NOT NULL,
                             enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             completed_at TIMESTAMP,
                             last_accessed_at TIMESTAMP,
                             active BOOLEAN NOT NULL DEFAULT TRUE,
                             FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
                             FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                             UNIQUE (student_id, course_id)
);

-- Create a progress table
CREATE TABLE progress (
                          id SERIAL PRIMARY KEY,
                          student_id INTEGER NOT NULL,
                          course_id INTEGER NOT NULL,
                          lesson_id INTEGER NOT NULL,
                          completed BOOLEAN NOT NULL DEFAULT FALSE,
                          completed_at TIMESTAMP,
                          FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
                          FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                          FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
                          UNIQUE (student_id, lesson_id)
);

-- Insert default roles
INSERT INTO roles (name) VALUES ('STUDENT');
INSERT INTO roles (name) VALUES ('INSTRUCTOR');
INSERT INTO roles (name) VALUES ('ADMIN');

-- Insert admin user
INSERT INTO users (email, password, first_name, last_name, enabled, locked, created_at)
VALUES ('admin@edu.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Admin', 'User', TRUE, FALSE, CURRENT_TIMESTAMP);

-- Assign an ADMIN role to an admin user
INSERT INTO user_roles (user_id, role_id)
VALUES (1, (SELECT id FROM roles WHERE name = 'ADMIN'));

-- Create indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_courses_instructor ON courses(instructor_id);
CREATE INDEX idx_lessons_course ON lessons(course_id);
CREATE INDEX idx_enrollments_student ON enrollments(student_id);
CREATE INDEX idx_enrollments_course ON enrollments(course_id);
CREATE INDEX idx_progress_student ON progress(student_id);
CREATE INDEX idx_progress_course ON progress(course_id);
CREATE INDEX idx_progress_lesson ON progress(lesson_id);

-- Output success message
SELECT 'Educational Platform database schema created successfully!' AS result;
