create table persons (
	person_id int primary key generated always as identity,
	person_first_name varchar(50) not null,
	person_name varchar(50) not null,
	person_middle_name varchar(50) not null,
	person_phone varchar(20) not null unique,
	person_mail varchar(30) not null unique
);

insert into persons values 
(default, 'Петров', 'Пётр', 'Петрович', '81111111111', '1@yndex.ru'),
(default, 'Иванов', 'Иван', 'Иванович', '82222222222', '2@yndex.ru'),
(default, 'Чернов', 'Алексей', 'Александрович', '83333333333', '3@yndex.ru');

create table banks (
	bank_bik char(9) primary key,
	bank_name varchar(50) not null unique
);

insert into banks values 
('111111111', 'Сбер'),
('222222222', 'Альфа'),
('333333333', 'Тинькофф');

create table companies (
	company_id smallint primary key generated always as identity,
	company_name varchar(120) not null unique,
	company_short_name varchar(50),
	company_inn char(10) not null unique,
	company_kpp char(9) not null unique,
	company_ogrn char(13) not null unique,
	company_address varchar(150) not null unique,
	company_actual_address varchar(150),
	company_raschetniy_chet varchar(30) not null unique,
	bank_bik char(9) not null references banks(bank_bik),
	company_cor_chet varchar,
	company_phone varchar(20) not null unique,
	company_sait_address varchar unique,
	company_mail varchar(30) unique,
	person_id int not null references persons(person_id)
);

insert into companies values
(default, 'Компания "Альфа"', null, '1111111111', '111111111', '1111111111111', 'адрес 1', 'адрес 1', '123', '111111111', null, '81111111111', null, null, 1),
(default, 'ООО "Бета"', null, '2222222222', '222222222', '2222222222222', 'адрес 2', 'адрес 2', '456', '222222222', null, '82222222222', null, null, 2),
(default, 'ИП "Гамма"', null, '3333333333', '333333333', '3333333333333', 'адрес 3', 'адрес 3', '789', '333333333', null, '83333333333', null, null, 3);

create table clients (
	client_id int primary key generated always as identity,
	person_id int not null references persons(person_id),
	company_id smallint not null references companies(company_id),
	unique(person_id, company_id)
);

insert into clients values 
(default, 1, 1),
(default, 2, 2),
(default, 3, 3);

create table company_services(
	company_service_id smallint primary key generated always as identity,
	company_service_name varchar(80) not null unique,
	company_service_is_ydalennoe_obslyjivanie boolean,
	company_service_count_plan_viezd smallint,
	company_service_count_extra_viezd smallint,
	company_service_description text not null,
	company_service_price_from numeric(7, 2) not null
);

insert into company_services values
(default, 'Нормализация БД', null, null, null, 'Преводим БД в 3НФ', 75000.27),
(default, 'Индексируем БД', null, null, null, 'Проводим индексацию таблицу с целью повышения производительности', 74000.27),
(default, 'Пишем скрипты на генерацию backup файлов', null, null, null, 'Пишем скрипты на генерацию backup файлов в целях безопасноти', 73000.27);

create table etaps_working (
	etap_working_id smallint primary key generated always as identity,
	etap_working_name varchar(50) not null unique
);

insert into etaps_working values 
(default, 'Этап 1'),
(default, 'Этап 2'),
(default, 'Этап 3');

create table otdels(
	otdel_id smallint primary key generated always as identity,
	otdel_name varchar(30) not null unique
);

insert into otdels values 
(default, 'Отдел 1'),
(default, 'Отдел 2'),
(default, 'Отдел 3');

create table doljnosti(
	doljnost_id smallint primary key generated always as identity,
	doljnost_name varchar(30) not null unique
);

insert into doljnosti values 
(default, 'Должность 1'),
(default, 'Должность 2'),
(default, 'Должность 3');

create table staff(
	emploe_id int primary key generated always as identity,
	otdel_id smallint not null references otdels(otdel_id),
	doljnost smallint not null references doljnosti(doljnost_id),
	person_id int not null unique references persons(person_id)
);

insert into staff values 
(default, 1, 1, 1),
(default, 2, 2, 2),
(default, 3, 3, 3);

create table projects_in_works(
	projects_in_work_id bigint primary key generated always as identity,
	company_service_id smallint not null references company_services(company_service_id),
	client_id int not null references clients(client_id),
	emploe_id int not null references staff(emploe_id),
	etap_working_id smallint not null references etaps_working(etap_working_id),
	date_end date default current_date
);

insert into projects_in_works values 
(default, 1, 1, 1, 1, default),
(default, 2, 2, 2, 2, default),
(default, 3, 3, 3, 3, default);

create table otchet_with_payment(
	otchet_with_payment_id bigint primary key generated always as identity,
	projects_in_work_id bigint not null unique references projects_in_works(projects_in_work_id),
	payment_date date default current_date,
	result_sum decimal(8, 2) not null,
	actual_sum decimal(8, 2)
);

insert into otchet_with_payment values 
(default, 1, default, 123456.78, null),
(default, 2, default, 901234.56, null),
(default, 3, default, 789012.34, null);