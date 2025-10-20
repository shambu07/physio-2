CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       full_name VARCHAR(150) NOT NULL,
                       email VARCHAR(180) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       CONSTRAINT uk_users_email UNIQUE (email)
);


CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role VARCHAR(50) NOT NULL,
                            CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id)
);


CREATE TABLE patients (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          user_id BIGINT NOT NULL,
                          phone VARCHAR(25),
                          CONSTRAINT fk_patient_user FOREIGN KEY (user_id) REFERENCES users(id)
);


CREATE TABLE doctors (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         user_id BIGINT NOT NULL,
                         specialization VARCHAR(100),
                         CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES users(id)
);