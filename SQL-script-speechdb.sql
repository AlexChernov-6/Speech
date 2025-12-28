/*create table user_(
	Id_User int primary key generated always as identity,
	Email_User varchar(50) not null unique,
	Visible_Name_User varchar(20),
	Name_User varchar(20) not null unique,
	Password_User varchar(50) not null,
	Birthday_User date not null
);*/

/*create user userSpeech with password '0QjWJ?3KXIzA';
grant all privileges on database speechdb to userSpeech;*/

-- -- Права на существующие таблицы
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO userSpeech;

-- -- Права на последовательности (для ID)
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO userSpeech;

-- -- Права на будущие таблицы
-- ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO userSpeech;
-- ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO userSpeech;