-- Historical partial schema example; NOT a complete application migration.
-- Run only in an isolated development schema after review.

CREATE TABLE patient (
    patient_id NUMBER PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    age NUMBER,
    gender VARCHAR2(10),
    email VARCHAR2(100)
);

CREATE TABLE patient_phone (
    patient_id NUMBER,
    phone VARCHAR2(20),
    PRIMARY KEY (patient_id, phone),
    FOREIGN KEY (patient_id) REFERENCES patient(patient_id)
);

CREATE TABLE doctor (
    doctor_id NUMBER PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    specialty VARCHAR2(100)
);

CREATE TABLE doctor_phone (
    doctor_id NUMBER,
    phone VARCHAR2(20),
    PRIMARY KEY (doctor_id, phone),
    FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id)
);

CREATE TABLE symptom (
    symptom_id NUMBER PRIMARY KEY,
    symptom_name VARCHAR2(100) NOT NULL
);

CREATE TABLE triage_session (
    session_id NUMBER PRIMARY KEY,
    patient_id NUMBER,
    doctor_id NUMBER,
    session_date DATE,
    status VARCHAR2(30),
    FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
    FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id)
);

CREATE TABLE session_symptom (
    session_id NUMBER,
    symptom_id NUMBER,
    severity VARCHAR2(20),
    PRIMARY KEY (session_id, symptom_id),
    FOREIGN KEY (session_id) REFERENCES triage_session(session_id),
    FOREIGN KEY (symptom_id) REFERENCES symptom(symptom_id)
);

CREATE TABLE ai_diagnosis (
    diagnosis_id NUMBER PRIMARY KEY,
    session_id NUMBER,
    diagnosis_result VARCHAR2(255),
    confidence_score NUMBER(5,2),
    FOREIGN KEY (session_id) REFERENCES triage_session(session_id)
);

CREATE TABLE patient_history (
    history_id NUMBER PRIMARY KEY,
    patient_id NUMBER,
    description VARCHAR2(255),
    history_date DATE,
    FOREIGN KEY (patient_id) REFERENCES patient(patient_id)
);

ALTER TABLE patient
ADD address VARCHAR2(200);

ALTER TABLE doctor
MODIFY specialty VARCHAR2(150);

ALTER TABLE patient
ADD CONSTRAINT unique_email UNIQUE(email);
