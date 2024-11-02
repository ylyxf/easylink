import groovy.sql.Sql

Sql calcSql = Sql.newInstance(_calc);

calcSql.execute('''
	CREATE TABLE HELLO(
		name varchar(36)
	);
	insert into HELLO (name) values ('NIUB');
	CREATE TABLE HELLO2(
		name varchar(36)
	);


''')

