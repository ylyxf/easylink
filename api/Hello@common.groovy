import org.slf4j.Logger;


import groovy.sql.Sql

//http://127.0.0.1:1997/api/Hello?_debug=hi

Sql calcSql = Sql.newInstance(_calc);

calcSql.execute('''
	CREATE LINKED TABLE SINGER_ABC('javax.naming.InitialContext','java:ds/xx', NULL, NULL,'SINGER_ABC');

	CREATE LINKED TABLE SONG_ABC('javax.naming.InitialContext','java:ds/yy', NULL, NULL,'SONG_ABC');
	
	create table  RESULT as  select SONG.*,SINGER.id as SINGER_id2,SINGER.name as SINGER_name from SINGER_ABC as SINGER ,SONG_ABC as SONG  where SINGER_ABC.id = SONG_ABC.SINGER_ID;

	drop table SINGER_ABC;

	drop table SONG_ABC;
''')

def result = calcSql.rows("select * from RESULT")
return result;
